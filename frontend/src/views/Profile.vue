<template>
  <div class="profile">
    <el-card shadow="never">
      <h2>{{ user?.nickname || '个人中心' }}</h2>
      <p class="sub">{{ user?.phone }} · {{ user?.dept }}</p>
    </el-card>

    <el-tabs v-model="tab" class="tabs" @tab-change="onTabChange">
      <el-tab-pane label="我发布的书籍" name="books">
        <el-table :data="pagedBooks" size="small">
          <el-table-column prop="title" label="书名" min-width="160" />
          <el-table-column prop="price" label="价格" width="80">
            <template #default="{ row }">¥{{ row.price }}</template>
          </el-table-column>
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
                {{ row.status === 1 ? '在售' : '已下架' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="210">
            <template #default="{ row }">
              <el-button link type="primary" @click="$router.push(`/publish?id=${row.id}`)">编辑</el-button>
              <el-button link type="primary" @click="toggle(row)">
                {{ row.status === 1 ? '下架' : '重新上架' }}
              </el-button>
              <el-button link type="danger" @click="remove(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination
          v-if="myBooks.length > pageSize"
          background
          small
          layout="prev, pager, next"
          :total="myBooks.length"
          :page-size="pageSize"
          v-model:current-page="bookPage"
          class="pager"
        />
      </el-tab-pane>

      <el-tab-pane label="我收到的留言" name="messages">
        <div v-for="m in received" :key="m.id" class="msg">
          <span class="who">{{ m.senderNickname }}</span>
          <span class="text">{{ m.content }}</span>
          <span class="time">{{ m.createdAt }}</span>
        </div>
        <el-empty v-if="received.length === 0" description="还没有人给你留言" :image-size="60" />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../api'

const tab = ref('books')
const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))
const myBooks = ref([])
const received = ref([])
const bookPage = ref(1)
const pageSize = 8

const pagedBooks = computed(() => {
  const start = (bookPage.value - 1) * pageSize
  return myBooks.value.slice(start, start + pageSize)
})

async function load() {
  myBooks.value = await request.get('/books/mine')
  received.value = await request.get('/messages/received')
}

async function onTabChange(name) {
  if (name === 'messages') {
    await request.put('/messages/read-all')
    window.dispatchEvent(new Event('unread-updated'))
    received.value = await request.get('/messages/received')
  }
}

async function toggle(row) {
  if (row.status === 1) {
    const { value } = await ElMessageBox.prompt('可填写下架原因（选填，将展示在书籍详情页）', '下架书籍', {
      confirmButtonText: '确认下架',
      cancelButtonText: '取消',
      inputPlaceholder: '例如：已线下交易出清'
    })
    await request.put(`/books/${row.id}/status`, null, { params: { status: 0, reason: value || '' } })
    ElMessage.success('已下架')
  } else {
    await request.put(`/books/${row.id}/status`, null, { params: { status: 1 } })
    ElMessage.success('已重新上架')
  }
  await load()
}

async function remove(row) {
  await ElMessageBox.confirm(`确定删除《${row.title}》吗？删除后历史留言仍会保留。`, '提示', { type: 'warning' })
  await request.delete(`/books/${row.id}`)
  ElMessage.success('已删除')
  await load()
}

onMounted(load)
</script>

<style scoped>
.profile {
  margin: 20px 0 40px;
}
h2 {
  margin: 0;
}
.sub {
  color: #909399;
  font-size: 13px;
  margin: 6px 0 0;
}
.tabs {
  margin-top: 16px;
}
.pager {
  margin-top: 14px;
  justify-content: center;
}
.msg {
  padding: 8px 0;
  border-bottom: 1px dashed #ebeef5;
  display: flex;
  gap: 10px;
  align-items: baseline;
}
.who {
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
</style>