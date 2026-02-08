<script lang="ts" generics="T extends Record<string, unknown>, K extends keyof T">
  let key: K;
  let value: T[K];
</script>
