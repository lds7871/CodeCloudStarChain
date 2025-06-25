import request from '@/utils/request'

/**
 * 获取dashboard数据
 */
export function getDashboardDataApi() {
  return request({
    url: '/sys/dashboard',
    method: 'get'
  })
}

// 获取文章统计数据
export function getBottomDataApi() {
  return request({
    url: '/sys/dashboard/bottom',
    method: 'get'
  })
}

// 获取访问趋势数据
export function getVisitTrendApi(type: string) {
  return request({
    url: '/sys/dashboard/trend',
    method: 'get',
    params: { type }
  })
}
