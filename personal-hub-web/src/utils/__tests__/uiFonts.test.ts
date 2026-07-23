import { describe, expect, it, vi, beforeEach, afterEach } from 'vitest'
import {
  normalizeUiFont,
  applyUiFontToDOM,
  DEFAULT_UI_FONT,
  getUiFontOption,
  resetUiFontStylesheetCache,
} from '../uiFonts'

function installDomMock() {
  const attrs = new Map<string, string>()
  const props = new Map<string, string>()
  const nodes = new Map<string, { id: string; href: string; rel: string }>()
  const headChildren: { id: string; href: string; rel: string }[] = []

  const documentElement = {
    style: {
      setProperty(name: string, value: string) {
        props.set(name, value)
      },
      getPropertyValue(name: string) {
        return props.get(name) ?? ''
      },
      removeProperty(name: string) {
        props.delete(name)
      },
    },
    setAttribute(name: string, value: string) {
      attrs.set(name, value)
    },
    getAttribute(name: string) {
      return attrs.get(name) ?? null
    },
    removeAttribute(name: string) {
      attrs.delete(name)
    },
  }

  const head = {
    appendChild(el: { id: string; href: string; rel: string }) {
      headChildren.push(el)
      nodes.set(el.id, el)
      return el
    },
    querySelectorAll(selector: string) {
      const hrefMatch = /link\[href="([^"]+)"\]/.exec(selector)
      if (hrefMatch) {
        return headChildren.filter((c) => c.href === hrefMatch[1])
      }
      if (selector.startsWith('link[id^="ph-ui-font-"]')) {
        return [...headChildren]
      }
      return []
    },
  }

  const documentMock = {
    documentElement,
    head,
    getElementById(id: string) {
      return nodes.get(id) ?? null
    },
    createElement(tag: string) {
      if (tag !== 'link') throw new Error(`unexpected tag ${tag}`)
      return { id: '', href: '', rel: '' }
    },
  }

  vi.stubGlobal('document', documentMock)
  return { attrs, props, headChildren, nodes }
}

describe('uiFonts', () => {
  beforeEach(() => {
    resetUiFontStylesheetCache()
    installDomMock()
  })

  afterEach(() => {
    vi.unstubAllGlobals()
    vi.restoreAllMocks()
  })

  it('normalizes unknown to source-sans', () => {
    expect(normalizeUiFont(undefined)).toBe(DEFAULT_UI_FONT)
    expect(normalizeUiFont('nope')).toBe('source-sans')
    expect(normalizeUiFont('inter')).toBe('inter')
  })

  it('applies stack and data attribute', () => {
    applyUiFontToDOM('system')
    expect(document.documentElement.getAttribute('data-ui-font')).toBe('system')
    expect(document.documentElement.style.getPropertyValue('--font-sans')).toContain('system-ui')
  })

  it('injects stylesheet for web fonts once', () => {
    applyUiFontToDOM('source-sans')
    const href = getUiFontOption('source-sans').stylesheetHref!
    expect(document.head.querySelectorAll(`link[href="${href}"]`).length).toBe(1)
    applyUiFontToDOM('source-sans')
    expect(document.head.querySelectorAll(`link[href="${href}"]`).length).toBe(1)
  })
})
