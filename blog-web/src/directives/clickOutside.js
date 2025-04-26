/**
 * 点击元素外部指令
 * 用于实现当点击指定元素外部时触发回调的功能
 * 常用于下拉菜单、弹出框等组件的关闭处理
 */
export default {
  // 指令绑定时调用
  bind(el, binding) {
    // 创建点击事件处理函数
    el.clickOutsideEvent = function(event) {
      // 判断点击的目标是否在元素外部
      // el === event.target：判断点击的是否是当前元素
      // el.contains(event.target)：判断点击的是否是当前元素的子元素
      if (!(el === event.target || el.contains(event.target))) {
        // 如果点击在元素外部，则调用绑定的回调函数
        binding.value(event)
      }
    }
    // 在document上添加点击事件监听
    document.addEventListener('click', el.clickOutsideEvent)
  },
  
  // 指令解绑时调用
  unbind(el) {
    // 移除事件监听，防止内存泄漏
    document.removeEventListener('click', el.clickOutsideEvent)
  }
}