const THEME_KEY = 'ui-theme'

export function normalizeTheme(value) {
  return value === 'dark' ? 'dark' : 'light'
}

export function getStoredTheme() {
  return normalizeTheme(localStorage.getItem(THEME_KEY) || 'light')
}

export function saveTheme(theme) {
  const normalized = normalizeTheme(theme)
  localStorage.setItem(THEME_KEY, normalized)
  return normalized
}

export function applyTheme(theme) {
  const normalized = normalizeTheme(theme)
  document.documentElement.dataset.theme = normalized
  return normalized
}

export function setTheme(theme) {
  const normalized = saveTheme(theme)
  applyTheme(normalized)
  return normalized
}
