import { cpSync, existsSync, mkdirSync } from 'node:fs'
import { dirname, join } from 'node:path'
import { fileURLToPath } from 'node:url'

const root = join(dirname(fileURLToPath(import.meta.url)), '..')
const src = join(root, 'node_modules/vditor/dist')
const dest = join(root, 'public/vditor/dist')

if (!existsSync(src)) {
  console.warn('[copy-vditor-assets] vditor dist not found, skip')
  process.exit(0)
}

mkdirSync(dest, { recursive: true })
cpSync(src, dest, { recursive: true })
console.log('[copy-vditor-assets] copied to public/vditor')
