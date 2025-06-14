<script setup lang="ts">

</script>

<template>
  <h1 class="text-4xl font-semibold text-gray-800 mb-4">
    Help view
  </h1>

  <Button @click="$router.push({ name: 'home'})" class="w-full flex items-center gap-2" variant="outline">
    Home
  </Button>
</template>