<script lang="ts" generics="Clearable extends boolean, ValueType extends string | number | null">
  interface Props {
    clearable?: Clearable;
    value?: ValueType;
  }

  let {clearable, value}: Props = $props();
</script>
