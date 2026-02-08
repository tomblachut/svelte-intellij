<script lang="ts" generics="T extends { text: string }">
  let {value}: { value: T } = $props();
</script>

<p>{value.text}</p>
