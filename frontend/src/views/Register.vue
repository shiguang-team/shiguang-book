<template>
  <el-card class="card">
    <h2>注册</h2>
    <el-form :model="form" :rules="rules" ref="formRef" label-width="70px">
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="form.phone" placeholder="11 位手机号" maxlength="11" />
      </el-form-item>
      <el-form-item label="密码" prop="password">
        <el-input v-model="form.password" type="password" show-password placeholder="6-20 位" />
      </el-form-item>
      <el-form-item label="昵称" prop="nickname">
        <el-input v-model="form.nickname" maxlength="20" placeholder="同学怎么称呼你" />
      </el-form-item>
      <el-form-item label="院系">
        <el-input v-model="form.dept" maxlength="30" placeholder="选填" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="loading" @click="submit">注册并登录</el-button>
        <el-button text @click="$router.push('/login')">已有账号</el-button>
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
const form = ref({ phone: '', password: '', nickname: '', dept: '' })

const rules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度需为 6-20 位', trigger: 'blur' }
  ],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }]
}

async function submit() {
  await formRef.value.validate()
  loading.value = true
  try {
    // 注册接口同样返回 token，注册完直接登录，少一次输入
    const data = await request.post('/auth/register', form.value)
    localStorage.setItem('token', data.token)
    localStorage.setItem('user', JSON.stringify(data.user))
    ElMessage.success('注册成功')
    router.replace('/')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.card {
  max-width: 460px;
  margin: 40px auto;
}
h2 {
  margin: 0 0 20px;
}
</style>
