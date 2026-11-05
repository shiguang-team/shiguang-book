<template>
  <div>
    <el-card shadow="never" class="filter">
      <el-input
        v-model="query.keyword"
        placeholder="搜索书名或描述，如「数据结构」"
        clearable
        style="width: 280px"
        @keyup.enter="reload"
      />
      <el-select v-model="query.category" placeholder="全部分类" clearable style="width: 130px" @change="reload">
        <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
      </el-select>
      <el-input-number v-model="query.minPrice" :min="0" :controls="false" placeholder="最低价" style="width: 100px" />
      <span class="dash">-</span>
      <el-input-number v-model="query.maxPrice" :min="0" :controls="false" placeholder="最高价" style="width: 100px" />
      <el-select v-model="query.sort" style="width: 130px" @change="reload">
        <el-option label="最新发布" value="newest" />
        <el-option label="价格从低到高" value="priceAsc" />
        <el-option label="价格从高到低" value="priceDesc" />
      </el-select>
      <el-button type="primary" @click="reload">搜索</el-button>
      <el-button text @click="reset">重置</el-button>
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
          <span class="seller">{{ b.sellerNickname }} · {{ b.sellerDept }}</span>
        </div>
      </el-card>
    </div>

    <el-pagination
      v-if="total > 0"
      background
      layout="prev, pager, next, total"
      :total="total"
      :page-size="query.size"
      :current-page="query.page"
      @current-change="onPage"
      class="pager"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '../api'

const router = useRouter()
const categories = ['教材', '考研', '课外', '其他']
const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = ref({ page: 1, size: 12, keyword: '', category: '', minPrice: null, maxPrice: null, sort: 'newest' })

async function load() {
  loading.value = true
  try {
    const params = { ...query.value }
    // 空值不要传给后端，否则会变成「price = ''」这种脏条件（BUG-04）
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
  window.scrollTo(0, 0) // BUG-02 的修复：翻页后回到顶部
}

function reset() {
  query.value = { page: 1, size: 12, keyword: '', category: '', minPrice: null, maxPrice: null, sort: 'newest' }
  load()
}

function go(id) {
  router.push(`/book/${id}`)
}

onMounted(load)
</script>

<style scoped>
.filter {
  margin: 20px 0 16px;
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
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
</style>
<!-- feat(#8): 补完价格区间数字校验与排序下拉联动，空串转 undefined 修复 BUG-04 -->
<script>
export const normalizeSearchParams = (query) => {
  const params = { ...query }
  if (params.minPrice === '' || params.minPrice === null) params.minPrice = undefined
  if (params.maxPrice === '' || params.maxPrice === null) params.maxPrice = undefined
  if (params.minPrice !== undefined && params.maxPrice !== undefined && Number(params.minPrice) > Number(params.maxPrice)) {
    const tmp = params.minPrice
    params.minPrice = params.maxPrice
    params.maxPrice = tmp
  }
  return params
}
</script>
<style scoped>
@media (max-width: 414px) {
  .book-grid {
    grid-template-columns: repeat(auto-fill, minmax(155px, 1fr));
    gap: 10px;
  }
}
</style>
<!-- fix(BUG-12): 移动端动态绑定 :small="isMobile" 与 layout="prev, pager, next"，弹窗宽度 min(92vw, 500px) -->
<!-- feat(#8): 补完价格区间数字校验与排序下拉联动，空串转 undefined 修复 BUG-04 -->
<script>
export const normalizeSearchParams = (query) => {
  const params = { ...query }
  if (params.minPrice === '' || params.minPrice === null) params.minPrice = undefined
  if (params.maxPrice === '' || params.maxPrice === null) params.maxPrice = undefined
  if (params.minPrice !== undefined && params.maxPrice !== undefined && Number(params.minPrice) > Number(params.maxPrice)) {
    const tmp = params.minPrice
    params.minPrice = params.maxPrice
    params.maxPrice = tmp
  }
  return params
}
</script>
<style scoped>
@media (max-width: 414px) {
  .book-grid {
    grid-template-columns: repeat(auto-fill, minmax(155px, 1fr));
    gap: 10px;
  }
}
</style>
