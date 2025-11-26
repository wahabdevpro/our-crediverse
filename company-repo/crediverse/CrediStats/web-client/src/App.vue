<template>
  <v-layout>
    <v-navigation-drawer 
      expand-on-hover
      rail
      permanent
    >
      <v-list-item style="min-height: 69px ">
        <v-img
          cover
          src="./assets/logo.png"
        ></v-img>
      </v-list-item>

      <v-divider></v-divider>

      <v-list density="compact" nav>
        <v-list-item 
          prepend-icon="mdi-home" 
          title="Home" 
          value="home" 
          @click="onHomeClicked"
        ></v-list-item>
        <v-list-item 
          prepend-icon="mdi-pencil-ruler" 
          title="Campaigns" 
          value="campaigns" 
          @click="onCampaignsClicked"
        ></v-list-item>
        <v-list-item 
          prepend-icon="mdi-segment" 
          title="Segments" 
          value="segments" 
          @click="onSegmentsClicked"
        ></v-list-item>
        <v-list-item 
          prepend-icon="mdi-map-marker-multiple-outline" 
          title="Location Lists" 
          value="locationLists" 
          @click="onLocationListsClicked"
        ></v-list-item>
      </v-list>
    </v-navigation-drawer>
    <v-main>
      <router-view /> 
    </v-main>
  </v-layout>
</template>

<script>

import { useRouter } from 'vue-router';
import { ref } from 'vue';

export default {
  setup() {
    const router = useRouter();

    // let drawer = ref(true);
    const onHomeClicked = () => {
      router.push('/'); 
    };
    const onCampaignsClicked = () => {
      router.push('/campaigns'); 
    };
    const onSegmentsClicked = () => {
      router.push('/segments'); 
    };
    const onLocationListsClicked = () => {
      router.push('/location_lists'); 
    };

    return {onHomeClicked ,onCampaignsClicked, onSegmentsClicked, onLocationListsClicked};
  },
}
</script>
