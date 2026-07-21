import { transformWikiLinks } from './wikiLinkUtils'
import { getNoteTitleIdMap } from './noteTitleIndex'

/** 预览前转换 wiki 链；索引失败时仍转换（未命中态），避免残留 [[ ]] */
export async function preparePreviewMarkdown(
  markdown: string,
  enabled: boolean,
): Promise<string> {
  if (!enabled || !markdown) return markdown
  let map = new Map<string, number>()
  try {
    map = await getNoteTitleIdMap()
  } catch {
    /* 仍继续 transform，全部按未命中处理 */
  }
  return transformWikiLinks(markdown, (t) => map.get(t) ?? null)
}

export function rewriteNoteAssetSrc(img: HTMLImageElement, noteId: number, token: string) {
  const src = img.getAttribute('src')
  if (!src) return
  if (src.startsWith('images/') || src.startsWith('attachments/')) {
    img.setAttribute('src', `/api/notes/${noteId}/${src}?token=${encodeURIComponent(token)}`)
  }
}

function waitForImage(img: HTMLImageElement): Promise<boolean> {
  if (img.complete && img.naturalWidth > 0) return Promise.resolve(true)
  return new Promise((resolve) => {
    const done = (ok: boolean) => {
      img.removeEventListener('load', onLoad)
      img.removeEventListener('error', onError)
      resolve(ok)
    }
    const onLoad = () => done(img.naturalWidth > 0)
    const onError = () => done(false)
    img.addEventListener('load', onLoad)
    img.addEventListener('error', onError)
  })
}

const LIGHTBOX_ID = 'ph-note-image-lightbox'

function closeLightbox() {
  document.getElementById(LIGHTBOX_ID)?.remove()
}

/**
 * 自定义图片灯箱（替代 medium-zoom）。
 * medium-zoom 在 transitionend 未触发时 isAnimating 会卡死，导致遮罩/放大图无法关闭。
 */
export async function setupPreviewImageZoom(
  preview: HTMLElement,
  noteId?: number | null,
): Promise<() => void> {
  const token = localStorage.getItem('token')
  const imgs = Array.from(preview.querySelectorAll('img'))
  if (noteId && token) {
    imgs.forEach((img) => rewriteNoteAssetSrc(img, noteId, token))
  }

  const loaded = (await Promise.all(
    imgs.map(async (img) => ((await waitForImage(img)) ? img : null)),
  )).filter((img): img is HTMLImageElement => !!img)

  const onImgClick = (e: Event) => {
    const img = e.currentTarget as HTMLImageElement
    if (!img.currentSrc && !img.src) return
    e.preventDefault()
    e.stopPropagation()
    closeLightbox()

    const root = document.createElement('div')
    root.id = LIGHTBOX_ID
    root.className = 'ph-img-lightbox'
    root.setAttribute('role', 'dialog')
    root.setAttribute('aria-modal', 'true')
    root.innerHTML = `
      <div class="ph-img-lightbox__backdrop" data-close="1"></div>
      <img class="ph-img-lightbox__img" alt="" />
      <button type="button" class="ph-img-lightbox__close" data-close="1" aria-label="关闭">×</button>
    `
    const full = root.querySelector('img') as HTMLImageElement
    full.src = img.currentSrc || img.src
    full.alt = img.alt || ''

    const onCloseClick = (ev: Event) => {
      const t = ev.target as HTMLElement
      if (t.dataset.close === '1' || t === root) closeLightbox()
    }
    root.addEventListener('click', onCloseClick)
    document.body.appendChild(root)
  }

  for (const img of loaded) {
    img.style.cursor = 'zoom-in'
    img.addEventListener('click', onImgClick)
  }

  const onKeyDown = (e: KeyboardEvent) => {
    if (e.key === 'Escape' || e.key === 'Esc') closeLightbox()
  }
  document.addEventListener('keydown', onKeyDown, true)

  return () => {
    document.removeEventListener('keydown', onKeyDown, true)
    for (const img of loaded) {
      img.removeEventListener('click', onImgClick)
      img.style.cursor = ''
    }
    closeLightbox()
  }
}
