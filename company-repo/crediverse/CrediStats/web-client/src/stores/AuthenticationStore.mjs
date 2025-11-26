import {defineStore} from "pinia";
import jwt_decode from "jwt-decode";

export const useAuthenticationStore = defineStore(
  "AuthenticationStore",
  { 
    state: () => ({ 
      userInfo: null,
      accessToken: null, 
    }),
    getters:{
      isTokenValid() {
        console.log("isTokenValid: ",this.userInfo );

        if (!this.userInfo) return false;


        if (!this.userInfo.exp) return false;

        return new Date().getTime() < this.userInfo.exp* 1000;
      },
    },
    actions: {
      logout() {
        this.userInfo=null;
        this.accessToken =null;
      },

      async login(username, password) {
        const credentials = {
          username,
          password,
        };

        const loginResponse = await fetch(
          "http://localhost:8801/credistats/login",
          {
            method: "POST",
            headers: {
              "Content-Type": "application/json",
              Accept: "application/json",
            },
            body: JSON.stringify(credentials),
          }
        );
        console.log({loginResponse});

        const accessToken = await loginResponse.json();

        console.log({accessToken });
        let userInfo = jwt_decode(accessToken.accessToken);

        console.log("Login: ",{userInfo});

        this.userInfo= userInfo ;
        
        console.log("Login: ",{userInfo: this.userInfo});

        this.accessToken = accessToken ;
      },
    },
  }
);

