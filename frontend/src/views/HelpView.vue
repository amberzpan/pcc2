<template>
  <section class="help-page">
    <article class="panel">
      <h2>帮助中心</h2>
      <p>这里专注回答用户在使用产品时最常见的问题。</p>
    </article>

    <article class="panel">
      <h3>快速上手</h3>
      <ul>
        <li>发布动态：进入“发布”页，输入内容后可添加媒体或表情包再发布。</li>
        <li>关注内容：在首页切到“关注”信息流即可看已关注作者的动态。</li>
        <li>管理收藏：在“收藏”页查看和取消收藏内容。</li>
        <li>个人主页：在“主页”页可编辑资料、查看动态和粉丝关注关系。</li>
        <li>账号安全：在“设置”页可修改密码与调整主题。</li>
      </ul>
    </article>

    <article class="panel">
      <h3>常见问题</h3>
      <ul>
        <li>为什么看不到“热门/关注”？未登录时只能查看首页公开流，登录后自动开放。</li>
        <li>头像和背景怎么改？在个人主页点击头像/背景上传，然后保存资料。</li>
        <li>帖子互动包括哪些？支持点赞、评论、收藏、转发和查看原帖。</li>
      </ul>
    </article>

    <article class="panel">
      <h3>更多说明</h3>
      <p>如果你需要查看完整规格说明，可以打开以下文档：</p>
      <div class="links">
        <a href="/SPEC.md" target="_blank" rel="noopener">产品规格（SPEC）</a>
      </div>
    </article>
  </section>
</template>

<script setup>
</script>

<style scoped>
.help-page {
  display: grid;
  gap: 14px;
  width: 100%;
  max-width: 920px;
  margin: 0 auto;
}

.panel {
  border: 1px solid var(--line);
  border-radius: 16px;
  background: var(--paper);
  padding: 16px;
}

h2,
h3 {
  margin: 0;
}

p {
  margin: 8px 0 0;
  color: var(--muted);
}

ul {
  margin: 10px 0 0;
  padding-left: 18px;
  color: var(--ink);
  line-height: 1.7;
}

.links {
  margin-top: 10px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.links a {
  border: 1px solid var(--line);
  border-radius: 999px;
  padding: 7px 12px;
  text-decoration: none;
  color: var(--ink);
  background: var(--surface);
  font-weight: 700;
}
</style>
