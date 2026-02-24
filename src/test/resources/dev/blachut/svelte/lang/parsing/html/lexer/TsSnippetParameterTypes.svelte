<script lang="ts">
  interface User {
    name: string;
  }
</script>

{#snippet greet(user: User)}
  <p>Hello {user.name}</p>
{/snippet}
