<template>
  <div class="dir-page">
    <h1 class="page-title">OUTPUT目录 - 处理后文件</h1>
    
    <el-card>
      <template #header>
        <div class="card-header">
          <span>文件列表</span>
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
            <el-empty v-if="!files[company] || !files[company].children || files[company].children.length === 0" description="暂无文件" />
            <el-tree
              v-else
              :data="files[company].children"
              :props="treeProps"
              node-key="path"
            >
              <template #default="{ node, data }">
                <div class="tree-node">
                  <el-icon v-if="data.type === 'directory'" style="margin-right: 5px; color: #409EFF;"><Folder /></el-icon>
                  <el-icon v-else style="margin-right: 5px; color: #67C23A;"><Document /></el-icon>
                  <span class="node-name">{{ data.name }}</span>
                  <span v-if="data.type === 'file'" class="node-info">
                    <span class="file-size">{{ formatSize(data.size) }}</span>
                    <span class="file-time">{{ formatTime(data.lastModified) }}</span>
                    <el-button 
                      type="primary" 
                      size="small" 
                      @click.stop="downloadFile(data.path)"
                    >
                      下载
                    </el-button>
                  </span>
                </div>
              </template>
            </el-tree>
          </el-collapse-item>
        </el-collapse>
      </div>
      
      <div v-else class="user-view">
        <el-empty v-if="!files.children || files.children.length === 0" description="暂无文件" />
        <el-tree
          v-else
          :data="[files]"
          :props="treeProps"
          node-key="path"
          default-expand-all
        >
          <template #default="{ node, data }">
            <div class="tree-node">
              <el-icon v-if="data.type === 'directory'" style="margin-right: 5px; color: #409EFF;"><Folder /></el-icon>
              <el-icon v-else style="margin-right: 5px; color: #67C23A;"><Document /></el-icon>
              <span class="node-name">{{ data.name }}</span>
              <span v-if="data.type === 'file'" class="node-info">
                <span class="file-size">{{ formatSize(data.size) }}</span>
                <span class="file-time">{{ formatTime(data.lastModified) }}</span>
                <el-button 
                  type="primary" 
                  size="small" 
                  @click.stop="downloadFile(data.path)"
                >
                  下载
                </el-button>
              </span>
            </div>
          </template>
        </el-tree>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Document, Folder } from '@element-plus/icons-vue'
import { useAuthStore } from '../stores/auth'
import { fileApi } from '../api'

const authStore = useAuthStore()
const files = ref({})
const companies = ref([])
const activeCompanies = ref([])
const loading = ref(false)

const treeProps = {
  children: 'children',
  label: 'name'
}

onMounted(() => {
  loadFiles()
})

const loadFiles = async () => {
  loading.value = true
  try {
    const response = await fileApi.getDirBFiles()
    
    if (authStore.isAdmin) {
      files.value = response.data
      companies.value = Object.keys(response.data)
      activeCompanies.value = []
    } else {
      files.value = response.data
    }
  } catch (error) {
    if (error.response && error.response.status === 400) {
      if (authStore.isAdmin) {
        companies.value = []
        activeCompanies.value = []
        files.value = {}
      } else {
        files.value = { children: [] }
      }
    } else {
      ElMessage.error('获取文件列表失败')
    }
  } finally {
    loading.value = false
  }
}

const downloadFile = async (path) => {
  try {
    const response = await fileApi.downloadDirBFile(path)
    const blob = new Blob([response.data])
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    const filename = path.split('/').pop()
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

const formatSize = (bytes) => {
  if (!bytes) return ''
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
    minute: '2-digit'
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

.tree-node {
  display: flex;
  align-items: center;
  width: 100%;
  padding: 5px 0;
}

.node-name {
  flex: 1;
  margin-left: 5px;
}

.node-info {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-left: 20px;
}

.file-size {
  color: #909399;
  font-size: 12px;
  min-width: 80px;
}

.file-time {
  color: #909399;
  font-size: 12px;
  min-width: 120px;
}
</style>
