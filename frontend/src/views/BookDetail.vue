<template>
  <el-card v-if="book" shadow="never" class="detail">
    <div class="head">
      <div class="cover">
        <img v-if="book.cover" :src="book.cover" alt="" />
        <span v-else>{{ book.title.slice(0, 2) }}</span>
      </div>
      <div class="info">
        <h2>
          {{ book.title }}
          <el-tag v-if="book.status === 0" type="info" size="small">已下架</el-tag>
        </h2>
        <div class="price">¥{{ book.price }}</div>
        <el-descriptions :column="2" border size="small">
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
      title="该书籍已下架，无法新增留言"
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
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../api'

const route = useRoute()
const book = ref(null)
const messages = ref([])
const content = ref('')

async function load() {
  book.value = await request.get(`/books/${route.params.id}`)
  messages.value = await request.get('/messages', { params: { bookId: route.params.id } })
}

async function send() {
  if (!localStorage.getItem('token')) {
    ElMessage.warning('请先登录')
    return
  }
  await request.post('/messages', { bookId: Number(route.params.id), content: content.value })
  content.value = ''
  await load()
}

onMounted(load)
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
.info h2 {
  margin: 0 0 8px;
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
</style>
