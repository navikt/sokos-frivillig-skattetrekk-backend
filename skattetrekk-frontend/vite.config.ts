import react from '@vitejs/plugin-react'
import eslint from 'vite-plugin-eslint2'
import stylelint from 'vite-plugin-stylelint'
import {fileURLToPath} from "url";
import {UserConfig, defineConfig } from "vite";

// https://vitejs.dev/config/
const buildConfig: UserConfig = {
  base: '/utbetaling/skattetrekk',
  build: {
    outDir: './dist',
    target: 'esnext',
    rollupOptions: {
      input: {
        appBorger: "./index.html"
      }
    },
  },
  plugins: [
    react(),
    eslint(),
    stylelint({fix: true}),
  ],
  resolve: {
    alias: {
      "@": fileURLToPath(new URL("./src", import.meta.url))
    }
  },
}

const devConfig: UserConfig = {
  base: '/utbetaling/skattetrekk',
  build: {
    manifest: true,
    rollupOptions: {
      input: {
        appBorger: './index.html',
      }
    },
    target: 'esnext'
  },
  server: {
    open: '/index.html',
  },
  plugins: [
    react(),
  ],
  resolve: {
    alias: {
      "@": fileURLToPath(new URL("./src", import.meta.url))
    }
  },
  define: {
    'process.env.isMock': true,
    'process.env.MOCK_PORT': 3000,
  },
}

// https://vitejs.dev/config/
export default ({ command }) => {
  if (command == 'serve') {
    return defineConfig(devConfig)
  } else {
    return defineConfig(buildConfig)
  }
}
