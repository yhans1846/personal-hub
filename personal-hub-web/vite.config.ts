import { defineConfig, type Plugin } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve, join } from 'path'
import { viteStaticCopy } from 'vite-plugin-static-copy'
import { existsSync, readFileSync } from 'fs'

/** 开发模式下将 /vditor 路径映射到 node_modules/vditor/dist */
function vditorDevServer(): Plugin {
  const VDITOR_DIST = resolve(__dirname, 'node_modules/vditor/dist')
  return {
    name: 'vditor-dev-server',
    configureServer(server) {
      server.middlewares.use('/vditor', (req, res, next) => {
        const url = new URL(req.url!, 'http://localhost')
        const filePath = join(VDITOR_DIST, url.pathname.replace(/^\/vditor\/?/, ''))
        if (existsSync(filePath)) {
          const content = readFileSync(filePath)
          const ext = filePath.split('.').pop() || ''
          const mime: Record<string, string> = {
            js: 'application/javascript',
            css: 'text/css',
            wasm: 'application/wasm',
            json: 'application/json',
            png: 'image/png',
            svg: 'image/svg+xml',
            woff: 'font/woff',
            woff2: 'font/woff2',
            ttf: 'font/ttf',
          }
          res.setHeader('Content-Type', mime[ext] || 'application/octet-stream')
          res.setHeader('Access-Control-Allow-Origin', '*')
          res.end(content)
          return
        }
        next()
      })
    },
  }
}

export default defineConfig({
  plugins: [
    vue(),
    vditorDevServer(),
    viteStaticCopy({
      targets: [
        {
          src: 'node_modules/vditor/dist/*',
          dest: 'vditor/dist',
        },
      ],
    }),
  ],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
    },
  },
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
  build: {
    rollupOptions: {
      output: {
        manualChunks(id) {
          if (!id.includes('node_modules')) return
          if (id.includes('element-plus')) return 'vendor-element'
          if (id.includes('vditor')) return 'vendor-vditor'
          if (id.includes('echarts')) return 'vendor-echarts'
          if (
            id.includes('/vue/') ||
            id.includes('vue-router') ||
            id.includes('pinia') ||
            id.includes('\\vue\\')
          ) {
            return 'vendor-vue'
          }
        },
      },
    },
  },
})
