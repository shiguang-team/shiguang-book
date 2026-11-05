<template>
  <header class="navbar">
    <div class="logo" @click="$router.push('/')">拾光二手书</div>
    <nav class="nav-links">
      <router-link to="/">首页</router-link>
      <router-link to="/publish">发布书籍</router-link>
      <router-link to="/favorites">我的收藏</router-link>
      <router-link to="/profile" class="profile-link">
        个人中心
        <el-badge v-if="unreadCount > 0" :value="unreadCount" :max="99" class="unread-badge" />
      </router-link>
      <el-button v-if="hasToken" link @click="handleLogout">退出登录</el-button>
    </nav>
  </header>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '../api/index'

const router = useRouter()
const unreadCount = ref(0)
const hasToken = ref(!!localStorage.getItem('token'))
let pollTimer = null

const fetchUnreadCount = async () => {
  if (!localStorage.getItem('token')) return
  const res = await request.get('/api/messages/unread-count')
  unreadCount.value = res.data?.unreadCount || 0
}

const handleLogout = async () => {
  try {
    await request.post('/api/auth/logout')
  } finally {
    if (pollTimer) clearInterval(pollTimer)
    localStorage.removeItem('token')
    unreadCount.value = 0
    hasToken.value = false
    router.push('/login')
  }
}

onMounted(() => {
  fetchUnreadCount()
  pollTimer = setInterval(fetchUnreadCount, 30000)
})

onUnmounted(() => {
  if (pollTimer) clearInterval(pollTimer)
})
</script>
