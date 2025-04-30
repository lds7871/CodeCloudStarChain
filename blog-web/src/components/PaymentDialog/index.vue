<template>
  <el-dialog
    title="支付"
    :visible.sync="visible"
    width="500px"
    :close-on-click-modal="false"
    custom-class="payment-dialog"
    @close="handleClose"
  >
    <div class="payment-content">
      <!-- 商品信息 -->
      <div class="product-info">
        <h3>{{ title }}</h3>
        <div class="price">￥{{ price }}</div>
      </div>

      <!-- 支付方式选择 -->
      <div class="payment-methods">
        <div class="method-title">选择支付方式</div>
        <div class="method-list">
          <div
            class="method-item"
            :class="{ active: paymentMethod === 'alipay' }"
            @click="paymentMethod = 'alipay'"
          >
            <i class="fab  method-icon"></i>
            <span class="method-name">支付宝</span>
            <i class="fas fa-check-circle check-icon"></i>
          </div>
          <div
            class="method-item"
            :class="{ active: paymentMethod === 'wechat' }"
            @click="paymentMethod = 'wechat'"
          >
            <i class="fab   fa-money method-icon"></i>
            <span class="method-name">余额支付</span>
            <i class="fas fa-check-circle check-icon"></i>
          </div>
        </div>
      </div>

    </div>

    <div slot="footer" class="dialog-footer">
      <template v-if="!showQRCode">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handlePay" :loading="loading">
          立即支付
        </el-button>
      </template>
      <template v-else>
        <el-button @click="handleClose">取消支付</el-button>
        <el-button type="primary" @click="checkPaymentStatus">
          已完成支付
        </el-button>
      </template>
    </div>
  </el-dialog>
</template>

<script>
import {rhgeSuccess} from '@/api/user'
export default {
  name: 'PaymentDialog',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    title: {
      type: String,
      required: true
    },
    price: {
      type: Number,
      required: true
    },
    articleId: {
      type: String,
      required: true
    }
  },
  data() {
    return {
      paymentMethod: 'alipay',
      loading: false,
      qrLoading: false,
      showQRCode: false,
      qrCodeUrl: this.paymentMethod === 'alipay' ? this.$store.state.webSiteInfo.aliPay : this.$store.state.webSiteInfo.weixinPay,
      orderNo: '',
      checkTimer: null,
      query: {
        userId: undefined,
        art_id: undefined,
        total_amount: undefined,
        out_trade_no: undefined,
        trade_status: undefined,
        gmt_payment: undefined,
      },
      user: {}
    }
  },
  methods: {
async handlePay() {
  this.loading = true;
  try {
    const timestamp = Date.now();
    const params = {
      subject: "付费文章",
      traceNo: timestamp,
      totalAmount: this.price
    };
    
    // 使用 import.meta.env 访问环境变量，并对参数进行编码
    const url = `${import.meta.env.VITE_APP_API_URL}/alipay/pay?` + 
      `subject=${encodeURIComponent("付费文章")}&` +
      `traceNo=${encodeURIComponent(timestamp)}&` +
      `totalAmount=${encodeURIComponent(this.price)}`;
    
    // 在新窗口打开支付页面
    const payWindow = window.open(url, '_self');
    
    // 检查窗口是否成功打开
    if (payWindow === null) {
      this.$message.error('请允许浏览器打开新窗口进行支付');
    }
    
  } catch (error) {
    console.error('支付错误:', error);
    this.$message.error('创建订单失败，请重试');
  } finally {
    this.loading = false;
  }
}, payReturn() {
      if (this.$route.query.out_trade_no) {
        console.log("进入支付回调...");
        this.query = {
          art_id: this.articleId, // 使用props传入的文章ID
          userId: JSON.parse(localStorage.getItem("user")).id,
          out_trade_no: this.$route.query.out_trade_no,
          total_amount: parseFloat(this.$route.query.total_amount),
          trade_status: this.$route.query.trade_status,
          gmt_payment: this.$route.query.gmt_payment
        };

        rhgeSuccess(this.query).then(res => {
          if (res.data.code === 1) {
            this.handlePaymentSuccess();
            // 刷新页面以显示完整内容
            window.location.reload();
          }
        }).catch(e => {
          console.error('支付验证失败:', e);
          this.$message.error('支付验证失败，请联系管理员');
        });
      }
    },

    handlePaymentSuccess() {
      this.$message.success('支付成功，即将显示完整内容');
      this.$emit('payment-success');
      this.handleClose();
      
      // 存储支付状态到本地
      localStorage.setItem(`article_paid_${this.articleId}`, 'true');
  },
    handleClose() {
      this.showQRCode = false
      this.qrCodeUrl = ''
      this.orderNo = ''
      this.$emit('update:visible', false)
    }
  },
}
</script>

<style lang="scss" scoped>
.payment-dialog {
  :deep(.el-dialog__body) {
    padding: 0;
  }
}

.payment-content {
  padding: $spacing-lg;
}

.product-info {
  text-align: center;
  margin-bottom: $spacing-xl;
  padding-bottom: $spacing-lg;
  border-bottom: 1px solid var(--border-color);

  h3 {
    color: var(--text-primary);
    font-size: 1.2em;
    margin-bottom: $spacing-md;
  }

  .price {
    color: #E6162D;
    font-size: 1.8em;
    font-weight: bold;
  }
}

.payment-methods {
  margin-bottom: $spacing-xl;

  .method-title {
    font-size: 1.1em;
    color: var(--text-primary);
    margin-bottom: $spacing-md;
  }

  .method-list {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: $spacing-md;
  }

  .method-item {
    position: relative;
    padding: $spacing-md;
    border: 2px solid var(--border-color);
    border-radius: $border-radius-lg;
    cursor: pointer;
    display: flex;
    align-items: center;
    gap: $spacing-md;
    transition: all 0.3s ease;

    .method-icon {
      font-size: 24px;
      color: var(--text-primary);
      
      &.fa-alipay {
        color: #1677FF;
      }
      
      &.fa-weixin {
        color: #07C160;
      }
    }

    .method-name {
      color: var(--text-primary);
      font-size: 1.1em;
    }

    .check-icon {
      position: absolute;
      right: $spacing-md;
      color: $primary;
      opacity: 0;
      transform: scale(0.8);
      transition: all 0.3s ease;
    }

    &:hover {
      border-color: rgba($primary, 0.3);
      background: rgba($primary, 0.02);
    }

    &.active {
      border-color: $primary;
      background: rgba($primary, 0.05);

      .check-icon {
        opacity: 1;
        transform: scale(1);
      }
    }
  }
}

.qr-section {
  min-height: 300px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: $spacing-xl;
  padding: $spacing-lg;
  background: var(--hover-bg);
  border-radius: $border-radius-lg;

  .qr-wrapper {
    text-align: center;

    .qr-code {
      width: 200px;
      height: 200px;
      margin: 0 auto $spacing-md;
      padding: $spacing-md;
      background: white;
      border-radius: $border-radius-md;
      box-shadow: $shadow-md;

      img {
        width: 100%;
        height: 100%;
        object-fit: contain;
      }
    }

    .qr-tip {
      color: var(--text-secondary);
      display: flex;
      align-items: center;
      justify-content: center;
      gap: $spacing-sm;

      i {
        font-size: 1.2em;
        
        &.fa-alipay {
          color: #1677FF;
        }
        
        &.fa-weixin {
          color: #07C160;
        }
      }
    }
  }

  .payment-status {
    text-align: center;
    color: var(--text-secondary);
    font-size: 0.9em;

    .amount {
      margin-bottom: $spacing-sm;
      
      span {
        color: #E6162D;
        font-weight: bold;
        font-size: 1.2em;
      }
    }

    .order-no {
      opacity: 0.8;
    }
  }
}
</style> 