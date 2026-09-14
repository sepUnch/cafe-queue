<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { apiClient, type MenuItem } from '@/services/apiClient'

const emit = defineEmits<{
  (e: 'select', item: MenuItem): void
}>()

const menuItems = ref<MenuItem[]>([])
const loading = ref(true)

onMounted(async () => {
  try {
    menuItems.value = await apiClient.fetchMenu()
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="menu-list">
    <h3>Menu List</h3>
    <div v-if="loading">Loading menu...</div>
    <div v-else class="grid">
      <div 
        v-for="item in menuItems" 
        :key="item.name" 
        class="menu-card"
        :class="{ 'opacity-50': !item.available }"
        @click="item.available && emit('select', item)"
      >
        <h4>{{ item.name }}</h4>
        <p>Rp {{ item.price }}</p>
        <span v-if="!item.available" class="sold-out">Sold Out</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.menu-list {
  margin-bottom: 2rem;
  border-bottom: 2px dashed #0F172A;
  padding-bottom: 1rem;
}
.menu-list h3 {
  font-family: 'Oswald', sans-serif;
  text-transform: uppercase;
  margin-bottom: 1rem;
  font-size: 1.5rem;
}
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 1rem;
}
.menu-card {
  border: 2px solid #0F172A;
  padding: 0.75rem;
  border-radius: 0;
  cursor: pointer;
  background: #FFFFFF;
  transition: background-color 0.15s ease;
}
@media (prefers-reduced-motion: reduce) {
  .menu-card {
    transition: none;
  }
}
.menu-card:hover {
  background-color: #E5E7EB;
}
.menu-card:focus-visible {
  outline: 3px dashed #2563EB;
  outline-offset: -6px;
}
.menu-card h4 {
  margin: 0 0 0.5rem 0;
  font-weight: 700;
}
.menu-card p {
  margin: 0;
  font-family: 'Oswald', sans-serif;
}
.opacity-50 {
  opacity: 0.5;
  cursor: not-allowed;
  background-color: #F8F9FA;
}
.sold-out {
  color: #B91C1C;
  font-weight: bold;
  text-transform: uppercase;
  font-size: 0.8rem;
  display: block;
  margin-top: 0.5rem;
}
</style>
