<script lang="ts">
  let value: string | number = 'test';
</script>

{#key (value as string | number)}
  <div>{value}</div>
{/key}
