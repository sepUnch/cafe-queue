<script setup lang="ts">
import { ref } from 'vue'
import { apiClient } from '@/services/apiClient'

const file = ref<File | null>(null)
const uploadStatus = ref('')
const discrepancies = ref<any[]>([])

const handleFileChange = (e: Event) => {
  const target = e.target as HTMLInputElement
  if (target.files && target.files.length > 0) {
    file.value = target.files[0]
  }
}

const uploadAndFetch = async () => {
  if (!file.value) return
  uploadStatus.value = 'Uploading...'
  
  try {
    const uploadRes = await apiClient.uploadReconciliation(file.value)
    uploadStatus.value = `Uploaded successfully. ID: ${uploadRes.id}`
    
    const results = await apiClient.fetchDiscrepancies(uploadRes.id)
    discrepancies.value = results
  } catch (e: any) {
    uploadStatus.value = `Error: ${e.message}`
  }
}
</script>

<template>
  <div class="reconciliation clipboard">
    <div class="clipboard-clip"></div>
    <h2 class="form-title">Batch Reconciliation</h2>
    <p class="subtitle">Upload cashier CSV report to find discrepancies.</p>
    
    <div class="upload-area">
      <input type="file" accept=".csv" @change="handleFileChange" class="file-input" />
      <button class="action-btn" @click="uploadAndFetch" :disabled="!file">Upload & Reconcile</button>
    </div>
    
    <div v-if="uploadStatus" class="status-box" :class="{ 'error-box': uploadStatus.startsWith('Error') }">
      {{ uploadStatus }}
    </div>

    <div v-if="discrepancies.length > 0" class="results">
      <h3>Discrepancies Found ({{ discrepancies.length }})</h3>
      <table class="data-grid">
        <thead>
          <tr>
            <th>Order ID</th>
            <th class="num-col">Amount Paid</th>
            <th>Status</th>
            <th>Note</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="d in discrepancies" :key="d.id">
            <td class="mono-text">{{ d.orderIdRaw }}</td>
            <td class="num-col">Rp {{ d.amountPaid.toLocaleString('id-ID') }}</td>
            <td><span class="badge" :class="d.reconciliationStatus">{{ d.reconciliationStatus.replace(/_/g, ' ') }}</span></td>
            <td>{{ d.note }}</td>
          </tr>
        </tbody>
      </table>
    </div>
    <div v-else-if="uploadStatus.includes('successfully')" class="perfect-box">
      <h3>PERFECT MATCH!</h3>
      <p>No discrepancies found. All transactions matched perfectly.</p>
    </div>
  </div>
</template>

<style scoped>
.clipboard {
  background-color: #FFFFFF;
  border: 4px solid #0F172A;
  padding: 3rem 2rem 2rem;
  position: relative;
  box-shadow: 8px 8px 0px rgba(15, 23, 42, 0.1);
  color: #0F172A;
}
.clipboard-clip {
  position: absolute;
  top: -10px;
  left: 50%;
  transform: translateX(-50%);
  width: 120px;
  height: 20px;
  background-color: #E5E7EB;
  border: 4px solid #0F172A;
}
.form-title {
  font-family: 'Oswald', sans-serif;
  text-transform: uppercase;
  text-align: center;
  font-size: 2rem;
  margin-bottom: 0.5rem;
  color: #0F172A;
}
.subtitle {
  text-align: center;
  margin-bottom: 2rem;
  font-weight: 600;
  text-transform: uppercase;
  font-size: 0.9rem;
  color: #6B7280;
}
.upload-area {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  margin-bottom: 2rem;
  padding: 1.5rem;
  border: 2px dashed #0F172A;
  background: #FAFAFA;
}
@media (min-width: 768px) {
  .upload-area {
    flex-direction: row;
    align-items: center;
  }
}
.file-input {
  flex: 1;
  padding: 0.5rem;
  font-family: inherit;
  font-weight: 600;
}
.action-btn {
  background-color: #0F172A;
  color: #FFFFFF;
  border: 4px solid #0F172A;
  font-family: 'Oswald', sans-serif;
  font-size: 1.2rem;
  text-transform: uppercase;
  padding: 0.75rem 2rem;
  cursor: pointer;
  transition: background-color 0.15s ease;
  white-space: nowrap;
}
@media (prefers-reduced-motion: reduce) {
  .action-btn { transition: none; }
}
.action-btn:hover:not(:disabled) {
  background-color: #1E293B;
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
.status-box {
  padding: 1rem;
  background-color: #0F172A;
  color: #FFFFFF;
  font-weight: 700;
  text-align: center;
  text-transform: uppercase;
  margin-bottom: 2rem;
}
.error-box {
  background-color: #B91C1C;
}
.results h3 {
  font-family: 'Oswald', sans-serif;
  text-transform: uppercase;
  font-size: 1.5rem;
  margin-bottom: 1rem;
  border-bottom: 3px solid #0F172A;
  padding-bottom: 0.5rem;
}
.data-grid {
  width: 100%;
  border-collapse: collapse;
  border: 1px solid #CBD5E1;
  border-radius: 4px;
}
.data-grid th, .data-grid td {
  padding: 1rem;
  text-align: left;
  vertical-align: middle;
}
.data-grid td {
  border-bottom: 1px solid #E2E8F0;
}
.data-grid th {
  background-color: #0F172A;
  color: #FFFFFF;
  text-transform: uppercase;
  font-weight: 600;
  font-size: 0.85rem;
  letter-spacing: 0.05em;
  white-space: nowrap;
}
.data-grid tbody tr:last-child td {
  border-bottom: none;
}
.data-grid tbody tr:nth-child(even) {
  background-color: #F8FAFC;
}
.data-grid tbody tr:hover {
  background-color: #F1F5F9;
}
.num-col {
  text-align: right !important;
  font-variant-numeric: tabular-nums;
  font-family: 'Oswald', ui-monospace, SFMono-Regular, monospace;
}
.mono-text {
  font-family: ui-monospace, SFMono-Regular, monospace;
  font-size: 0.85rem;
  color: #334155;
  word-break: break-all;
  max-width: 250px;
}
.badge {
  display: inline-block;
  padding: 0.35rem 0.75rem;
  font-weight: 700;
  font-size: 0.75rem;
  text-transform: uppercase;
  border-radius: 4px;
  white-space: nowrap;
}
.AMOUNT_MISMATCH {
  background-color: #FEF3C7;
  color: #92400E;
  border: 1px solid #F59E0B;
}
.ORDER_NOT_FOUND {
  background-color: #FEE2E2;
  color: #991B1B;
  border: 1px solid #EF4444;
}
.perfect-box {
  text-align: center;
  padding: 3rem;
  border: 4px dashed #10B981;
  background-color: #ECFDF5;
  color: #065F46;
}
.perfect-box h3 {
  font-family: 'Oswald', sans-serif;
  font-size: 2.5rem;
  margin-bottom: 0.5rem;
}
</style>
