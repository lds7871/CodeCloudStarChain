class MetingJSElement extends HTMLElement {

  connectedCallback() {
    if (window.APlayer && window.fetch) {
      this._init()
      this._parse()
    }
  }

  disconnectedCallback() {
    if (!this.lock) {
      this.aplayer.destroy()
    }
  }

  _camelize(str) {
    return str
      .replace(/^[_.\- ]+/, '')
      .toLowerCase()
      .replace(/[_.\- ]+(\w|$)/g, (m, p1) => p1.toUpperCase())
  }

  _init() {
    let config = {}
    for (let i = 0; i < this.attributes.length; i += 1) {
      config[this._camelize(this.attributes[i].name)] = this.attributes[i].value
    }
    let keys = [
      'server', 'type', 'id', 'api', 'auth',
      'auto', 'lock',
      'name', 'title', 'artist', 'author', 'url', 'cover', 'pic', 'lyric', 'lrc',
    ]
    this.meta = {}
    for (let key of keys) {
      this.meta[key] = config[key]
      delete config[key]
    }
    this.config = config

    console.log('Meting.js: 解析的配置:', config);
    console.log('Meting.js: 解析的meta:', this.meta);

    this.api = this.meta.api || window.meting_api || 'https://api.injahow.cn/meting/?server=:server&type=:type&id=:id&r=:r'
    if (this.meta.auto) this._parse_link()
  }

  _parse_link() {
    let rules = [
      ['music.163.com.*song.*id=(\\d+)', 'netease', 'song'],
      ['music.163.com.*album.*id=(\\d+)', 'netease', 'album'],
      ['music.163.com.*artist.*id=(\\d+)', 'netease', 'artist'],
      ['music.163.com.*playlist.*id=(\\d+)', 'netease', 'playlist'],
      ['music.163.com.*discover/toplist.*id=(\\d+)', 'netease', 'playlist'],
      ['y.qq.com.*song/(\\w+).html', 'tencent', 'song'],
      ['y.qq.com.*album/(\\w+).html', 'tencent', 'album'],
      ['y.qq.com.*singer/(\\w+).html', 'tencent', 'artist'],
      ['y.qq.com.*playsquare/(\\w+).html', 'tencent', 'playlist'],
      ['y.qq.com.*playlist/(\\w+).html', 'tencent', 'playlist'],
      ['xiami.com.*song/(\\w+)', 'xiami', 'song'],
      ['xiami.com.*album/(\\w+)', 'xiami', 'album'],
      ['xiami.com.*artist/(\\w+)', 'xiami', 'artist'],
      ['xiami.com.*collect/(\\w+)', 'xiami', 'playlist'],
    ]

    for (let rule of rules) {
      let patt = new RegExp(rule[0])
      let res = patt.exec(this.meta.auto)
      if (res !== null) {
        this.meta.server = rule[1]
        this.meta.type = rule[2]
        this.meta.id = res[1]
        return
      }
    }
  }

  _parse() {
    if (this.meta.url) {
      let result = {
        name: this.meta.name || this.meta.title || 'Audio name',
        artist: this.meta.artist || this.meta.author || 'Audio artist',
        url: this.meta.url,
        cover: this.meta.cover || this.meta.pic,
        lrc: this.meta.lrc || this.meta.lyric || '',
        type: this.meta.type || 'auto',
      }
      if (!result.lrc) {
        this.meta.lrcType = 0
      }
      if (this.innerText) {
        result.lrc = this.innerText
        this.meta.lrcType = 2
      }
      this._loadPlayer([result])
      return
    }

    let url = this.api
      .replace(':server', this.meta.server)
      .replace(':type', this.meta.type)
      .replace(':id', this.meta.id)
      .replace(':auth', this.meta.auth)
      .replace(':r', Math.random())

    console.log('Meting.js: 调用API:', url);

    fetch(url)
      .then(res => res.json())
      .then(result => this._loadPlayer(result))
  }

  _loadPlayer(data) {

    let defaultOption = {
      audio: data,
      mutex: true,
      lrcType: this.meta.lrcType || 3,
      storageName: 'metingjs',
      listFolded: window.innerWidth < 768 ? true : false
    }

    if (!data.length) {
      console.error('Meting.js: 没有音频数据');
      return;
    }

    let options = {
      ...defaultOption,
      ...this.config,
    }
    for (let optkey in options) {
      if (options[optkey] === 'true' || options[optkey] === 'false') {
        options[optkey] = (options[optkey] === 'true')
      }
    }

    console.log('Meting.js: 播放器配置:', options);

    let div = document.createElement('div')
    options.container = div
    this.appendChild(div)

    this.aplayer = new APlayer(options)
    window.ap = this.aplayer;
    console.log('Meting.js: APlayer实例创建完成:', this.aplayer);

      pyj.setupMediaSessionHandlers(this.aplayer);

    // 触发全局事件，通知其他组件播放器已就绪
    console.log('Meting.js: 触发music-player-ready事件...');
    window.dispatchEvent(new CustomEvent('music-player-ready', {
      detail: { player: this.aplayer }
    }));
  }

}

if (window.customElements && !window.customElements.get('meting-js')) {
  window.MetingJSElement = MetingJSElement
  window.customElements.define('meting-js', MetingJSElement)
}