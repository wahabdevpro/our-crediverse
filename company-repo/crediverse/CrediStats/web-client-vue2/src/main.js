import Vue from "vue";
import router from "./router";

import BootstrapVue from "bootstrap-vue";

import App from "./App";
import Router from "vue-router";

import Default from "./Layout/Wrappers/baseLayout.vue";
import Pages from "./Layout/Wrappers/pagesLayout.vue";
import store from "./store";

Vue.config.productionTip = false;

Vue.use(BootstrapVue);

Vue.component("default-layout", Default);
Vue.component("userpages-layout", Pages);
Vue.use(Router);
new Vue({
    el: "#app",
    store,
    router,
    template: "<App/>",
    components: { App },
});
