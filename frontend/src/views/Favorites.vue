<template>
  <div class="favorites-page">
    <h2>我的收藏</h2>
    <div class="fav-grid">
      <div v-for="item in favorites" :key="item.bookId" class="fav-card" :class="{ disabled: item.status !== 'ON_SHELF' }">
        <img :src="item.coverUrl" :alt="item.title" class="cover" />
        <div v-if="item.status === 'OFF_SHELF'" class="status-mask">已下架</div>
        <div v-else-if="item.status === 'DELETED'" class="status-mask">已删除</div>
        <div class="info">
          <h4>{{ item.title }}</h4>
          <p class="price">¥{{ item.price }}</p>
          <el-button size="small" type="danger" plain @click="handleRemove(item.bookId)">移除收藏</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMyFavorites, removeFavorite } from '../api/favorite'

const favorites = ref([])
const loadFavorites = async () => {
  const res = await getMyFavorites()
  favorites.value = res.data || []
}
const handleRemove = async (bookId) => {
  await removeFavorite(bookId)
  await loadFavorites()
}
onMounted(loadFavorites)
</script>
