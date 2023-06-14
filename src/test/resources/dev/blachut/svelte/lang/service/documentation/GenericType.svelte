<script lang="ts">
    class Box<T> {
        constructor(public value: T) {}
    }

    /**
     * docs have to be removed!
     */
    let local = new Box(323 as number | null);
</script>

{<caret>local.value}