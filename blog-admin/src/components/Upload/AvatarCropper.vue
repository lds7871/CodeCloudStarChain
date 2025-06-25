<template>
  <el-dialog v-model="visible" title="修改头像" width="800px" @close="close">
    <el-row>
      <el-col :xs="24" :md="12" :style="{height: '350px'}">
        <vue-cropper
          ref="cropper"
          :img="img"
          :autoCrop="true"
          :autoCropWidth="200"
          :autoCropHeight="200"
          :fixedBox="true"
          :outputType="'png'"
          :info="true"
          @realTime="realTimePreview"
        />
      </el-col>
      <el-col :xs="24" :md="12" :style="{height: '350px'}">
        <div class="avatar-upload-preview">
          <img :src="previews.url" :style="previews.img" v-if="previews.url" />
        </div>
      </el-col>
    </el-row>
    <br />
    <el-row>
      <el-col :lg="2" :sm="3" :xs="3">
        <el-upload :show-file-list="false" :before-upload="beforeUpload" accept="image/*">
          <el-button>选择图片</el-button>
        </el-upload>
      </el-col>
      <el-col :lg="{span: 1, offset: 2}" :sm="2" :xs="2">
        <el-button :icon="Plus" @click="changeScale(1)" :disabled="!img"></el-button>
      </el-col>
      <el-col :lg="{span: 1, offset: 1}" :sm="2" :xs="2">
        <el-button :icon="Minus" @click="changeScale(-1)" :disabled="!img"></el-button>
      </el-col>
      <el-col :lg="{span: 1, offset: 1}" :sm="2" :xs="2">
        <el-button :icon="RefreshLeft" @click="rotateLeft" :disabled="!img"></el-button>
      </el-col>
      <el-col :lg="{span: 1, offset: 1}" :sm="2" :xs="2">
        <el-button :icon="RefreshRight" @click="rotateRight" :disabled="!img"></el-button>
      </el-col>
      <el-col :lg="{span: 2, offset: 6}" :sm="2" :xs="2">
        <el-button type="primary" @click="submitCrop" :disabled="!img">修改</el-button>
      </el-col>
    </el-row>
  </el-dialog>
</template>

<script setup lang="ts">
import {ref, watch} from 'vue'
import 'vue-cropper/dist/index.css'
import {VueCropper} from 'vue-cropper'
import {uploadApi} from '@/api/file'
import {ElMessage} from 'element-plus'
import {Minus, Plus, RefreshLeft, RefreshRight} from '@element-plus/icons-vue'

const props = defineProps<{ modelValue: boolean }>()
const emit = defineEmits(['update:modelValue', 'success'])

const visible = ref(props.modelValue)
watch(() => props.modelValue, (v: boolean) => visible.value = v)
watch(visible, (v: boolean) => emit('update:modelValue', v))

const img = ref<string | null>(null)
const cropper = ref<any>(null)
const previews = ref<any>({})

function beforeUpload(file: File) {
  const reader = new FileReader()
  reader.onload = (e) => {
    img.value = e.target?.result as string
  }
  reader.readAsDataURL(file)
  return false // 阻止el-upload自动上传
}

function realTimePreview(data: any) {
  previews.value = data || {}
}

function changeScale(num: number) {
  if (cropper.value) {
    cropper.value.changeScale(num)
  }
}

function rotateLeft() {
  if (cropper.value) {
    cropper.value.rotateLeft()
  }
}

function rotateRight() {
  if (cropper.value) {
    cropper.value.rotateRight()
  }
}

async function submitCrop() {
  if (!cropper.value) return
  cropper.value.getCropBlob(async (blob: Blob) => {
    const formData = new FormData()
    formData.append('file', blob, 'avatar.png')
    try {
      const { data } = await uploadApi(formData, 'avatar')
      ElMessage.success('头像上传成功')
      emit('success', data) // 返回图片url
      visible.value = false
    } catch (e) {
      ElMessage.error('上传失败')
    }
  })
}
</script>

<style scoped>
.avatar-upload-preview {
  position: relative;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 200px;
  height: 200px;
  border-radius: 50%;
  box-shadow: 0 0 4px #ccc;
  overflow: hidden;
}
</style>