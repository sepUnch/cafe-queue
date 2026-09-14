<script setup lang="ts">
import { ref } from 'vue'
import MenuList from './MenuList.vue'
import { apiClient, type MenuItem, type OrderItemRequest, type PreOrderRequest } from '@/services/apiClient'

const customerName = ref('')
const pickupTime = ref('')
const cart = ref<OrderItemRequest[]>([])
const message = ref('')

const addToCart = (item: MenuItem) => {
  const existing = cart.value.find((i: OrderItemRequest) => i.menuItemName === item.name)
  if (existing) {
    existing.quantity++
  } else {
    cart.value.push({ menuItemName: item.name, price: item.price, quantity: 1 })
  }
}

const submitPreOrder = async () => {
  if (!customerName.value || !pickupTime.value || cart.value.length === 0) {
    message.value = 'Please fill all fields and add items'
    return
  }
  
  // Format to ISO 8601 without timezone
  const formattedTime = new Date(pickupTime.value).toISOString().slice(0, 19)

  const req: PreOrderRequest = {
    customerName: customerName.value,
    pickupTime: formattedTime,
    items: cart.value
  }

  try {
    const res = await apiClient.createPreOrder(req)
    message.value = `PreOrder scheduled! ID: ${res.id}`
    cart.value = []
    customerName.value = ''
    pickupTime.value = ''
  } catch (e: any) {
    message.value = `Error: ${e.message}`
  }
}
</script>

<template>
  <div class="split-layout">
    <div class="menu-section">
      <MenuList @select="addToCart" />
    </div>

    <div class="preorder-form clipboard">
      <div class="clipboard-clip"></div>
      <h2 class="form-title po-title">Pre-Order</h2>
      
      <div class="form-container">
        <div class="input-group">
          <label>Customer Name:</label>
          <input v-model="customerName" type="text" placeholder="Jane Doe" />
        </div>
        <div class="input-group">
          <label>Pickup Time:</label>
          <input v-model="pickupTime" type="datetime-local" />
        </div>
        
        <div class="cart">
          <h4>Order Details</h4>
          <ul v-if="cart.length > 0">
            <li v-for="item in cart" :key="item.menuItemName">
              <span class="qty">{{ item.quantity }}x</span> 
              <span class="name">{{ item.menuItemName }}</span> 
              <span class="price">Rp {{ item.price * item.quantity }}</span>
            </li>
          </ul>
          <div v-else class="empty-cart">- No items selected -</div>
        </div>

        <button class="action-btn po-btn" @click="submitPreOrder" :disabled="cart.length === 0">Schedule Order</button>
        <div v-if="message" class="message">{{ message }}</div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.split-layout {
  display: flex;
  flex-direction: column;
  gap: 2rem;
  color: #0F172A;
  width: 100%;
}
@media (min-width: 1024px) {
  .split-layout {
    flex-direction: row;
    align-items: flex-start;
  }
}
.menu-section {
  flex: 1 1 60%;
  width: 100%;
}
.preorder-form {
  flex: 1 1 40%;
  width: 100%;
}
.clipboard {
  background-color: #FFFFFF;
  border: 4px solid #0F172A;
  padding: 3rem 2rem 2rem;
  position: relative;
  box-shadow: 8px 8px 0px rgba(15, 23, 42, 0.1);
}
.clipboard-clip {
  position: absolute;
  top: -10px;
  left: 50%;
  transform: translateX(-50%);
  width: 100px;
  height: 20px;
  background-color: #E5E7EB;
  border: 4px solid #0F172A;
}
.form-title {
  font-family: 'Oswald', sans-serif;
  text-transform: uppercase;
  text-align: center;
  font-size: 2rem;
  margin-bottom: 2rem;
  letter-spacing: 0.05em;
  color: #0F172A;
}
.po-title {
  color: #B91C1C;
}
.input-group {
  margin-bottom: 1rem;
}
.input-group label {
  display: block;
  font-weight: 700;
  margin-bottom: 0.5rem;
  text-transform: uppercase;
  font-size: 0.9rem;
}
input[type="text"], input[type="datetime-local"] {
  width: 100%;
  padding: 0.75rem;
  border: 2px solid #0F172A;
  border-radius: 0;
  font-family: inherit;
  font-size: 1rem;
  background-color: #FFFFFF;
  box-sizing: border-box;
  color: #0F172A;
}
input:focus {
  outline: 3px dashed #B91C1C;
  outline-offset: -2px;
}
.cart {
  margin: 2rem 0;
  padding: 1.5rem;
  border: 2px dashed #0F172A;
  background: #FAFAFA;
}
.cart h4 {
  font-family: 'Oswald', sans-serif;
  text-transform: uppercase;
  margin: 0 0 1rem 0;
  border-bottom: 2px solid #0F172A;
  padding-bottom: 0.5rem;
}
.cart ul {
  list-style: none;
  padding: 0;
  margin: 0;
}
.cart li {
  display: flex;
  justify-content: space-between;
  margin-bottom: 0.5rem;
  font-family: 'Oswald', sans-serif;
}
.qty { width: 30px; }
.name { flex: 1; border-bottom: 1px dotted #0F172A; margin: 0 10px; position: relative; top: -4px; }
.empty-cart {
  text-align: center;
  font-style: italic;
  color: #6B7280;
}
.action-btn {
  width: 100%;
  background-color: #0F172A;
  color: #FFFFFF;
  border: 4px solid #0F172A;
  font-family: 'Oswald', sans-serif;
  font-size: 1.5rem;
  text-transform: uppercase;
  padding: 1rem;
  cursor: pointer;
  transition: background-color 0.15s ease;
}
.po-btn {
  background-color: #B91C1C;
  border-color: #0F172A;
}
@media (prefers-reduced-motion: reduce) {
  .action-btn {
    transition: none;
  }
}
.po-btn:hover:not(:disabled) {
  background-color: #991B1B;
}
.action-btn:disabled {
  background-color: #E5E7EB;
  color: #6B7280;
  border-color: #0F172A;
  cursor: not-allowed;
}
.action-btn:focus-visible {
  outline: 4px dashed #0F172A;
  outline-offset: 4px;
}
.message {
  margin-top: 1.5rem;
  padding: 1rem;
  background-color: #0F172A;
  color: #FFFFFF;
  font-weight: 700;
  text-align: center;
  text-transform: uppercase;
}
</style>
