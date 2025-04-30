export function getThemeMode() {
  const savedMode = localStorage.getItem('theme-mode')
  if (savedMode) {
    return savedMode
  }
  
  if (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) {
    return 'dark'
  }
  
  return 'light'
}

export function setThemeMode(mode) {
  localStorage.setItem('theme-mode', mode)
  const body = document.documentElement;
  if (mode === 'dark') {
    document.documentElement.setAttribute('data-theme', 'dark')
    body.classList.remove('light-theme');
  } else {
    document.documentElement.removeAttribute('data-theme')
    body.classList.add('light-theme');
  }
}

export function initTheme() {
  const mode = getThemeMode()
  setThemeMode(mode)
  return mode === 'dark'
} 