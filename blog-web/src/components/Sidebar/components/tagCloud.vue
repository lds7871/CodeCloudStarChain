<template>
  <div class="tag-wall">
    <div 
      class="tag-cloud" 
      ref="wrapper"
      @mouseenter="stopRotate"
      @mouseleave="startRotate"
    >
      <p
        v-for="(item, index) in data"
        :key="index"
        ref="tag"
        @click="clickTag(item)"
        @mouseenter="handleTagHover(index)"
        @mouseleave="handleTagLeave"
        :class="{ 'tag-dimmed': hoveredIndex !== null && hoveredIndex !== index }"
      >
        {{ item.name }}
      </p>
    </div>
  </div>
</template>

<script>
import { getTagsApi } from '@/api/tags'
export default {
  data() {
    return {
      data: [],
      option: {
        radius: 140, // 滚动半径，单位px
        maxFont: 24, // 最大字体大小
        color: null, // 字体颜色。为空时随机
        rotateAngleXbase: 600, // 默认旋转速度基数，数越小速度越快
        rotateAngleYbase: 600,
      },
      tagList: [],
      isRotating: true,
      hoveredIndex: null,
    };
  },
  beforeDestroy() {
    if (this.timer) {
      clearInterval(this.timer);
      this.timer = null;
    }
  },
  watch: {
    data() {
      this.$nextTick(() => {
        this._initTags();
      });
    },
  },
  mounted() {
    this._initTags();
    getTagsApi().then(res => {
      this.data = res.data
    })
  },
  methods: {
    /**
     * 初始化标签
     */
    _initTags() {
      this.rotateAngleX = Math.PI / this.option.rotateAngleXbase;
      this.rotateAngleY = Math.PI / this.option.rotateAngleYbase;

      for (var i = 0, length = this.data.length; i < length; i++) {
        // 获取球面上均匀的点的经纬度 θ = arccos( ((2*num)-1)/all - 1); Φ = θ*sqrt(all * π);
        let angleX = Math.acos((2 * (i + 1) - 1) / length - 1);
        let angleY = angleX * Math.sqrt(length * Math.PI);
        // 根据经纬度获取点的坐标，球中心的点坐标是 (0,0,0) x=r*sinθ*cosΦ   y=r*sinθ*sinΦ   z=r*cosθ;
        const x = this.option.radius * Math.sin(angleX) * Math.cos(angleY);
        const y = this.option.radius * Math.sin(angleX) * Math.sin(angleY);
        const z = this.option.radius * Math.cos(angleX);
        if (this.option.color) {
          this.$refs.tag[i].style.color = this.option.color;
        } else {
          // 随机颜色
          this.$refs.tag[i].style.color =
            "rgb(" +
            Math.round(255 * Math.random()) +
            "," +
            Math.round(255 * Math.random()) +
            "," +
            Math.round(255 * Math.random()) +
            ")";
        }
        // 每个标签对象都有四对值
        var tag = {
          x: x,
          y: y,
          z: z,
          ele: this.$refs.tag[i],
        };
        this.tagList.push(tag);
      }
      this.startRotate();
    },
    /**
     * 设置每个标签的坐标位置和字体大小以及透明度
     */
    setPosition(tag, r, maxFont) {
      // 设置每个标签的坐标位置和字体大小以及透明度
      if (this.$refs.wrapper) {
        tag.ele.style.transform =
          "translate(" +
          (tag.x +
            this.$refs.wrapper.offsetWidth / 2 -
            tag.ele.offsetWidth / 2) +
          "px," +
          (tag.y +
            this.$refs.wrapper.offsetHeight / 2 -
            tag.ele.offsetHeight / 2) +
          "px)";
        tag.ele.style.opacity = tag.z / r / 2 + 0.7;
        tag.ele.style.fontSize = (tag.z / r / 2 + 0.5) * maxFont + "px";
      }
    },
    /**
     * 旋转X轴
     */
    rotateX(tag) {
      var cos = Math.cos(this.rotateAngleX);
      var sin = Math.sin(this.rotateAngleX);
      var y1 = tag.y * cos - tag.z * sin;
      var z1 = tag.y * sin + tag.z * cos;
      tag.y = y1;
      tag.z = z1;
    },
    /**
     * 旋转Y轴
     */
    rotateY(tag) {
      var cos = Math.cos(this.rotateAngleY);
      var sin = Math.sin(this.rotateAngleY);
      var x1 = tag.z * sin + tag.x * cos;
      var z1 = tag.z * cos - tag.x * sin;
      tag.x = x1;
      tag.z = z1;
    },
    /**
     * 点击标签
     */
    clickTag(item) {
      this.$router.push({
        path: '/tags',
        query: {
          tagId: item.id,
          tagName: item.name
        }
      })
    },
    /**
     * 停止旋转
     */
    stopRotate() {
      this.isRotating = false;
      if (this.timer) {
        clearInterval(this.timer);
        this.timer = null;
      }
    },
    /**
     * 开始旋转
     */
    startRotate() {
      if (this.timer) {
        return;  // 如果定时器已存在，直接返回
      }
      
      this.isRotating = true;
      const _self = this;
      this.timer = setInterval(function () {
        if (!_self.isRotating) {
          clearInterval(_self.timer);
          _self.timer = null;
          return;
        }
        for (var i = 0; i < _self.tagList.length; i++) {
          _self.rotateX(_self.tagList[i]);
          _self.rotateY(_self.tagList[i]);
          _self.setPosition(
            _self.tagList[i],
            _self.option.radius,
            _self.option.maxFont
          );
        }
      }, 20);
    },
    handleTagHover(index) {
      this.hoveredIndex = index;
    },
    handleTagLeave() {
      this.hoveredIndex = null;
    },
  },
};
</script>

<style lang="scss" scoped>
.tag-wall {
  position: relative;
  width: 100%;
  height: 300px;
  overflow: hidden;
  perspective: 1000px;
  background: rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(10px);
  border-radius: 15px;
  
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: repeating-conic-gradient(
      from 0deg at 50% 50%,
      transparent 0deg,
      rgba(0, 245, 255, 0.03) 15deg,
      transparent 30deg
    );
    animation: rotate 20s linear infinite;
    pointer-events: none;
  }
  
  &::after {
    content: '';
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    width: 200px;
    height: 200px;
    border: 1px solid rgba(0, 245, 255, 0.2);
    border-radius: 50%;
    box-shadow: 
      0 0 20px rgba(0, 245, 255, 0.1),
      inset 0 0 20px rgba(0, 245, 255, 0.1);
    pointer-events: none;
  }
}

.tag-cloud {
  position: relative;
  width: 100%;
  height: 100%;
  transform-style: preserve-3d;
  cursor: grab;
  
  &:active {
    cursor: grabbing;
  }
  
  p {
    position: absolute;
    margin: 0;
    padding: 8px 16px;
    border-radius: 20px;
    font-size: 14px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
    text-decoration: none;
    user-select: none;
    white-space: nowrap;
    
    // 基础科幻样式
    background: rgba(0, 0, 0, 0.4);
    border: 1px solid rgba(0, 245, 255, 0.3);
    backdrop-filter: blur(10px);
    color: rgba(255, 255, 255, 0.8);
    text-shadow: 0 0 10px currentColor;
    box-shadow: 
      0 0 10px rgba(0, 245, 255, 0.2),
      inset 0 0 10px rgba(0, 245, 255, 0.05);
    
    // 每个标签随机的霓虹灯颜色
    &:nth-child(6n+1) {
      border-color: rgba(0, 245, 255, 0.4);
      color: #00f5ff;
      box-shadow: 
        0 0 15px rgba(0, 245, 255, 0.3),
        inset 0 0 15px rgba(0, 245, 255, 0.1);
    }
    
    &:nth-child(6n+2) {
      border-color: rgba(131, 56, 236, 0.4);
      color: #8338ec;
      box-shadow: 
        0 0 15px rgba(131, 56, 236, 0.3),
        inset 0 0 15px rgba(131, 56, 236, 0.1);
    }
    
    &:nth-child(6n+3) {
      border-color: rgba(255, 0, 110, 0.4);
      color: #ff006e;
      box-shadow: 
        0 0 15px rgba(255, 0, 110, 0.3),
        inset 0 0 15px rgba(255, 0, 110, 0.1);
    }
    
    &:nth-child(6n+4) {
      border-color: rgba(0, 128, 255, 0.4);
      color: #0080ff;
      box-shadow: 
        0 0 15px rgba(0, 128, 255, 0.3),
        inset 0 0 15px rgba(0, 128, 255, 0.1);
    }
    
    &:nth-child(6n+5) {
      border-color: rgba(16, 185, 129, 0.4);
      color: #10b981;
      box-shadow: 
        0 0 15px rgba(16, 185, 129, 0.3),
        inset 0 0 15px rgba(16, 185, 129, 0.1);
    }
    
    &:nth-child(6n) {
      border-color: rgba(245, 158, 11, 0.4);
      color: #f59e0b;
      box-shadow: 
        0 0 15px rgba(245, 158, 11, 0.3),
        inset 0 0 15px rgba(245, 158, 11, 0.1);
    }
    
    // 悬停效果
    &:hover {
      transform: scale(1.2) translateZ(20px);
      background: rgba(0, 0, 0, 0.8);
      border-width: 2px;
      text-shadow: 0 0 20px currentColor;
      z-index: 100;
      
      &:nth-child(6n+1) {
        box-shadow: 
          0 0 30px rgba(0, 245, 255, 0.6),
          inset 0 0 20px rgba(0, 245, 255, 0.2);
      }
      
      &:nth-child(6n+2) {
        box-shadow: 
          0 0 30px rgba(131, 56, 236, 0.6),
          inset 0 0 20px rgba(131, 56, 236, 0.2);
      }
      
      &:nth-child(6n+3) {
        box-shadow: 
          0 0 30px rgba(255, 0, 110, 0.6),
          inset 0 0 20px rgba(255, 0, 110, 0.2);
      }
      
      &:nth-child(6n+4) {
        box-shadow: 
          0 0 30px rgba(0, 128, 255, 0.6),
          inset 0 0 20px rgba(0, 128, 255, 0.2);
      }
      
      &:nth-child(6n+5) {
        box-shadow: 
          0 0 30px rgba(16, 185, 129, 0.6),
          inset 0 0 20px rgba(16, 185, 129, 0.2);
      }
      
      &:nth-child(6n) {
        box-shadow: 
          0 0 30px rgba(245, 158, 11, 0.6),
          inset 0 0 20px rgba(245, 158, 11, 0.2);
      }
    }
    
    // 暗淡效果（当其他标签被hover时）
    &.tag-dimmed {
      opacity: 0.3;
      transform: scale(0.9);
      filter: blur(1px);
    }
    
    // 点击效果
    &:active {
      transform: scale(1.1) translateZ(10px);
      transition: transform 0.1s ease;
    }
    
    // 扫描线效果
    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: -100%;
      width: 100%;
      height: 100%;
      background: linear-gradient(90deg, 
        transparent, 
        rgba(255, 255, 255, 0.2), 
        transparent);
      border-radius: 20px;
      transition: left 0.5s ease;
    }
    
    &:hover::before {
      left: 100%;
    }
  }
}

// 旋转动画
@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

// 脉冲动画（用于强调某些标签）
@keyframes pulse {
  0%, 100% {
    box-shadow: 
      0 0 10px rgba(0, 245, 255, 0.2),
      inset 0 0 10px rgba(0, 245, 255, 0.05);
  }
  50% {
    box-shadow: 
      0 0 20px rgba(0, 245, 255, 0.4),
      inset 0 0 20px rgba(0, 245, 255, 0.1);
  }
}

// 响应式调整
@media (max-width: 768px) {
  .tag-wall {
    height: 250px;
    
    &::after {
      width: 150px;
      height: 150px;
    }
  }
  
  .tag-cloud p {
    padding: 6px 12px;
    font-size: 12px;
    
    &:hover {
      transform: scale(1.15) translateZ(15px);
    }
  }
}

@media (max-width: 480px) {
  .tag-wall {
    height: 200px;
    
    &::after {
      width: 120px;
      height: 120px;
    }
  }
  
  .tag-cloud p {
    padding: 5px 10px;
    font-size: 11px;
  }
}

// 特殊效果：当没有hover任何标签时，随机几个标签会发光
.tag-cloud:not(:hover) {
  p {
    &:nth-child(7n+1) {
      animation: pulse 3s ease-in-out infinite;
      animation-delay: 0s;
    }
    
    &:nth-child(11n+3) {
      animation: pulse 3s ease-in-out infinite;
      animation-delay: 1s;
    }
    
    &:nth-child(13n+5) {
      animation: pulse 3s ease-in-out infinite;
      animation-delay: 2s;
    }
  }
}
</style>
