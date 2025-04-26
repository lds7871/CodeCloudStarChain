// 从当前文档标题获取网站名称，如果没有则使用默认值
var siteName = document.title.split('-')[0] || '拾扒博客'

// 页面可见性变化监听
document.addEventListener("visibilitychange", () => {
    if (document.hidden) {
        // 离开此标签页
        document.title = "不要走啊o(>﹏<)o"
    } else {
        // 进入此标签页
        document.title = "欢迎回来O(∩_∩)O哈哈~"

        window.setTimeout(() => {
            document.title = `${siteName}` + "-一个专注于技术分享的博客平台"
        }, 800)
    }
}, false);

// 禁用右键菜单
document.addEventListener('contextmenu', function(e) {
    e.preventDefault();  // 阻止默认事件
});

// 禁止键盘F12键
document.addEventListener('keydown', function(e) {
    if(e.key == 'F12') {
        e.preventDefault(); // 如果按下键F12,阻止事件
    }
});

/* 控制台样式代码 */
var styleTitle1 = `
font-size: 20px;
font-weight: 600;
color: rgb(244,167,89);
`
var styleTitle2 = `
font-style: oblique;
font-size:14px;
color: rgb(244,167,89);
font-weight: 400;
`
var styleContent = `
color: rgb(30,152,255);
`

/* 控制台输出内容 */
var title = `🎮 ${siteName}`
// 获取当前网站地址
var offiUrl = window.location.origin
var content = `
版本号：1.0     
编译日期：${new Date()}
站点介绍：
1. 一个想当全栈程序员的个人博客.
🏠官网: ${offiUrl}
📭QQ/VX: 1248954763
`
console.log(`%c${title}\n%c${content}`, styleTitle1, styleContent)