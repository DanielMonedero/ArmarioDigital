<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import * as garmentApi from '@/api/garments'
import GarmentForm from '@/components/GarmentForm.vue'
import type { GarmentCreateRequest } from '@/types/api'
import { useToastStore } from '@/stores/toast'

const router = useRouter()
const toast = useToastStore()

const loading = ref(false)
const error = ref<unknown>(null)

async function submit(payload: GarmentCreateRequest) {
  loading.value = true
  error.value = null
  try {
    const created = await garmentApi.createGarment(payload)
    toast.success('Prenda creada')
    await router.push({ name: 'garment-detail', params: { id: created.id } })
  } catch (err) {
    error.value = err
  } finally {
    loading.value = false
  }
}

function cancel() {
  router.push({ name: 'wardrobe' })
}
</script>

<template>
  <section class="page">
    <RouterLink :to="{ name: 'wardrobe' }" class="back-link">← Volver</RouterLink>

    <header class="page__header">
      <div class="page__title">
        <h1>Nueva prenda</h1>
        <p class="page__subtitle">
          Rellena los datos básicos. Podrás añadir fotos después.
        </p>
      </div>
    </header>

    <GarmentForm
      submit-label="Crear prenda"
      :loading="loading"
      :error="error"
      @submit="submit"
      @cancel="cancel"
    />
  </section>
</template>

<style scoped>
.back-link {
  display: inline-flex;
  align-items: center;
  font-size: 0.88rem;
  color: var(--color-text-muted);
  margin-bottom: var(--space-3);
}
</style>
