import Vditor from 'vditor'

export const VDITOR_CDN = '/vditor'
export const EDITOR_ID_PREFIX = 'ph-note-editor'

export function buildEditorId(noteId?: number | null) {
  return noteId ? `${EDITOR_ID_PREFIX}-${noteId}` : `${EDITOR_ID_PREFIX}-new`
}

export function mapAppThemeToVditor(theme: 'light' | 'dark'): 'classic' | 'dark' {
  return theme === 'dark' ? 'dark' : 'classic'
}

export function buildVditorBaseOptions(partial?: Partial<IOptions>): Partial<IOptions> {
  return {
    mode: 'ir',
    toolbar: [],
    toolbarConfig: { hide: true },
    cache: { enable: false },
    counter: { enable: false },
    outline: { enable: false, position: 'left' },
    cdn: VDITOR_CDN,
    placeholder: '开始写作...',
    preview: {
      markdown: {
        footnotes: true,
        mathBlockPreview: true,
        codeBlockPreview: true,
        gfmAutoLink: true,
      },
      theme: {
        current: 'light',
      },
    },
    ...partial,
  }
}

export function buildPreviewOptions(theme: 'light' | 'dark' = 'light') {
  return {
    mode: theme === 'dark' ? 'dark' : 'light',
    cdn: VDITOR_CDN,
    theme: {
      current: theme === 'dark' ? 'dark' : 'light',
    },
    markdown: {
      footnotes: true,
      mathBlockPreview: true,
      codeBlockPreview: true,
      gfmAutoLink: true,
    },
  } satisfies Parameters<typeof Vditor.preview>[2]
}
