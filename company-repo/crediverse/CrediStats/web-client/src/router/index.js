// Composables
import { createRouter, createWebHistory } from "vue-router";
import { defineAsyncComponent } from "vue";

import { useAuthenticationStore } from "../stores/AuthenticationStore";

const Default = ()=>import("@/layouts/default/Default.vue");
const Home = ()=>import(/* webpackChunkName: "home" */ "@/components/Home.vue");
const Campaigns = ()=>import("@/components/Campaigns.vue");
const Campaign = () => import("@/components/Campaign.vue");
const LocationLists = () => import("@/components/LocationLists.vue");
const Segments = () => import("@/components/Segments.vue");
const Login = () => import("@/components/Login.vue");

const routes = [
  {
    path: "/",
    component: Default ,
    children: [
      {
        path: "",
        name: "Home",
        displayName: "Home",
        component: Home,
      },
      //{
      //  path: "about",
      //  name: "About",
      //  displayName: "About",
      //  // route level code-splitting
      //  // this generates a separate chunk (about.[hash].js) for this route
      //  // which is lazy-loaded when the route is visited.
      //  component: () => import(/* webpackChunkName: "home" */ "@/components/About.vue"),
      //},
      //{
      //  path: "dashboard",
      //  name: "Dashboard",
      //  displayName: "Dashboard",
      //  // route level code-splitting
      //  // this generates a separate chunk (about.[hash].js) for this route
      //  // which is lazy-loaded when the route is visited.
      //  component: () => import(/* webpackChunkName: "home" */ "@/components/Dashboard.vue"),
      //},
      {
        path: "/campaigns",
        name: "Campaigns",
        displayName: "Campaigns",
        component: Campaigns,
      },
      {
        path: "/campaign/:id/:rev",
        name: "Campaign",
        displayName: "Campaign",
        component: Campaign, 
        props: true,
      },
      {
        path: "/segments",
        name: "Segments",
        displayName: "Segments",
        component: Segments, 
        props: true,
      },
       {
        path: "/location_lists",
        name: "LocationLists",
        displayName: "Location Lists",
        component: LocationLists, 
        props: true,
      },
        {
        path: "/login",
        name: "Login",
        displayName: "Login",
        component: Login, 
        props: true,
      },
 
    ],
  },
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes,
})

router.beforeEach(async (to, from, next) => {
  const publicPages = ["/login"];
  const authRequired = !publicPages.includes(to.path);
  console.log({createRouter: {authRequired }});

  const authenticationStore =  useAuthenticationStore();

  console.log({createRouter: {isTokenValid: authenticationStore.isTokenValid }});
  console.log({createRouter: {to_name: to.name}});

  if (to.name==="Login" && authenticationStore.isTokenValid) { 
      next("/")
  } else {
    if (authRequired && ! authenticationStore.isTokenValid) {

      console.log("authRequired && ! authenticationStore.isTokenValid");

      authenticationStore.logout();
      next("login");
    } else {
      console.log("We are logged in, Going to  ", to.name);
      next();
    }

  }
});



export default router
