{#if true}
  {@const a = (() => {
    const r = 'H"e"l"l"o'.replace(/"/g, "");
    return r;
  })()}
  <div>{a}</div>
{/if}
