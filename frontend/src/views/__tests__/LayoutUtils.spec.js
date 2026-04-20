import { describe, it } from 'node:test'
import assert from 'node:assert/strict'
import { resolveLayoutFlags } from '../../utils/layout.js'

function resolveLayoutClassNames(pathname, loggedIn) {
  const flags = resolveLayoutFlags(pathname, loggedIn)
  return {
    authLayout: flags.isAuthPage,
    guestLayout: flags.isGuestPage,
    guestWideLayout: flags.expandGuestHomeLayout
  }
}

describe('layout flags', () => {
  it('should hide sidebar on login/register pages', () => {
    assert.deepEqual(resolveLayoutFlags('/login', false), {
      isAuthPage: true,
      showSidebar: false,
      isGuestPage: false,
      expandGuestHomeLayout: false,
      compactTopbar: false
    })

    assert.deepEqual(resolveLayoutFlags('/register', false), {
      isAuthPage: true,
      showSidebar: false,
      isGuestPage: false,
      expandGuestHomeLayout: false,
      compactTopbar: false
    })
  })

  it('should keep normal layout on home page', () => {
    assert.deepEqual(resolveLayoutFlags('/', true), {
      isAuthPage: false,
      showSidebar: true,
      isGuestPage: false,
      expandGuestHomeLayout: false,
      compactTopbar: false
    })
  })

  it('should hide sidebar for unauthenticated home page', () => {
    assert.deepEqual(resolveLayoutFlags('/', false), {
      isAuthPage: false,
      showSidebar: false,
      isGuestPage: true,
      expandGuestHomeLayout: true,
      compactTopbar: false
    })
  })

  it('should apply guest layout class for unauthenticated home', () => {
    assert.deepEqual(resolveLayoutClassNames('/', false), {
      authLayout: false,
      guestLayout: true,
      guestWideLayout: true
    })
  })

  it('should keep guest pages other than home in standard width', () => {
    assert.deepEqual(resolveLayoutFlags('/search', false), {
      isAuthPage: false,
      showSidebar: false,
      isGuestPage: true,
      expandGuestHomeLayout: false,
      compactTopbar: false
    })

    assert.deepEqual(resolveLayoutClassNames('/search', false), {
      authLayout: false,
      guestLayout: true,
      guestWideLayout: false
    })
  })
})
