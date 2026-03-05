// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package dev.blachut.svelte.lang.stubs

import com.intellij.psi.PsiFile
import com.intellij.psi.StubBuilder
import com.intellij.psi.stubs.DefaultStubBuilder
import com.intellij.psi.stubs.LanguageStubDefinition
import com.intellij.psi.stubs.StubElement
import com.intellij.xml.HtmlLanguageStubVersionUtil
import dev.blachut.svelte.lang.psi.SvelteFileStub
import dev.blachut.svelte.lang.psi.SvelteHtmlFile
import dev.blachut.svelte.lang.psi.SvelteJSElementTypes

internal class SvelteLanguageStubDefinition : LanguageStubDefinition {
  companion object {
    private const val SVELTE_STUB_VERSION: Int = 1 // +1 for lang mode support
  }

  override val stubVersion: Int
    get() = HtmlLanguageStubVersionUtil.getHtmlStubVersion() + SvelteJSElementTypes.STUB_VERSION + SVELTE_STUB_VERSION

  override val builder: StubBuilder
    get() = object : DefaultStubBuilder() {
      override fun createStubForFile(file: PsiFile): StubElement<*> {
        return if (file is SvelteHtmlFile) SvelteFileStub(file) else super.createStubForFile(file)
      }
    }
}
