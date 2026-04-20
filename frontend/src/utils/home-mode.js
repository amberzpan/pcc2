const MODE_META = {
  all: {
    label: '全部动态',
    memberDescription: '按时间线查看社区动态，快速跟进今天的新内容。',
    guestDescription: '游客可浏览公开动态，登录后可点赞、评论与收藏。'
  },
  hot: {
    label: '热门帖',
    memberDescription: '系统按互动热度推荐正在讨论中的高热内容。',
    guestDescription: '游客可查看当前最受欢迎的公开帖子。'
  },
  following: {
    label: '关注动态',
    memberDescription: '只看你关注的人，保持个人信息流的专注与连续。',
    guestDescription: '请先登录后查看关注动态。'
  },
  discover: {
    label: '发现动态',
    memberDescription: '探索正在讨论的新鲜话题和你可能感兴趣的新创作者。',
    guestDescription: '游客可探索公开内容，发现社区里的新鲜话题。'
  }
}

export function normalizeFeedMode(mode) {
  return Object.prototype.hasOwnProperty.call(MODE_META, mode) ? mode : 'all'
}

export function resolveAccessibleFeedMode(mode, loggedIn) {
  const normalized = normalizeFeedMode(mode)
  if (!loggedIn && normalized === 'following') {
    return 'all'
  }
  return normalized
}

export function getFeedModeMeta(mode, loggedIn) {
  const normalized = normalizeFeedMode(mode)
  const entry = MODE_META[normalized]
  return {
    mode: normalized,
    label: entry.label,
    description: loggedIn ? entry.memberDescription : entry.guestDescription
  }
}
