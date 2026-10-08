import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/devices': 'http://localhost:8085',
      '/topology': 'http://localhost:8085',
    },
  },
})