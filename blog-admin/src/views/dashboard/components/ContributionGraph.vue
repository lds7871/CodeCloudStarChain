<template>
  <div class="contribution-graph">
    <!-- 年份选择器 -->
    <div class="year-selector">
      <div class="year-label">选择年份</div>
      <el-select v-model="selectedYear" placeholder="选择年份" @change="updateDisplayData" size="small">
        <el-option
          v-for="year in availableYears"
          :key="year"
          :label="`${year}年`"
          :value="year"
        />
      </el-select>
    </div>

    <!-- 图表主体 -->
    <div class="graph-wrapper">
      <!-- 月份标题行 -->
      <div class="months-header">
        <div class="weekday-label"></div> <!-- 空白处留给星期标签 -->
        <div class="months">
          <span v-for="(month, index) in months" :key="index" class="month-label">{{ month }}</span>
        </div>
      </div>
      
      <!-- 内容区域 -->
      <div class="content-area">
        <!-- 星期几标签 -->
        <div class="weekdays">
          <span v-for="(day, index) in ['周日', '周一', '周二', '周三', '周四', '周五', '周六']" :key="index">
            {{ day }}
          </span>
        </div>
        
        <!-- 日期网格 -->
        <div class="grid-container">
          <!-- 每一行是一个星期几 -->
          <div v-for="dayOfWeek in 7" :key="`week-${dayOfWeek}`" class="week-row">
            <!-- 每一列是一个月份 -->
            <div v-for="(month, monthIndex) in monthlyData" 
                 :key="`month-${monthIndex}-${dayOfWeek}`" 
                 class="month-column">
              <!-- 该星期几在该月的所有日期 -->
              <div v-for="(day, weekIndex) in getMonthDaysForWeekday(month, dayOfWeek - 1)" 
                   :key="`day-${monthIndex}-${dayOfWeek}-${weekIndex}`" 
                   class="day-wrapper"
                   :style="{ animationDelay: `${(monthIndex * 7 + weekIndex) * 0.01}s` }">
                <div class="day"
                     :class="[getActivityClass(day.count), {'has-data': day.date}]"
                     :data-empty="!day.date"
               :title="day.date ? `${formatDate(day.date)} · ${day.count} 次贡献` : ''">
                  <span class="day-count" v-if="day.count > 0">{{ day.count }}</span>
                </div>
                <div class="day-tooltip" v-if="day.date && day.count > 0">
                  <div class="tooltip-date">{{ formatDate(day.date) }}</div>
                  <div class="tooltip-count">{{ day.count }} 次贡献</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 图例 -->
    <div class="legend">
      <span>较少</span>
        <div v-for="level in 5" 
             :key="level" 
             class="day"
             :class="`activity-${level - 1}`">
        </div>
      <span>较多</span>
    </div>
  </div>
</template>

<script setup lang="ts">
interface DayData {
  date: string;
  count: number;
}

import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'
import { ElSelect, ElOption } from 'element-plus'
import { ref, computed, onMounted, watch } from 'vue'

dayjs.locale('zh-cn')

const props = defineProps<{
  data: any[]
}>()

// 年份选择
const currentYear = dayjs().year()
const selectedYear = ref(currentYear)
const availableYears = computed(() => {
  if (!props.data || !Array.isArray(props.data)) return [currentYear]
  
  // 从数据中提取所有年份
  const years = new Set<number>()
  props.data.forEach(item => {
    if (item.date) {
      const year = Number(item.date.split('-')[0])
      if (!isNaN(year)) years.add(year)
    }
  })
  
  // 如果没有年份数据，至少显示当前年
  if (years.size === 0) years.add(currentYear)
  
  return Array.from(years).sort((a, b) => b - a) // 降序排列
})

// 当前选中年份的数据
const currentYearData = computed(() => {
  if (!props.data || !Array.isArray(props.data)) return []
  
  return props.data.filter(item => {
    if (!item.date) return false
    const year = item.date.split('-')[0]
    return year === String(selectedYear.value)
  })
})

// 固定显示12个月份
const months = ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']

// 初始化12个月的数据结构
const initializeMonthlyData = () => {
  const monthData = []
  
  // 为每个月创建空数据结构
  for (let month = 0; month < 12; month++) {
    const weeks = []
    // 每个月最多6周
    for (let week = 0; week < 6; week++) {
      const days = []
      // 每周7天
      for (let day = 0; day < 7; day++) {
        days.push({ date: '', count: 0 })
      }
      weeks.push(days)
    }
    monthData.push(weeks)
  }
  
  return monthData
}

// 月份数据，每个月分别显示
const monthlyData = ref(initializeMonthlyData())

// 获取某个月特定星期几的所有日期
const getMonthDaysForWeekday = (monthData: DayData[][], dayOfWeek: number): DayData[] => {
  // 从每周数据中提取特定星期几的日期
  return monthData.map((week: DayData[]) => week[dayOfWeek])
}

// 更新显示数据
const updateDisplayData = () => {
  // 重置月份数据
  monthlyData.value = initializeMonthlyData()
  
  // 填充当前年份的数据
  currentYearData.value.forEach(item => {
    const date = dayjs(item.date)
    const month = date.month() // 0-11
    const dayOfMonth = date.date() - 1 // 0-30
    const dayOfWeek = date.day() // 0-6, 0是周日
    
    // 计算在哪一周
    const weekOfMonth = Math.floor(dayOfMonth / 7)
    
    // 安全检查，确保索引有效
    if (month >= 0 && month < 12 && 
        weekOfMonth >= 0 && weekOfMonth < 6 && 
        dayOfWeek >= 0 && dayOfWeek < 7) {
      
      // 更新日期数据
      monthlyData.value[month][weekOfMonth][dayOfWeek] = {
        date: item.date,
        count: item.count
      }
    }
  })
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  return dayjs(dateStr).format('YYYY年M月D日 dddd')
}

// 修改活动级别计算函数，将任意数值映射到0-4范围
const getActivityClass = (value: number) => {
  if (value <= 0) return 'activity-0'
  if (value <= 3) return 'activity-1'
  if (value <= 8) return 'activity-2'
  if (value <= 15) return 'activity-3'
  return 'activity-4' // 15以上都是最高级别
}

// 初始化
onMounted(() => {
  updateDisplayData()
})

// 监听数据变化
watch(() => props.data, () => {
  updateDisplayData()
}, { deep: true })
</script>

<style scoped>
.contribution-graph {
  padding: 0;
  font-size: 14px;
  position: relative;
  border-radius: 12px;
  transition: all 0.3s ease;
}

.year-selector {
  position: absolute;
  top: -50px;
  right: 0;
  z-index: 10;
  display: flex;
  align-items: center;
  gap: 8px;
  background-color: rgba(255, 255, 255, 0.9);
  padding: 6px 12px;
  border-radius: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  backdrop-filter: blur(8px);
}

.year-label {
  font-size: 13px;
  color: #64748b;
  font-weight: 600;
}

.year-selector :deep(.el-select) {
  width: 110px;
}

.year-selector :deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px rgba(31, 35, 41, 0.15);
  border-radius: 20px;
  transition: all 0.3s ease;
}

.year-selector :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #6366f1;
}

.graph-wrapper {
  display: flex;
  flex-direction: column;
  margin-top: 15px;
  background-color: white;
  border-radius: 16px;
  padding: 30px 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  animation: fadeIn 0.8s ease-out;
  position: relative;
  overflow: hidden;
}

.graph-wrapper::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #6366f1, #a5b4fc);
  z-index: 1;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 月份标题行 */
.months-header {
  display: flex;
  margin-bottom: 15px;
}

.weekday-label {
  width: 45px; /* 和weekdays宽度匹配 */
  flex-shrink: 0;
}

.months {
  display: flex;
  flex: 1;
}

.months span {
  flex: 1;
  text-align: center;
  font-size: 13px;
  font-weight: 600;
  color: #64748b;
  padding: 5px 0;
}

.month-label {
  position: relative;
  transition: all 0.3s ease;
}

.month-label:hover {
  color: #6366f1;
  transform: translateY(-2px);
}

/* 内容区域 */
.content-area {
  display: flex;
}

/* 星期几标签 */
.weekdays {
  display: flex;
  flex-direction: column;
  gap: 5px;
  width: 45px;
  flex-shrink: 0;
  padding-right: 5px;
}

.weekdays span {
  height: 20px;
  line-height: 20px;
  font-size: 12px;
  color: #64748b;
  text-align: right;
  padding-right: 10px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.weekdays span:hover {
  color: #6366f1;
  transform: translateX(-2px);
}

/* 日期网格容器 */
.grid-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 5px;
}

/* 每个星期几的行 */
.week-row {
  display: flex;
  height: 20px;
}

/* 每个月份的列 */
.month-column {
  flex: 1;
  display: flex;
  gap: 3px;
  justify-content: center;
}

/* 日期方块容器 */
.day-wrapper {
  position: relative;
  animation: popIn 0.5s cubic-bezier(0.34, 1.56, 0.64, 1) backwards;
}

@keyframes popIn {
  from {
    opacity: 0;
    transform: scale(0.5);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

/* 日期方块 */
.day {
  width: 16px;
  height: 16px;
  border-radius: 4px;
  background-color: #f1f5f9;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05), inset 0 1px 0 rgba(255, 255, 255, 0.15);
}

.day.has-data:hover {
  transform: scale(1.3) translateZ(5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.15);
  z-index: 10;
}

.day::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.2), rgba(255, 255, 255, 0));
  border-radius: inherit;
}

.day[data-empty="true"] {
  visibility: visible;
  background-color: rgba(241, 245, 249, 0.5);
  opacity: 0.6;
  cursor: default;
}

.day-count {
  font-size: 9px;
  color: white;
  font-weight: 600;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.day.has-data:hover .day-count {
  opacity: 1;
}

/* 全新的渐变配色 */
.activity-0 { 
  background-color: #f1f5f9; 
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.activity-1 { 
  background: linear-gradient(135deg, #93c5fd, #bfdbfe);
  box-shadow: 0 2px 4px rgba(147, 197, 253, 0.25);
}

.activity-2 { 
  background: linear-gradient(135deg, #818cf8, #a5b4fc);
  box-shadow: 0 2px 4px rgba(129, 140, 248, 0.25);
}

.activity-3 { 
  background: linear-gradient(135deg, #6366f1, #818cf8); 
  box-shadow: 0 2px 4px rgba(99, 102, 241, 0.3);
}

.activity-4 { 
  background: linear-gradient(135deg, #4f46e5, #6366f1);
  box-shadow: 0 2px 4px rgba(79, 70, 229, 0.4);
}

/* 提示框样式 */
.day-tooltip {
  position: absolute;
  top: -80px;
  left: 50%;
  transform: translateX(-50%) scale(0.8);
  background-color: #1e293b;
  color: #fff;
  padding: 10px 14px;
  border-radius: 8px;
  font-size: 12px;
  white-space: nowrap;
  pointer-events: none;
  z-index: 100;
  opacity: 0;
    visibility: hidden;
  transition: all 0.2s ease;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.2);
  text-align: center;
  line-height: 1.5;
}

.day-tooltip::after {
  content: '';
  position: absolute;
  bottom: -5px;
  left: 50%;
  transform: translateX(-50%);
  width: 0;
  height: 0;
  border-left: 6px solid transparent;
  border-right: 6px solid transparent;
  border-top: 6px solid #1e293b;
}

.day-wrapper:hover .day-tooltip {
  opacity: 1;
  visibility: visible;
  transform: translateX(-50%) scale(1);
  top: -70px;
}

.tooltip-date {
  font-weight: 600;
  margin-bottom: 2px;
  color: #e2e8f0;
}

.tooltip-count {
  font-size: 11px;
  color: #94a3b8;
}

.legend {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 20px;
  justify-content: flex-end;
  color: #64748b;
  font-size: 13px;
  padding: 8px 12px;
  background-color: white;
  border-radius: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
}

.legend .day {
  margin: 0 2px;
  width: 16px;
  height: 16px;
  cursor: default;
  animation: none;
}

.legend .day:hover {
  transform: none;
  box-shadow: none;
}

/* 暗色主题适配 */
@media (prefers-color-scheme: dark) {
  .year-selector {
    background-color: rgba(30, 41, 59, 0.9);
  }
  
  .graph-wrapper {
    background-color: #1e293b;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.25);
  }
  
  .activity-0 { 
    background-color: #334155; 
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
  }
  
  .activity-1 { 
    background: linear-gradient(135deg, #475569, #64748b);
    box-shadow: 0 2px 4px rgba(71, 85, 105, 0.4);
  }
  
  .activity-2 { 
    background: linear-gradient(135deg, #6366f1, #818cf8);
    box-shadow: 0 2px 4px rgba(99, 102, 241, 0.4);
  }
  
  .activity-3 { 
    background: linear-gradient(135deg, #8b5cf6, #a78bfa);
    box-shadow: 0 2px 4px rgba(139, 92, 246, 0.4);
  }
  
  .activity-4 { 
    background: linear-gradient(135deg, #c084fc, #d8b4fe);
    box-shadow: 0 2px 4px rgba(192, 132, 252, 0.4);
  }
  
  .legend {
    background-color: #1e293b;
  }
  
  .day[data-empty="true"] {
    background-color: rgba(51, 65, 85, 0.5);
  }
}

/* 响应式调整 */
@media screen and (max-width: 768px) {
  .year-selector {
    position: absolute;
    top: -50px;
    right: 0;
    justify-content: flex-end;
    padding: 8px 0;
  }
  
  .graph-wrapper {
    padding: 20px 10px;
  }
  
  .day {
    width: 12px;
    height: 12px;
    border-radius: 3px;
  }
  
  .weekdays {
    width: 35px;
  }
  
  .weekday-label {
    width: 35px;
  }
  
  .weekdays span {
    font-size: 10px;
    padding-right: 5px;
  }
  
  .months span {
    font-size: 10px;
  }
}
</style> 