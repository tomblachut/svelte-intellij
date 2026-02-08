<script lang="ts" generics="<caret>T extends { id: number; data: string[] }">
  export let items: T[];
  export let map: (item: T) => T[];
</script>