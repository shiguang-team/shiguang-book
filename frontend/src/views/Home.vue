<template>
  <div>
    <el-card shadow="never" class="filter">
      <div class="filter-row">
        <el-input
          v-model="query.keyword"
          placeholder="搜索书名或描述，如「数据结构」"
          clearable
          class="kw-input"
          @keyup.enter="reload"
        />
        <el-select v-model="query.category" placeholder="全部分类" clearable style="width: 120px" @change="reload">
          <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
        </el-select>
        <el-input-number v-model="query.minPrice" :min="0" :controls="false" placeholder="最低价" style="width: 95px" />
        <span class="dash">-</span>
        <el-input-number v-model="query.maxPrice" :min="0" :controls="false" placeholder="最高价" style="width: 95px" />
        <el-select v-model="query.sort" style="width: 135px" @change="reload">
          <el-option label="最新发布" value="newest" />
          <el-option label="价格从低到高" value="priceAsc" />
          <el-option label="价格从高到低" value="priceDesc" />
          <el-option label="成色从高到低" value="conditionDesc" />
        </el-select>
        <el-button type="primary" @click="reload">搜索</el-button>
        <el-button text @click="reset">重置</el-button>
      </div>
    </el-card>

    <el-empty v-if="!loading && list.length === 0" description="还没有符合条件的书，换个关键词试试" />

    <div class="grid">
      <el-card v-for="b in list" :key="b.id" shadow="hover" class="item" @click="go(b.id)">
        <div class="cover">
          <img v-if="b.cover" :src="b.cover" alt="" />
          <span v-else>{{ b.title.slice(0, 2) }}</span>
        </div>
        <div class="title">{{ b.title }}</div>
        <div class="meta">
          <el-tag size="small" type="info">{{ b.category }}</el-tag>
          <span class="cond">{{ b.conditionLevel }} 成新</span>
        </div>
        <div class="bottom">
          <span class="price">¥{{ b.price }}</span>
          <span class="seller">{{ b.sellerNickname }}</span>
        </div>
      </el-card>
    </div>

    <el-pagination
      v-if="total > 0"
      background
      :small="isMobile"
      :layout="isMobile ? 'prev, pager, next' : 'prev, pager, next, total'"
      :total="total"
      :page-size="query.size"
      :current-page="query.page"
      @current-change="onPage"
      class="pager"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '../api'

const router = useRouter()
const categories = ['教材', '考研', '课外', '其他']
const list = ref([])
const total = ref(0)
const loading = ref(false)
const isMobile = ref(window.innerWidth <= 640)
const query = ref({ page: 1, size: 12, keyword: '', category: '', minPrice: null, maxPrice: null, sort: 'newest' })

function handleResize() {
  isMobile.value = window.innerWidth <= 640
}

async function load() {
  loading.value = true
  try {
    const params = { ...query.value }
    Object.keys(params).forEach((k) => {
      if (params[k] === '' || params[k] === null || params[k] === undefined) delete params[k]
    })
    const data = await request.get('/books', { params })
    list.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function reload() {
  query.value.page = 1
  load()
}

function onPage(p) {
  query.value.page = p
  load()
  window.scrollTo(0, 0)
}

function reset() {
  query.value = { page: 1, size: 12, keyword: '', category: '', minPrice: null, maxPrice: null, sort: 'newest' }
  load()
}

function go(id) {
  router.push(`/book/${id}`)
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
.filter {
  margin: 16px 0;
}
.filter-row {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
}
.kw-input {
  width: 260px;
}
.dash {
  color: #c0c4cc;
}
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(210px, 1fr));
  gap: 14px;
}
.item {
  cursor: pointer;
}
.cover {
  height: 120px;
  border-radius: 6px;
  background: #f2f3f5;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
  color: #c0392b;
  overflow: hidden;
}
.cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.title {
  margin: 10px 0 6px;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.meta {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #909399;
  font-size: 12px;
}
.bottom {
  margin-top: 10px;
  display: flex;
  justify-content: space-between;
  align-items: baseline;
}
.price {
  color: #c0392b;
  font-size: 18px;
  font-weight: 700;
}
.seller {
  font-size: 12px;
  color: #a8abb2;
}
.pager {
  margin: 24px 0;
  justify-content: center;
}
@media (max-width: 640px) {
  .kw-input {
    width: 100%;
  }
  .grid {
    grid-template-columns: repeat(2, minmax(145px, 1fr));
    gap: 10px;
  }
}
</style>