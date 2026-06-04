<script lang="ts">
    let base: number = 0;
</script>

{#if true}
    {const typed: number = 1}
    {let counter: number = base}
    {const {a, b}: { a: number; b: string } = obj}
{/if}
