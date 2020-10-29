package dev.blachut.svelte.lang.codeInsight

data class SvelteComponentDescriptor(
    val props: Map<String, String>,
    val events: Map<String, String>,
    val slots: Map<String, Map<String, String>>,
)
