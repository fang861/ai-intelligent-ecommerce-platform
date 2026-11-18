export function formatTime(value) {
  if (!value) return '—'
  return String(value).replace('T', ' ').slice(0, 16)
}

/** Resolve media URL (cover / video / avatar) */
export function mediaUrl(url) {
  if (!url) return ''
  const raw = String(url).trim()
  if (!raw) return ''
  if (/^https?:\/\//i.test(raw) || raw.startsWith('data:')) return raw

  let path = raw.replace(/\\/g, '/')
  if (!path.startsWith('/')) path = `/${path}`

  // 已带 /api 前缀
  if (path.startsWith('/api/uploads/')) return path
  if (path.startsWith('/api/')) return path

  // 统一走后端 context-path /api
  if (path.startsWith('/uploads/')) return `/api${path}`

  return `/api/uploads${path}`
}

export function resolveImageUrl(url) {
  return mediaUrl(url)
}

export function formatDuration(seconds) {
  const s = Number(seconds) || 0
  const m = Math.floor(s / 60)
  const r = s % 60
  return `${m}:${String(r).padStart(2, '0')}`
}

export function formatPrice(price) {
  const n = Number(price)
  if (Number.isNaN(n)) return '¥0.00'
  return `¥${n.toFixed(2)}`
}

/** 订单状态：后端存 0-4，前端兼容字符串枚举 */
export function normalizeOrderStatus(status) {
  if (status == null || status === '') return null
  const raw = String(status).trim()
  const alias = {
    '0': 0,
    PENDING_PAY: 0,
    UNPAID: 0,
    '1': 1,
    PAID: 1,
    PENDING_SHIP: 1,
    '2': 2,
    SHIPPED: 2,
    PENDING_RECEIVE: 2,
    '3': 3,
    COMPLETED: 3,
    FINISHED: 3,
    REVIEWED: 3,
    '4': 4,
    CANCELLED: 4,
    CANCELED: 4
  }
  if (Object.prototype.hasOwnProperty.call(alias, raw)) return alias[raw]
  const n = Number(raw)
  return Number.isInteger(n) && n >= 0 && n <= 4 ? n : null
}

export function orderStatusLabel(status) {
  const code = normalizeOrderStatus(status)
  const map = {
    0: '待付款',
    1: '待发货',
    2: '待收货',
    3: '已完成',
    4: '已取消'
  }
  if (code != null) return map[code]
  return status == null || status === '' ? '未知' : String(status)
}

export function orderStatusType(status) {
  const code = normalizeOrderStatus(status)
  const map = {
    0: 'warning',
    1: '',
    2: 'primary',
    3: 'success',
    4: 'info'
  }
  return code != null ? map[code] : 'info'
}

export function isPendingPayStatus(status) {
  return normalizeOrderStatus(status) === 0
}

export function isShippedStatus(status) {
  return normalizeOrderStatus(status) === 2
}

export function isCompletedStatus(status) {
  return normalizeOrderStatus(status) === 3
}

export function productCover(item) {
  if (!item) return ''
  if (item.cover) return mediaUrl(item.cover)
  if (item.productCover) return mediaUrl(item.productCover)
  if (item.image) return mediaUrl(item.image)
  if (item.thumb) return mediaUrl(item.thumb)
  if (item.thumbnail) return mediaUrl(item.thumbnail)
  if (Array.isArray(item.images) && item.images.length) return mediaUrl(item.images[0])
  if (typeof item.images === 'string' && item.images) {
    const first = item.images.split(',')[0]?.trim()
    return mediaUrl(first)
  }
  return ''
}

export function levelLabel(level) {
  const map = {
    BEGINNER: '入门',
    INTERMEDIATE: '进阶',
    ADVANCED: '高级',
    入门: '入门',
    进阶: '进阶',
    高级: '高级',
    实战: '实战'
  }
  return map[level] || level || '通用'
}

export function parseOptions(options) {
  if (!options) return []
  if (Array.isArray(options)) return options
  try {
    const parsed = JSON.parse(options)
    return Array.isArray(parsed) ? parsed : []
  } catch {
    return String(options)
      .split(/[,|]/)
      .map((s) => s.trim())
      .filter(Boolean)
  }
}

export function questionTypeLabel(type) {
  const map = {
    SINGLE: '单选题',
    MULTIPLE: '多选题',
    BLANK: '填空题',
    ESSAY: '简答题',
    CODE: '编程题'
  }
  return map[type] || type || '题目'
}

/** Strip HTML tags for list excerpts / validation */
export function plainTextOf(html) {
  return String(html || '')
    .replace(/<[^>]+>/g, ' ')
    .replace(/&nbsp;/gi, ' ')
    .replace(/&lt;/gi, '<')
    .replace(/&gt;/gi, '>')
    .replace(/&amp;/gi, '&')
    .replace(/\s+/g, ' ')
    .trim()
}

/** Render post/article body: keep HTML, wrap plain text as paragraphs */
export function toContentHtml(raw, emptyText = '暂无内容') {
  const source = String(raw || '').trim()
  const plain = plainTextOf(source)
  if (!plain) return `<p>${emptyText}</p>`
  if (/<[a-z][\s\S]*>/i.test(source)) return source
  return source
    .split(/\n+/)
    .map((line) => line.trim())
    .filter(Boolean)
    .map((p) => `<p>${p}</p>`)
    .join('')
}
