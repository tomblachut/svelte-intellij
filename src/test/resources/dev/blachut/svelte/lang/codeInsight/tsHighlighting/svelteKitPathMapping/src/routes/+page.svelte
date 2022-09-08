<script lang="ts">
    import TestCounter from '$lib/TestCounter.svelte'
</script>

<svelte:head>
	<title>Home</title>
	<meta name="description" content="Svelte demo app" />
</svelte:head>

<section>
	<<error><warning>Cou<caret>nter</warning></error> />
	<TestCounter />
</section>