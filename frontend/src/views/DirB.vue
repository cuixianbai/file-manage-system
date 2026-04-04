<template>
  <div class="dir-page">
    <h1 class="page-title">OUTPUT目录 - 处理后文件</h1>
    
    <el-card>
      <template #header>
        <div class="card-header">
          <span>PDF/JPG文件列表</span>
          <el-button type="primary" @click="loadFiles">
            <el-icon><Refresh /></el-icon>刷新
          </el-button>
        </div>
      </template>
      
      <div v-if="authStore.isAdmin" class="admin-view">
        <el-collapse v-model="activeCompanies">
          <el-collapse-item
            v-for="company in companies"
            :key="company"
            :title="company"
            :name="company"
          >
            <el-empty v-if="!files[company] || files[company].length === 0" description="暂无文件" />
            <el-list v-else>
              <el-list-item v-for="file in files[company]" :key="file.fileName" class="file-item">
                <div style="display: flex; align-items: center; width: 100%;">
                  <el-icon style="margin-right: 10px;"><Document /></el-icon>
                  <span class="file-name" style="flex: 1;">{{ file.fileName }}</span>
                  <span class="file-time" style="color: #909399; font-size: 14px; margin-right: 15px;">{{ formatTime(file.lastModified) }}</span>
                  <el-button 
                    type="primary" 
                    size="small" 
                    @click="downloadFile(company, file.fileName)"
                  >
                    下载
                  </el-button>
                </div>
              </el-list-item>
            </el-list>
          </el-collapse-item>
        </el-collapse>
      </div>
      
      <div v-else class="user-view">
        <el-empty v-if="files.length === 0" description="暂无文件" />
        <el-list v-else>
          <el-list-item v-for="file in files" :key="file.fileName" class="file-item">
            <div style="display: flex; align-items: center; width: 100%;">
              <el-icon style="margin-right: 10px;"><Document /></el-icon>
              <span class="file-name" style="flex: 1;">{{ file.fileName }}</span>
              <span class="file-time" style="color: #909399; font-size: 14px; margin-right: 15px;">{{ formatTime(file.lastModified) }}</span>
              <el-button 
                type="primary" 
                size="small" 
                @click="downloadFile(authStore.user?.companyName, file.fileName)"
              >
                下载
              </el-button>
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
const files = ref({})
const companies = ref([])
const activeCompanies = ref([])
const loading = ref(false)

onMounted(() => {
  loadFiles()
})

const loadFiles = async () => {
  loading.value = true
  try {
    const response = await fileApi.getDirBFiles()
    
    if (authStore.isAdmin) {
      // Admin sees all companies and their files
      files.value = response.data
      companies.value = Object.keys(response.data)
      activeCompanies.value = Object.keys(response.data)
    } else {
      // User sees only their company files
      files.value = response.data
    }
  } catch (error) {
    // 如果是目录为空的情况，不要显示错误
    if (error.response && error.response.status === 400) {
      // 目录为空，设置为空数组或对象
      if (authStore.isAdmin) {
        companies.value = []
        activeCompanies.value = []
        files.value = {}
      } else {
        files.value = []
      }
    } else {
      ElMessage.error('获取文件列表失败')
    }
  } finally {
    loading.value = false
  }
}

const downloadFile = async (companyName, filename) => {
  try {
    const response = await fileApi.downloadDirBFile(companyName, filename)
    const blob = new Blob([response.data])
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', filename)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    ElMessage.success('下载成功')
  } catch (error) {
    ElMessage.error('下载失败')
  }
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

.file-item {
  display: flex;
  align-items: center;
  padding: 10px;
  border-bottom: 1px solid #ebeef5;
}

.file-name {
  flex: 1;
  margin-left: 10px;
}
</style>
