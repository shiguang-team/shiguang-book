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
    </nav>
  </header>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../api/index'

const unreadCount = ref(0)
let pollTimer = null

const fetchUnreadCount = async () => {
  const res = await request.get('/api/messages/unread-count')
  unreadCount.value = res.data?.unreadCount || 0
}

onMounted(() => {
  fetchUnreadCount()
  pollTimer = setInterval(fetchUnreadCount, 30000)
})
</script>
