<script setup lang="ts">
import ReconciliationUpload from '@/components/ReconciliationUpload.vue'
</script>

<template>
  <div class="admin-view-container">
    <h1 class="admin-title">Reconciliation Tools</h1>
    <div class="admin-view">
      <ReconciliationUpload />
    </div>
  </div>
</template>

<style scoped>
.admin-view-container {
  min-height: calc(100vh - 80px);
  background-color: #E5E7EB;
  padding: 2rem;
  color: #0F172A;
}
.admin-title {
  text-align: center;
  font-family: 'Oswald', sans-serif;
  text-transform: uppercase;
  font-size: 3rem;
  margin-bottom: 2rem;
  padding-bottom: 1rem;
  color: #0F172A;
  border-bottom: 4px solid #0F172A;
}
.admin-view {
  max-width: 1000px;
  margin: 0 auto;
}
</style>
