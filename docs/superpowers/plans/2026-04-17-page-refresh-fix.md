# 刷新页面内容不显示修复计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 修复刷新页面后首页内容不显示的问题

**Architecture:** 在 HomeView 中添加更可靠的状态加载机制，确保 onMounted 和 watch 都能正确触发内容加载

**Tech Stack:** Vue 3, Composition API

---

### Task 1: 修复 HomeView 内容加载逻辑

**Files:**
- Modify: `E:\vscode\code\pcc2\frontend\src\views\HomeView.vue:77-120`

- [ ] **Step 1: 检查当前 loadPosts 函数实现**

读取 HomeView.vue 的 loadPosts 函数，了解当前实现

- [ ] **Step 2: 修改 loadPosts 添加错误重试**

```javascript
const loadPosts = async (retryCount = 0) => {
  loading.value = true
  page.value = 1
  try {
    const res = await getPostList(page.value, size)
    if (res.code === 200 && res.data) {
      posts.value = res.data.map(p => ({ ...p, showComments: false, comments: [], newComment: '' }))
      hasMore.value = res.data.length === size
    } else {
      posts.value = []
      hasMore.value = false
    }
  } catch (e) {
    console.error('加载失败:', e)
    if (retryCount < 2) {
      setTimeout(() => loadPosts(retryCount + 1), 1000)
    } else {
      showToast('加载失败', 'error')
    }
  } finally {
    loading.value = false
  }
}
```

- [ ] **Step 3: 确保 onMounted 正确调用 loadPosts**

当前 onMounted 代码:
```javascript
onMounted(async () => {
  await loadPosts()
})
```

确认已正确实现

- [ ] **Step 4: 验证 watch 路由变化**

当前 watch 代码:
```javascript
watch(() => route.path, () => {
  if (route.path === '/') {
    loadPosts()
  }
})
```

确认已正确实现

- [ ] **Step 5: 测试验证**

使用 Node.js 测试 API:
```javascript
const http = require('http');
const options = { hostname: 'localhost', port: 5173, path: '/api/post/list?page=1&size=5', method: 'GET' };
http.request(options, (res) => {
  let data = '';
  res.on('data', chunk => data += chunk);
  res.on('end', () => console.log(data));
}).end();
```

预期: 返回帖子列表数据

---

### Task 2: 可选 - 添加 App 级别状态同步

**Files:**
- Modify: `E:\vscode\code\pcc2\frontend\src\App.vue:78-80`

- [ ] **Step 1: 检查当前 App.vue onMounted 实现**

- [ ] **Step 2: 添加 user 加载后的全局事件**

在 loadUser 成功后可以触发一个全局事件，让子组件响应

```javascript
const loadUser = async () => {
  if (token.value) {
    try {
      const res = await getUserInfo()
      if (res.code === 200) {
        user.value = res.data
        // 可以在这里通知子组件用户已加载
      }
    } catch (e) {
      console.error(e)
    }
  }
}
```

---

**验证命令:**
1. 后端 API 测试: `E:\Node\node.exe E:\vscode\code\pcc2\test-api.js`
2. 前端刷新测试: 访问 http://localhost:5173 并刷新页面