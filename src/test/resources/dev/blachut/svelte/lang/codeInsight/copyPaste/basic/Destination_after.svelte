<script>
import welcome from "./svelte-welcome.webp";
import welcome_fallback from "./svelte-welcome.png";
import Counter from "./Counter.svelte";
</script>

<span class="welcome">
        <picture>
            <source srcset={welcome} type="image/webp" />
            {welcome_fallback}
        </picture>
    </span>

<Counter /><caret>