<script setup lang="ts">
import { onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import ToastStack from '@/components/ToastStack.vue'
import AppLayout from '@/components/AppLayout.vue'

const auth = useAuthStore()

onMounted(async () => {
  if (auth.status === 'idle') {
    await auth.bootstrap()
  }
})
</script>

<template>
  <AppLayout>
    <RouterView v-slot="{ Component, route }">
      <component :is="Component" :key="route.fullPath" />
    </RouterView>
  </AppLayout>
  <ToastStack />
</template>
