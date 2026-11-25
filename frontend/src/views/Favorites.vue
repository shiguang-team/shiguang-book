<template>
  <div class="favorites-page">
    <h2>我的收藏</h2>
    <el-empty v-if="!loading && favorites.length === 0" description="还没有收藏任何书籍" />

    <div class="fav-grid">
      <el-card
        v-for="item in favorites"
        :key="item.bookId"
        shadow="hover"
        class="fav-card"
        :class="{ disabled: item.status !== 'ON_SHELF' }"
      >
        <div class="cover-box" @click="goDetail(item)">
          <img v-if="item.coverUrl" :src="item.coverUrl" :alt="item.title" />
          <span v-else>{{ (item.title || '书').slice(0, 2) }}</span>
          <div v-if="item.status === 'OFF_SHELF'" class="status-mask">已下架</div>
          <div v-else-if="item.status === 'DELETED'" class="status-mask">已删除</div>
        </div>
        <div class="info">
          <div class="title" @click="goDetail(item)">{{ item.title }}</div>
          <div class="meta-row">
            <span v-if="item.price !== null && item.price !== undefined" class="price">¥{{ item.price }}</span>
            <span v-else class="deleted-tip">原书已失效</span>
            <el-button size="small" type="danger" plain @click="handleRemove(item.bookId)">移除收藏</el-button>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getMyFavorites, removeFavorite } from '../api/favorite'

const router = useRouter()
const favorites = ref([])
const loading = ref(false)

async function loadFavorites() {
  loading.value = true
  try {
    favorites.value = (await getMyFavorites()) || []
  } finally {
    loading.value = false
  }
}

function goDetail(item) {
  if (item.status === 'DELETED') {
    ElMessage.info('该书籍已被卖家删除，无法查看详情')
    return
  }
  router.push(`/book/${item.bookId}`)
}

async function handleRemove(bookId) {
  await removeFavorite(bookId)
  ElMessage.success('已移除收藏')
  await loadFavorites()
}

onMounted(loadFavorites)
</script>

<style scoped>
.favorites-page {
  margin: 20px 0 40px;
}
h2 {
  margin: 0 0 16px;
}
.fav-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(210px, 1fr));
  gap: 14px;
}
.fav-card.disabled {
  opacity: 0.78;
}
.cover-box {
  position: relative;
  height: 120px;
  border-radius: 6px;
  background: #f2f3f5;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
  color: #c0392b;
  overflow: hidden;
  cursor: pointer;
}
.cover-box img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.status-mask {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.52);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
}
.title {
  margin: 10px 0 8px;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  cursor: pointer;
}
.meta-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.price {
  color: #c0392b;
  font-size: 18px;
  font-weight: 700;
}
.deleted-tip {
  color: #909399;
  font-size: 12px;
}
@media (max-width: 640px) {
  .fav-grid {
    grid-template-columns: repeat(2, minmax(145px, 1fr));
    gap: 10px;
  }
}
</style>