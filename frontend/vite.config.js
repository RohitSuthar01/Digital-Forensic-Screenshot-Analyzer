import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  resolve: { preserveSymlinks: true },
  server: {
    host: '0.0.0.0',
    port: 5188,
    proxy: {
      '/api': {
        target: process.env.DFSA_API_PROXY_TARGET || 'http://localhost:8091',
        changeOrigin: true,
        secure: false,
        withCredentials: true
      }
    }
  }
})
