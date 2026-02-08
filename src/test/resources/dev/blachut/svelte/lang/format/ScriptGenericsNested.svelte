<script lang="ts" generics="T extends Array<{id:number,data:Record<string,unknown>}>,U extends Promise<Array<T>>">
  export let items: T;
  export let promise: U;
</script>
