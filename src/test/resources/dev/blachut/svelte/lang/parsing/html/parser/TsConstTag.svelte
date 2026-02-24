<script lang="ts">
  let items: number[] = [1, 2, 3];
</script>

{#each items as item}
  {@const doubled = (item as number) * 2}
  {@const message = `Value: ${item satisfies number}`}
  <div>{doubled}: {message}</div>
{/each}
