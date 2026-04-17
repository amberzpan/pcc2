# 刷新页面内容不显示修复方案

> **目标：** 修复刷新页面后首页内容不显示的问题

**问题分析：**
- 刷新页面时，App.vue 的 loadUser() 是异步的
- HomeView 的 watch route.path 可能在 user 状态加载完成前触发
- 导致内容加载失败或状态不一致

**解决方案：**
1. HomeView onMounted 直接加载内容，不依赖 user 状态
2. 添加错误处理和重试机制
3. 确保路由变化时正确刷新

**涉及文件：**
- frontend/src/views/HomeView.vue