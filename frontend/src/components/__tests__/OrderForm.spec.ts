import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import OrderForm from '../OrderForm.vue'
import { apiClient } from '@/services/apiClient'
import { nextTick } from 'vue'

// Mock the API client
vi.mock('@/services/apiClient', () => ({
  apiClient: {
    fetchMenu: vi.fn().mockResolvedValue([
      { name: 'Kopi Susu', price: 20000, category: 'COFFEE', available: true, attributes: {} }
    ]),
    createOrder: vi.fn().mockResolvedValue({ ticketNumber: 42 })
  }
}))

describe('OrderForm.vue', () => {
  it('renders correctly and fetches menu', async () => {
    const wrapper = mount(OrderForm)
    
    // Wait for the mounted hook to complete fetching menu list
    await nextTick()
    await nextTick()
    await nextTick()

    // Find the submit button, it should be disabled initially
    const submitBtn = wrapper.find('button')
    expect(submitBtn.attributes('disabled')).toBeDefined()
    
    // Set customer name
    const nameInput = wrapper.find('input[type="text"]')
    await nameInput.setValue('Budi')
    
    // Click the menu card to add to cart
    const menuCard = wrapper.find('.menu-card')
    expect(menuCard.exists()).toBe(true)
    await menuCard.trigger('click')
    
    await nextTick()
    
    // Now button should be enabled
    expect(submitBtn.attributes('disabled')).toBeUndefined()
    
    // Submit order
    await submitBtn.trigger('click')
    await nextTick()
    await nextTick()
    
    expect(apiClient.createOrder).toHaveBeenCalledWith({
      customerName: 'Budi',
      orderType: 'DINE_IN',
      items: [{ menuItemName: 'Kopi Susu', price: 20000, quantity: 1 }]
    })
    
    // Check if success message is displayed
    expect(wrapper.text()).toContain('Ticket #42')
  })
})
