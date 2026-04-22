import { beforeEach, describe, it } from 'node:test'
import assert from 'node:assert/strict'
import { applyTheme, getStoredTheme, saveTheme, setTheme } from '../../utils/theme.js'

const storage = new Map()

global.localStorage = {
  getItem(key) {
    return storage.has(key) ? storage.get(key) : null
  },
  setItem(key, value) {
    storage.set(key, String(value))
  },
  removeItem(key) {
    storage.delete(key)
  },
  clear() {
    storage.clear()
  }
}

global.document = {
  documentElement: {
    dataset: {}
  }
}

describe('theme persistence', () => {
  beforeEach(() => {
    storage.clear()
    document.documentElement.dataset = {}
  })

  it('should default to light theme when storage is empty', () => {
    assert.equal(getStoredTheme(), 'light')
  })

  it('should normalize invalid stored value to light', () => {
    localStorage.setItem('ui-theme', 'unknown')

    assert.equal(getStoredTheme(), 'light')
  })

  it('should persist and apply dark theme', () => {
    assert.equal(saveTheme('dark'), 'dark')
    assert.equal(localStorage.getItem('ui-theme'), 'dark')

    assert.equal(applyTheme('dark'), 'dark')
    assert.equal(document.documentElement.dataset.theme, 'dark')
  })

  it('setTheme should persist and apply in one call', () => {
    assert.equal(setTheme('dark'), 'dark')
    assert.equal(localStorage.getItem('ui-theme'), 'dark')
    assert.equal(document.documentElement.dataset.theme, 'dark')
  })
})
