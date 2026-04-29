import { createApp } from 'vue'
import App from './App.vue'

// 全局样式
import './styles/global.css'

// Vant 全量引入
import Vant from 'vant'
import 'vant/lib/index.css'

// Pinia 状态管理
import pinia from './store'

// 国际化
import i18n from './locales'

// 路由
import router from './router'
import { setupRouterGuard } from './router/guard'
import { useAppStore } from './store/modules/app'

const app = createApp(App)

// 注册插件
app.use(pinia)
app.use(Vant)
app.use(i18n)
app.use(router)

const appStore = useAppStore()
appStore.initTheme()

// 注册路由守卫
setupRouterGuard(router)

app.mount('#app')
