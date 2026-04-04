import axios from 'axios'

const API_URL = '/api'

// 创建axios实例
const axiosInstance = axios.create()

// 请求拦截器
axiosInstance.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// User API
export const userApi = {
  getAll: () => axiosInstance.get(`${API_URL}/users`),
  updateStatus: (id, status, sendEmail) => axiosInstance.put(`${API_URL}/users/${id}/status`, {
    status,
    sendEmail
  }),
  resetPassword: (id, sendEmail) => axiosInstance.post(`${API_URL}/users/${id}/reset-password`, {
    sendEmail
  }),
  delete: (id) => axiosInstance.delete(`${API_URL}/users/${id}`),
  getMe: () => axiosInstance.get(`${API_URL}/users/me`)
}

// Company API
export const companyApi = {
  getAll: () => axiosInstance.get(`${API_URL}/companies`),
  create: (name) => axiosInstance.post(`${API_URL}/companies`, { name }),
  delete: (id) => axiosInstance.delete(`${API_URL}/companies/${id}`)
}

// File API
export const fileApi = {
  upload: (file, companyId) => {
    const formData = new FormData()
    formData.append('file', file)
    if (companyId) {
      formData.append('companyId', companyId)
    }
    return axiosInstance.post(`${API_URL}/files/upload`, formData)
  },
  getRecords: () => axiosInstance.get(`${API_URL}/files/records`),
  getDirAFiles: () => axiosInstance.get(`${API_URL}/files/dirA`),
  getDirBFiles: () => axiosInstance.get(`${API_URL}/files/dirB`),
  downloadDirBFile: (companyName, filename) => {
    return axiosInstance.get(`${API_URL}/files/dirB/${companyName}/${filename}`, {
      responseType: 'blob'
    })
  }
}
