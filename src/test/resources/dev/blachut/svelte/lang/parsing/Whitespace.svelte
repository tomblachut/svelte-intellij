{#each assets as asset , i ( asset.id ) }{ /each }


{#eachassets as asset,i }{/ each}


{ #if a}
    <p>First branch</p>
{:elseif b}
    <p>Second branch</p>
{ :else }
    <p>Third branch</p>
{ /if }
