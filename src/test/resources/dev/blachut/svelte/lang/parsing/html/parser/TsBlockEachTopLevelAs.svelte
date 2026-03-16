<script lang="ts">
  interface Item { name: string }
  let items: Item[] = [];
  let list: {y: number}[] = [];
</script>

{#each items as Item[] as item}
  <p>{item.name}</p>
{/each}

{#each items as Item[] as item, i (item.name)}
  <p>{i}: {item.name}</p>
{/each}

{#each list as { y = 0 }}
  <p>{y}</p>
{/each}

{#each items as unknown as Item[] as unknown as Item as item}
  <p>{item.name}</p>
{/each}

{#each [{name: 'A'}, {name: 'B'}] as item}
  <p>{item.name}</p>
{/each}

{#each items as { name }, i (name)}
  <p>{i}: {name}</p>
{/each}

{#each { length: 8 }, rank}
  <p>{rank}</p>
{/each}

{#each { length: 8 } as _, rank}
  <p>{rank}</p>
{/each}

<!-- depth testing: as inside (), [], {} should not be treated as binding -->
{#each (items as Item[]) as unknown as Item as item}
  <p>{item.name}</p>
{/each}

{#each [items as unknown] as item}
  <p>{item}</p>
{/each}

{#each fn(x as Item, y as Item) as item}
  <p>{item}</p>
{/each}

{#each {val: items as Item[]}.val as item}
  <p>{item}</p>
{/each}

{#each items as { name = '' as string }}
  <p>{name}</p>
{/each}

{#each items as item, i (item.id as unknown as string)}
  <p>{item.name}</p>
{/each}

<!-- TS assertion + as binding + comma index -->
{#each { length: 8 } as number[] as _, rank}
  <p>{rank}</p>
{/each}

{#each items as Item[] as item, i (item.name)}
  <p>{i}: {item.name}</p>
{/each}

{#each (items as Item[]) as unknown[] as { name }, i (name)}
  <p>{i}: {name}</p>
{/each}
