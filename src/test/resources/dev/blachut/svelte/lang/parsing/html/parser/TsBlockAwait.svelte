<script lang="ts">
  interface User {
    name: string;
  }
  let promise: Promise<User> = Promise.resolve({ name: 'Alice' });
</script>

{#await (promise as Promise<User>)}
  <p>Loading...</p>
{:then user}
  <p>{user.name}</p>
{:catch error}
  <p>{(error as Error).message}</p>
{/await}
