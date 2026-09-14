import { useQueueStore, type Ticket } from '@/stores/queueStore'

export class SseClient {
  private eventSource: EventSource | null = null;

  connect() {
    if (this.eventSource) return;

    this.eventSource = new EventSource('/queue-board/stream');

    this.eventSource.addEventListener('status-changed', (event) => {
      try {
        const ticket: Ticket = JSON.parse(event.data);
        const store = useQueueStore();
        store.updateOrAddTicket(ticket);
      } catch (error) {
        console.error('Error parsing SSE data:', error);
      }
    });

    // Also listen to general messages just in case
    this.eventSource.onmessage = (event) => {
      try {
        const ticket: Ticket = JSON.parse(event.data);
        const store = useQueueStore();
        store.updateOrAddTicket(ticket);
      } catch (error) {
        // ignore parse error for keep-alive
      }
    };

    this.eventSource.onerror = (error) => {
      console.error('SSE Error:', error);
      this.eventSource?.close();
      this.eventSource = null;
      // Reconnect after 3 seconds
      setTimeout(() => this.connect(), 3000);
    };
  }

  disconnect() {
    if (this.eventSource) {
      this.eventSource.close();
      this.eventSource = null;
    }
  }
}

export const sseClient = new SseClient();
