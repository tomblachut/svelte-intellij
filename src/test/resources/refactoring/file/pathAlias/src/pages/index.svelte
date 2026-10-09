<script lang="ts">
  import Hello from "@components/Hello.svelte";
  import { items } from "@components/animals.js";
</script>

<Hello/>
<ul>
  {#each items as item}
    <li>{item}</li>
  {/each}
</ul>
