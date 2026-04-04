<template>
  <div class="users-page">
    <h1 class="page-title">用户管理</h1>
    
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户列表</span>
          <el-button type="primary" @click="loadUsers">
            <el-icon><Refresh /></el-icon>刷新
          </el-button>
        </div>
      </template>
      
      <el-table :data="users" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="账号" width="120" />
        <el-table-column prop="company.name" label="公司" width="150" />
        <el-table-column prop="email" label="邮箱" min-width="200" />
        <el-table-column prop="role" label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'success'">
              {{ row.role === 'ADMIN' ? '管理员' : '普通用户' }}
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { userApi } from '../api'

const users = ref([])
const loading = ref(false)
const actionLoading = ref(false)
const statusDialogVisible = ref(false)
const passwordDialogVisible = ref(false)
const deleteDialogVisible = ref(false)
const selectedUser = ref(null)
const sendEmail = ref(false)

onMounted(() => {
  loadUsers()
})

const loadUsers = async () => {
  loading.value = true
  try {
    const response = await userApi.getAll()
    users.value = response.data
  } catch (error) {
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

const toggleStatus = (user) => {
  selectedUser.value = user
  sendEmail.value = false
  statusDialogVisible.value = true
}

const confirmStatusChange = async () => {
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
</style>
