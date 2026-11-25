# 前端组件与响应式适配规范文档（李卓轩，Issue #14 / #21）

## 一、全局请求与路由守卫规范
1. src/api/index.js 响应拦截器遇到 401 仅负责清除 localStorage 中的 token，绝不直接调用 location.href 跳转。
2. 登录态拦截统一由 src/router/index.js 路由守卫根据 meta.auth 判断并携带 redirect 参数跳转。
3. FormData 上传图片时严禁手动设置 Content-Type，交由浏览器自动生成 boundary。

## 二、组件复用与移动端 375px / 414px 断点规范
1. Publish.vue 同时承载“发布”与“编辑回填”，必须使用 watch(() => route.query.id) 监听参数变化，切新建时主动调用 resetFields()。
2. 移动端适配（#21）：首页卡片流切换为 minmax(155px, 1fr) 手机双列布局，筛选区折叠进 el-collapse，分页条动态绑定 :small="isMobile"。
