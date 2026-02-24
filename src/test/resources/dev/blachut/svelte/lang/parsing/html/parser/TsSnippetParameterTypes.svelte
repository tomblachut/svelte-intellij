<script lang="ts">
  interface User {
    name: string;
    age: number;
  }
</script>

{#snippet userCard(user: User, index: number)}
  <div>{user.name} - {index}</div>
{/snippet}

{#snippet header(title: string)}
  <h1>{title}</h1>
{/snippet}

{#snippet complex(data: User[], callback: (item: User) => void)}
  {#each data as item}
    <div on:click={() => callback(item)}>{item.name}</div>
  {/each}
{/snippet}

{@render userCard({ name: 'Alice', age: 30 }, 0)}
