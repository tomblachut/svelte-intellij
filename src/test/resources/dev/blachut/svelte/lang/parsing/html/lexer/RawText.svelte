<svelte:head>
  <title>This is <std>title</std> and { an + expression } within&nbsp;it</title>
  <title>{#if iffy} OK {/if}{#ife wrong}{/if}</title>
  <title>An {#if iffy}inside{/if} a text</title>
</svelte:head>
<div>
  <Title>This is <custom>title</custom> and { an + expression } within&nbsp;it</Title>
  <Title>{#if iffy} OK {/if}{#ife wrong}{/if}</Title>
  <Title>An {#if iffy}inside{/if} a text</Title>
</div>