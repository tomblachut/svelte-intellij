<script lang="ts" generics="T extends BaseItem">
  interface Props {
    items: T[];

    select(item: T): void;
  }

  let {items, select}: Props = $props();
</script>

{#each items as item}
  <button onclick={() => select(item)}>
    {item.text}
  </button>
{/each}
