<script lang="ts">
  let value: unknown = 'hello';
  let maybeItem: { name: string } | null = { name: 'first' };
  const promiseValue: Promise<{ name: string }> = Promise.resolve({ name: 'test' });
</script>

<!-- {#if} with satisfies inside parentheses -->
{#if ({ tag: "if" } satisfies TypeShape)}
  <p>if-satisfies</p>
{/if}

<!-- {#if} with type assertion inside parentheses -->
{#if (value as string)}
  <p>if-as-assertion</p>
{/if}

<!-- {#await} with then clause -->
{#await promiseValue then result}
  <p>{result.name}</p>
{/await}

<!-- on: directive with typed arrow function parameters -->
<button on:blur={(a: string, b: number) => `${a}${b}`} />

<!-- {@const} with type assertion -->
{@const x = (value as string)}

<!-- {@const} with non-null assertion -->
{@const constNonNull = maybeItem!}
