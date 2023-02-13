{#if true}
    {@const y = 3}
    {@const (y = 3)}
    {@const {x, y} = {x: 1, y: 3}}
    {@const ({x, y} = {x: 1, y: 3})}
    {@const y: TypesAreUnsupported = 3}
    {@const (y: TypesAreUnsupported = 3)}
    {@const onlyEqualsIsSupported += 3}
    {@const y = 3, commaIsUnsupported = 4}
    {@const (y = 3, commaIsUnsupported = 4)}
{/if}
