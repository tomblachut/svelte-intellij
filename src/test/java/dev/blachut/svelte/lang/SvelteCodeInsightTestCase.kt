// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package dev.blachut.svelte.lang

import com.intellij.javascript.testFramework.web.WebFrameworkTestCase
import com.intellij.polySymbols.testFramework.HybridTestMode

abstract class SvelteCodeInsightTestCase(
  override val testCasePath: String,
) : WebFrameworkTestCase(HybridTestMode.BasePlatform) {

  override val testDataRoot: String
    get() = getSvelteTestDataPath()

  override val defaultDependencies: Map<String, String> = emptyMap()

  override val defaultExtension: String
    get() = "svelte"
}
