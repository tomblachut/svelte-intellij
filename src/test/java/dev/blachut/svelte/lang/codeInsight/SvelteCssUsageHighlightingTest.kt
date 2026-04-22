// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package dev.blachut.svelte.lang.codeInsight

import dev.blachut.svelte.lang.SvelteCodeInsightTestCase

class SvelteCssUsageHighlightingTest : SvelteCodeInsightTestCase("css/usageHighlighting") {

  fun testClassInStringLiteralExpression() = doUsageHighlightingTest()

  fun testClassInObjectPropertyKey() = doUsageHighlightingTest()

  fun testClassInArrayLiteral() = doUsageHighlightingTest()

  fun testClassInDirective() = doUsageHighlightingTest()
}
