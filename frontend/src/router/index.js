import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('../views/Login.vue'),
      meta: { public: true }
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('../views/Register.vue'),
      meta: { public: true }
    },
    {
      path: '/',
      name: 'Layout',
      component: () => import('../views/Layout.vue'),
      redirect: '/dashboard',
      children: [
        {
          path: 'dashboard',
          name: 'Dashboard',
          component: () => import('../views/Dashboard.vue')
        },
        {
          path: 'upload',
          name: 'Upload',
          component: () => import('../views/Upload.vue')
        },
        {
          path: 'records',
          name: 'Records',
          component: () => import('../views/Records.vue')
        },
        {
          path: 'dirA',
          name: 'DirA',
          component: () => import('../views/DirA.vue')
        },
        {
          path: 'dirB',
          name: 'DirB',
          component: () => import('../views/DirB.vue')
        },
        {
          path: 'users',
          name: 'Users',
          component: () => import('../views/Users.vue'),
          meta: { superAdminOnly: true }
        },
        {
          path: 'companies',
          name: 'Companies',
          component: () => import('../views/Companies.vue'),
          meta: { superAdminOnly: true }
        },
        {
          path: 'password',
          name: 'Password',
          component: () => import('../views/Password.vue')
        }
      ]
    }
  ]
})

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()

  if (!to.meta.public && !authStore.token) {
    next('/login')
  } else if (to.meta.superAdminOnly && authStore.user?.role !== 'ADMIN') {
    next('/dashboard')
  } else {
    next()
  }
})

export default router
