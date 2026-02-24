<script lang="ts">
  interface User {
    name: string;
    email: string;
  }

  let user: User | undefined;
  let value: string | undefined;
  let count: number = 0;
</script>

<!-- Test bind directive with type assertion -->
<input bind:value={(user?.name as string | undefined)} />

<!-- Test bind with satisfies operator -->
<input bind:value={(value satisfies string | undefined)} />

<!-- Test on directive with typed event parameter -->
<button on:click={(e: MouseEvent) => console.log(e.clientX)}>
  Click me
</button>

<!-- Test on directive with type assertion in handler -->
<input on:input={(e: Event) => {
  const target = e.target as HTMLInputElement;
  console.log(target.value);
}} />

<!-- Test class directive with typed expression -->
<div class:active={(count as number) > 0}></div>

<!-- Test use directive with type assertion -->
<div use:action={(count as number)}></div>
