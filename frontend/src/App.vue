<template>
  <el-container class="layout">
    <el-header class="header">
      <div class="brand" @click="$router.push('/')">
        拾光<span class="sub">校园二手书</span>
      </div>
      <div class="actions">
        <el-button type="primary" plain @click="$router.push('/publish')">发布书籍</el-button>
        <template v-if="user">
          <el-button text @click="$router.push('/profile')">{{ user.nickname }}</el-button>
          <el-button text @click="logout">退出</el-button>
        </template>
        <template v-else>
          <el-button text @click="$router.push('/login')">登录</el-button>
          <el-button text @click="$router.push('/register')">注册</el-button>
        </template>
      </div>
    </el-header>

    <el-main class="main">
      <router-view />
    </el-main>

    <el-footer class="footer">拾光小组 · 软件工程 Alpha 阶段 v0.1</el-footer>
  </el-container>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()
const user = ref(null)

function refreshUser() {
  const raw = localStorage.getItem('user')
  user.value = raw ? JSON.parse(raw) : null
}

onMounted(refreshUser)
// 登录/退出后路由会变化，这里同步一下顶部状态，避免要手动刷新页面
watch(() => route.fullPath, refreshUser)

function logout() {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  user.value = null
  router.push('/')
}
</script>

<style>
body {
  margin: 0;
  background: #f6f7f9;
  font-family: system-ui, -apple-system, 'Microsoft YaHei', sans-serif;
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
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
.main {
  max-width: 1080px;
  width: 100%;
  margin: 0 auto;
}
.footer {
  text-align: center;
  color: #a8abb2;
  font-size: 12px;
  line-height: 48px;
}
</style>
