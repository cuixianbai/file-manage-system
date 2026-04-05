import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import axios from 'axios'

const API_URL = '/api'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(JSON.parse(localStorage.getItem('user') || '{}'))

  const isAuthenticated = computed(() => !!token.value)
  const isAdmin = computed(() => user.value?.role === 'ADMIN')
  const isManager = computed(() => user.value?.role === 'MANAGER')
  const isAdminOrManager = computed(() => user.value?.role === 'ADMIN' || user.value?.role === 'MANAGER')
  const currentUser = computed(() => user.value)

  // Set axios default header
  if (token.value) {
    axios.defaults.headers.common['Authorization'] = `Bearer ${token.value}`
  }

  async function login(username, password) {
    const response = await axios.post(`${API_URL}/auth/login`, {
      username,
      password
    })

    const data = response.data
    token.value = data.token
    user.value = {
      id: data.id,
      username: data.username,
      email: data.email,
      companyId: data.companyId,
      companyName: data.companyName,
      role: typeof data.role === 'string' ? data.role : data.role?.name || 'USER',
      status: data.status
    }

    localStorage.setItem('token', data.token)
    localStorage.setItem('user', JSON.stringify(user.value))
    axios.defaults.headers.common['Authorization'] = `Bearer ${data.token}`

    return data
  }

  async function register(username, password, email, companyName) {
    const response = await axios.post(`${API_URL}/auth/register`, {
      username,
      password,
      email,
      companyName
    })
    return response.data
  }

  function logout() {
    token.value = ''
    user.value = {}
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    delete axios.defaults.headers.common['Authorization']
  }

  return {
    token,
    user,
    isAuthenticated,
    isAdmin,
    isManager,
    isAdminOrManager,
    currentUser,
    login,
    register,
    logout
  }
})
