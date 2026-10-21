import {defineConfig} from 'vite';
import react from '@vitejs/plugin-react';
import {fileURLToPath} from 'node:url';
const root=fileURLToPath(new URL('../frontend',import.meta.url));
const runtime=fileURLToPath(new URL('./runtime.ts',import.meta.url));
export default defineConfig({
 root,plugins:[react(),{name:'android-runtime',transformIndexHtml:{order:'pre',handler:()=>[{tag:'script',attrs:{type:'module',src:'/android-runtime.js'},injectTo:'head'}]},resolveId(id){if(id==='/android-runtime.js')return runtime}}],
 define:{'import.meta.env.VITE_API_BASE_URL':JSON.stringify(process.env.VITE_API_BASE_URL||'https://purohith-api.onrender.com/api')},
 build:{outDir:fileURLToPath(new URL('./www',import.meta.url)),emptyOutDir:true}
});
