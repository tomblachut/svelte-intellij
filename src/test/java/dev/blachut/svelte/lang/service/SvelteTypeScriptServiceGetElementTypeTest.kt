// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package dev.blachut.svelte.lang.service

import com.intellij.lang.javascript.JSTestUtils
import com.intellij.lang.javascript.psi.JSVariable
import com.intellij.lang.typescript.tsc.types.TypeScriptCompilerObjectTypeImpl
import org.junit.Test

class SvelteTypeScriptServiceGetElementTypeTest : SvelteServiceTestBase() {

  override fun setUp() {
    super.setUp()
    addTypeScriptCommonFiles()
    SvelteGetElementTypeTestUtil.setUpSPTE(myFixture, project, testRootDisposable)
  }

  @Test
  fun testCompletePropsSvelte() {
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
    val type = SvelteGetElementTypeTestUtil.calculateTypeAndVerifyDeclarations(element)
    assertInstanceOf(type, TypeScriptCompilerObjectTypeImpl::class.java)
  }
}
