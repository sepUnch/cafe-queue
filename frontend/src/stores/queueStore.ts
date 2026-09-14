import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export interface Ticket {
  ticketNumber: string;
  status: 'QUEUED' | 'COOKING' | 'READY';
  timestamp: string;
  pickupTime?: string | null;
}

export const useQueueStore = defineStore('queue', () => {
  const tickets = ref<Ticket[]>([])

  const queuedTickets = computed(() => tickets.value.filter(t => t.status === 'QUEUED'))
  const cookingTickets = computed(() => tickets.value.filter(t => t.status === 'COOKING'))
  const readyTickets = computed(() => tickets.value.filter(t => t.status === 'READY'))

  function setTickets(initialTickets: Ticket[]) {
    tickets.value = initialTickets
  }

  function updateOrAddTicket(ticket: Ticket) {
    const index = tickets.value.findIndex(t => t.ticketNumber === ticket.ticketNumber)
    if (index !== -1) {
      tickets.value[index] = ticket
    } else {
      tickets.value.push(ticket)
    }
  }

  return { tickets, queuedTickets, cookingTickets, readyTickets, setTickets, updateOrAddTicket }
})
