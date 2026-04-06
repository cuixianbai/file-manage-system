<template>
  <div class="users-page">
    <h1 class="page-title">用户管理</h1>
    
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户列表</span>
          <div class="header-actions">
            <el-button type="primary" @click="showCreateDialog">
              <el-icon><Plus /></el-icon>创建用户
            </el-button>
            <el-button type="primary" @click="loadUsers">
              <el-icon><Refresh /></el-icon>刷新
            </el-button>
          </div>
        </div>
      </template>
      
      <div class="filter-container">
        <el-input
          v-model="filters.username"
          placeholder="按账号过滤"
          clearable
          style="width: 150px; margin-right: 10px;"
          @clear="loadUsers"
          @keyup.enter="loadUsers"
        />
        <el-input
          v-model="filters.companyName"
          placeholder="按公司名称过滤"
          clearable
          style="width: 150px; margin-right: 10px;"
          @clear="loadUsers"
          @keyup.enter="loadUsers"
        />
        <el-select
          v-model="filters.status"
          placeholder="按状态过滤"
          clearable
          style="width: 120px; margin-right: 10px;"
          @change="loadUsers"
        >
          <el-option label="启用" value="ENABLED" />
          <el-option label="禁用" value="DISABLED" />
        </el-select>
        <el-button type="primary" @click="loadUsers">
          <el-icon><Search /></el-icon>搜索
        </el-button>
      </div>
      
      <el-table :data="users" v-loading="loading" stripe style="margin-top: 15px;">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="账号" width="120" />
        <el-table-column prop="company.name" label="公司" width="150" />
        <el-table-column prop="email" label="邮箱" min-width="200" />
        <el-table-column prop="role" label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="getRoleType(row.role)">
              {{ getRoleName(row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ENABLED' ? 'success' : 'danger'">
              {{ row.status === 'ENABLED' ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="380" fixed="right">
          <template #default="{ row }">
            <template v-if="row.role === 'ADMIN' && row.username === 'admin'">
              <el-button 
                type="info"
                size="small"
                disabled
              >
                系统管理员
              </el-button>
              <el-button 
                type="warning"
                size="small"
                @click="resetPassword(row)"
              >
                重置密码
              </el-button>
              <el-button 
                type="danger"
                size="small"
                disabled
              >
                删除
              </el-button>
            </template>
            <template v-else>
              <el-button 
                :type="row.status === 'ENABLED' ? 'danger' : 'success'"
                size="small"
                @click="toggleStatus(row)"
              >
                {{ row.status === 'ENABLED' ? '禁用' : '启用' }}
              </el-button>
              <el-button 
                type="warning"
                size="small"
                @click="resetPassword(row)"
              >
                重置密码
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
    
    <!-- Status Change Dialog -->
    <el-dialog
      v-model="statusDialogVisible"
      title="切换用户状态"
      width="400px"
    >
      <p>确定要{{ selectedUser?.status === 'ENABLED' ? '禁用' : '启用' }}用户 "{{ selectedUser?.username }}" 吗？</p>
      <el-checkbox v-model="sendEmail">发送邮件通知用户</el-checkbox>
      <template #footer>
        <el-button @click="statusDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmStatusChange" :loading="actionLoading">
          确定
        </el-button>
      </template>
    </el-dialog>
    
    <!-- Password Reset Dialog -->
    <el-dialog
      v-model="passwordDialogVisible"
      title="重置密码"
      width="400px"
    >
      <p>确定要重置用户 "{{ selectedUser?.username }}" 的密码吗？</p>
      <p>新密码将为: <strong>qweasdzxc</strong></p>
      <el-checkbox v-model="sendEmail">发送邮件通知用户</el-checkbox>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmResetPassword" :loading="actionLoading">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- Delete User Dialog -->
    <el-dialog
      v-model="deleteDialogVisible"
      title="删除用户"
      width="400px"
    >
      <p>确定要删除用户 "{{ selectedUser?.username }}" 吗？</p>
      <p v-if="selectedUser?.role === 'ADMIN' && selectedUser?.username === 'admin'" style="color: #f56c6c;">
        不能删除默认管理员账号！
      </p>
      <template #footer>
        <el-button @click="deleteDialogVisible = false">取消</el-button>
        <el-button 
          type="danger" 
          @click="confirmDelete" 
          :loading="actionLoading"
          :disabled="selectedUser?.role === 'ADMIN' && selectedUser?.username === 'admin'"
        >
          删除
        </el-button>
      </template>
    </el-dialog>
    
    <!-- Create User Dialog -->
    <el-dialog
      v-model="createDialogVisible"
      title="创建用户"
      width="500px"
    >
      <el-form :model="createForm" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="createForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="createForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="公司" prop="companyId">
          <el-select v-model="createForm.companyId" placeholder="请选择公司" style="width: 100%" @change="handleCompanyChange">
            <el-option
              v-for="company in companies"
              :key="company.id"
              :label="company.name"
              :value="company.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="createForm.role" placeholder="请选择角色" style="width: 100%">
            <el-option v-if="isSystemCompany" label="超级管理员" value="ADMIN" />
            <el-option v-if="isSystemCompany" label="一般管理员" value="MANAGER" />
            <el-option v-else label="普通用户" value="USER" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmCreate" :loading="actionLoading">
          创建
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Search, Plus } from '@element-plus/icons-vue'
import { userApi, companyApi } from '../api'

const users = ref([])
const companies = ref([])
const loading = ref(false)
const actionLoading = ref(false)
const statusDialogVisible = ref(false)
const passwordDialogVisible = ref(false)
const deleteDialogVisible = ref(false)
const createDialogVisible = ref(false)
const selectedUser = ref(null)
const sendEmail = ref(false)
const filters = reactive({
  username: '',
  companyName: '',
  status: ''
})
const createForm = reactive({
  username: '',
  email: '',
  companyId: null,
  role: 'USER'
})

const isSystemCompany = ref(false)

onMounted(async () => {
  loadUsers()
  await loadCompanies()
})

const loadUsers = async () => {
  loading.value = true
  try {
    const params = {}
    if (filters.username) params.username = filters.username
    if (filters.companyName) params.companyName = filters.companyName
    if (filters.status) params.status = filters.status
    
    const response = await userApi.getAll(params)
    users.value = response.data
  } catch (error) {
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

const loadCompanies = async () => {
  try {
    const response = await companyApi.getAll()
    companies.value = response.data
  } catch (error) {
    ElMessage.error('获取公司列表失败')
  }
}

const toggleStatus = (user) => {
  selectedUser.value = user
  sendEmail.value = false
  statusDialogVisible.value = true
}

const confirmStatusChange = async () => {
  // 系统管理员不能禁用
  if (selectedUser.value.role === 'ADMIN' && selectedUser.value.username === 'admin') {
    ElMessage.warning('系统管理员账号不能禁用')
    statusDialogVisible.value = false
    return
  }
  
  actionLoading.value = true
  try {
    const newStatus = selectedUser.value.status === 'ENABLED' ? 'DISABLED' : 'ENABLED'
    const response = await userApi.updateStatus(
      selectedUser.value.id,
      newStatus,
      sendEmail.value
    )
    ElMessage.success(response.data.message)
    statusDialogVisible.value = false
    loadUsers()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '操作失败')
  } finally {
    actionLoading.value = false
  }
}

const resetPassword = (user) => {
  selectedUser.value = user
  sendEmail.value = false
  passwordDialogVisible.value = true
}

const confirmResetPassword = async () => {
  actionLoading.value = true
  try {
    const response = await userApi.resetPassword(
      selectedUser.value.id,
      sendEmail.value
    )
    ElMessage.success(response.data.message)
    passwordDialogVisible.value = false
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '操作失败')
  } finally {
    actionLoading.value = false
  }
}

const handleDelete = (user) => {
  selectedUser.value = user
  deleteDialogVisible.value = true
}

const confirmDelete = async () => {
  if (!selectedUser.value) return
  
  actionLoading.value = true
  try {
    const response = await userApi.delete(selectedUser.value.id)
    ElMessage.success(response.data.message)
    deleteDialogVisible.value = false
    loadUsers()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '删除失败')
  } finally {
    actionLoading.value = false
  }
}

const showCreateDialog = () => {
  createForm.username = ''
  createForm.email = ''
  createForm.companyId = null
  createForm.role = 'USER'
  isSystemCompany.value = false
  createDialogVisible.value = true
}

const handleCompanyChange = (companyId) => {
  if (companyId) {
    const selectedCompany = companies.value.find(company => company.id === companyId)
    isSystemCompany.value = selectedCompany?.name === '系统管理'
    // 自动设置默认角色
    if (isSystemCompany.value) {
      createForm.role = 'MANAGER'
    } else {
      createForm.role = 'USER'
    }
  } else {
    isSystemCompany.value = false
    createForm.role = 'USER'
  }
}

const confirmCreate = async () => {
  if (!createForm.username || !createForm.email || !createForm.companyId || !createForm.role) {
    ElMessage.warning('请填写完整的用户信息')
    return
  }
  
  // 校验角色与公司的匹配关系
  const selectedCompany = companies.value.find(company => company.id === createForm.companyId)
  if (!selectedCompany) {
    ElMessage.warning('请选择有效的公司')
    return
  }
  
  if (selectedCompany.name === '系统管理') {
    if (!['ADMIN', 'MANAGER'].includes(createForm.role)) {
      ElMessage.warning('系统管理公司只能创建超级管理员或一般管理员')
      return
    }
  } else {
    if (createForm.role !== 'USER') {
      ElMessage.warning('非系统管理公司只能创建普通用户')
      return
    }
  }
  
  actionLoading.value = true
  try {
    const response = await userApi.create(createForm)
    ElMessage.success(response.data.message)
    createDialogVisible.value = false
    loadUsers()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '创建用户失败')
  } finally {
    actionLoading.value = false
  }
}

const getRoleName = (role) => {
  switch (role) {
    case 'ADMIN':
      return '超级管理员'
    case 'MANAGER':
      return '一般管理员'
    case 'USER':
      return '普通用户'
    default:
      return '未知角色'
  }
}

const getRoleType = (role) => {
  switch (role) {
    case 'ADMIN':
      return 'danger'
    case 'MANAGER':
      return 'warning'
    case 'USER':
      return 'success'
    default:
      return 'info'
  }
}
</script>

<style scoped>
.users-page {
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

.header-actions {
  display: flex;
  gap: 10px;
}

.filter-container {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px 0;
}
</style>
