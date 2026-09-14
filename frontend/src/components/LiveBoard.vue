<script setup lang="ts">
import { onMounted, onUnmounted } from 'vue'
import { useQueueStore } from '@/stores/queueStore'
import { sseClient } from '@/services/sseClient'
import { apiClient } from '@/services/apiClient'
import TicketCard from './TicketCard.vue'

const queueStore = useQueueStore()

onMounted(async () => {
  // 1. Fetch initial snapshot
  try {
    const currentQueue = await apiClient.fetchCurrentQueue()
    queueStore.setTickets(currentQueue)
  } catch (e) {
    console.error('Failed to load initial queue', e)
  }

  // 2. Open SSE Connection
  sseClient.connect()
})

onUnmounted(() => {
  sseClient.disconnect()
})
</script>

<template>
  <div class="live-board">
    <div class="column queued">
      <h2 class="col-title">QUEUED</h2>
      <div class="ticket-list">
        <TicketCard 
          v-for="ticket in queueStore.queuedTickets" 
          :key="ticket.ticketNumber"
          :ticketNumber="ticket.ticketNumber"
          :timestamp="ticket.timestamp"
          :pickupTime="ticket.pickupTime"
          tabindex="0"
        />
      </div>
    </div>
    
    <div class="column cooking">
      <h2 class="col-title">COOKING</h2>
      <div class="ticket-list">
        <TicketCard 
          v-for="ticket in queueStore.cookingTickets" 
          :key="ticket.ticketNumber"
          :ticketNumber="ticket.ticketNumber"
          :timestamp="ticket.timestamp"
          :pickupTime="ticket.pickupTime"
          tabindex="0"
        />
      </div>
    </div>
    
    <div class="column ready">
      <h2 class="col-title">READY</h2>
      <div class="ticket-list">
        <TicketCard 
          v-for="ticket in queueStore.readyTickets" 
          :key="ticket.ticketNumber"
          :ticketNumber="ticket.ticketNumber"
          :timestamp="ticket.timestamp"
          :pickupTime="ticket.pickupTime"
          tabindex="0"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Oswald:wght@500;700&display=swap');

.live-board {
  display: flex;
  flex-direction: row;
  height: calc(100vh - 80px); /* Accounting for header */
  background-color: #E5E7EB;
  border: 4px solid #0F172A;
  box-sizing: border-box;
}

@media (max-width: 768px) {
  .live-board {
    flex-direction: column;
    height: auto;
    min-height: 100vh;
  }
}

.column {
  flex: 1;
  background-color: #FFFFFF;
  display: flex;
  flex-direction: column;
  border-right: 4px solid #0F172A;
}

.column:last-child {
  border-right: none;
}

@media (max-width: 768px) {
  .column {
    border-right: none;
    border-bottom: 4px solid #0F172A;
  }
  .column:last-child {
    border-bottom: none;
  }
}

.col-title {
  font-family: 'Oswald', sans-serif;
  font-size: clamp(1.5rem, 4vw, 2.5rem);
  font-weight: 700;
  text-align: center;
  margin: 0;
  padding: 1rem 0;
  background-color: #0F172A;
  color: #FFFFFF;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  border-bottom: 4px solid #0F172A;
}

.ticket-list {
  overflow-y: auto;
  flex-grow: 1;
  padding: 0;
  background: #FFFFFF;
}

/* Industrial Scrollbar */
.ticket-list::-webkit-scrollbar {
  width: 10px;
}
.ticket-list::-webkit-scrollbar-track {
  background: #FFFFFF;
  border-left: 2px solid #0F172A;
}
.ticket-list::-webkit-scrollbar-thumb {
  background: #0F172A;
}
</style>
