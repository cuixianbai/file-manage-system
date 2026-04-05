<template>
  <el-container class="layout-container">
    <el-header class="header">
      <div class="header-left">
        <h2 class="system-title">企业多公司文件管理系统</h2>
      </div>
      <div class="header-right">
        <span class="user-info">
          {{ authStore.user?.username }} ({{ authStore.user?.role === 'ADMIN' ? '管理员' : '普通用户' }})
        </span>
        <el-button type="danger" @click="handleLogout">退出登录</el-button>
      </div>
    </el-header>
    
    <el-container>
      <el-aside width="200px" class="sidebar">
        <el-menu
          :default-active="$route.path"
          router
          class="sidebar-menu"
        >
          <el-menu-item index="/dashboard">
            <el-icon><HomeFilled /></el-icon>
            <span>首页</span>
          </el-menu-item>
          
          <el-menu-item index="/upload">
            <el-icon><UploadFilled /></el-icon>
            <span>文件上传</span>
          </el-menu-item>
          
          <el-menu-item index="/records">
            <el-icon><Document /></el-icon>
            <span>上传记录</span>
          </el-menu-item>
          
          <el-menu-item index="/dirA">
            <el-icon><Folder /></el-icon>
            <span>INPUT目录</span>
          </el-menu-item>
          
          <el-menu-item index="/dirB">
            <el-icon><FolderOpened /></el-icon>
            <span>OUTPUT目录</span>
          </el-menu-item>
          
          <el-menu-item v-if="authStore.user?.role === 'ADMIN'" index="/users">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          
          <el-menu-item v-if="authStore.user?.role === 'ADMIN'" index="/companies">
            <el-icon><OfficeBuilding /></el-icon>
            <span>公司管理</span>
          </el-menu-item>
          
          <el-menu-item index="/password">
            <el-icon><Lock /></el-icon>
            <span>设置密码</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useAuthStore } from '../stores/auth'
import { 
  HomeFilled, UploadFilled, Document, Folder, 
  FolderOpened, User, OfficeBuilding, Lock 
} from '@element-plus/icons-vue'

const router = useRouter()
const authStore = useAuthStore()

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    authStore.logout()
    router.push('/login')
  })
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #409eff;
  color: white;
  padding: 0 20px;
}

.header-left {
  display: flex;
  align-items: center;
}

.system-title {
  margin: 0;
  font-size: 20px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 15px;
}

.user-info {
  color: white;
  font-size: 14px;
}

.sidebar {
  background: #f5f5f5;
  border-right: 1px solid #e0e0e0;
}

.sidebar-menu {
  border: none;
  background: transparent;
}

.main-content {
  background: #fff;
  padding: 20px;
  overflow-y: auto;
}
</style>
