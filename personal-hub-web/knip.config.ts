import type { KnipConfig } from 'knip'

/**
 * 死代码扫描。katex/mermaid：由 vditor CDN 渲染，源码无静态 import。
 */
const config: KnipConfig = {
  entry: ['src/router/**/*.{ts,vue}', 'index.html'],
  project: ['src/**/*.{ts,vue}'],
  ignoreDependencies: ['katex', 'mermaid'],
  ignoreExportsUsedInFile: true,
}

export default config
