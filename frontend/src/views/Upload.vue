<template>
  <div class="upload-page">
    <h1 class="page-title">文件上传</h1>
    
    <el-card>
      <template #header>
        <span>上传文件</span>
      </template>
      
      <el-form v-if="authStore.isAdminOrManager" :model="form" label-width="120px">
        <el-form-item label="目标公司">
          <el-select v-model="form.companyId" placeholder="请选择公司" style="width: 100%">
            <el-option
              v-for="company in companies"
              :key="company.id"
              :label="company.name"
              :value="company.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      
      <el-upload
        ref="uploadRef"
        class="upload-area"
        drag
        :auto-upload="false"
        :on-change="handleChange"
        :file-list="fileList"
        accept="*"
      >
        <el-icon class="upload-icon"><UploadFilled /></el-icon>
        <div class="upload-text">
          拖拽文件到此处或<em>点击上传</em>
        </div>
        <template #tip>
          <div class="upload-tip">
            <p>文件命名规则: 公司名_原始文件名</p>
            <p v-if="!authStore.isAdminOrManager">
              文件将上传至: {{ authStore.user?.companyName }}
            </p>
            <p v-else>
              请先选择目标公司
            </p>
          </div>
        </template>
      </el-upload>
      
      <div class="upload-actions">
        <el-button type="primary" :loading="uploading" @click="handleUpload">
          上传
        </el-button>
        <el-button @click="handleClear">清空</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import { useAuthStore } from '../stores/auth'
import { companyApi, fileApi } from '../api'

const authStore = useAuthStore()
const uploadRef = ref(null)
const uploading = ref(false)
const fileList = ref([])
const companies = ref([])

const form = reactive({
  companyId: null
})

const uploadUrl = computed(() => '/api/files/upload')
const headers = computed(() => ({
  Authorization: `Bearer ${authStore.token}`
}))
const uploadData = computed(() => {
  if (authStore.isAdminOrManager && form.companyId) {
    return { companyId: form.companyId }
  }
  return {}
})

onMounted(async () => {
  if (authStore.isAdminOrManager) {
    try {
      const response = await companyApi.getAll()
      // 过滤掉系统管理公司
      companies.value = response.data.filter(company => company.name !== '系统管理')
    } catch (error) {
      ElMessage.error('获取公司列表失败')
    }
  }
})

const handleChange = (file, files) => {
  fileList.value = files
}

const handleUpload = async () => {
  if (fileList.value.length === 0) {
    ElMessage.warning('请选择文件')
    return
  }
  
  if (authStore.isAdminOrManager && !form.companyId) {
    ElMessage.warning('请选择目标公司')
    return
  }

  uploading.value = true
  
  try {
    console.log('开始上传文件...')
    const file = fileList.value[0].raw
    console.log('文件信息:', file.name, file.size)
    
    const response = await fileApi.upload(file, form.companyId)
    console.log('上传响应:', response)
    
    if (response.data.success) {
      ElMessage.success(response.data.message)
      handleClear()
    } else {
      ElMessage.error(response.data.message)
    }
  } catch (error) {
    console.error('上传错误:', error)
    ElMessage.error(error.response?.data?.message || '上传失败')
  } finally {
    console.log('上传完成，重置loading状态')
    uploading.value = false
  }
}

const handleSuccess = (response) => {
  ElMessage.success(response.message || '上传成功')
}

const handleError = (error) => {
  ElMessage.error(error.message || '上传失败')
}

const handleClear = () => {
  fileList.value = []
  if (uploadRef.value) {
    uploadRef.value.clearFiles()
  }
}
</script>

<style scoped>
.upload-page {
  padding: 10px;
}

.page-title {
  margin-bottom: 20px;
  color: #303133;
}

.upload-area {
  margin: 20px 0;
}

.upload-icon {
  font-size: 67px;
  color: #8c939d;
  margin-bottom: 16px;
}

.upload-text {
  color: #606266;
  font-size: 14px;
}

.upload-text em {
  color: #409eff;
  font-style: normal;
}

.upload-tip {
  margin-top: 10px;
  color: #909399;
  font-size: 12px;
}

.upload-tip p {
  margin: 5px 0;
}

.upload-actions {
  text-align: center;
  margin-top: 20px;
}
</style>
