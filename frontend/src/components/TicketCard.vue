<script setup lang="ts">
defineProps<{
  ticketNumber: string;
  timestamp: string;
  pickupTime?: string | null;
}>()

const formatTime = (time: string | undefined | null) => {
  if (!time) return '';
  return new Date(time.endsWith('Z') ? time : time + 'Z').toLocaleTimeString();
}
</script>

<template>
  <div class="ticket-card" :class="{ 'is-preorder': pickupTime }">
    <div class="ticket-number">#{{ ticketNumber }}</div>
    <div class="ticket-time">Masuk: {{ formatTime(timestamp) }}</div>
    <div v-if="pickupTime" class="pickup-time">PO: {{ formatTime(pickupTime) }}</div>
  </div>
</template>

<style scoped>
.ticket-card {
  background-color: transparent;
  border: none;
  border-bottom: 2px dashed #0F172A;
  border-radius: 0;
  padding: 1.5rem 1rem;
  margin-bottom: 0;
  text-align: center;
  box-shadow: none;
  color: #0F172A;
  transition: background-color 0.15s ease-in-out;
}

@media (prefers-reduced-motion: reduce) {
  .ticket-card {
    transition: none;
  }
}

.ticket-card:focus-visible {
  outline: 3px dashed #2563EB;
  outline-offset: -6px;
}

.ticket-card.is-preorder {
  background-color: #B91C1C;
  color: #FFFFFF;
  border-bottom: 2px dashed #0F172A;
}

.ticket-number {
  font-family: 'Oswald', sans-serif;
  font-size: 4rem;
  font-weight: 700;
  line-height: 1;
  margin-bottom: 0.5rem;
}

.ticket-time {
  font-family: system-ui, -apple-system, sans-serif;
  font-size: 1rem;
  font-weight: 600;
  text-transform: uppercase;
}

.pickup-time {
  font-family: system-ui, -apple-system, sans-serif;
  font-size: 1.1rem;
  font-weight: 700;
  margin-top: 0.5rem;
  padding: 0.2rem 0.5rem;
  display: inline-block;
  background-color: #0F172A;
  color: #FFFFFF;
}

.ticket-card.is-preorder .pickup-time {
  background-color: #FFFFFF;
  color: #B91C1C;
}
</style>

