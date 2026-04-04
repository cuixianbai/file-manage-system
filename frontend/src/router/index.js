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
          meta: { adminOnly: true }
        },
        {
          path: 'companies',
          name: 'Companies',
          component: () => import('../views/Companies.vue'),
          meta: { adminOnly: true }
        }
      ]
    }
  ]
})

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()

  if (!to.meta.public && !authStore.token) {
    next('/login')
  } else if (to.meta.adminOnly && !authStore.isAdmin) {
    next('/dashboard')
  } else {
    next()
  }
})

export default router
