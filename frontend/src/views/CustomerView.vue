<script setup lang="ts">
import { ref } from 'vue'
import OrderForm from '@/components/OrderForm.vue'
import PreOrderForm from '@/components/PreOrderForm.vue'

const activeTab = ref<'regular' | 'preorder'>('regular')
</script>

<template>
  <div class="customer-view">
    <h1 class="main-title">Cafe Queue Kasir</h1>
    
    <div class="tab-container">
      <button 
        class="tab-btn" 
        :class="{ active: activeTab === 'regular' }" 
        @click="activeTab = 'regular'"
      >Regular Order</button>
      <button 
        class="tab-btn" 
        :class="{ active: activeTab === 'preorder' }" 
        @click="activeTab = 'preorder'"
      >Pre-Order</button>
    </div>

    <div class="forms-wrapper">
      <OrderForm v-if="activeTab === 'regular'" />
      <PreOrderForm v-if="activeTab === 'preorder'" />
    </div>
  </div>
</template>

<style scoped>
.customer-view {
  min-height: calc(100vh - 80px);
  background-color: #E5E7EB;
  padding: 2rem;
  color: #0F172A;
}
.main-title {
  text-align: center;
  font-family: 'Oswald', sans-serif;
  text-transform: uppercase;
  font-size: 3rem;
  margin-bottom: 2rem;
  padding-bottom: 1rem;
  color: #0F172A;
  border-bottom: 4px solid #0F172A;
}
.tab-container {
  display: flex;
  justify-content: center;
  gap: 1rem;
  margin-bottom: 2rem;
}
.tab-btn {
  font-family: 'Oswald', sans-serif;
  font-size: 1.25rem;
  text-transform: uppercase;
  padding: 0.75rem 2rem;
  background-color: #FFFFFF;
  color: #0F172A;
  border: 3px solid #0F172A;
  cursor: pointer;
  transition: all 0.2s ease;
}
@media (prefers-reduced-motion: reduce) {
  .tab-btn { transition: none; }
}
.tab-btn:hover {
  background-color: #E5E7EB;
}
.tab-btn.active {
  background-color: #0F172A;
  color: #FFFFFF;
}
.tab-btn:focus-visible {
  outline: 3px dashed #2563EB;
  outline-offset: 4px;
}
</style>
