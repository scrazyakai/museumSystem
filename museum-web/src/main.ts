import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import 'undraw-ui/dist/style.css'
import App from './App.vue'
import router from './router'

const app = createApp(App)

// 安装 Pinia
const pinia = createPinia()
app.use(pinia)

// 安装 Router
app.use(router)

// 安装 Element Plus（中文）
app.use(ElementPlus, {
  locale: zhCn
})

app.mount('#app')
