export interface MenuItem {
  name: string;
  price: number;
  category: string;
  available: boolean;
  attributes: Record<string, any>;
}

export interface Ticket {
  ticketNumber: string;
  status: string;
  timestamp: string;
  pickupTime?: string | null;
}

export interface OrderItemRequest {
  menuItemName: string;
  quantity: number;
  price: number;
}

export interface OrderRequest {
  customerName: string;
  orderType: 'DINE_IN' | 'TAKEAWAY';
  pickupTime?: string | null;
  items: OrderItemRequest[];
}

export interface PreOrderRequest {
  customerName: string;
  pickupTime: string;
  items: OrderItemRequest[];
}

// Di dev: BASE_URL = '' sehingga Vite proxy yang bekerja (e.g. /orders → localhost:8080)
// Di production (Vercel): BASE_URL = URL backend Render (e.g. https://cafe-queue.onrender.com)
const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '';

export const apiClient = {
  async fetchMenu(): Promise<MenuItem[]> {
    const res = await fetch(`${BASE_URL}/menu`);
    if (!res.ok) throw new Error('Failed to fetch menu');
    return res.json();
  },

  async createOrder(order: OrderRequest) {
    const res = await fetch(`${BASE_URL}/orders`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(order)
    });
    if (!res.ok) throw new Error(await res.text());
    return res.json();
  },

  async createPreOrder(preOrder: PreOrderRequest) {
    const res = await fetch(`${BASE_URL}/preorders`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(preOrder)
    });
    if (!res.ok) throw new Error(await res.text());
    return res.json();
  },

  async fetchCurrentQueue() {
    const res = await fetch(`${BASE_URL}/queue-board/current`);
    if (!res.ok) throw new Error('Failed to fetch queue');
    return res.json();
  },

  async uploadReconciliation(file: File) {
    const formData = new FormData();
    formData.append('file', file);
    
    const res = await fetch(`${BASE_URL}/reconciliation/upload`, {
      method: 'POST',
      body: formData
    });
    if (!res.ok) throw new Error(await res.text());
    return res.json();
  },

  async fetchDiscrepancies(uploadId: string) {
    const res = await fetch(`${BASE_URL}/reconciliation/${uploadId}/discrepancies`);
    if (!res.ok) throw new Error('Failed to fetch discrepancies');
    return res.json();
  }
}
