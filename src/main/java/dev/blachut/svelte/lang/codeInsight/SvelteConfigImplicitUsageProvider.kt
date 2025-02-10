package dev.blachut.svelte.lang.codeInsight

import com.intellij.lang.javascript.inspections.JSConfigImplicitUsageProvider

private const val SVELTE_CONFIG_NAME = "svelte.config"

class SvelteConfigImplicitUsageProvider : JSConfigImplicitUsageProvider() {
  override val configNames: Set<String> = setOf(SVELTE_CONFIG_NAME)
}