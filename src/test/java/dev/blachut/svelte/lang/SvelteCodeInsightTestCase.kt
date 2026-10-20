// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package dev.blachut.svelte.lang

import com.intellij.javascript.testFramework.web.WebFrameworkFixture
import com.intellij.javascript.testFramework.web.webFrameworkFixture

abstract class SvelteCodeInsightTestCase(testCasePath: String) {

  protected val web: WebFrameworkFixture by webFrameworkFixture(
    testDataRoot = getSvelteTestDataPath(),
    testCasePath = testCasePath,
    defaultExtension = "svelte",
  )
}
