<script lang="ts">
  /**
   * docs have to be removed!
   */
  let local = 323 as number | string;
</script>

{#if typeof local === "number"}
  {<caret>local.toFixed()}
{/if}
