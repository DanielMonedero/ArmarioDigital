<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<template>
  <div class="not-found">
    <div class="not-found__card">
      <h1>404</h1>
      <p>No hemos encontrado la página que buscas.</p>
      <button class="btn btn--primary" type="button" @click="router.replace({ name: 'wardrobe' })">
        Volver al inicio
      </button>
    </div>
  </div>
</template>

<style scoped>
.not-found {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: var(--space-5);
}

.not-found__card {
  background: var(--color-surface);
  padding: var(--space-6);
  border-radius: var(--radius-xl);
  border: 1px solid var(--color-border);
  text-align: center;
  max-width: 360px;
  box-shadow: var(--shadow-soft);
}

.not-found__card h1 {
  font-family: var(--font-serif);
  font-size: 2.5rem;
  margin-bottom: var(--space-2);
}

.not-found__card p {
  color: var(--color-text-muted);
  margin-bottom: var(--space-4);
}
</style>
