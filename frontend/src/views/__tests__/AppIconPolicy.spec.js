import { describe, it } from 'node:test'
import assert from 'node:assert/strict'
import fs from 'node:fs'
import { fileURLToPath } from 'node:url'

function readApp() {
  const url = new URL('../../App.vue', import.meta.url)
  return fs.readFileSync(fileURLToPath(url), 'utf8')
}

describe('app navigation icon policy', () => {
  it('should import lucide icons and avoid emoji nav', () => {
    const content = readApp()

    assert.match(content, /from 'lucide-vue-next'/)
    assert.doesNotMatch(content, /🌊|🏠|✍️|⭐|👤|🔥|👥|🧭|🔎/)
  })

  it('should use unified short sidebar labels', () => {
    const content = readApp()

    assert.match(content, /<span>首页<\/span>/)
    assert.match(content, /<span>热门<\/span>/)
    assert.match(content, /<span>关注<\/span>/)
    assert.match(content, /<span>发布<\/span>/)
    assert.match(content, /<span>收藏<\/span>/)
    assert.match(content, /<span>主页<\/span>/)
    assert.match(content, /<span>设置<\/span>/)
    assert.doesNotMatch(content, /热门动态|关注动态|发布动态|个人主页/)
  })
})
