import OpenCreateProjectView from "@/views/OpenCreateProjectView.vue";
import MainView from "@/views/MainView.vue";
import LoginView from '@/views/LoginView.vue';
import HelpView from "@/views/HelpView.vue";
import UserManagementView from '@/views/UserManagementView.vue';
import UserRoleManagementView from '@/views/UserRoleManagementView.vue';
import NewUserView from '@/views/NewUserView.vue';
import NewUserRoleView from '@/views/NewUserRoleView.vue';
import EditUserView from '@/views/EditUserView.vue';
import EditUserRoleView from '@/views/EditUserRoleView.vue';
import { createRouter, createWebHashHistory, createWebHistory } from "vue-router";
import isElectron from 'is-electron';

const routes = [
    { path: '/', name: "home", component: OpenCreateProjectView },
    { path: '/login', name: "login", component: LoginView },
    { path: '/user', name: "userManagement", component: UserManagementView },
    { path: '/newUser', name: "newUser", component: NewUserView },
    {
        path: '/editUser/:id',
        name: "editUser",
        component: EditUserView,
        props: (route:any) => ({
            id: route.params.id
        }),
    },
    { path: '/role', name: "userRoleManagement", component: UserRoleManagementView },
    { path: '/newRole', name: "newRole", component: NewUserRoleView },

    {
        path: '/editRole/:id',
        name: "editRole",
        component: EditUserRoleView,
        props: (route:any) => ({
            id: route.params.id
        }),
    },
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

function getCookie(name: string): string | null {
  const cookies = document.cookie.split('; ')
  for (const cookie of cookies) {
    const [key, value] = cookie.split('=')
    if (key === name) {
      return decodeURIComponent(value)
    }
  }
  return null
}

export const router = createRouter({
    // history only works in hash mode in electron
    // cf {https://nklayman.github.io/vue-cli-plugin-electron-builder/guide/commonIssues.html#blank-screen-on-builds-but-works-fine-on-serve}
    history: isElectron() ? createWebHashHistory() : createWebHistory(),
    routes,
})

router.beforeEach((to, from, next) => {
  const publicPages = ['/login']
  const authRequired = !publicPages.includes(to.path)
  const isLoggedIn = getCookie('authHeader');

  if (authRequired && !isLoggedIn) {
    return next('/login')
  }

  next()
})
