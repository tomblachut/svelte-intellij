<p>
    Plain html
</p>

{#if visible}
    <Box {name}/>
    {#await promise then x}
        <p>{x}<p>
    {/await}
{:else}
    {@debug name}
    <p>Kappa</p>
{/if}

<ul>
    {#each assets as a}
        <li>{a}</li>
    {/each}
</ul>


