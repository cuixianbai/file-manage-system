<template>
  <div class="password-container">
    <h2>设置密码</h2>
    <el-form :model="passwordForm" :rules="rules" ref="passwordFormRef" label-width="120px">
      <el-form-item label="当前密码" prop="currentPassword">
        <el-input v-model="passwordForm.currentPassword" type="password" placeholder="请输入当前密码" />
      </el-form-item>
      
      <el-form-item label="新密码" prop="newPassword">
        <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" />
      </el-form-item>
      
      <el-form-item label="确认新密码" prop="confirmPassword">
        <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请确认新密码" />
      </el-form-item>
      
      <el-form-item>
        <el-button type="primary" @click="handleSubmit" :loading="loading">确认修改</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { userApi } from '../api'

const passwordFormRef = ref(null)
const loading = ref(false)

const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const rules = {
  currentPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '新密码长度不能少于6个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const handleSubmit = async () => {
  if (!passwordFormRef.value) return
  
  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const response = await userApi.changePassword({
          currentPassword: passwordForm.currentPassword,
          newPassword: passwordForm.newPassword
        })
        ElMessage.success(response.data.message)
        handleReset()
      } catch (error) {
        ElMessage.error(error.response?.data?.message || '修改密码失败')
      } finally {
        loading.value = false
      }
    }
  })
}

const handleReset = () => {
  if (passwordFormRef.value) {
    passwordFormRef.value.resetFields()
  }
}
</script>

<style scoped>
.password-container {
  max-width: 500px;
  margin: 0 auto;
  padding: 20px;
  background: #f5f5f5;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

h2 {
  margin-bottom: 20px;
  color: #303133;
  text-align: center;
}

.el-form {
  background: white;
  padding: 20px;
  border-radius: 8px;
}
</style>