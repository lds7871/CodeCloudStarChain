<template>
  <el-dialog title="" :visible.sync="visible" width="500px" :close-on-click-modal="false"
    custom-class="modern-payment-dialog" @close="handleClose" :show-close="false">
    <!-- 自定义头部 -->
    <div class="payment-header">
      <div class="header-content">
        <div class="payment-icon">
          <i class="fas fa-credit-card"></i>
        </div>
        <h2 class="payment-title">安全支付</h2>
        <p class="payment-subtitle">选择您的支付方式完成购买</p>
      </div>
      <button class="close-btn" @click="handleClose">
        <i class="fas fa-times"></i>
      </button>
    </div>

    <div class="payment-content">
      <!-- 商品信息 -->
      <div class="product-info">
        <div class="product-details">
          <h3 class="product-title">{{ title }}</h3>
          <div class="product-description">畅享优质内容，解锁全文阅读</div>
        </div>
        <div class="price-section">
          <div class="price-label">支付金额</div>
          <div class="price">￥{{ price }}</div>
        </div>
      </div>

      <!-- 支付方式 -->
      <div class="payment-methods">
        <div class="method-header">
          <i class="fas fa-shield-alt security-icon"></i>
          <span class="method-title">支付方式</span>
          <div class="security-badge">
            <i class="fas fa-lock"></i>
            <span>安全加密</span>
          </div>
        </div>

        <div class="alipay-method">
          <div class="method-card active">
            <div class="method-left">
              <div class="alipay-icon">
                <svg viewBox="0 0 1024 1024" width="32" height="32">
                  <path fill="#1677FF"
                    d="M256 85.333h512c94.208 0 170.667 76.459 170.667 170.667v512c0 94.208-76.459 170.667-170.667 170.667H256c-94.208 0-170.667-76.459-170.667-170.667V256c0-94.208 76.459-170.667 170.667-170.667z" />
                  <path fill="#FFF" d="M320 320h384v64H320zM320 448h256v64H320zM320 576h384v64H320z" />
                </svg>
              </div>
              <div class="method-info">
                <div class="method-name">支付宝</div>
                <div class="method-desc">安全便捷，支持花呗分期</div>
              </div>
            </div>
            <div class="method-right">
              <div class="check-circle">
                <i class="fas fa-check"></i>
              </div>
            </div>
          </div>
        </div>

        <!-- 安全提示 -->
        <div class="security-tips">
          <div class="tip-item">
            <i class="fas fa-shield-check"></i>
            <span>256位SSL加密传输</span>
          </div>
          <div class="tip-item">
            <i class="fas fa-user-shield"></i>
            <span>个人信息严格保护</span>
          </div>
          <div class="tip-item">
            <i class="fas fa-award"></i>
            <span>支付宝官方认证</span>
          </div>
        </div>
      </div>
    </div>

    <div slot="footer" class="payment-footer">
      <div class="footer-content">
        <el-button class="cancel-btn" @click="handleClose" :disabled="loading">
          取消
        </el-button>
        <el-button type="primary" class="pay-btn" @click="handlePay" :loading="loading">
          <i class="fab fa-alipay" v-if="!loading"></i>
          <span>{{ loading ? '正在跳转支付宝...' : `立即支付 ￥${price}` }}</span>
        </el-button>
      </div>
      <div class="footer-note">
        <i class="fas fa-info-circle"></i>
        <span>点击支付按钮将跳转到支付宝安全支付页面</span>
      </div>
    </div>
  </el-dialog>
</template>

<script>
import { rhgeSuccess } from '@/api/user'
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
        const param = this.$route.params.id;

        // 使用 import.meta.env 访问环境变量，并对参数进行编码
        const url = `${import.meta.env.VITE_APP_API_URL}/alipay/pay?` +
          `subject=${encodeURIComponent("付费文章")}&` +
          `traceNo=${encodeURIComponent(timestamp)}&` +
          `totalAmount=${encodeURIComponent(this.price)}&` +
          `id=${encodeURIComponent(param)}`;

        // 在当前窗口打开支付页面
        window.location.href = url;

      } catch (error) {
        console.error('支付错误:', error);
        this.$message.error('创建订单失败，请重试');
        this.loading = false;
      }
    },

    payReturn() {
      if (this.$route.query.out_trade_no) {
        console.log("进入支付回调...");
        this.query = {
          art_id: this.articleId,
          userId: JSON.parse(localStorage.getItem("user")).id,
          out_trade_no: this.$route.query.out_trade_no,
          total_amount: parseFloat(this.$route.query.total_amount),
          trade_status: this.$route.query.trade_status,
          gmt_payment: this.$route.query.gmt_payment
        };

        rhgeSuccess(this.query).then(res => {
          if (res.data.code === 1) {
            this.handlePaymentSuccess();
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
      localStorage.setItem(`article_paid_${this.articleId}`, 'true');
    },

    handleClose() {
      this.loading = false;
      this.$emit('update:visible', false);
    }
  },
}
</script>

<style lang="scss" scoped>
// 全局对话框样式重置
:deep(.modern-payment-dialog) {
  .el-dialog {
    border-radius: 16px;
    overflow: hidden;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
    border: 1px solid rgba(255, 255, 255, 0.1);
  }

  .el-dialog__header {
    display: none;
  }

  .el-dialog__body {
    padding: 0;
  }

  .el-dialog__footer {
    padding: 0;
  }
}

.payment-header {
  position: relative;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 32px 24px 24px;
  text-align: center;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><pattern id="grain" width="100" height="100" patternUnits="userSpaceOnUse"><circle cx="25" cy="25" r="1" fill="white" opacity="0.1"/><circle cx="75" cy="75" r="1" fill="white" opacity="0.1"/><circle cx="50" cy="10" r="0.5" fill="white" opacity="0.15"/><circle cx="20" cy="80" r="0.5" fill="white" opacity="0.15"/></pattern></defs><rect width="100%" height="100%" fill="url(%23grain)"/></svg>');
    opacity: 0.3;
  }

  .header-content {
    position: relative;
    z-index: 1;
  }

  .payment-icon {
    width: 64px;
    height: 64px;
    background: rgba(255, 255, 255, 0.2);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 16px;
    backdrop-filter: blur(10px);
    border: 2px solid rgba(255, 255, 255, 0.3);

    i {
      font-size: 28px;
      color: white;
    }
  }

  .payment-title {
    font-size: 28px;
    font-weight: 700;
    margin: 0 0 8px;
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  }

  .payment-subtitle {
    font-size: 16px;
    opacity: 0.9;
    margin: 0;
    font-weight: 400;
  }

  .close-btn {
    position: absolute;
    top: 16px;
    right: 16px;
    width: 36px;
    height: 36px;
    background: rgba(255, 255, 255, 0.1);
    border: 1px solid rgba(255, 255, 255, 0.2);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: all 0.3s ease;
    backdrop-filter: blur(10px);

    i {
      color: white;
      font-size: 14px;
    }

    &:hover {
      background: rgba(255, 255, 255, 0.2);
      transform: rotate(90deg);
    }
  }
}

.payment-content {
  padding: 32px 24px;
  background: var(--card-bg);
}

.product-info {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 32px;
  border: 1px solid rgba(102, 126, 234, 0.1);
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 3px;
    background: linear-gradient(90deg, #667eea, #764ba2);
  }

  .product-details {
    margin-bottom: 20px;
  }

  .product-title {
    color: var(--text-primary);
    font-size: 20px;
    font-weight: 600;
    margin: 0 0 8px;
    line-height: 1.4;
  }

  .product-description {
    color: var(--text-secondary);
    font-size: 14px;
    opacity: 0.8;
  }

  .price-section {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-top: 16px;
    border-top: 1px dashed rgba(102, 126, 234, 0.2);

    .price-label {
      color: var(--text-secondary);
      font-size: 14px;
      font-weight: 500;
    }

    .price {
      color: #E6162D;
      font-size: 28px;
      font-weight: 700;
      text-shadow: 0 2px 4px rgba(230, 22, 45, 0.1);
    }
  }
}

.payment-methods {
  .method-header {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 20px;
    padding-bottom: 12px;
    border-bottom: 1px solid var(--border-color);

    .security-icon {
      color: #10B981;
      font-size: 20px;
    }

    .method-title {
      font-size: 18px;
      font-weight: 600;
      color: var(--text-primary);
    }

    .security-badge {
      margin-left: auto;
      display: flex;
      align-items: center;
      gap: 4px;
      background: rgba(16, 185, 129, 0.1);
      color: #10B981;
      padding: 4px 8px;
      border-radius: 12px;
      font-size: 12px;
      font-weight: 500;

      i {
        font-size: 10px;
      }
    }
  }

  .alipay-method {
    margin-bottom: 24px;
  }

  .method-card {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 20px;
    border: 2px solid #1677FF;
    border-radius: 16px;
    background: linear-gradient(135deg, rgba(22, 119, 255, 0.05) 0%, rgba(22, 119, 255, 0.02) 100%);
    cursor: pointer;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    position: relative;
    overflow: hidden;

    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: -100%;
      width: 100%;
      height: 100%;
      background: linear-gradient(90deg, transparent, rgba(22, 119, 255, 0.1), transparent);
      transition: left 0.6s ease;
    }

    &:hover::before {
      left: 100%;
    }

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 8px 32px rgba(22, 119, 255, 0.2);
    }

    .method-left {
      display: flex;
      align-items: center;
      gap: 16px;
    }

    .alipay-icon {
      width: 48px;
      height: 48px;
      background: #1677FF;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      box-shadow: 0 4px 16px rgba(22, 119, 255, 0.3);

      svg {
        filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.1));
      }
    }

    .method-info {
      .method-name {
        font-size: 16px;
        font-weight: 600;
        color: var(--text-primary);
        margin-bottom: 4px;
      }

      .method-desc {
        font-size: 14px;
        color: var(--text-secondary);
        opacity: 0.8;
      }
    }

    .method-right {
      .check-circle {
        width: 28px;
        height: 28px;
        background: #1677FF;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        box-shadow: 0 2px 8px rgba(22, 119, 255, 0.3);

        i {
          color: white;
          font-size: 14px;
        }
      }
    }
  }

  .security-tips {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 16px;

    .tip-item {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 12px;
      background: rgba(16, 185, 129, 0.05);
      border-radius: 8px;
      border: 1px solid rgba(16, 185, 129, 0.1);

      i {
        color: #10B981;
        font-size: 14px;
      }

      span {
        font-size: 12px;
        color: var(--text-secondary);
        font-weight: 500;
      }
    }
  }
}

.payment-footer {
  padding: 24px;
  background: var(--hover-bg);
  border-top: 1px solid var(--border-color);

  .footer-content {
    display: flex;
    gap: 12px;
    margin-bottom: 16px;

    .cancel-btn {
      flex: 1;
      height: 48px;
      border-radius: 12px;
      border: 1px solid var(--border-color);
      background: var(--card-bg);
      color: var(--text-secondary);
      font-weight: 500;
      transition: all 0.3s ease;

      &:hover {
        border-color: #E6162D;
        color: #E6162D;
      }
    }

    .pay-btn {
      flex: 2;
      height: 48px;
      border-radius: 12px;
      background: linear-gradient(135deg, #1677FF 0%, #0056E0 100%);
      border: none;
      font-size: 16px;
      font-weight: 600;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      box-shadow: 0 4px 16px rgba(22, 119, 255, 0.3);
      transition: all 0.3s ease;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 8px 24px rgba(22, 119, 255, 0.4);
      }

      &:active {
        transform: translateY(0);
      }

      i {
        font-size: 18px;
      }
    }
  }

  .footer-note {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 6px;
    color: var(--text-secondary);
    font-size: 12px;
    opacity: 0.8;

    i {
      color: #10B981;
      font-size: 12px;
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .payment-header {
    padding: 24px 16px 20px;

    .payment-title {
      font-size: 24px;
    }
  }

  .payment-content {
    padding: 24px 16px;
  }

  .product-info {
    padding: 20px;

    .price {
      font-size: 24px;
    }
  }

  .payment-methods {
    .security-tips {
      grid-template-columns: 1fr;
      gap: 12px;
    }
  }

  .payment-footer {
    padding: 20px 16px;

    .footer-content {
      flex-direction: column;

      .cancel-btn,
      .pay-btn {
        width: 100%;
      }
    }
  }
}

// 深色模式适配
@media (prefers-color-scheme: dark) {
  .payment-header {
    background: linear-gradient(135deg, #4F46E5 0%, #7C3AED 100%);
  }

  .method-card {
    border-color: #3B82F6;
    background: linear-gradient(135deg, rgba(59, 130, 246, 0.1) 0%, rgba(59, 130, 246, 0.05) 100%);

    &:hover {
      box-shadow: 0 8px 32px rgba(59, 130, 246, 0.3);
    }

    .alipay-icon {
      background: #3B82F6;
      box-shadow: 0 4px 16px rgba(59, 130, 246, 0.4);
    }

    .check-circle {
      background: #3B82F6;
      box-shadow: 0 2px 8px rgba(59, 130, 246, 0.4);
    }
  }

  .pay-btn {
    background: linear-gradient(135deg, #3B82F6 0%, #1D4ED8 100%) !important;
    box-shadow: 0 4px 16px rgba(59, 130, 246, 0.4) !important;

    &:hover {
      box-shadow: 0 8px 24px rgba(59, 130, 246, 0.5) !important;
    }
  }
}
</style>