import { createRouter, createWebHashHistory } from 'vue-router'
import Login from "../views/login/LoginUtenteGiaRegistrato";

const routes = [
  {
    path: '/',
    alias: '/login',
    component: Login
  },
  {
    path: '/registrazione',
    // route level code-splitting (lazy-loaded when the route is visited)
    component: () => import('../views/login/RegistrazioneNuovoConsumer.vue')
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes: routes
});

export default router;