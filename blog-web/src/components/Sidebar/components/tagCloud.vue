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
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.9) 0%,
    rgba(240, 248, 255, 0.95) 25%,
    rgba(248, 250, 252, 0.9) 50%,
    rgba(245, 245, 245, 0.95) 75%,
    rgba(250, 250, 250, 0.9) 100%
  );
  backdrop-filter: blur(10px);
  border-radius: 15px;
  border: 1px solid rgba(255, 255, 255, 0.8);
  box-shadow: 
    0 8px 32px rgba(0, 0, 0, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
  
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
      rgba(59, 130, 246, 0.05) 15deg,
      rgba(139, 92, 246, 0.03) 30deg,
      rgba(236, 72, 153, 0.05) 45deg,
      transparent 60deg
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
    border: 2px solid rgba(59, 130, 246, 0.2);
    border-radius: 50%;
    background: radial-gradient(circle, 
      rgba(59, 130, 246, 0.03) 0%,
      rgba(139, 92, 246, 0.02) 50%,
      transparent 100%
    );
    box-shadow: 
      0 0 30px rgba(59, 130, 246, 0.15),
      inset 0 0 30px rgba(139, 92, 246, 0.1);
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
    
    // 基础现代样式
    background: linear-gradient(135deg, 
      rgba(255, 255, 255, 0.9) 0%,
      rgba(249, 250, 251, 0.95) 100%
    );
    border: 1px solid rgba(59, 130, 246, 0.2);
    backdrop-filter: blur(8px);
    color: rgba(55, 65, 81, 0.9);
    text-shadow: 0 1px 2px rgba(255, 255, 255, 0.8);
    box-shadow: 
      0 4px 6px rgba(0, 0, 0, 0.05),
      0 1px 3px rgba(0, 0, 0, 0.1),
      inset 0 1px 0 rgba(255, 255, 255, 0.9);
    
    // 每个标签彩色渐变样式
    &:nth-child(8n+1) {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border-color: rgba(102, 126, 234, 0.5);
      color: #ffffff;
      text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
      box-shadow: 
        0 4px 15px rgba(102, 126, 234, 0.3),
        inset 0 1px 0 rgba(255, 255, 255, 0.2);
    }
    
    &:nth-child(8n+2) {
      background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
      border-color: rgba(240, 147, 251, 0.5);
      color: #ffffff;
      text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
      box-shadow: 
        0 4px 15px rgba(240, 147, 251, 0.3),
        inset 0 1px 0 rgba(255, 255, 255, 0.2);
    }
    
    &:nth-child(8n+3) {
      background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
      border-color: rgba(79, 172, 254, 0.5);
      color: #ffffff;
      text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
      box-shadow: 
        0 4px 15px rgba(79, 172, 254, 0.3),
        inset 0 1px 0 rgba(255, 255, 255, 0.2);
    }
    
    &:nth-child(8n+4) {
      background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
      border-color: rgba(67, 233, 123, 0.5);
      color: #ffffff;
      text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
      box-shadow: 
        0 4px 15px rgba(67, 233, 123, 0.3),
        inset 0 1px 0 rgba(255, 255, 255, 0.2);
    }
    
    &:nth-child(8n+5) {
      background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
      border-color: rgba(250, 112, 154, 0.5);
      color: #ffffff;
      text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
      box-shadow: 
        0 4px 15px rgba(250, 112, 154, 0.3),
        inset 0 1px 0 rgba(255, 255, 255, 0.2);
    }
    
    &:nth-child(8n+6) {
      background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);
      border-color: rgba(168, 237, 234, 0.5);
      color: #4a5568;
      text-shadow: 0 1px 0 rgba(255, 255, 255, 0.5);
      box-shadow: 
        0 4px 15px rgba(168, 237, 234, 0.3),
        inset 0 1px 0 rgba(255, 255, 255, 0.3);
    }
    
    &:nth-child(8n+7) {
      background: linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%);
      border-color: rgba(255, 236, 210, 0.5);
      color: #744210;
      text-shadow: 0 1px 0 rgba(255, 255, 255, 0.5);
      box-shadow: 
        0 4px 15px rgba(255, 236, 210, 0.3),
        inset 0 1px 0 rgba(255, 255, 255, 0.3);
    }
    
    &:nth-child(8n) {
      background: linear-gradient(135deg, #d299c2 0%, #fef9d7 100%);
      border-color: rgba(210, 153, 194, 0.5);
      color: #553c4e;
      text-shadow: 0 1px 0 rgba(255, 255, 255, 0.5);
      box-shadow: 
        0 4px 15px rgba(210, 153, 194, 0.3),
        inset 0 1px 0 rgba(255, 255, 255, 0.3);
    }
    
    // 悬停效果
    &:hover {
      transform: scale(1.2) translateZ(20px);
      border-width: 2px;
      z-index: 100;
      filter: brightness(1.1) saturate(1.2);
      
      &:nth-child(8n+1) {
        box-shadow: 
          0 8px 25px rgba(102, 126, 234, 0.4),
          0 3px 10px rgba(102, 126, 234, 0.2),
          inset 0 1px 0 rgba(255, 255, 255, 0.3);
      }
      
      &:nth-child(8n+2) {
        box-shadow: 
          0 8px 25px rgba(240, 147, 251, 0.4),
          0 3px 10px rgba(240, 147, 251, 0.2),
          inset 0 1px 0 rgba(255, 255, 255, 0.3);
      }
      
      &:nth-child(8n+3) {
        box-shadow: 
          0 8px 25px rgba(79, 172, 254, 0.4),
          0 3px 10px rgba(79, 172, 254, 0.2),
          inset 0 1px 0 rgba(255, 255, 255, 0.3);
      }
      
      &:nth-child(8n+4) {
        box-shadow: 
          0 8px 25px rgba(67, 233, 123, 0.4),
          0 3px 10px rgba(67, 233, 123, 0.2),
          inset 0 1px 0 rgba(255, 255, 255, 0.3);
      }
      
      &:nth-child(8n+5) {
        box-shadow: 
          0 8px 25px rgba(250, 112, 154, 0.4),
          0 3px 10px rgba(250, 112, 154, 0.2),
          inset 0 1px 0 rgba(255, 255, 255, 0.3);
      }
      
      &:nth-child(8n+6) {
        box-shadow: 
          0 8px 25px rgba(168, 237, 234, 0.4),
          0 3px 10px rgba(168, 237, 234, 0.2),
          inset 0 1px 0 rgba(255, 255, 255, 0.4);
      }
      
      &:nth-child(8n+7) {
        box-shadow: 
          0 8px 25px rgba(255, 236, 210, 0.4),
          0 3px 10px rgba(255, 236, 210, 0.2),
          inset 0 1px 0 rgba(255, 255, 255, 0.4);
      }
      
      &:nth-child(8n) {
        box-shadow: 
          0 8px 25px rgba(210, 153, 194, 0.4),
          0 3px 10px rgba(210, 153, 194, 0.2),
          inset 0 1px 0 rgba(255, 255, 255, 0.4);
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
      0 4px 6px rgba(0, 0, 0, 0.05),
      0 1px 3px rgba(0, 0, 0, 0.1),
      inset 0 1px 0 rgba(255, 255, 255, 0.9);
  }
  50% {
    box-shadow: 
      0 8px 15px rgba(59, 130, 246, 0.2),
      0 3px 6px rgba(59, 130, 246, 0.1),
      inset 0 1px 0 rgba(255, 255, 255, 0.9);
    transform: translateY(-1px);
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
