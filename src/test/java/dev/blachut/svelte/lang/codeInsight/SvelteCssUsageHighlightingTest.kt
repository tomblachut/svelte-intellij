// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package dev.blachut.svelte.lang.codeInsight

import com.intellij.javascript.testFramework.web.WebFrameworkFixture
import com.intellij.testFramework.junit5.TestApplication
import dev.blachut.svelte.lang.svelteFixture
import org.junit.jupiter.api.Test

@TestApplication
class SvelteCssUsageHighlightingTest {

  private val web: WebFrameworkFixture by svelteFixture("css/usageHighlighting")

  @Test
  fun testClassInStringLiteralExpression() = web.doUsageHighlightingTest()

  @Test
  fun testClassInObjectPropertyKey() = web.doUsageHighlightingTest()

  @Test
  fun testClassInArrayLiteral() = web.doUsageHighlightingTest()

  @Test
  fun testClassInDirective() = web.doUsageHighlightingTest()
}
