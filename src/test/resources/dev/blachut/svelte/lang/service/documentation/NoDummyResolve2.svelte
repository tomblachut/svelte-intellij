<script lang="ts">
  let unrelatedData = {
    unrelatedValue: 42
  };

  let unrelatedData2 = {
    unrelatedValue: true
  };

  export let usedData: any;

  console.log(unrelatedData, unrelatedData2);
</script>

{usedData.<caret>unrelatedValue}
