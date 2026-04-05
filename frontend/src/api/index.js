import axios from 'axios'

const API_URL = '/api'

const axiosInstance = axios.create()

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

export const userApi = {
  getAll: (params) => axiosInstance.get(`${API_URL}/users`, { params }),
  create: (userData) => axiosInstance.post(`${API_URL}/users`, userData),
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

export const companyApi = {
  getAll: (params) => axiosInstance.get(`${API_URL}/companies`, { params }),
  create: (name) => axiosInstance.post(`${API_URL}/companies`, { name }),
  delete: (id) => axiosInstance.delete(`${API_URL}/companies/${id}`),
  updateStatus: (id, status) => axiosInstance.put(`${API_URL}/companies/${id}/status`, { status })
}

export const fileApi = {
  upload: (file, companyId) => {
    const formData = new FormData()
    formData.append('file', file)
    if (companyId) {
      formData.append('companyId', companyId)
    }
    return axiosInstance.post(`${API_URL}/files/upload`, formData)
  },
  getRecords: (params) => axiosInstance.get(`${API_URL}/files/records`, { params }),
  getDirAFiles: () => axiosInstance.get(`${API_URL}/files/dirA`),
  getDirBFiles: () => axiosInstance.get(`${API_URL}/files/dirB`),
  downloadDirBFile: (path) => {
    return axiosInstance.get(`${API_URL}/files/dirB/download`, {
      params: { path },
      responseType: 'blob'
    })
  }
}
