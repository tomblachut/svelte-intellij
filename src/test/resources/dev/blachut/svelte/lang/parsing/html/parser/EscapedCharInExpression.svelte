{#each items as item}
  <span>{item.replace(/\{/g, '(')}</span>
{/each}
