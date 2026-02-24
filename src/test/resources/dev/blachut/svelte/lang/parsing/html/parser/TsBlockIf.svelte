<script lang="ts">
  let count: number = 5;
  let message: string | null = null;
</script>

{#if (count as number) > 0}
  <p>Positive</p>
{:else if count satisfies number === 0}
  <p>Zero</p>
{:else}
  <p>Negative</p>
{/if}
