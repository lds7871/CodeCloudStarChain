<template>
    <el-dialog v-model="visible" title="裁剪头像" width="600px" @close="close">
      <vue-cropper
        ref="cropper"
        :img="img"
        :autoCrop="true"
        :autoCropWidth="200"
        :autoCropHeight="200"
        :fixedBox="true"
        :outputType="'png'"
        style="height: 300px; width: 100%;"
      />
      <template #footer>
        <el-upload
          :show-file-list="false"
          :before-upload="beforeUpload"
          accept="image/*"
        >
          <el-button>选择图片</el-button>
        </el-upload>
        <el-button type="primary" @click="submitCrop">上传头像</el-button>
        <el-button @click="close">取消</el-button>
      </template>
    </el-dialog>
  </template>
  
  <script setup lang="ts">
  import {ref, watch} from 'vue'
  import 'vue-cropper/dist/index.css'
  import {VueCropper} from "vue-cropper"
  import {uploadApi} from '@/api/file'
  import {ElMessage} from 'element-plus'

  const props = defineProps<{
    modelValue: boolean
  }>()
  const emit = defineEmits(['update:modelValue', 'success'])
  
  const visible = ref(props.modelValue)
  watch(() => props.modelValue, v => visible.value = v)
  watch(visible, v => emit('update:modelValue', v))
  
  const img = ref<string | null>(null)
  const cropper = ref()
  
  function beforeUpload(file: File) {
    const reader = new FileReader()
    reader.onload = (e) => {
      img.value = e.target?.result as string
    }
    reader.readAsDataURL(file)
    return false // 阻止el-upload自动上传
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
  
  function close() {
    visible.value = false
    img.value = null
  }
  </script>