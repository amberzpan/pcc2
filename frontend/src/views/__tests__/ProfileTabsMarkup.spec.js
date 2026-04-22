import { describe, it } from 'node:test'
import assert from 'node:assert/strict'
import fs from 'node:fs'
import { fileURLToPath } from 'node:url'

function readProfile() {
  const url = new URL('../ProfileView.vue', import.meta.url)
  return fs.readFileSync(fileURLToPath(url), 'utf8')
}

describe('profile tabs markup', () => {
  it('should include liked and favorites tabs only', () => {
    const content = readProfile()

    assert.match(content, /data-tab="liked"/)
    assert.match(content, /data-tab="favorites"/)
    assert.doesNotMatch(content, /data-tab="history"/)
    assert.match(content, /注册于/)
    assert.doesNotMatch(content, /性别/) 
    assert.doesNotMatch(content, /头像 URL|背景图 URL/)
    assert.match(content, /camera-btn/)
    assert.match(content, /openImagePreview/)
  })
})
