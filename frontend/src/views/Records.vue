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
      
      <div class="filter-container">
        <el-input
          v-model="filters.originalFilename"
          placeholder="按原始文件名过滤"
          clearable
          style="width: 180px; margin-right: 10px;"
          @clear="loadRecords"
          @keyup.enter="loadRecords"
        />
        <el-input
          v-model="filters.newFilename"
          placeholder="按新文件名过滤"
          clearable
          style="width: 180px; margin-right: 10px;"
          @clear="loadRecords"
          @keyup.enter="loadRecords"
        />
        <el-date-picker
          v-model="filters.dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          style="width: 240px; margin-right: 10px;"
          @change="loadRecords"
        />
        <el-button type="primary" @click="loadRecords">
          <el-icon><Search /></el-icon>搜索
        </el-button>
      </div>
      
      <el-table :data="records" v-loading="loading" stripe style="margin-top: 15px;">
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
import { ref, onMounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Search } from '@element-plus/icons-vue'
import { fileApi } from '../api'

const records = ref([])
const loading = ref(false)
const filters = reactive({
  originalFilename: '',
  newFilename: '',
  dateRange: []
})

onMounted(() => {
  loadRecords()
})

const loadRecords = async () => {
  loading.value = true
  try {
    const params = {}
    if (filters.originalFilename) params.originalFilename = filters.originalFilename
    if (filters.newFilename) params.newFilename = filters.newFilename
    
    // 处理日期范围
    if (filters.dateRange && filters.dateRange.length === 2) {
      const startDate = filters.dateRange[0]
      const endDate = filters.dateRange[1]
      
      // 转换为ISO格式并设置时间
      const startDateTime = new Date(startDate)
      startDateTime.setHours(0, 0, 0, 0)
      params.startDate = startDateTime.toISOString()
      
      const endDateTime = new Date(endDate)
      endDateTime.setHours(23, 59, 59, 999)
      params.endDate = endDateTime.toISOString()
    }
    
    const response = await fileApi.getRecords(params)
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

.filter-container {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px 0;
}
</style>
