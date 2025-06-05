import OpenCreateProjectView from "@/views/OpenCreateProjectView.vue";
import MainView from "@/views/MainView.vue";
import LoginView from '@/views/LoginView.vue';
import HelpView from "@/views/HelpView.vue";
import { createRouter, createWebHashHistory, createWebHistory } from "vue-router";
import isElectron from 'is-electron';

const routes = [
    { path: '/', name: "home", component: OpenCreateProjectView },
    { path: '/login', name: "login", component: LoginView },
    {
        path: '/configuration/:id',
        name: "mainview",
        component: MainView,
        props: (route:any) => ({
            id: route.params.id
        }),
    },
    { path: '/help', name: "help", component: HelpView },
    { path: '/:pathMatch(.*)*', redirect: { name: "home" } },
]

export const router = createRouter({
    // history only works in hash mode in electron
    // cf {https://nklayman.github.io/vue-cli-plugin-electron-builder/guide/commonIssues.html#blank-screen-on-builds-but-works-fine-on-serve}
    history: isElectron() ? createWebHashHistory() : createWebHistory(),
    routes,
})

router.beforeEach((to, from, next) => {
  const publicPages = ['/login']
  const authRequired = !publicPages.includes(to.path)
  const isLoggedIn = localStorage.getItem('token')

  if (authRequired && !isLoggedIn) {
    return next('/login')
  }

  next()
})
