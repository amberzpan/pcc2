import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    config.headers['Cache-Control'] = 'no-cache'
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

request.interceptors.response.use(
  response => response.data,
  error => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      window.dispatchEvent(new Event('auth:expired'))
      const authPages = ['/login', '/register']
      const publicPages = ['/', '/search']
      const currentPath = window.location.pathname
      const isPublicPage = authPages.includes(currentPath)
        || publicPages.includes(currentPath)
        || currentPath.startsWith('/post/')
        || currentPath.startsWith('/profile/')
      if (!isPublicPage) {
        window.location.replace('/login')
      }
    }
    return Promise.reject(error)
  }
)

export const login = (data) => request.post('/user/login', data)
export const register = (data) => request.post('/user/register', data)
export const forgotPassword = (data) => request.post('/user/forgot-password', data)
export const getUserInfo = () => request.get('/user/info')
export const getUserProfile = (userId) => request.get(`/user/${userId}/profile`)
export const updateUserInfo = (data) => request.put('/user/info', data)
export const searchUsers = (keyword, page = 1, size = 10) => request.get('/user/search', { params: { keyword, page, size } })

export const getPostList = (page, size) => request.get('/post/list', { params: { page, size, _t: Date.now() } })
export const getUserPosts = (userId, page, size) => request.get(`/post/user/${userId}`, { params: { page, size } })
export const getPost = (id) => request.get(`/post/${id}`)
export const searchPosts = (keyword, page = 1, size = 10) => request.get('/post/search', { params: { keyword, page, size } })
export const getFollowingPosts = (page = 1, size = 10) => request.get('/post/following', { params: { page, size } })
export const getHotPosts = (page = 1, size = 10) => request.get('/post/hot', { params: { page, size } })
export const getTodayHotPosts = (page = 1, size = 10) => request.get('/post/today-hot', { params: { page, size } })
export const createPost = (data) => request.post('/post', data)
export const deletePost = (id) => request.delete(`/post/${id}`)
export const uploadImage = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/post/upload', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
}
export const uploadVideo = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/post/uploadVideo', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
}

export const getComments = (postId, sort = 'time_desc') => request.get(`/comment/post/${postId}`, { params: { sort } })
export const createComment = (data) => request.post('/comment', data)
export const deleteComment = (id) => request.delete(`/comment/${id}`)
export const toggleCommentLike = (commentId) => request.post(`/comment/${commentId}/like`)
export const getCommentLikeStatus = (commentId) => request.get(`/comment/${commentId}/like/status`)

export const toggleLike = (postId) => request.post(`/like/${postId}`)
export const getLikeStatus = (postId) => request.get(`/like/status/${postId}`)
export const getLikedPosts = (page = 1, size = 10) => request.get('/like/list', { params: { page, size } })

export const toggleFollow = (userId) => request.post(`/follow/${userId}`)
export const getFollowStatus = (userId) => request.get(`/follow/status/${userId}`)
export const getFollowers = (userId, page, size) => request.get(`/follow/followers/${userId}`, { params: { page, size } })
export const getFollowing = (userId, page, size) => request.get(`/follow/following/${userId}`, { params: { page, size } })

export const toggleFavorite = (postId) => request.post(`/favorite/${postId}`)
export const getFavoriteStatus = (postId) => request.get(`/favorite/status/${postId}`)
export const getFavorites = (page, size) => request.get('/favorite/list', { params: { page, size } })

export const changePassword = (data) => request.put('/user/password', data)
