import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import en from 'element-plus/es/locale/lang/en'
import 'undraw-ui/dist/style.css'
import App from './App.vue'
import router from './router'
import i18n from './i18n'

const app = createApp(App)

// 安装 Pinia
const pinia = createPinia()
app.use(pinia)

// 安装 Router
app.use(router)

// 安装 i18n
app.use(i18n)

// 安装 Element Plus（根据当前语言动态设置）
const locale = i18n.global.locale.value === 'en' ? en : zhCn
app.use(ElementPlus, {
  locale
})

app.mount('#app')
