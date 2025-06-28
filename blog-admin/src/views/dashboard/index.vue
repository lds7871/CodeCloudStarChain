<template>
  <div class="dashboard-container">
    <!-- 数据卡片 -->
    <el-row :gutter="24">
      <el-col :xs="24" :sm="12" :md="6" v-for="(item, index) in statistics" :key="item.title">
        <el-card 
          shadow="hover" 
          :body-style="{ padding: '24px' }"
          class="data-card"
          :style="{ animationDelay: `${index * 0.1}s` }"
        >
          <div class="card-content">
            <div class="icon-wrapper" :class="item.type">
              <el-icon><component :is="item.icon" /></el-icon>
            </div>
            <div class="data-wrapper">
              <count-to
                :start-val="0"
                :end-val="item.value"
                :duration="2000"
                class="card-value"
                :separator="','"
              />
              <div class="card-title">{{ item.title }}</div>
              <div class="card-trend" v-if="item.trend">
                <el-tag size="small" :type="item.trend > 0 ? 'success' : 'danger'" effect="light" class="trend-tag">
                  <el-icon class="trend-icon">
                    <component :is="item.trend > 0 ? icons.CaretTop : icons.CaretBottom" />
                  </el-icon>
                  {{ Math.abs(item.trend) }}%
                </el-tag>
                <span class="trend-period">较上周</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 文章贡献图 -->
    <el-row :gutter="24" class="chart-row">
      <el-col :span="24">
        <el-card shadow="hover" class="chart-card contribution-card">
          <template #header>
            <div class="card-header">
              <div class="header-left">
                <span class="title">文章贡献图</span>
                <el-tag size="small" effect="plain" class="tag">年度统计</el-tag>
              </div>
              <el-tooltip content="显示您一年内发布文章的情况" placement="top">
                <el-icon class="info-icon"><InfoFilled /></el-icon>
              </el-tooltip>
            </div>
          </template>
          <ContributionGraph :data="contributionData" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="24" class="chart-row">
      <el-col :xs="24" :sm="24" :md="16" class="chart-col">
        <el-card shadow="hover" class="chart-card trend-card">
          <template #header>
            <div class="card-header">
              <div class="header-left">
                <span class="title">访问趋势</span>
                <el-tag size="small" effect="plain" class="tag">{{ trendTimeRange === 'week' ? '周统计' : '月统计' }}</el-tag>
              </div>
              <div class="header-actions">
                <el-radio-group v-model="trendTimeRange" size="small">
                  <el-radio-button label="week">本周</el-radio-button>
                  <el-radio-button label="month">本月</el-radio-button>
                </el-radio-group>
              </div>
            </div>
          </template>
          <div ref="lineChartRef" class="chart"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="24" :md="8" class="chart-col">
        <el-card shadow="hover" class="chart-card pie-card">
          <template #header>
            <div class="card-header">
              <div class="header-left">
                <span class="title">分类统计</span>
                <el-tag size="small" effect="plain" class="tag">占比</el-tag>
              </div>
              <el-dropdown trigger="click" size="small">
                <el-icon class="more-icon"><MoreFilled /></el-icon>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item>查看详情</el-dropdown-item>
                    <el-dropdown-item>刷新</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </template>
          <div ref="pieChartRef" class="chart"></div>
        </el-card>
      </el-col>
    </el-row>

  </div>
</template>

<script setup lang="ts">
import * as echarts from 'echarts'
import type { EChartsOption } from 'echarts'
import { 
  CaretTop, 
  CaretBottom,
  Document,
  Collection,
  ChatLineRound,
  View,
  InfoFilled,
  MoreFilled
} from '@element-plus/icons-vue'
import CountTo from '@/views/dashboard/components/CountTo.vue'
import ContributionGraph from './components/ContributionGraph.vue'
import { getDashboardDataApi, getBottomDataApi, getVisitTrendApi } from '@/api/system'

const icons = {
  Document: markRaw(Document),
  Collection: markRaw(Collection),
  ChatLineRound: markRaw(ChatLineRound),
  View: markRaw(View),
  CaretTop: markRaw(CaretTop),
  CaretBottom: markRaw(CaretBottom),
  InfoFilled: markRaw(InfoFilled),
  MoreFilled: markRaw(MoreFilled)
}

const statistics = ref([
  { 
    title: '文章总数', 
    value: 0, 
    type: 'primary',
    icon: icons.Document,
    trend: 5
  },
  { 
    title: '用户总数', 
    value: 0, 
    type: 'success',
    icon: icons.Collection,
    trend: 2
  },
  { 
    title: '留言总数', 
    value: 0, 
    type: 'warning',
    icon: icons.ChatLineRound,
    trend: -1
  },
  { 
    title: '访问量', 
    value: 0, 
    type: 'info',
    icon: icons.View,
    trend: 12
  }
])

const contributionData = ref([])
const trendTimeRange = ref('week')

// 图表相关
const lineChartRef = ref<HTMLElement>()
const pieChartRef = ref<HTMLElement>()
const lineChart = shallowRef<echarts.ECharts | null>(null)
const pieChart = shallowRef<echarts.ECharts | null>(null)

// 折线图配置
const getLineChartOption = (): EChartsOption => ({
  color: ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de', '#3ba272', '#fc8452', '#9a60b4'],
  tooltip: {
    trigger: 'axis',
    backgroundColor: 'rgba(255, 255, 255, 0.95)',
    borderWidth: 1,
    borderColor: '#f0f0f0',
    borderRadius: 8,
    textStyle: {
      color: '#333'
    },
    axisPointer: {
      type: 'shadow',
      shadowStyle: {
        color: 'rgba(0, 0, 0, 0.05)'
      }
    },
    padding: [12, 16],
    extraCssText: 'box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);'
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    containLabel: true
  },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
    axisLine: {
      lineStyle: {
        color: '#eaeaea'
      }
    },
    axisLabel: {
      color: '#909399',
      fontWeight: 500,
      fontSize: 12
    },
    axisTick: {
      alignWithLabel: true,
      lineStyle: {
        color: '#eaeaea'
      }
    }
  },
  yAxis: {
    type: 'value',
    splitLine: {
      lineStyle: {
        color: '#f5f5f5',
        type: 'dashed'
      }
    },
    axisLabel: {
      color: '#909399',
      fontWeight: 500,
      fontSize: 12,
      formatter: '{value}'
    },
    axisLine: {
      show: false
    },
    axisTick: {
      show: false
    }
  },
  series: [
    {
      name: '访问量',
      type: 'line',
      smooth: true,
      data: [820, 932, 901, 934, 1290, 1330, 1320],
      areaStyle: {
        opacity: 0.4,
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(80, 141, 255, 0.8)' },
          { offset: 0.5, color: 'rgba(80, 141, 255, 0.3)' },
          { offset: 1, color: 'rgba(80, 141, 255, 0.1)' }
        ])
      },
      itemStyle: {
        color: '#508dff',
        borderWidth: 3,
        borderColor: 'rgba(80, 141, 255, 0.2)',
        shadowColor: 'rgba(80, 141, 255, 0.5)',
        shadowBlur: 10
      },
      lineStyle: {
        width: 4,
        shadowColor: 'rgba(80, 141, 255, 0.2)',
        shadowBlur: 10
      },
      symbol: 'circle',
      symbolSize: 10,
      emphasis: {
        scale: true,
        itemStyle: {
          borderWidth: 3,
          borderColor: '#fff',
          shadowBlur: 12,
          shadowColor: 'rgba(80, 141, 255, 0.8)'
        }
      }
    },
    {
      name: '浏览量',
      type: 'line',
      smooth: true,
      data: [620, 732, 701, 734, 1090, 1130, 1120],
      areaStyle: {
        opacity: 0.4,
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(65, 212, 146, 0.8)' },
          { offset: 0.5, color: 'rgba(65, 212, 146, 0.3)' },
          { offset: 1, color: 'rgba(65, 212, 146, 0.1)' }
        ])
      },
      itemStyle: {
        color: '#41d492',
        borderWidth: 3,
        borderColor: 'rgba(65, 212, 146, 0.2)',
        shadowColor: 'rgba(65, 212, 146, 0.5)',
        shadowBlur: 10
      },
      lineStyle: {
        width: 4,
        shadowColor: 'rgba(65, 212, 146, 0.2)',
        shadowBlur: 10
      },
      symbol: 'circle',
      symbolSize: 10,
      emphasis: {
        scale: true,
        itemStyle: {
          borderWidth: 3,
          borderColor: '#fff',
          shadowBlur: 12,
          shadowColor: 'rgba(65, 212, 146, 0.8)'
        }
      }
    }
  ],
  animation: true,
  animationDuration: 2000,
  animationDelay: function (idx: number) {
    return idx * 150;
  },
  animationEasing: 'cubicOut'
})

// 饼图配置
const getPieChartOption = (): EChartsOption => ({
  color: [
    '#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de', 
    '#3ba272', '#fc8452', '#9a60b4', '#ea7ccc', '#007acc',
    '#41d492', '#508dff', '#ff9668', '#cf6dcf', '#ffd955'
  ],
  tooltip: {
    trigger: 'item',
    backgroundColor: 'rgba(255, 255, 255, 0.95)',
    borderWidth: 1,
    borderColor: '#f0f0f0',
    borderRadius: 8,
    textStyle: {
      color: '#333'
    },
    formatter: '{a} <br/>{b}: {c} ({d}%)',
    padding: [12, 16],
    extraCssText: 'box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);'
  },
  legend: {
    orient: 'vertical',
    left: '5%',
    top: 'center',
    itemGap: 16,
    textStyle: {
      color: '#909399',
      fontSize: 12,
      fontWeight: 500
    },
    icon: 'circle',
    itemWidth: 10,
    itemHeight: 10,
    pageIconSize: 12,
    pageTextStyle: {
      color: '#909399'
    },
    pageIconColor: '#909399',
    pageIconInactiveColor: '#d9d9d9'
  },
  series: [{
    name: '分类统计',
    type: 'pie',
    radius: ['45%', '70%'],
    center: ['65%', '50%'],
    avoidLabelOverlap: true,
    itemStyle: {
      borderRadius: 10,
      borderColor: '#fff',
      borderWidth: 2,
      shadowBlur: 10,
      shadowColor: 'rgba(0, 0, 0, 0.1)'
    },
    label: {
      show: false,
      position: 'center'
    },
    emphasis: {
      focus: 'series',
      scaleSize: 15,
      label: {
        show: true,
        fontSize: 16,
        fontWeight: 'bold',
        color: '#303133'
      },
      itemStyle: {
        shadowBlur: 20,
        shadowOffsetX: 0,
        shadowColor: 'rgba(0, 0, 0, 0.3)'
      }
    },
    labelLine: {
      show: false
    },
    data: [] as any[],
    animationType: 'scale',
    animationEasing: 'elasticOut',
    animationDelay: function (idx: number) {
      return idx * 100;
    }
  }]
})

// 初始化图表
const initCharts = () => {
  // 获取底部分类数据
  getBottomDataApi().then(res => {
    if (pieChartRef.value) {
      pieChart.value = echarts.init(pieChartRef.value)
        const option = getPieChartOption()
        if (option.series && Array.isArray(option.series)) {
          option.series[0].data = res.data
        }
        pieChart.value?.setOption(option)
    }
  })

  // 获取访问趋势数据
  loadTrendData()
}

// 加载访问趋势数据
const loadTrendData = () => {
  getVisitTrendApi(trendTimeRange.value).then(res => {
    if (lineChartRef.value) {
      if (!lineChart.value) {
        lineChart.value = echarts.init(lineChartRef.value)
      }
      
      const option = getLineChartOption()
      
      // 使用后端返回的数据
      if (res.data && res.data.dateLabels) {
        option.xAxis = {
          ...option.xAxis,
          data: res.data.dateLabels
        }
        
        if (Array.isArray(option.series)) {
          option.series[0].data = res.data.visitData || [] 
          option.series[1].data = res.data.viewData || []
        }
      }
      
      lineChart.value.setOption(option, true)
    }
  })
}

// 处理窗口大小变化
const handleResize = () => {
  lineChart.value?.resize()
  pieChart.value?.resize()
}

// 监听趋势时间范围变化
watch(trendTimeRange, () => {
  // 重新加载趋势数据
  loadTrendData()
})

onMounted(() => {
  getDashboardDataApi().then(res => {
    statistics.value[0].value = res.data.articleCount
    statistics.value[1].value = res.data.userCount
    statistics.value[2].value = res.data.messageCount
    statistics.value[3].value = res.data.visitCount
    contributionData.value = res.data.contributionData
    initCharts()
  })
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  lineChart.value?.dispose()
  pieChart.value?.dispose()
})
</script>

<style scoped>
.dashboard-container {
  padding: 10px 10px;
}

/* 数据卡片样式 */
.data-card {
  animation: slideUp 0.8s ease-out forwards;
  opacity: 0;
  transform: translateY(20px);
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.05);
  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
  margin-bottom: 20px;
  position: relative;
  z-index: 1;
  background-color: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.data-card::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(120deg, rgba(255,255,255,0) 30%, rgba(255,255,255,0.6) 50%, rgba(255,255,255,0) 70%);
  z-index: -1;
  transform: translateX(-100%);
  transition: transform 0.8s ease;
}

.data-card:hover {
  transform: translateY(-8px) scale(1.02);
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.1);
}

.data-card:hover::after {
  transform: translateX(100%);
}

@keyframes slideUp {
  0% {
    opacity: 0;
    transform: translateY(40px);
  }
  50% {
    opacity: 0.8;
  }
  100% {
    opacity: 1;
    transform: translateY(0);
  }
}

.card-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.icon-wrapper {
  width: 68px;
  height: 68px;
  border-radius: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.5s cubic-bezier(0.34, 1.56, 0.64, 1);
  position: relative;
  overflow: hidden;
}

.icon-wrapper::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.05);
  opacity: 0;
  transition: opacity 0.3s ease;
  border-radius: inherit;
  z-index: 0;
}

.icon-wrapper::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.4), rgba(255, 255, 255, 0));
  border-radius: inherit;
  z-index: 1;
}

.icon-wrapper:hover {
  transform: scale(1.1) rotate(8deg);
}

.icon-wrapper:hover::before {
  opacity: 1;
}

.icon-wrapper .el-icon {
  font-size: 34px;
  color: #fff;
  z-index: 2;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.2));
  transition: transform 0.3s ease;
}

.icon-wrapper:hover .el-icon {
  transform: scale(1.1);
  animation: pulse 1s infinite alternate;
}

@keyframes pulse {
  0% { transform: scale(1); }
  100% { transform: scale(1.15); }
}

.icon-wrapper.primary {
  background: linear-gradient(135deg, #0066ff, #4a94ff);
  box-shadow: 0 8px 20px rgba(0, 102, 255, 0.25), inset 0 2px 0 rgba(255, 255, 255, 0.1);
}

.icon-wrapper.success {
  background: linear-gradient(135deg, #28c76f, #48da89);
  box-shadow: 0 8px 20px rgba(40, 199, 111, 0.25), inset 0 2px 0 rgba(255, 255, 255, 0.1);
}

.icon-wrapper.warning {
  background: linear-gradient(135deg, #ff9500, #ffb340);
  box-shadow: 0 8px 20px rgba(255, 149, 0, 0.25), inset 0 2px 0 rgba(255, 255, 255, 0.1);
}

.icon-wrapper.info {
  background: linear-gradient(135deg, #00cfdd, #47eaf8);
  box-shadow: 0 8px 20px rgba(0, 207, 221, 0.25), inset 0 2px 0 rgba(255, 255, 255, 0.1);
}

.data-wrapper {
  flex: 1;
}

.card-value {
  font-size: 32px;
  font-weight: 700;
  line-height: 1.1;
  margin-bottom: 10px;
  position: relative;
  display: inline-block;
}

.card-value::after {
  content: "";
  position: absolute;
  bottom: -4px;
  left: 0;
  width: 30px;
  height: 3px;
  background: linear-gradient(90deg, var(--el-color-primary), transparent);
  border-radius: 3px;
}

.card-title {
  font-size: 15px;
  color: #909399;
  margin-bottom: 8px;
  font-weight: 500;
}

.card-trend {
  display: flex;
  align-items: center;
  gap: 8px;
}

.trend-tag {
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 2px;
}

.trend-icon {
  font-size: 12px;
  margin-right: 2px;
}

.trend-period {
  font-size: 12px;
  color: #909399;
}

/* 图表区域样式 */
.chart-row {
  margin-top: 20px;
}

.chart-col {
  transition: all 0.3s ease;
}

.chart {
  height: 350px;
  width: 100%;
  position: relative;
}

.chart-card {
  height: 100%;
  border-radius: 16px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.05);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  overflow: hidden;
  animation: fadeIn 0.8s ease-out forwards;
  opacity: 0;
  transform: translateY(20px);
  background-color: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

@keyframes fadeIn {
  0% {
    opacity: 0;
    transform: translateY(30px);
  }
  100% {
    opacity: 1;
    transform: translateY(0);
  }
}

.chart-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.1);
}

.contribution-card {
  position: relative;
  animation-delay: 0.1s;
}

.trend-card {
  animation-delay: 0.2s;
}

.pie-card {
  animation-delay: 0.3s;
}

/* 卡片标题样式 */
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  background-color: rgba(250, 250, 250, 0.5);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.tag {
  margin-top: 1px;
  font-weight: normal;
  border-radius: 6px;
  padding: 0 8px;
  transition: all 0.3s ease;
}

.title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  position: relative;
  padding-left: 12px;
}

.title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 16px;
  background: var(--el-color-primary);
  border-radius: 2px;
}

.info-icon,
.more-icon {
  cursor: pointer;
  transition: transform 0.3s, color 0.3s;
  font-size: 18px;
  color: #909399;
  border-radius: 50%;
  padding: 6px;
}

.info-icon:hover,
.more-icon:hover {
  transform: scale(1.1);
  color: var(--el-color-primary);
  background-color: rgba(64, 158, 255, 0.1);
}

.header-actions {
  display: flex;
  align-items: center;
}

.header-actions :deep(.el-radio-button__inner) {
  border-radius: 4px;
  padding: 6px 14px;
  transition: all 0.3s ease;
}

.header-actions :deep(.el-radio-button:first-child .el-radio-button__inner) {
  border-radius: 4px 0 0 4px;
}

.header-actions :deep(.el-radio-button:last-child .el-radio-button__inner) {
  border-radius: 0 4px 4px 0;
}

/* 暗色主题适配 */
@media (prefers-color-scheme: dark) {
  .data-card {
    background-color: rgba(30, 30, 30, 0.8);
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.2);
    border: 1px solid rgba(255, 255, 255, 0.05);
  }
  
  .data-card:hover {
    box-shadow: 0 15px 30px rgba(0, 0, 0, 0.25);
  }

  .card-value {
    color: #e6e6e6;
  }

  .chart-card {
    background-color: rgba(30, 30, 30, 0.8);
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.2);
    border: 1px solid rgba(255, 255, 255, 0.05);
  }
  
  .chart-card:hover {
    box-shadow: 0 15px 30px rgba(0, 0, 0, 0.25);
  }
  
  .title {
    color: #e6e6e6;
  }
  
  .card-header {
    border-bottom: 1px solid rgba(255, 255, 255, 0.05);
    background-color: rgba(40, 40, 40, 0.5);
  }
}

/* 响应式调整 */
@media screen and (max-width: 768px) {
  .dashboard-container {
    padding: 10px 5px;
  }
  
  .card-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
  
  .icon-wrapper {
    width: 56px;
    height: 56px;
    border-radius: 14px;
  }
  
  .icon-wrapper .el-icon {
    font-size: 28px;
  }
  
  .card-value {
    font-size: 28px;
  }
  
  .card-title {
    font-size: 14px;
  }
  
  .chart {
    height: 280px;
  }
  
  .chart-card {
    margin-bottom: 20px;
  }
  
  .card-header {
    padding: 12px 15px;
  }
  
  .title {
    font-size: 15px;
  }
}
</style> 