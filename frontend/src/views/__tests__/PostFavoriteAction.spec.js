import { describe, it } from 'node:test'
import assert from 'node:assert/strict'
import fs from 'node:fs'
import { fileURLToPath } from 'node:url'

function readPostCard() {
  const url = new URL('../../components/PostCard.vue', import.meta.url)
  return fs.readFileSync(fileURLToPath(url), 'utf8')
}

describe('post favorite action', () => {
  it('favorite button should be icon-only', () => {
    const content = readPostCard()

    assert.match(content, /class="action favorite-action"/)
    assert.doesNotMatch(content, /已收藏|收藏<\/span>/)
  })
})
