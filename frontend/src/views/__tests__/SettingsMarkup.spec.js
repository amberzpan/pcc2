import { describe, it } from 'node:test'
import assert from 'node:assert/strict'
import fs from 'node:fs'
import { fileURLToPath } from 'node:url'

function readSettings() {
  const url = new URL('../SettingsView.vue', import.meta.url)
  return fs.readFileSync(fileURLToPath(url), 'utf8')
}

function readRouter() {
  const url = new URL('../../router/index.js', import.meta.url)
  return fs.readFileSync(fileURLToPath(url), 'utf8')
}

describe('settings view markup', () => {
  it('should include theme controls and change password form', () => {
    const content = readSettings()

    assert.match(content, /主题模式/)
    assert.match(content, /@submit\.prevent="changePassword"/)
    assert.match(content, /to="\/help"/)
  })

  it('router should register settings and help routes', () => {
    const router = readRouter()
    assert.match(router, /path:\s*'\/settings'/)
    assert.match(router, /path:\s*'\/help'/)
  })
})
