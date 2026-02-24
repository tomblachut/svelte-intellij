<script lang="ts">
  interface Props {
    name: string;
  }
</script>

{#snippet greeting(props: Props)}
  <p>Hello, {(props as Props).name}!</p>
{/snippet}

{@render greeting({ name: 'World' } as Props)}
