<template>
  <div class="dir-page">
    <h1 class="page-title">目录A - 上传文件</h1>
    
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
            <el-empty v-if="!files[company] || files[company].length === 0" description="暂无文件" />
            <el-list v-else>
              <el-list-item v-for="file in files[company]" :key="file">
                <el-icon><Document /></el-icon>
                <span class="file-name">{{ file }}</span>
              </el-list-item>
            </el-list>
          </el-collapse-item>
        </el-collapse>
      </div>
      
      <div v-else class="user-view">
        <el-empty v-if="files.length === 0" description="暂无文件" />
        <el-list v-else>
          <el-list-item v-for="file in files" :key="file">
            <el-icon><Document /></el-icon>
            <span class="file-name">{{ file }}</span>
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
const companies = ref([])
const activeCompanies = ref([])
const loading = ref(false)

onMounted(() => {
  loadFiles()
})

const loadFiles = async () => {
  loading.value = true
  try {
    const response = await fileApi.getDirAFiles()
    
    if (authStore.isAdmin) {
      // Admin sees all companies
      companies.value = response.data
      activeCompanies.value = response.data
      
      // Load files for each company
      for (const company of response.data) {
        // This is simplified - in real scenario, you'd have an API to get files per company
        files.value[company] = []
      }
    } else {
      // User sees only their company files
      files.value = response.data
    }
  } catch (error) {
    ElMessage.error('获取文件列表失败')
  } finally {
    loading.value = false
  }
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
</style>
