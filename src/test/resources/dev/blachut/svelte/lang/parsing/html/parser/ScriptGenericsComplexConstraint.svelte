<script lang="ts" generics="T extends { id: number, name: string }, U extends Array<T>">
  let {items}: { items: U } = $props();
</script>

{#each items as item}
  <p>{item.id}: {item.name}</p>
{/each}
