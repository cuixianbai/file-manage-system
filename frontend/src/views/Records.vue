<template>
  <div class="records-page">
    <h1 class="page-title">上传记录</h1>
    
    <el-card>
      <template #header>
        <div class="card-header">
          <span>文件上传历史</span>
          <el-button type="primary" @click="loadRecords">
            <el-icon><Refresh /></el-icon>刷新
          </el-button>
        </div>
      </template>
      
      <el-table :data="records" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="originalFilename" label="原始文件名" min-width="200" show-overflow-tooltip />
        <el-table-column prop="newFilename" label="新文件名" min-width="250" show-overflow-tooltip />
        <el-table-column prop="company.name" label="公司" width="150" />
        <el-table-column prop="uploadedBy.username" label="上传人" width="120" />
        <el-table-column prop="fileSize" label="文件大小" width="120">
          <template #default="{ row }">
            {{ formatFileSize(row.fileSize) }}
          </template>
        </el-table-column>
        <el-table-column prop="uploadedAt" label="上传时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.uploadedAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { fileApi } from '../api'

const records = ref([])
const loading = ref(false)

onMounted(() => {
  loadRecords()
})

const loadRecords = async () => {
  loading.value = true
  try {
    const response = await fileApi.getRecords()
    records.value = response.data
  } catch (error) {
    ElMessage.error('获取记录失败')
  } finally {
    loading.value = false
  }
}

const formatFileSize = (bytes) => {
  if (!bytes) return '-'
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(1024))
  return Math.round(bytes / Math.pow(1024, i) * 100) / 100 + ' ' + sizes[i]
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

const getStatusType = (status) => {
  const types = {
    'UPLOADED': 'success',
    'PROCESSING': 'warning',
    'COMPLETED': 'success',
    'FAILED': 'danger'
  }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = {
    'UPLOADED': '已上传',
    'PROCESSING': '处理中',
    'COMPLETED': '已完成',
    'FAILED': '失败'
  }
  return texts[status] || status
}
</script>

<style scoped>
.records-page {
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
</style>
