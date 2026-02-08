<script lang="ts" generics="T extends { text: string },U extends Array<T>,V = string">
  export let items: T[];
  export let extra: U;
  export let value: V;
</script>

<div>
  {#each items as item}
    <p>{item.text}</p>
  {/each}
</div>
