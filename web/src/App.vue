<template>
  <div class="bg-layer"></div>
  <router-view />
</template>

<script setup>
</script>

<style>
html, body, #app {
  height: 100%;
  margin: 0;
  background: var(--bg);
  overflow: hidden;
}

.bg-layer {
  position: fixed;
  inset: 0;
  pointer-events: none;
  z-index: 0;
  background: 
    radial-gradient(ellipse at 20% 20%, rgba(124,131,255,0.08) 0%, transparent 50%),
    radial-gradient(ellipse at 80% 80%, rgba(255,107,181,0.06) 0%, transparent 50%),
    radial-gradient(ellipse at 50% 50%, rgba(74,222,128,0.04) 0%, transparent 50%),
    var(--bg, #FAFAFA);
}
</style>
