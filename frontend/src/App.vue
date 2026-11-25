<template>
  <el-container class="layout">
    <NavBar />

    <el-main class="main">
      <router-view />
    </el-main>

    <el-footer class="footer">拾光小组 · 校园二手书交易平台</el-footer>
  </el-container>
</template>

<script setup>
import NavBar from './components/NavBar.vue'
</script>

<style>
body {
  margin: 0;
  background: #f6f7f9;
  font-family: system-ui, -apple-system, 'Microsoft YaHei', sans-serif;
}
.main {
  max-width: 1080px;
  width: 100%;
  margin: 0 auto;
  box-sizing: border-box;
  padding: 0 12px;
}
.footer {
  text-align: center;
  color: #a8abb2;
  font-size: 12px;
  line-height: 48px;
}
</style>