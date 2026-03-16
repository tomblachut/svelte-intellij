// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package dev.blachut.svelte.lang.stubs

import com.intellij.psi.StubBuilder
import com.intellij.psi.stubs.DefaultStubBuilder
import com.intellij.psi.stubs.LanguageStubDefinition
import com.intellij.xml.HtmlLanguageStubVersionUtil
import dev.blachut.svelte.lang.psi.SvelteJSElementTypes

internal class SvelteLanguageStubDefinition : LanguageStubDefinition {
  override val stubVersion: Int
    get() = HtmlLanguageStubVersionUtil.getHtmlStubVersion() + SvelteJSElementTypes.STUB_VERSION

  override val builder: StubBuilder
    get() = DefaultStubBuilder()
}
