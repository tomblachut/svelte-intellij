<script lang="ts">
  interface Item {
    id: number;
    name: string;
  }

  const maybeItem: Item | null = { id: 1, name: "first" };
</script>

{#snippet interfaceRef(item: Item)}
  {@const constNonNull: Item = maybeItem!}
  <p>{item.name}</p>
{/snippet}
