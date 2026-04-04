import axios from 'axios'

const API_URL = '/api'

// User API
export const userApi = {
  getAll: () => axios.get(`${API_URL}/users`),
  updateStatus: (id, status, sendEmail) => axios.put(`${API_URL}/users/${id}/status`, {
    status,
    sendEmail
  }),
  resetPassword: (id, sendEmail) => axios.post(`${API_URL}/users/${id}/reset-password`, {
    sendEmail
  }),
  getMe: () => axios.get(`${API_URL}/users/me`)
}

// Company API
export const companyApi = {
  getAll: () => axios.get(`${API_URL}/companies`),
  create: (name) => axios.post(`${API_URL}/companies`, { name })
}

// File API
export const fileApi = {
  upload: (file, companyId) => {
    const formData = new FormData()
    formData.append('file', file)
    if (companyId) {
      formData.append('companyId', companyId)
    }
    return axios.post(`${API_URL}/files/upload`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },
  getRecords: () => axios.get(`${API_URL}/files/records`),
  getDirAFiles: () => axios.get(`${API_URL}/files/dirA`),
  getDirBFiles: () => axios.get(`${API_URL}/files/dirB`),
  downloadDirBFile: (companyName, filename) => {
    return axios.get(`${API_URL}/files/dirB/${companyName}/${filename}`, {
      responseType: 'blob'
    })
  }
}
