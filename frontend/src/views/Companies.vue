<template>
  <div class="companies-page">
    <h1 class="page-title">公司管理</h1>
    
    <el-card>
      <template #header>
        <div class="card-header">
          <span>公司列表</span>
          <el-button type="primary" @click="showCreateDialog">
            <el-icon><Plus /></el-icon>创建公司
          </el-button>
        </div>
      </template>
      
      <div class="filter-container">
        <el-input
          v-model="filters.name"
          placeholder="按公司名称过滤"
          clearable
          style="width: 200px; margin-right: 10px;"
          @clear="loadCompanies"
          @keyup.enter="loadCompanies"
        />
        <el-button type="primary" @click="loadCompanies">
          <el-icon><Search /></el-icon>搜索
        </el-button>
      </div>
      
      <el-table :data="companies" v-loading="loading" stripe style="margin-top: 15px;">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="公司名称" min-width="200" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ENABLED' ? 'success' : 'danger'">
              {{ row.status === 'ENABLED' ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <template v-if="row.name === '系统管理'">
              <el-button 
                type="info"
                size="small"
                disabled
              >
                系统公司
              </el-button>
            </template>
            <template v-else>
              <el-button 
                v-if="row.status === 'ENABLED'"
                type="warning"
                size="small"
                @click="handleToggleStatus(row)"
              >
                禁用
              </el-button>
              <el-button 
                v-else
                type="success"
                size="small"
                @click="handleToggleStatus(row)"
              >
                启用
              </el-button>
              <el-button 
                type="danger"
                size="small"
                @click="handleDelete(row)"
              >
                删除
              </el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- Create Company Dialog -->
    <el-dialog
      v-model="createDialogVisible"
      title="创建公司"
      width="400px"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="公司名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入公司名称" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreate" :loading="actionLoading">
          创建
        </el-button>
      </template>
    </el-dialog>

    <!-- Delete Company Dialog -->
    <el-dialog
      v-model="deleteDialogVisible"
      title="删除公司"
      width="400px"
    >
      <p>确定要删除公司 "{{ selectedCompany?.name }}" 吗？</p>
      <p v-if="selectedCompany && selectedCompany.users && selectedCompany.users.length > 0" style="color: #f56c6c;">
        该公司下还有 {{ selectedCompany.users.length }} 个用户，无法删除！
      </p>
      <template #footer>
        <el-button @click="deleteDialogVisible = false">取消</el-button>
        <el-button 
          type="danger" 
          @click="confirmDelete" 
          :loading="actionLoading"
          :disabled="selectedCompany && selectedCompany.users && selectedCompany.users.length > 0"
        >
          删除
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { companyApi } from '../api'

const companies = ref([])
const loading = ref(false)
const actionLoading = ref(false)
const createDialogVisible = ref(false)
const deleteDialogVisible = ref(false)
const selectedCompany = ref(null)
const formRef = ref(null)
const form = reactive({
  name: ''
})
const filters = reactive({
  name: ''
})

const rules = {
  name: [
    { required: true, message: '请输入公司名称', trigger: 'blur' }
  ]
}

onMounted(() => {
  loadCompanies()
})

const loadCompanies = async () => {
  loading.value = true
  try {
    const params = {}
    if (filters.name) params.name = filters.name
    
    const response = await companyApi.getAll(params)
    companies.value = response.data
  } catch (error) {
    ElMessage.error('获取公司列表失败')
  } finally {
    loading.value = false
  }
}

const showCreateDialog = () => {
  form.name = ''
  createDialogVisible.value = true
}

const handleCreate = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      actionLoading.value = true
      try {
        const response = await companyApi.create(form.name)
        ElMessage.success(response.data.message)
        createDialogVisible.value = false
        loadCompanies()
      } catch (error) {
        ElMessage.error(error.response?.data?.message || '创建失败')
      } finally {
        actionLoading.value = false
      }
    }
  })
}

const handleDelete = (company) => {
  selectedCompany.value = company
  deleteDialogVisible.value = true
}

const handleToggleStatus = async (company) => {
  const newStatus = company.status === 'ENABLED' ? 'DISABLED' : 'ENABLED'
  const actionText = newStatus === 'ENABLED' ? '启用' : '禁用'
  
  try {
    const response = await companyApi.updateStatus(company.id, newStatus)
    ElMessage.success(response.data.message)
    loadCompanies()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || `${actionText}失败`)
  }
}

const confirmDelete = async () => {
  if (!selectedCompany.value) return
  
  actionLoading.value = true
  try {
    const response = await companyApi.delete(selectedCompany.value.id)
    ElMessage.success(response.data.message)
    deleteDialogVisible.value = false
    loadCompanies()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '删除失败')
  } finally {
    actionLoading.value = false
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}
</script>

<style scoped>
.companies-page {
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
