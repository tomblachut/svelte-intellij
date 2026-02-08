<script lang="ts" generics="T">
  export let first: T;
</script>

<script lang="ts"
<error descr="Only one script tag can have the 'generics' attribute">generics</error>="U">
export let second: U;
</script>

<script lang="ts"
<error descr="Only one script tag can have the 'generics' attribute">generics</error>="V">
export let third: V;
</script>
