<script lang="ts">
  let className: string = 'active';
  let isDisabled: boolean = false;
</script>

<button class={className as string}>Click</button>
<input disabled={isDisabled satisfies boolean} />
<div title={(className as string).toUpperCase()}>Test</div>
