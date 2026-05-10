<template>
  <section class="help-page">
    <article class="panel hero">
      <div class="hero-copy">
        <p class="eyebrow">帮助中心</p>
        <h2>把常用功能一次看明白</h2>
        <p class="hero-text">
          这页把 SimpleSocial 最常用的功能整理成面向用户的说明。无论你是第一次浏览，还是准备开始发布内容，都可以从这里快速找到入口。
        </p>
      </div>

      <nav class="jump-links" aria-label="帮助导航">
        <a v-for="group in helpGroups" :key="group.id" class="jump-link" :href="`#${group.id}`">
          <span>{{ group.kicker }}</span>
          <strong>{{ group.title }}</strong>
        </a>
      </nav>
    </article>

    <article class="panel">
      <header class="section-head">
        <p class="eyebrow">快速开始</p>
        <h3>第一次使用，先从这三步开始</h3>
      </header>

      <ol class="step-list">
        <li v-for="(item, index) in gettingStarted" :key="item.title" class="step-item">
          <span class="step-index">{{ String(index + 1).padStart(2, '0') }}</span>
          <div class="step-copy">
            <strong>{{ item.title }}</strong>
            <p>{{ item.body }}</p>
          </div>
        </li>
      </ol>
    </article>

    <article
      v-for="group in helpGroups"
      :id="group.id"
      :key="group.id"
      class="panel help-section"
    >
      <header class="section-head">
        <p class="eyebrow">{{ group.kicker }}</p>
        <h3>{{ group.title }}</h3>
        <p class="section-summary">{{ group.summary }}</p>
      </header>

      <div class="topic-list">
        <section v-for="item in group.items" :key="item.title" class="topic">
          <div class="topic-title-row">
            <h4>{{ item.title }}</h4>
            <span v-if="item.tag" class="topic-tag">{{ item.tag }}</span>
          </div>
          <p>{{ item.body }}</p>
          <ul v-if="item.points?.length">
            <li v-for="point in item.points" :key="point">{{ point }}</li>
          </ul>
        </section>
      </div>
    </article>

    <article class="panel faq-panel">
      <header class="section-head faq-head">
        <p class="eyebrow">常见问题</p>
        <h3>你可能会遇到的使用问题</h3>
        <p class="section-summary">点开下面每一项，可以看到对应说明。</p>
      </header>

      <div class="faq-list">
        <details v-for="item in faqs" :key="item.question" class="faq-item">
          <summary>{{ item.question }}</summary>
          <p>{{ item.answer }}</p>
        </details>
      </div>
    </article>

  </section>
</template>

<script setup>
const gettingStarted = [
  {
    title: '先浏览公开内容',
    body: '未登录时也可以浏览首页和帖子详情，先熟悉信息流的内容结构，再决定是否登录继续互动。'
  },
  {
    title: '登录后开始发布和互动',
    body: '登录后就能发布内容、评论、点赞、收藏、转发，也可以切换到热度和关注信息流。'
  },
  {
    title: '整理好个人主页',
    body: '个人主页支持编辑昵称、简介、头像和背景图，也能集中查看自己的帖子、喜欢和收藏。'
  }
]

const helpGroups = [
  {
    id: 'browse',
    kicker: '浏览与发现',
    title: '从首页开始，找到你真正想看的内容',
    summary: '这里整理的是浏览路径，包括首页、热度、关注、搜索、热搜和推荐关注。',
    items: [
      {
        title: '首页、热度、关注',
        tag: '信息流',
        body: '首页会综合时间和互动热度展示内容；登录后可以切换到热度和关注页。游客模式下只显示首页。'
      },
      {
        title: '搜索与帖子详情',
        tag: '查找内容',
        body: '搜索页可以查找帖子和用户；进入帖子详情后，可以继续查看评论、媒体内容和完整互动记录。'
      },
      {
        title: '今日热搜与推荐关注',
        tag: '发现新内容',
        body: '右侧栏会展示今日热搜和推荐关注。点击热搜会直接进入对应帖子，推荐关注则方便你快速找到值得继续关注的作者。'
      }
    ]
  },
  {
    id: 'publish',
    kicker: '发布与互动',
    title: '发布内容很直接，互动反馈也会马上给到',
    summary: '如果你主要想知道怎么发帖、怎么看图片视频、怎么收藏，这一组最实用。',
    items: [
      {
        title: '发布内容',
        tag: '发帖',
        body: '发布页支持文字、图片、视频和常用表情，图片与视频都会先预览，再决定是否发送。',
        points: [
          '图片、视频和文字可以组合成一条动态。',
          '表情面板使用常见的微博微信式表情，方便快速插入。',
          '发出后会立即回到首页查看最新内容。'
        ]
      },
      {
        title: '点赞、评论、转发、收藏',
        tag: '互动',
        body: '每条帖子都支持点赞、评论、转发和收藏。按钮状态会直接变化，方便你确认操作已经生效。'
      },
      {
        title: '收藏内容',
        tag: '整理喜欢的内容',
        body: '收藏按钮位于帖子操作区最右侧。收藏后的内容会统一出现在收藏页，方便稍后继续查看。'
      }
    ]
  },
  {
    id: 'profile',
    kicker: '个人主页',
    title: '把你的资料、内容和关系都放在一个地方',
    summary: '个人主页不仅是展示页，也是你管理资料和查看内容的主要入口。',
    items: [
      {
        title: '个人主页会显示什么',
        tag: '资料总览',
        body: '主页会显示昵称、用户名、注册信息、简介、帖子数、关注数和粉丝数，整体信息一眼就能读完。'
      },
      {
        title: '编辑个人资料',
        tag: '资料管理',
        body: '进入编辑状态后，可以修改昵称、简介、头像和背景图。保存后，主页内容会同步更新。'
      },
      {
        title: '查看自己的内容',
        tag: '内容整理',
        body: '在个人主页里，你可以切换查看自己的帖子、喜欢和收藏，也能打开关注和粉丝列表继续浏览。'
      }
    ]
  },
  {
    id: 'account',
    kicker: '账号与设置',
    title: '登录、找回密码和显示设置都放在固定位置',
    summary: '如果你遇到登录问题，或者只是想调整主题和密码，这一组会比较关键。',
    items: [
      {
        title: '注册与登录',
        tag: '账号入口',
        body: '游客可以先浏览公开内容；注册或登录之后，就能解锁发布、热度、关注和个人资料管理。'
      },
      {
        title: '忘记密码与修改密码',
        tag: '账号安全',
        body: '登录页提供忘记密码入口；已登录后，可在设置页修改密码，保持账号安全。'
      },
      {
        title: '主题切换',
        tag: '显示偏好',
        body: '设置页提供浅色和深色主题切换。游客默认使用浅色主题，登录后也可以按自己的使用习惯调整。'
      }
    ]
  }
]

const faqs = [
  {
    question: '为什么我看不到“热度”或“关注”？',
    answer: '这两个信息流需要登录后才会开放。游客模式下，只显示首页的公开内容。'
  },
  {
    question: '图片和视频怎么查看？',
    answer: '在帖子里直接点击图片或视频本身即可预览，不需要额外打开其他入口。'
  },
  {
    question: '我能删除自己的帖子吗？',
    answer: '可以。只要是你自己发布的帖子，就能在帖子菜单里找到删除操作。'
  },
  {
    question: '推荐关注为什么有时会变化？',
    answer: '推荐关注会根据近期内容和热度动态更新，用来帮助你更快找到值得继续看的作者。'
  }
]
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
  border-radius: 18px;
  background: var(--paper);
  padding: 16px;
  box-shadow: 0 10px 24px color-mix(in srgb, #0f172a 7%, transparent);
  scroll-margin-top: calc(var(--topbar-height) + 18px);
}

.hero {
  display: grid;
  gap: 18px;
  background: color-mix(in srgb, var(--surface) 82%, var(--paper));
}

.hero-copy {
  display: grid;
  gap: 6px;
  max-width: 56ch;
}

.eyebrow {
  margin: 0;
  font-size: 0.78rem;
  font-weight: 800;
  color: var(--muted);
  letter-spacing: 0.04em;
}

h2,
h3,
h4 {
  margin: 0;
  color: var(--ink);
}

h2 {
  font-size: 1.5rem;
  line-height: 1.24;
}

h3 {
  font-size: 1.06rem;
  line-height: 1.3;
}

h4 {
  font-size: 0.95rem;
  line-height: 1.35;
}

p {
  margin: 0;
}

.hero-text,
.section-summary,
.topic p,
.step-copy p,
.faq-item p {
  color: var(--muted);
  line-height: 1.65;
}

.jump-links {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(172px, 1fr));
  gap: 8px;
}

.jump-link {
  display: grid;
  gap: 4px;
  padding: 12px;
  border: 1px solid color-mix(in srgb, var(--line) 78%, transparent);
  border-radius: 12px;
  background: color-mix(in srgb, var(--surface) 88%, var(--paper));
  color: var(--ink);
  text-decoration: none;
  transition: border-color 0.16s ease, background-color 0.16s ease, transform 0.16s ease;
}

.jump-link:hover {
  border-color: color-mix(in srgb, var(--line) 62%, var(--muted));
  background: color-mix(in srgb, var(--surface) 96%, var(--paper));
  transform: translateY(-1px);
}

.jump-link span {
  color: var(--muted);
  font-size: 0.75rem;
  font-weight: 700;
}

.section-head {
  display: grid;
  gap: 6px;
}

.step-list,
.topic ul {
  margin: 0;
  padding: 0;
}

.step-list {
  list-style: none;
  display: grid;
}

.step-item {
  display: grid;
  grid-template-columns: 42px minmax(0, 1fr);
  gap: 12px;
  padding: 14px 0;
  border-top: 1px solid color-mix(in srgb, var(--line) 78%, transparent);
}

.step-item:first-child {
  border-top: 0;
  padding-top: 0;
}

.step-index {
  color: var(--muted);
  font-size: 0.9rem;
  font-weight: 800;
}

.step-copy {
  display: grid;
  gap: 4px;
}

.topic-list {
  display: grid;
  margin-top: 14px;
}

.topic {
  display: grid;
  gap: 8px;
  padding: 14px 0;
  border-top: 1px solid color-mix(in srgb, var(--line) 78%, transparent);
}

.topic:first-child {
  border-top: 0;
  padding-top: 0;
}

.topic-title-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.topic-tag {
  flex: 0 0 auto;
  border: 1px solid color-mix(in srgb, var(--line) 82%, transparent);
  border-radius: 999px;
  padding: 4px 8px;
  color: var(--muted);
  font-size: 0.72rem;
  font-weight: 700;
  background: color-mix(in srgb, var(--surface) 90%, var(--paper));
}

.topic ul {
  list-style: disc;
  padding-left: 18px;
  display: grid;
  gap: 6px;
  color: var(--muted);
  line-height: 1.6;
}

.faq-list {
  display: grid;
  gap: 8px;
  margin-top: 14px;
}

.faq-item {
  border: 1px solid color-mix(in srgb, var(--line) 78%, transparent);
  border-radius: 14px;
  background: color-mix(in srgb, var(--surface) 90%, var(--paper));
  padding: 0 14px;
}

.faq-item summary {
  list-style: none;
  cursor: pointer;
  font-weight: 700;
  color: var(--ink);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 0;
}

.faq-item summary::after {
  content: '+';
  flex: 0 0 auto;
  color: var(--muted);
  font-size: 1rem;
  line-height: 1;
}

.faq-item[open] summary::after {
  content: '−';
}

.faq-item summary::-webkit-details-marker {
  display: none;
}

.faq-item p {
  margin: 0;
  padding: 0 0 14px;
  border-top: 1px solid color-mix(in srgb, var(--line) 74%, transparent);
}

@media (max-width: 720px) {
  .panel {
    padding: 14px;
    border-radius: 16px;
  }

  .jump-links {
    grid-template-columns: 1fr;
  }

  .topic-title-row {
    display: grid;
  }
}
</style>
