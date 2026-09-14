import { createRouter, createWebHistory } from 'vue-router'
import CustomerView from '@/views/CustomerView.vue'
import QueueBoardView from '@/views/QueueBoardView.vue'
import AdminReconciliationView from '@/views/AdminReconciliationView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'customer',
      component: CustomerView
    },
    {
      path: '/queue-board',
      name: 'queue-board',
      component: QueueBoardView
    },
    {
      path: '/admin/reconciliation',
      name: 'admin-reconciliation',
      component: AdminReconciliationView
    }
  ]
})

export default router
