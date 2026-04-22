import { describe, it } from 'node:test'
import assert from 'node:assert/strict'
import fs from 'node:fs'
import { fileURLToPath } from 'node:url'

function readApp() {
  const url = new URL('../../App.vue', import.meta.url)
  return fs.readFileSync(fileURLToPath(url), 'utf8')
}

describe('selection policy', () => {
  it('should disable selection globally and allow editing zones', () => {
    const content = readApp()

    assert.match(content, /body:not\(\.allow-selection\)\s*\{[\s\S]*user-select:\s*none/)
    assert.match(content, /input,[\s\S]*textarea,[\s\S]*select,[\s\S]*\[contenteditable='true'\]/)
    assert.match(content, /\.allow-selection,\s*[\s\S]*\.allow-selection \*/)
  })

  it('should enforce non-selectable sidebar region', () => {
    const content = readApp()

    assert.match(content, /\.sidebar,\s*[\s\S]*\.sidebar \*/)
    assert.match(content, /-webkit-user-drag:\s*none/)
  })
})
