<p>
Plain html
</p>

{#if visible}
<Box {name}/>
<ul>
{#each assets as {title} (a.id)}
<li>{title}</li>
{/each}
</ul>
{:else}
<p>Kappa</p>
{/if}

<ul>
{#each assets as a}
<li>{a}</li>
{/each}
</ul>
