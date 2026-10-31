<template>
  <el-card class="card">
    <h2>登录</h2>
    <el-form :model="form" :rules="rules" ref="formRef" label-width="70px">
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="form.phone" placeholder="11 位手机号" maxlength="11" />
      </el-form-item>
      <el-form-item label="密码" prop="password">
        <el-input v-model="form.password" type="password" show-password placeholder="至少 6 位" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="loading" @click="submit">登录</el-button>
        <el-button text @click="$router.push('/register')">还没有账号？去注册</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../api'

const router = useRouter()
const route = useRoute()
const formRef = ref()
const loading = ref(false)
const form = ref({ phone: '', password: '' })

const rules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function submit() {
  await formRef.value.validate()
  loading.value = true
  try {
    const data = await request.post('/auth/login', form.value)
    localStorage.setItem('token', data.token)
    localStorage.setItem('user', JSON.stringify(data.user))
    ElMessage.success('登录成功')
    router.replace(route.query.redirect || '/')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.card {
  max-width: 420px;
  margin: 60px auto;
}
h2 {
  margin: 0 0 20px;
}
</style>
