<script lang="ts">
  interface Props {
    class: string;
    disabled: boolean;
  }
  let props: Props = { class: 'btn', disabled: false };
</script>

<button {...(props as Props)}>Click</button>
<input {...props satisfies Props} />
