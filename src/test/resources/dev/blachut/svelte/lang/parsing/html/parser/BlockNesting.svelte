<p>Text</p>

{#if visible}
    {#await promise then x}
        <p>{@html x}</p>
    {/await}
{:else}
    {@debug name}
    <p>Kappa</p>
{/if}

<ul>
    {#each assets as a}{a}{/each}
</ul>


