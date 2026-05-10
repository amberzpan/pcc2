import { describe, it } from 'node:test'
import assert from 'node:assert/strict'
import fs from 'node:fs'
import { fileURLToPath } from 'node:url'

function readHelp() {
  const url = new URL('../HelpView.vue', import.meta.url)
  return fs.readFileSync(fileURLToPath(url), 'utf8')
}

describe('help content policy', () => {
  it('should be user-task oriented instead of deployment oriented', () => {
    const content = readHelp()

    assert.match(content, /帮助中心/)
    assert.match(content, /发布内容/)
    assert.match(content, /推荐关注/)
    assert.match(content, /收藏内容/)
    assert.match(content, /修改密码/)
    assert.match(content, /个人主页/)
    assert.doesNotMatch(content, /9090|5173|social_platform|MySQL/)
    assert.doesNotMatch(content, /Spring Boot|Vue 3|JWT/)
  })
})
