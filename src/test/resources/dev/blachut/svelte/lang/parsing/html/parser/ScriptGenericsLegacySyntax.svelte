<script lang="ts" generics="T">
  export let items: T[];
  export let renderItem: (item: T) => string;
</script>

<ul>
  {#each items as item}
    <li>{@html renderItem(item)}</li>
  {/each}
</ul>
