<script setup lang="ts">
import { RouterView, RouterLink } from 'vue-router'
</script>

<template>
  <header class="global-header">
    <nav class="main-nav">
      <RouterLink to="/" class="nav-link">Cashier</RouterLink>
      <RouterLink to="/queue-board" class="nav-link">Kitchen Display</RouterLink>
      <RouterLink to="/admin/reconciliation" class="nav-link">Admin (Daily Report)</RouterLink>
    </nav>
  </header>

  <main class="global-main">
    <RouterView />
  </main>
</template>

<style>
/* Global Resets */
html, body, #app {
  margin: 0;
  padding: 0;
  background-color: #E5E7EB; /* Set global background to Workbench Gray */
  min-height: 100vh;
  font-family: system-ui, -apple-system, sans-serif;
  color: #0F172A;
}
</style>

<style scoped>
.global-header {
  background-color: #0F172A;
  border-bottom: 4px solid #000000;
}
.main-nav {
  display: flex;
  justify-content: center;
}
.nav-link {
  font-family: 'Oswald', sans-serif;
  text-transform: uppercase;
  text-decoration: none;
  color: #9CA3AF;
  font-size: 1.1rem;
  font-weight: 700;
  padding: 1rem 2rem;
  letter-spacing: 0.05em;
  border-right: 2px dashed #334155;
  transition: all 0.2s ease;
}
@media (prefers-reduced-motion: reduce) {
  .nav-link { transition: none; }
}
.nav-link:first-child {
  border-left: 2px dashed #334155;
}
.nav-link:hover {
  background-color: #1E293B;
  color: #FFFFFF;
}
.nav-link.router-link-exact-active {
  background-color: #2563EB;
  color: #FFFFFF;
}
.nav-link:focus-visible {
  outline: 3px dashed #FFFFFF;
  outline-offset: -4px;
}
.global-main {
  width: 100%;
}
</style>
