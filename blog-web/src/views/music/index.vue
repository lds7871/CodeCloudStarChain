<template>
  <div class="music-player-page">
    <div class="music-header">
      <h1>音乐播放器</h1>
      <p>基于HeoMusic的Vue音乐播放器组件</p>
    </div>
    
    <div class="music-controls">
      <el-form :model="musicConfig" label-width="120px" class="config-form">
        <el-form-item label="音乐平台">
          <el-select v-model="musicConfig.userServer" placeholder="选择音乐平台">
            <el-option label="腾讯音乐" value="tencent"></el-option>
            <el-option label="网易云音乐" value="netease"></el-option>
            <el-option label="酷狗音乐" value="kugou"></el-option>
            <el-option label="小米音乐" value="xiami"></el-option>
            <el-option label="百度音乐" value="baidu"></el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="播放列表ID">
          <el-input v-model="musicConfig.userId" placeholder="请输入播放列表ID"></el-input>
        </el-form-item>
        
        <el-form-item label="播放类型">
          <el-select v-model="musicConfig.userType" placeholder="选择播放类型">
            <el-option label="歌单" value="playlist"></el-option>
            <el-option label="单曲" value="song"></el-option>
            <el-option label="专辑" value="album"></el-option>
            <el-option label="搜索结果" value="search"></el-option>
            <el-option label="歌手" value="artist"></el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="updateMusic">更新播放器</el-button>
          <el-button @click="resetConfig">重置配置</el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <div class="music-player-container">
      <HeoMusic 
        :key="playerKey"
        :userId="musicConfig.userId"
        :userServer="musicConfig.userServer"
        :userType="musicConfig.userType"
        :localMusic="localMusic"
        :remoteMusic="remoteMusic"
      />
    </div>
    
    <div class="music-info">
      <h3>使用说明</h3>
      <ul>
        <li><strong>空格键</strong>：暂停/播放音乐</li>
        <li><strong>上/下方向键</strong>：增加/减少音量</li>
        <li><strong>左/右方向键</strong>：上一曲/下一曲</li>
        <li><strong>播放列表ID</strong>：可以从音乐歌单分享的链接中获取</li>
      </ul>
      
      <h3>示例歌单</h3>
      <div class="example-playlists">
        <el-button 
          v-for="playlist in examplePlaylists" 
          :key="playlist.id"
          size="small"
          @click="loadPlaylist(playlist)"
        >
          {{ playlist.name }}
        </el-button>
      </div>
    </div>
  </div>
</template>

<script>
import HeoMusic from '@/components/HeoMusic.vue'

export default {
  name: 'MusicPlayer',
  components: {
    HeoMusic
  },
  data() {
    return {
      playerKey: 0, // 用于强制重新渲染组件
      musicConfig: {
        userId: '8668419170',
        userServer: 'tencent',
        userType: 'playlist'
      },
      localMusic: [],
      remoteMusic: '',
      examplePlaylists: [
        {
          id: 1,
          name: 'HeoMusic Top',
          userId: '8668419170',
          userServer: 'tencent',
          userType: 'playlist'
        },
        {
          id: 2,
          name: '鸡你太美',
          userId: '2762963245',
          userServer: 'netease',
          userType: 'playlist'
        },
        {
          id: 3,
          name: '经典老歌',
          userId: '8152976493',
          userServer: 'netease',
          userType: 'playlist'
        }
      ]
    }
  },
  methods: {
    updateMusic() {
      // 通过改变key来强制重新渲染组件
      this.playerKey++
      this.$message.success('播放器已更新')
    },
    
    resetConfig() {
      this.musicConfig = {
        userId: '8668419170',
        userServer: 'tencent',
        userType: 'playlist'
      }
      this.playerKey++
      this.$message.success('配置已重置')
    },
    
    loadPlaylist(playlist) {
      this.musicConfig = {
        userId: playlist.userId,
        userServer: playlist.userServer,
        userType: playlist.userType
      }
      this.playerKey++
      this.$message.success(`已加载歌单：${playlist.name}`)
    }
  }
}
</script>

<style scoped>
.music-player-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.music-header {
  text-align: center;
  margin-bottom: 30px;
}

.music-header h1 {
  color: #409EFF;
  margin-bottom: 10px;
}

.music-header p {
  color: #666;
  font-size: 16px;
}

.music-controls {
  background: #f5f7fa;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 30px;
}

.config-form {
  max-width: 500px;
  margin: 0 auto;
}



.music-info h3 {
  color: #409EFF;
  margin-bottom: 15px;
}

.music-info ul {
  margin-bottom: 20px;
}

.music-info li {
  margin-bottom: 8px;
  color: #666;
}

.example-playlists {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.example-playlists .el-button {
  margin-bottom: 10px;
}
</style> 