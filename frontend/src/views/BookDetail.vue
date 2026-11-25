<template>
  <el-card v-if="book" shadow="never" class="detail">
    <div class="head">
      <div class="cover">
        <img v-if="book.cover" :src="book.cover" alt="" />
        <span v-else>{{ book.title.slice(0, 2) }}</span>
      </div>
      <div class="info">
        <div class="title-row">
          <h2>
            {{ book.title }}
            <el-tag v-if="book.status === 0" type="info" size="small">已下架</el-tag>
          </h2>
          <el-button
            :type="isFav ? 'warning' : 'default'"
            :plain="!isFav"
            size="small"
            :loading="favLoading"
            @click="toggleFavorite"
          >
            {{ isFav ? '已收藏' : '加入收藏' }}
          </el-button>
        </div>
        <div class="price">¥{{ book.price }}</div>
        <el-descriptions :column="isMobile ? 1 : 2" border size="small">
          <el-descriptions-item label="分类">{{ book.category }}</el-descriptions-item>
          <el-descriptions-item label="成色">{{ book.conditionLevel }} 成新</el-descriptions-item>
          <el-descriptions-item label="卖家">{{ book.sellerNickname }}</el-descriptions-item>
          <el-descriptions-item label="院系">{{ book.sellerDept }}</el-descriptions-item>
        </el-descriptions>
        <p class="desc">{{ book.description || '卖家没有写描述' }}</p>
      </div>
    </div>
  </el-card>

  <el-card shadow="never" class="msgs">
    <h3>留言（{{ messages.length }}）</h3>

    <el-alert
      v-if="book && book.status === 0"
      type="info"
      :closable="false"
      :title="book.offShelfReason ? `该书籍已下架（原因：${book.offShelfReason}），无法新增留言` : '该书籍已下架，无法新增留言'"
      show-icon
    />

    <div v-for="m in messages" :key="m.id" class="msg">
      <span class="who">{{ m.senderNickname }}</span>
      <span class="text">{{ m.content }}</span>
      <span class="time">{{ m.createdAt }}</span>
    </div>
    <el-empty v-if="messages.length === 0" description="还没有人留言" :image-size="60" />

    <div v-if="book && book.status === 1" class="form">
      <el-input v-model="content" maxlength="200" show-word-limit placeholder="问问成色、能不能自提……" />
      <el-button type="primary" :disabled="!content.trim()" @click="send">发送</el-button>
    </div>
  </el-card>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../api'
import { addFavorite, removeFavorite, checkFavorite } from '../api/favorite'

const route = useRoute()
const router = useRouter()
const book = ref(null)
const messages = ref([])
const content = ref('')
const isFav = ref(false)
const favLoading = ref(false)
const isMobile = ref(window.innerWidth <= 640)

function handleResize() {
  isMobile.value = window.innerWidth <= 640
}

async function load() {
  const id = route.params.id
  book.value = await request.get(`/books/${id}`)
  messages.value = await request.get('/messages', { params: { bookId: id } })
  if (localStorage.getItem('token')) {
    isFav.value = !!(await checkFavorite(id))
  } else {
    isFav.value = false
  }
}

async function toggleFavorite() {
  if (!localStorage.getItem('token')) {
    ElMessage.warning('请先登录后再收藏')
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  favLoading.value = true
  try {
    const id = route.params.id
    if (isFav.value) {
      await removeFavorite(id)
      isFav.value = false
      ElMessage.success('已取消收藏')
    } else {
      await addFavorite(id)
      isFav.value = true
      ElMessage.success('已加入收藏')
    }
  } finally {
    favLoading.value = false
  }
}

async function send() {
  if (!localStorage.getItem('token')) {
    ElMessage.warning('请先登录')
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  await request.post('/messages', { bookId: Number(route.params.id), content: content.value })
  content.value = ''
  await load()
}

onMounted(() => {
  load()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.detail {
  margin: 20px 0 14px;
}
.head {
  display: flex;
  gap: 20px;
}
.cover {
  width: 160px;
  height: 200px;
  border-radius: 8px;
  background: #f2f3f5;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 34px;
  color: #c0392b;
  overflow: hidden;
  flex: none;
}
.cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.info {
  flex: 1;
}
.title-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 10px;
  margin-bottom: 8px;
}
.info h2 {
  margin: 0;
}
.price {
  color: #c0392b;
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 10px;
}
.desc {
  margin-top: 12px;
  color: #606266;
  line-height: 1.7;
}
.msgs {
  margin-bottom: 30px;
}
.msg {
  padding: 8px 0;
  border-bottom: 1px dashed #ebeef5;
  display: flex;
  gap: 10px;
  align-items: baseline;
}
.who {
  color: #303133;
  font-weight: 600;
  min-width: 70px;
}
.text {
  flex: 1;
  color: #606266;
}
.time {
  color: #c0c4cc;
  font-size: 12px;
}
.form {
  display: flex;
  gap: 10px;
  margin-top: 14px;
}
@media (max-width: 640px) {
  .head {
    flex-direction: column;
  }
  .cover {
    width: 100%;
    height: 180px;
  }
}
</style>