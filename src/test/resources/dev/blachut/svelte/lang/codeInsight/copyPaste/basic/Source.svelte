<script>
	import Counter from './Counter.svelte';
	import welcome from './svelte-welcome.webp';
	import welcome_fallback from './svelte-welcome.png';

	welcome_fallback
</script>

<svelte:head>
	<title>Home</title>
	<meta name="description" content="Svelte demo app" />
</svelte:head>

<section>
    <selection><span class="welcome">
        <picture>
            <source srcset={welcome} type="image/webp" />
            {welcome_fallback}
        </picture>
    </span>

	<Counter /></selection>
</section>