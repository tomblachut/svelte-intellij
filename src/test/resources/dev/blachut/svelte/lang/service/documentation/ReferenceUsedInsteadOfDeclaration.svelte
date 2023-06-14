<script lang="ts">
    /**
     * docs have to be removed!
     */
    let local = 323 as number | null;
</script>

{#if local != null}
    {<caret>local.toFixed()}
{/if}
