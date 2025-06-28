<template>
  <div>
    <!-- banner -->
    <div class="message-banner" :style="cover">
      <!-- 半透明渐变遮罩层 -->
      <div class="banner-mask"></div>
      <!-- 弹幕输入框 -->
      <div class="message-container">
        <h1 class="message-title">树洞</h1>
        <div class="message-input-wrapper">
          <el-input class="input" v-model="content" placeholder="说点什么吧" @keyup.enter.native="addToList"
            @focus="show = true"></el-input>
          <el-button style="opacity: .6;" class="ml-3" round @click="addToList" v-show="show">发送</el-button>
        </div>
      </div>
      <!-- 弹幕列表 -->
      <div class="barrage-container">
        <vue-danmaku
        class="danmaku"
          :danmus="barrageList"
          style="height: 100%; width: 100%"
          useSlot
          :speeds="150"
          :channels="15"
        >
          <template v-slot:dm="{ danmu }">
            <span class="barrage-items">
              <img
                :src="danmu.avatar"
                width="30"
                height="30"
                style="border-radius: 50%"
              />
              {{ danmu.nickname }}:{{ danmu.content }}</span
            >
          </template>
        </vue-danmaku>
      </div>
    </div>
  </div>
</template>

<script>
import coverImage from '@/assets/133487507047962275.jpg';
import {addMessageApi, getMessagesApi} from "@/api/message";
import VueDanmaku from 'vue-danmaku';

export default {
  components: {
    VueDanmaku
  },

  data() {
    return {
      show: false,
      content: "",
      count: null,
      timer: null,
      barrageList: [],
      user: this.$store.state.userInfo,
    };
  },
  mounted() {
    this.listMessage();
  },
  methods: {
    /**
     * 添加留言
     */
    addToList() {
      if (this.count) {
        this.$message.error("30秒后才能再次留言");
        return false;
      }
      if (this.content.trim() === "") {

        this.$message.error("留言不能为空");
        return false;
      }
      var message = {
        avatar: this.user ? this.user.avatar : this.$store.state.webSiteInfo.touristAvatar,
        status: 1,
        nickname: this.user ? this.user.nickname : "游客",
        content: this.content
      };

      this.content = "";
      addMessageApi(message).then(res => {
        this.barrageList.push(message);
        this.$message.success("留言成功");
      }).catch(err => {
        this.$message.error("留言失败");
      });
      const TIME_COUNT = 30;
      if (!this.timer) {
        this.count = TIME_COUNT;
        this.timer = setInterval(() => {
          if (this.count > 0 && this.count <= 30) {
            this.count--;
          } else {
            clearInterval(this.timer);
            this.timer = null;
          }
        }, 1000);
      }
    },
    /**
     * 获取留言列表
     */
    listMessage() {
      getMessagesApi().then(res => {
        this.barrageList = res.data;
      });
    }
  },
 computed: {
    cover() {
      return `background: url(${coverImage}) center center / cover no-repeat`;
    }
  }
};
</script>

<style lang="scss" scoped>
:deep(.el-input__inner) {
  border-radius: 20px;
  background: #fff;
  color: #333;
  font-size: 1rem;
  padding: 0 16px;
  height: 40px;
  border: 1.5px solid #e0e6ed;
  box-shadow: 0 1px 4px rgba(33,147,176,0.04);
  transition: border 0.2s, box-shadow 0.2s;
  &:focus {
    border: 1.5px solid #2193b0;
    box-shadow: 0 2px 8px rgba(33,147,176,0.10);
    background: #fafdff;
  }
}

.message-banner {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: $primary;
  animation: header-effect 1s;

  // 半透明渐变遮罩层
  .banner-mask {
    position: absolute;
    inset: 0;
    background: linear-gradient(135deg, rgba(0,0,0,0.4) 0%, rgba(0,0,0,0.2) 100%);
    z-index: 1;
    pointer-events: none;
  }

  .message-container, .barrage-container {
    position: relative;
    z-index: 2;
  }

  .message-container {
    width: 360px;
    top: 35%;
    left: 0;
    right: 0;
    text-align: center;
    z-index: 5;
    margin: 0 auto;
    color: #fff;
    position: absolute;

    .message-title {
      color: #fff;
      font-size: 2.2rem;
      font-weight: bold;
      text-shadow: 0 2px 16px #2193b0, 0 1px 0 #000;
      letter-spacing: 2px;
      margin-bottom: 1.2rem;
      animation: title-scale 1s;
    }

    .message-input-wrapper {
      display: flex;
      justify-content: center;
      height: 2.5rem;
      margin-top: 2rem;

      .ml-3 {
        background: #2193b0;
        color: #fff;
        font-weight: 500;
        font-size: 1rem;
        border-radius: 20px;
        padding: 0 20px;
        height: 40px;
        margin-left: 10px;
        box-shadow: 0 1px 4px rgba(33,147,176,0.08);
        border: none;
        transition: background 0.2s, box-shadow 0.2s;
        animation: left-in 1s ease;
        &:hover {
          background: #38b6ff;
          box-shadow: 0 2px 8px rgba(33,147,176,0.16);
          transform: translateY(-2px) scale(1.03);
        }
        @keyframes left-in {
          0% {
            transform: translateY(-500%);
          }
          100% {
            transform: translateX(0);
          }
        }
      }
    }
  }

  .barrage-container {
    position: absolute;
    top: 80px;
    left: 0;
    right: 0;
    bottom: 0;
    width: 100%;

    .barrage-items {
      background: rgba(0,0,0,0.7);
      border-radius: 100px;
      color: #fff;
      padding: 5px 16px 5px 8px;
      align-items: center;
      display: flex;
      margin-top: 10px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.18);
      border: 1.5px solid transparent;
      background-clip: padding-box;
      transition: border 0.2s;
      &:hover {
        border: 1.5px solid #6dd5ed;
      }
      img {
        margin-right: 8px;
        box-shadow: 0 2px 8px rgba(33,147,176,0.18);
      }
    }
  }
}

@media (max-width: 600px) {
  .message-container {
    width: 95vw;
    min-width: 0;
    padding: 0 8px;
  }
  .barrage-container {
    top: 120px;
  }
}
</style>