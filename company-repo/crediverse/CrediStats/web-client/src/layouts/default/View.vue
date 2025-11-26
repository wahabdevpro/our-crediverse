<template>
  <v-main>
    <!-- Suspense-->
      <template #default>
        <router-view />
      </template>
      <template #fallback>
        <div>Loading...</div> <!-- or any other loading UI -->
      </template>
    <!-- /Suspense -->
  </v-main>
</template>

<script setup>
  //
</script>
