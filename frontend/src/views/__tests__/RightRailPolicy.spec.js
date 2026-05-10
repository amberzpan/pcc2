import { describe, it } from 'node:test'
import assert from 'node:assert/strict'
import fs from 'node:fs'
import { fileURLToPath } from 'node:url'

function readApp() {
  const url = new URL('../../App.vue', import.meta.url)
  return fs.readFileSync(fileURLToPath(url), 'utf8')
}

describe('right rail policy', () => {
  it('keeps fixed right rail item counts and fetch windows', () => {
    const content = readApp()

    assert.match(content, /const RIGHT_RAIL_TREND_LIMIT = 12/)
    assert.match(content, /const SUGGESTION_LIMIT = 5/)
    assert.match(content, /getTodayHotPosts\(1,\s*40\)/)
    assert.match(content, /getPostList\(1,\s*24\)/)
  })
})
