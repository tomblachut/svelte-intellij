<script lang="ts" generics="
  Row extends Record<string,unknown>,
  Key extends keyof Row=keyof Row,
  Id extends string|number=string
">
  export let items: Row[];
  export let key: Key;
  export let id: Id;
</script>
