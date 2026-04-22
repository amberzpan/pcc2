import { describe, it } from 'node:test'
import assert from 'node:assert/strict'
import fs from 'node:fs'
import { fileURLToPath } from 'node:url'

function readView(relativePath) {
  const url = new URL(relativePath, import.meta.url)
  return fs.readFileSync(fileURLToPath(url), 'utf8')
}

describe('page width consistency', () => {
  it('publish and favorites should share same max width scaffold', () => {
    const publish = readView('../PublishView.vue')
    const favorites = readView('../FavoritesView.vue')

    assert.match(publish, /max-width:\s*920px/)
    assert.match(favorites, /max-width:\s*920px/)
    assert.match(publish, /margin:\s*0 auto/)
    assert.match(favorites, /margin:\s*0 auto/)
  })

  it('home and search should also follow unified center layout', () => {
    const home = readView('../HomeView.vue')
    const search = readView('../SearchView.vue')

    assert.match(home, /max-width:\s*920px/)
    assert.match(search, /max-width:\s*920px/)
    assert.match(home, /margin:\s*0 auto/)
    assert.match(search, /margin:\s*0 auto/)
  })

  it('profile and post detail should keep same content width', () => {
    const profile = readView('../ProfileView.vue')
    const detail = readView('../PostDetailView.vue')

    assert.match(profile, /max-width:\s*920px/)
    assert.match(detail, /max-width:\s*920px/)
    assert.match(profile, /margin:\s*0 auto/)
    assert.match(detail, /margin:\s*0 auto/)
  })
})
