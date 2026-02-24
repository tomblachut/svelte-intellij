<script lang="ts">
  interface Item {
    id: number;
    name: string;
  }
  let items: Item[] = [{ id: 1, name: 'A' }, { id: 2, name: 'B' }];
</script>

{#each (items as Item[]) as item, i}
  <div>{item.name}</div>
{/each}

{#each items satisfies Item[] as {id, name}}
  <div>{name}: {id}</div>
{/each}
