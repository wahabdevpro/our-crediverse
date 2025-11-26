import Vue from "vue";
import Router from "vue-router";
import {logout, isTokenValid} from "../services/Authentication";

Vue.use(Router);

const router = new Router({
  scrollBehavior() {
    return window.scrollTo({ top: 0, behavior: "smooth" });
  },
  routes: [
    // Dashboard
    {
      path: "/",
      component: () => import("../Pages/Dashboards/Home.vue"),
      meta: { layout: "default", authRequired: true },
    },
    // About
    {
      path: "/about",
      component: () => import("../Pages/About/About.vue"),
      meta: { layout: "default", authRequired: false },
    },
    // Campaigns
    {
      path: "/campaigns",
      component: () => import("../Pages/SRCM/Campaigns/Campaigns.vue"),
      meta: { layout: "default", authRequired: true },
    },
    {
      path: '/campaign',
      name: 'campaign',
      component: () => import('../Pages/SRCM/Campaign/Campaign.vue'),
      props: true,
    },
    // Login
    {
      path: "/login",
      component: () => import("../Pages/Login/Login.vue"),
      meta: { layout: "userpages", authRequired: false },
    },
  ],
});

router.beforeEach(async (to, from, next) => {
  const publicPages = ["/login", "/about"];
  const authRequired = !publicPages.includes(to.path);

  if (authRequired && !isTokenValid()) {
    logout();
    next("/login");
  } else {
    next();
  }
});

export default router;
