<template>
  <div class="count-to-wrapper">
    <span class="count-to" ref="countRef">{{ displayValue }}</span>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, nextTick } from 'vue'

const props = withDefaults(defineProps<{
  startVal?: number
  endVal: number
  duration?: number
  decimals?: number
  autoplay?: boolean
  separator?: string
  prefix?: string
  suffix?: string
  useEasing?: boolean
  theme?: 'default' | 'gradient' | 'neon'
}>(), {
  startVal: 0,
  duration: 2000,
  decimals: 0,
  autoplay: true,
  separator: ',',
  prefix: '',
  suffix: '',
  useEasing: true,
  theme: 'gradient'
})

const displayValue = ref(formatValue(props.startVal))
const countRef = ref<HTMLElement | null>(null)
let startTime: number | null = null
let timer: number | null = null

function formatValue(num: number): string {
  // 格式化数字
  const fixed = num.toFixed(props.decimals)
  
  // 添加千位分隔符
  const parts = fixed.toString().split('.')
  parts[0] = parts[0].replace(/\B(?=(\d{3})+(?!\d))/g, props.separator)
  
  // 添加前缀和后缀
  return props.prefix + parts.join('.') + props.suffix
}

// 缓动函数 - 四次方缓出
function easeOutQuart(x: number): number {
  return 1 - Math.pow(1 - x, 4)
}

// 弹性缓动函数
function easeOutElastic(x: number): number {
  const c4 = (2 * Math.PI) / 3
  
  return x === 0
    ? 0
    : x === 1
    ? 1
    : Math.pow(2, -10 * x) * Math.sin((x * 10 - 0.75) * c4) + 1
}

const animate = (timestamp: number) => {
  if (!startTime) startTime = timestamp
  const progress = Math.min(timestamp - startTime, props.duration)
  const easedProgress = props.useEasing 
    ? easeOutElastic(progress / props.duration)
    : progress / props.duration
    
  const currentValue = props.startVal + ((props.endVal - props.startVal) * easedProgress)

  if (progress < props.duration) {
    displayValue.value = formatValue(currentValue)
    timer = requestAnimationFrame(animate)
  } else {
    displayValue.value = formatValue(props.endVal)
    applyTheme()
  }
}

const applyTheme = () => {
  nextTick(() => {
    if (!countRef.value) return
    
    // 根据主题应用不同的样式
    switch (props.theme) {
      case 'neon':
        countRef.value.classList.add('neon-theme')
        break
      case 'gradient':
        countRef.value.classList.add('gradient-theme')
        break
      default:
        countRef.value.classList.add('default-theme')
    }
    
    // 添加完成动画
    countRef.value.classList.add('completed')
  })
}

const start = () => {
  if (timer) cancelAnimationFrame(timer)
  displayValue.value = formatValue(props.startVal)
  startTime = null
  
  if (countRef.value) {
    countRef.value.classList.remove('completed')
  }
  
  timer = requestAnimationFrame(animate)
}

watch(() => props.endVal, () => {
  if (props.autoplay) start()
})

watch(() => props.theme, () => {
  applyTheme()
})

onMounted(() => {
  if (props.autoplay) start()
  applyTheme()
})
</script>

<style scoped>
.count-to-wrapper {
  display: inline-block;
  position: relative;
}

.count-to {
  display: inline-block;
  font-feature-settings: "tnum";
  font-variant-numeric: tabular-nums;
  letter-spacing: -0.5px;
  transform: translateZ(0);
  will-change: content;
  transition: transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  position: relative;
}

.count-to.completed {
  animation: pulse 0.6s cubic-bezier(0.34, 1.56, 0.64, 1);
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.05); }
}

.default-theme {
  color: var(--el-color-primary);
  text-shadow: 0px 2px 8px rgba(0, 0, 0, 0.1);
}

.gradient-theme {
  background-image: linear-gradient(90deg, #6366f1, #a5b4fc);
  background-size: 200% auto;
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  animation: shine 4s linear infinite;
  text-shadow: 0px 2px 8px rgba(0, 0, 0, 0.1);
}

@keyframes shine {
  to { background-position: 200% center; }
}

.neon-theme {
  color: #fff;
  text-shadow: 
    0 0 5px #6366f1,
    0 0 10px #6366f1,
    0 0 20px #6366f1,
    0 0 40px #6366f1;
  animation: flicker 3s infinite alternate;
}

@keyframes flicker {
  0%, 19%, 21%, 23%, 25%, 54%, 56%, 100% {
    text-shadow: 
      0 0 5px #6366f1,
      0 0 10px #6366f1,
      0 0 20px #6366f1,
      0 0 40px #6366f1;
  }
  20%, 24%, 55% {
    text-shadow: none;
  }
}
</style> 