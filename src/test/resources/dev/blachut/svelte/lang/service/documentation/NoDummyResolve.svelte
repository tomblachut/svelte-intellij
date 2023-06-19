<script lang="ts">
  let unrelatedData = {
    unrelatedValue: 42
  };

  export let usedData: any;

  console.log(unrelatedData);
</script>

{usedData.<caret>unrelatedValue}
