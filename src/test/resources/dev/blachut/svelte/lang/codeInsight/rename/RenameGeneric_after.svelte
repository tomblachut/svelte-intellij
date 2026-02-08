<script lang="ts" generics="Entity extends { id: number; data: string[] }">
  export let items: Entity[];
  export let map: (item: Entity) => Entity[];
</script>