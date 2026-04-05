<template>
  <div class="dir-page">
    <h1 class="page-title">INPUT目录 - 上传文件</h1>
    
    <el-card>
      <template #header>
        <div class="card-header">
          <span>文件列表</span>
          <el-button type="primary" @click="loadFiles">
            <el-icon><Refresh /></el-icon>刷新
          </el-button>
        </div>
      </template>
      
      <div class="file-list">
        <el-empty v-if="files.length === 0" description="暂无文件" />
        <el-list v-else style="width: 100%">
          <el-list-item v-for="file in files" :key="file.fileName" style="display: block; margin-bottom: 10px;">
            <div style="display: flex; align-items: center; width: 100%;">
              <el-icon style="margin-right: 10px;"><Document /></el-icon>
              <span class="file-name" style="flex: 1;">{{ file.fileName }}</span>
              <span class="file-size" style="color: #909399; font-size: 14px; margin-right: 20px;">{{ formatSize(file.fileSize) }}</span>
              <span class="file-time" style="color: #909399; font-size: 14px;">{{ formatTime(file.lastModified) }}</span>
            </div>
          </el-list-item>
        </el-list>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Document } from '@element-plus/icons-vue'
import { useAuthStore } from '../stores/auth'
import { fileApi } from '../api'

const authStore = useAuthStore()
const files = ref([])
const loading = ref(false)

onMounted(() => {
  loadFiles()
})

const loadFiles = async () => {
  loading.value = true
  try {
    const response = await fileApi.getDirAFiles()
    files.value = response.data
  } catch (error) {
    if (error.response && error.response.status === 400) {
      files.value = []
    } else {
      ElMessage.error('获取文件列表失败')
    }
  } finally {
    loading.value = false
  }
}

const formatSize = (bytes) => {
  if (!bytes) return '0 B'
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(1024))
  return Math.round(bytes / Math.pow(1024, i) * 100) / 100 + ' ' + sizes[i]
}

const formatTime = (timestamp) => {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}
</script>

<style scoped>
.dir-page {
  padding: 10px;
}

.page-title {
  margin-bottom: 20px;
  color: #303133;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.file-name {
  margin-left: 10px;
}

.file-size {
  min-width: 80px;
  text-align: right;
}
</style>
