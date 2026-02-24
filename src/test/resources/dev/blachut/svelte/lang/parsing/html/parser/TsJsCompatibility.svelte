<script>
  let count = 0;
  let message = 'Hello';
</script>

<div>{count}</div>
<button class={message}>Click</button>
{#if count > 0}
  <p>Positive</p>
{/if}
