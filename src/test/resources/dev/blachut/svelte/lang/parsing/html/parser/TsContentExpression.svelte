<script lang="ts">
  interface User {
    name: string;
    age: number;
  }
  let user: User = { name: 'Alice', age: 30 };
</script>

<div>{user.name as string}</div>
<div>{user.age satisfies number}</div>
<div>{(user as User).name}</div>
