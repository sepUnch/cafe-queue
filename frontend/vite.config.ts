import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

// https://vitejs.dev/config/
export default defineConfig(({ mode }) => {
  const backendUrl = process.env.VITE_BACKEND_URL || 'http://localhost:8080'

  return {
    plugins: [vue()],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url))
      }
    },
    // Proxy hanya aktif saat dev server (npm run dev)
    // Saat production build, frontend menggunakan VITE_API_BASE_URL langsung
    server: {
      proxy: {
        '/orders': { target: backendUrl, changeOrigin: true },
        '/preorders': { target: backendUrl, changeOrigin: true },
        '/menu': { target: backendUrl, changeOrigin: true },
        '/reconciliation': { target: backendUrl, changeOrigin: true },
        '/queue-board/': { target: backendUrl, changeOrigin: true },
        '/payment': { target: backendUrl, changeOrigin: true }
      }
    }
  }
})

