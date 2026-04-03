// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package dev.blachut.svelte.lang.service

import com.intellij.javascript.types.TSType
import com.intellij.lang.javascript.JSTestUtils
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.JSVariable
import com.intellij.lang.typescript.compiler.TypeScriptServiceHolder
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.TypeScriptTypeRequestKind
import com.intellij.lang.typescript.tsc.TypeScriptServiceGetElementTypeTest
import com.intellij.lang.typescript.tsc.TypeScriptServiceTestMixin
import com.intellij.lang.typescript.tsc.types.TypeScriptCompilerObjectTypeImpl
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.registry.RegistryManager
import com.intellij.psi.PsiElement
import dev.blachut.svelte.lang.SvelteTestModule
import dev.blachut.svelte.lang.configureSvelteDependencies
import dev.blachut.svelte.lang.service.settings.SvelteServiceMode
import dev.blachut.svelte.lang.service.settings.getSvelteServiceSettings
import org.junit.Test

class SvelteTypeScriptServiceGetElementTypeTest : TypeScriptServiceGetElementTypeTest() {

  override fun setUpTypeScriptService() {
    // Enable bundled typescript-svelte-plugin (required for SveltePluginTypeScriptService to start)
    RegistryManager.getInstance().get("svelte.language.server.bundled.enabled").setValue(true, testRootDisposable)

    // Enable Svelte service mode
    val serviceSettings = getSvelteServiceSettings(project)
    val oldMode = serviceSettings.serviceMode
    serviceSettings.serviceMode = SvelteServiceMode.ENABLED
    Disposer.register(testRootDisposable) { serviceSettings.serviceMode = oldMode }

    myFixture.configureSvelteDependencies(SvelteTestModule.SVELTE_5)
    TypeScriptServiceTestMixin.setUpTypeScriptService(myFixture) {
      it is SveltePluginTypeScriptService
    }
  }

  override fun calculateType(element: PsiElement, typeRequestKind: TypeScriptTypeRequestKind): JSType? {
    return super.calculateType(element, typeRequestKind).also {
      assertInstanceOf(TypeScriptServiceHolder.getForFile(project, file.virtualFile), SveltePluginTypeScriptService::class.java)
    }
  }

  override fun calculateTSType(element: PsiElement, typeRequestKind: TypeScriptTypeRequestKind): TSType? {
    return super.calculateTSType(element, typeRequestKind).also {
      assertInstanceOf(TypeScriptServiceHolder.getForFile(project, file.virtualFile), SveltePluginTypeScriptService::class.java)
    }
  }

  @Test
  fun testCompletePropsSvelte() {
    // Match the tsconfig used in SvelteServiceTestBase.addTypeScriptCommonFiles()
    myFixture.addFileToProject("tsconfig.json", """
      {
        "compilerOptions": {
          "allowJs": true,
          "checkJs": true,
          "strict": true,
          "esModuleInterop": true,
          "resolveJsonModule": true,
          "skipLibCheck": true
        },
        "include": ["**/*.js", "**/*.ts", "**/*.d.ts", "**/*.svelte"]
      }
    """.trimIndent())

    // Ambient declarations used in the original working test
    myFixture.addFileToProject("ambient.d.ts", """
      /// <reference types="svelte" />

      declare function __sveltets_2_invalidate<T>(getValue: () => T): T;
    """.trimIndent())

    myFixture.addFileToProject("Button.svelte", """
      <script lang="ts">
        export let label: string;
      </script>
      <button>{label}</button>
    """.trimIndent())

    myFixture.configureByText("usage.ts", """
      import Button from "./Button.svelte";
      const btn = new Button({target: document.body, props: {label: "hi"}});
    """.trimIndent())

    val element = JSTestUtils.findElementByText(myFixture, "btn", JSVariable::class.java)
    val type = calculateType(element)
    assertNotNull("Type should not be null", type)
    assertInstanceOf(type, TypeScriptCompilerObjectTypeImpl::class.java)
  }
}
