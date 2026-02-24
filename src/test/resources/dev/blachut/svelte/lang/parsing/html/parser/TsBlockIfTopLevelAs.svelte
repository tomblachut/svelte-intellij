<script lang="ts">
  let value: unknown = 'hello';
  interface Item { name: string; }
  let promiseValue: Promise<Item> = Promise.resolve({ name: 'test' });
</script>

<!-- top-level 'as' in {#if} works as TypeScript type assertion -->
{#if value as string}
  <p>if-top-level-as-assertion</p>
{/if}

<!-- 'as' inside parens must work as TypeScript assertion -->
{#if (value as string)}
  <p>parens-assertion-works</p>
{/if}

<!-- TS assertion in {#await} expression works with parens -->
{#await (promiseValue as Promise<Item>)}
  <p>loading</p>
{:then result}
  <p>{result.name}</p>
{/await}

<!-- {#await} with inline 'then' uses 'then' keyword, not 'as' -->
{#await promiseValue then result}
  <p>{result.name}</p>
{/await}
