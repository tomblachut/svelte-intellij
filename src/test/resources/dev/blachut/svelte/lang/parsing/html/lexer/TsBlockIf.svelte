<script lang="ts">
  let count: number = 0;
</script>

{#if count satisfies number}
  <p>Yes</p>
{/if}
