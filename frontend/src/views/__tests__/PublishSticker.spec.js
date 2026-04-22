import { describe, it } from 'node:test'
import assert from 'node:assert/strict'
import fs from 'node:fs'
import { fileURLToPath } from 'node:url'

function readPublish() {
  const url = new URL('../PublishView.vue', import.meta.url)
  return fs.readFileSync(fileURLToPath(url), 'utf8')
}

describe('publish sticker support', () => {
  it('should include sticker panel and insertion handlers', () => {
    const content = readPublish()

    assert.match(content, /微博微信常用表情/)
    assert.match(content, /insertSticker/)
    assert.match(content, /sticker-grid/)
    assert.match(content, /smile|laugh|wink|cry|angry|ok/)
    assert.match(content, /toolbar-btn/)
    assert.match(content, /ImagePlus/)
  })
})
