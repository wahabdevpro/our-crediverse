<template>
  <v-container>
    <v-row justify="center">
      <v-col cols="12" md="6">
        <v-card>
          <v-card-title>Login</v-card-title>
          <v-card-text>
            <v-text-field 
              label="Username" 
              v-model="username" 
              required
            ></v-text-field>
            <v-text-field 
              label="Password" 
              v-model="password" 
              type="password" 
              required
            ></v-text-field>
          </v-card-text>
          <v-card-actions>
            <v-btn @click="handleLogin">Login</v-btn>
          </v-card-actions>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script>
import { ref } from 'vue';
import { useAuthenticationStore } from "../stores/AuthenticationStore";

import { useRouter } from "vue-router"


export default {
  name: 'LoginForm',
  setup() {
    const username = ref("");
    const password = ref("");
    
    const router = useRouter();

    async function handleLogin() {

      console.log("handleLogin() logging in with", {username: username.value,password: password.value});

      if(username.value && password.value) {
        let authenticationStore =  useAuthenticationStore();

        await authenticationStore.login(username.value, password.value); 
        // Do your login logic here

        router.push({ name: "Home" });

        console.log('Logging in...');
      } else {
        console.error('Fill in all fields');
      }


    }

    return {
      username,
      password,
      handleLogin
    };
  }
}
</script>
