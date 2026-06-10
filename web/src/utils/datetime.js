const LOCALE_ALIASES = {
  en: 'en-US',
}

function resolveLocale(locale) {
  return LOCALE_ALIASES[locale] || locale || 'zh-CN'
}

export function formatDateTime(value, locale) {
  if (!value) return '—'

  const date = value instanceof Date ? value : new Date(value)
  if (Number.isNaN(date.getTime())) return '—'

  return new Intl.DateTimeFormat(resolveLocale(locale), {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false,
  }).format(date).replace(', ', ' ')
}