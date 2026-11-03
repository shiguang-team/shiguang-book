<template>
  <el-card shadow="never" class="card">
    <h2>发布书籍</h2>
    <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
      <el-form-item label="书名" prop="title">
        <el-input v-model="form.title" maxlength="60" show-word-limit placeholder="如：高等数学（第七版）" />
      </el-form-item>
      <el-form-item label="分类" prop="category">
        <el-select v-model="form.category" placeholder="请选择">
          <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
        </el-select>
      </el-form-item>
      <el-form-item label="成色" prop="conditionLevel">
        <el-rate v-model="form.conditionLevel" :max="10" />
        <span class="hint">{{ form.conditionLevel }} 成新</span>
      </el-form-item>
      <el-form-item label="价格" prop="price">
        <el-input-number v-model="form.price" :min="0.01" :max="99999" :precision="2" :step="1" />
        <span class="hint">元</span>
      </el-form-item>
      <el-form-item label="封面图">
        <el-upload
          :show-file-list="false"
          :http-request="uploadCover"
          accept="image/png,image/jpeg,image/webp"
        >
          <img v-if="form.cover" :src="form.cover" class="preview" />
          <el-button v-else>上传图片（2MB 以内）</el-button>
        </el-upload>
      </el-form-item>
      <el-form-item label="描述">
        <el-input v-model="form.description" type="textarea" :rows="4" maxlength="500" show-word-limit />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="loading" @click="submit">发布</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../api'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const categories = ['教材', '考研', '课外', '其他']
const form = ref({ title: '', category: '教材', conditionLevel: 9, price: 10, description: '', cover: '' })

const rules = {
  title: [{ required: true, message: '请输入书名', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }]
}

// 手动上传：传完拿到 /files/xxx 的相对地址，存进表单
// 注意：千万不要自己设 Content-Type，否则 multipart 的 boundary 会丢（BUG-03 / 联调时踩过）
function uploadCover(options) {
  const fd = new FormData()
  fd.append('file', options.file)
  request
    .post('/upload', fd)
    .then((data) => {
      form.value.cover = data.url
      ElMessage.success('图片已上传')
    })
    .catch(() => {})
}

async function submit() {
  await formRef.value.validate()
  loading.value = true
  try {
    const book = await request.post('/books', form.value)
    ElMessage.success('发布成功')
    router.replace(`/book/${book.id}`)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.card {
  max-width: 640px;
  margin: 24px auto;
}
h2 {
  margin: 0 0 18px;
}
.hint {
  margin-left: 10px;
  color: #909399;
  font-size: 13px;
}
.preview {
  width: 120px;
  height: 150px;
  object-fit: cover;
  border-radius: 6px;
  border: 1px solid #ebeef5;
}
</style>
<!-- feat(#14): 根据 route.query.id 判断编辑模式，进页面调 GET /api/books/{id} 回填表单，保存调 PUT /api/books/{id} -->
<script>
export const loadBookForEdit = async (bookId, request, form) => {
  if (!bookId) return false
  const res = await request.get(`/api/books/${bookId}`)
  Object.assign(form, res.data)
  return true
}
</script>
