<script lang="ts"
        generics="T extends { name: string } & { id: number },U extends string | number | boolean,V extends (string & {
            length: 10
        }) | (number & { value: 5 })">
  export let item: T;
  export let value: U;
  export let complex: V;
</script>
