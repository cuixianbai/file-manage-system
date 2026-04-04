<template>
  <div class="dashboard">
    <h1 class="page-title">欢迎使用企业多公司文件管理系统</h1>
    
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card class="stat-card">
          <template #header>
            <div class="card-header">
              <el-icon size="30" color="#409eff"><User /></el-icon>
              <span>当前用户</span>
            </div>
          </template>
          <div class="stat-content">
            <p class="stat-value">{{ authStore.user?.username }}</p>
            <p class="stat-label">所属公司: {{ authStore.user?.companyName }}</p>
            <el-tag :type="authStore.isAdmin ? 'danger' : 'success'">
              {{ authStore.isAdmin ? '管理员' : '普通用户' }}
            </el-tag>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card class="stat-card">
          <template #header>
            <div class="card-header">
              <el-icon size="30" color="#67c23a"><UploadFilled /></el-icon>
              <span>快速上传</span>
            </div>
          </template>
          <div class="stat-content">
            <p class="stat-desc">点击快速跳转至上载页面</p>
            <el-button type="primary" @click="$router.push('/upload')">
              前往上传
            </el-button>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card class="stat-card">
          <template #header>
            <div class="card-header">
              <el-icon size="30" color="#e6a23c"><Folder /></el-icon>
              <span>查看目录</span>
            </div>
          </template>
          <div class="stat-content">
            <p class="stat-desc">查看公司文件目录</p>
            <el-button type="warning" @click="$router.push('/dirA')">
              查看目录A
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-card class="info-card" style="margin-top: 20px;">
      <template #header>
        <span>系统功能说明</span>
      </template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="文件上传">
          支持点击选择和拖拽上传，管理员需选择目标公司
        </el-descriptions-item>
        <el-descriptions-item label="上传记录">
          查看文件上传历史记录
        </el-descriptions-item>
        <el-descriptions-item label="目录A">
          查看上传文件存储目录
        </el-descriptions-item>
        <el-descriptions-item label="目录B">
          查看外部程序处理后的PDF/JPG文件
        </el-descriptions-item>
        <el-descriptions-item v-if="authStore.isAdmin" label="用户管理">
          管理员可启用/禁用用户，重置密码
        </el-descriptions-item>
        <el-descriptions-item v-if="authStore.isAdmin" label="公司管理">
          管理员可创建和管理公司
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup>
import { useAuthStore } from '../stores/auth'
import { User, UploadFilled, Folder } from '@element-plus/icons-vue'

const authStore = useAuthStore()
</script>

<style scoped>
.dashboard {
  padding: 10px;
}

.page-title {
  margin-bottom: 30px;
  color: #303133;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
}

.stat-content {
  text-align: center;
  padding: 10px 0;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin: 10px 0;
}

.stat-label {
  color: #909399;
  margin-bottom: 10px;
}

.stat-desc {
  color: #909399;
  margin-bottom: 15px;
}
</style>
