<script>
  let jsVar = 'plain js';
</script>

<script lang="ts">
  let tsVar: string = 'typescript';
</script>

<div>{tsVar as string}</div>
<div>{jsVar}</div>
