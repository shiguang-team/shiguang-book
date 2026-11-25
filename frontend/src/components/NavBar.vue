<template>
  <el-header class="header">
    <div class="brand" @click="$router.push('/')">
      拾光<span class="sub">校园二手书</span>
    </div>
    <div class="actions">
      <el-button type="primary" plain size="small" @click="$router.push('/publish')">发布书籍</el-button>
      <template v-if="user">
        <el-button text size="small" @click="$router.push('/favorites')">我的收藏</el-button>
        <el-badge :value="unreadCount" :hidden="unreadCount <= 0" :max="99" class="badge-wrap">
          <el-button text size="small" @click="$router.push('/profile')">{{ user.nickname }}</el-button>
        </el-badge>
        <el-button text size="small" @click="handleLogout">退出</el-button>
      </template>
      <template v-else>
        <el-button text size="small" @click="$router.push('/login')">登录</el-button>
        <el-button text size="small" @click="$router.push('/register')">注册</el-button>
      </template>
    </div>
  </el-header>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../api'

const router = useRouter()
const route = useRoute()
const user = ref(null)
const unreadCount = ref(0)
let pollTimer = null

function refreshUser() {
  const raw = localStorage.getItem('user')
  user.value = raw ? JSON.parse(raw) : null
}

async function fetchUnreadCount() {
  if (!localStorage.getItem('token')) {
    unreadCount.value = 0
    return
  }
  try {
    const data = await request.get('/messages/unread-count')
    unreadCount.value = data?.unreadCount || 0
  } catch {
    // 忽略轮询瞬时错误
  }
}

async function handleLogout() {
  try {
    if (localStorage.getItem('token')) {
      await request.post('/auth/logout')
    }
  } catch {
    // 即使网络异常也继续清理本地登录态
  } finally {
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    user.value = null
    unreadCount.value = 0
    ElMessage.success('已退出登录')
    router.push('/')
  }
}

watch(
  () => route.fullPath,
  () => {
    refreshUser()
    fetchUnreadCount()
  }
)

onMounted(() => {
  refreshUser()
  fetchUnreadCount()
  pollTimer = setInterval(fetchUnreadCount, 30000)
  window.addEventListener('unread-updated', fetchUnreadCount)
})

onUnmounted(() => {
  if (pollTimer) clearInterval(pollTimer)
  window.removeEventListener('unread-updated', fetchUnreadCount)
})
</script>

<style scoped>
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 8px;
  min-height: 60px;
  height: auto;
  padding: 8px 16px;
  background: #fff;
  border-bottom: 1px solid #ebeef5;
}
.brand {
  font-size: 20px;
  font-weight: 700;
  color: #c0392b;
  cursor: pointer;
}
.brand .sub {
  font-size: 12px;
  font-weight: 400;
  color: #909399;
  margin-left: 8px;
}
.actions {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px;
}
.badge-wrap :deep(.el-badge__content.is-fixed) {
  top: 4px;
  right: 6px;
}
</style>