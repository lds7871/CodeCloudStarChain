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

// 消除控制台打印
var HoldLog = console.log;
console.log = function () {};
let now1 = new Date();
queueMicrotask(() => {
    const Log = function () {
        HoldLog.apply(console, arguments);
    }; //在恢复前输出日志
    const grt = new Date("04/10/2025 00:00:00"); //此处修改你的建站时间或者网站上线时间
    now1.setTime(now1.getTime() + 250);
    const days = (now1 - grt) / 1000 / 60 / 60 / 24;
    const dnum = Math.floor(days);
    const ascll = [
        `欢迎使用码云星链!`,
        `生活明朗, 万物可爱`,
        `
    ██████╗ ██████╗  ██████╗ ███╗   ██╗██╗   ██╗
    ██╔══██╗██╔══██╗██╔═══██╗████╗  ██║╚██╗ ██╔╝
    ██████╔╝██████╔╝██║   ██║██╔██╗ ██║ ╚████╔╝ 
    ██╔══██╗██╔══██╗██║   ██║██║╚██╗██║  ╚██╔╝  
    ██████╔╝██║  ██║╚██████╔╝██║ ╚████║   ██║   
    ╚═════╝ ╚═╝  ╚═╝ ╚═════╝ ╚═╝  ╚═══╝   ╚═╝   
        `,
        "已上线",
        dnum,
        "天",
        "©2025 By 码云星链 V1.0.0",
    ];
    const ascll2 = [`NCC2-036`, `调用前置摄像头拍照成功，识别为【小笨蛋】.`, `Photo captured: `, `🤪`];

    setTimeout(
        Log.bind(
            console,
            `\n%c${ascll[0]} %c ${ascll[1]} %c ${ascll[2]} %c${ascll[3]}%c ${ascll[4]}%c ${ascll[5]}\n\n%c ${ascll[6]}\n`,
            "color:#3b70fc",
            "",
            "color:#3b70fc",
            "color:#3b70fc",
            "",
            "color:#3b70fc",
            ""
        )
    );
    setTimeout(
        Log.bind(
            console,
            `%c ${ascll2[0]} %c ${ascll2[1]} %c \n${ascll2[2]} %c\n${ascll2[3]}\n`,
            "color:white; background-color:#4fd953",
            "",
            "",
            'background:url("https://npm.elemecdn.com/anzhiyu-blog@1.1.6/img/post/common/tinggge.gif") no-repeat;font-size:450%'
        )
    );

    setTimeout(Log.bind(console, "%c WELCOME %c 你好，小笨蛋.", "color:white; background-color:#4f90d9", ""));

    setTimeout(
        console.warn.bind(
            console,
            "%c ⚡ Powered by 码云星链 %c 你正在访问 brony 的博客.",
            "color:white; background-color:#f0ad4e",
            ""
        )
    );

    setTimeout(Log.bind(console, "%c W23-12 %c 你已打开控制台.", "color:white; background-color:#4f90d9", ""));

    setTimeout(
        console.warn.bind(console, "%c S013-782 %c 你现在正处于监控中.", "color:white; background-color:#d9534f", "")
    );
});