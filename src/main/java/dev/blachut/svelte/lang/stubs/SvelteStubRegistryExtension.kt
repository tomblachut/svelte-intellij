package dev.blachut.svelte.lang.stubs

import com.intellij.lang.javascript.stubs.register
import com.intellij.lang.javascript.stubs.serializers.JSEmbeddedContentStubSerializer
import com.intellij.lang.javascript.stubs.serializers.JSFileStubSerializer
import com.intellij.psi.stubs.StubRegistry
import com.intellij.psi.stubs.StubRegistryExtension
import dev.blachut.svelte.lang.parsing.js.SVELTEJS_FILE
import dev.blachut.svelte.lang.parsing.ts.SVELTETS_FILE
import dev.blachut.svelte.lang.psi.SvelteJSElementTypes.EMBEDDED_CONTENT_MODULE
import dev.blachut.svelte.lang.psi.SvelteJSElementTypes.EMBEDDED_CONTENT_MODULE_TS

private class SvelteStubRegistryExtension : StubRegistryExtension {
  override fun register(registry: StubRegistry) {
    listOf(
      SVELTEJS_FILE,
      SVELTETS_FILE,
    ).forEach {
      registry.registerStubSerializer(it, JSFileStubSerializer(it.language))
    }

    listOf(
      SvelteParameterStubFactory(),
      SvelteParameterStubSerializer(),

      SvelteEmbeddedContentModuleStubFactory { EMBEDDED_CONTENT_MODULE },
      JSEmbeddedContentStubSerializer(EMBEDDED_CONTENT_MODULE),

      SvelteEmbeddedContentModuleStubFactory { EMBEDDED_CONTENT_MODULE_TS },
      JSEmbeddedContentStubSerializer(EMBEDDED_CONTENT_MODULE_TS),
    ).forEach(registry::register)
  }
}