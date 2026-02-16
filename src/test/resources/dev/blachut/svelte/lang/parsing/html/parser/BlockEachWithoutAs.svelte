{#each array}
<p>No item</p>
{/each}

{#each array, i}
<p>Index: {i}</p>
{/each}

{#each { length: 10 }, i}
<p>This will be repeated 10 times, index: {i}</p>
{/each}

{#each (a, b) as item}
<p>Parenthesized comma expression: {item}</p>
{/each}

{#each { length: 10 }, i (i)}
<p>Repeated with key: {i}</p>
{/each}
