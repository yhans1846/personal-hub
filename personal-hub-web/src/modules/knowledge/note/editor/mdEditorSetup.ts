import { config } from 'md-editor-v3'
import mermaid from 'mermaid'
import katex from 'katex'
import 'katex/dist/katex.min.css'

let initialized = false

/** 一次性初始化 md-editor 扩展（Mermaid / KaTeX 本地包，避免 CDN 依赖） */
export function setupMdEditor() {
  if (initialized) return
  initialized = true

  config({
    editorExtensions: {
      mermaid: { instance: mermaid },
      katex: { instance: katex },
    },
  })
}
