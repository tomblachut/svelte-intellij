// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package dev.blachut.svelte.lang.codeInsight

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import dev.blachut.svelte.lang.getSvelteTestDataPath
import dev.blachut.svelte.lang.inspections.SvelteEmptyGenericsInspection
import dev.blachut.svelte.lang.inspections.SvelteGenericsOnInstanceScriptOnlyInspection
import dev.blachut.svelte.lang.inspections.SvelteGenericsRequiresTypeScriptInspection
import dev.blachut.svelte.lang.inspections.SvelteMultipleGenericsScriptsInspection

/**
 * Tests for inspections related to the `generics` attribute on script tags.
 */
class SvelteGenericsInspectionTest : BasePlatformTestCase() {
  override fun getTestDataPath(): String = "$svelteTestDataPath/dev/blachut/svelte/lang/codeInsight/inspections/generics"

  override fun setUp() {
    super.setUp()
    myFixture.enableInspections(
      SvelteGenericsRequiresTypeScriptInspection(),
      SvelteGenericsOnInstanceScriptOnlyInspection(),
      SvelteMultipleGenericsScriptsInspection(),
      SvelteEmptyGenericsInspection()
    )
  }

  private val svelteTestDataPath: String
    get() = getSvelteTestDataPath()

  // File-based tests for SvelteEmptyGenericsInspection
  fun testEmptyGenerics() = doTest()
  fun testWhitespaceOnlyGenerics() = doTest()

  // File-based tests for SvelteGenericsRequiresTypeScriptInspection
  fun testGenericsRequiresTypeScript() = doTest()
  fun testGenericsWithWrongLang() = doTest()

  // File-based tests for SvelteGenericsOnInstanceScriptOnlyInspection
  fun testGenericsOnModuleScript() = doTest()

  // File-based tests for SvelteMultipleGenericsScriptsInspection
  fun testMultipleGenericsScripts() = doTest()
  fun testThreeGenericsScripts() = doTest()

  // Valid cases (no errors expected)
  fun testValidGenerics() = doTest()
  fun testValidGenericsWithModuleScript() = doTest()

  private fun doTest() {
    myFixture.testHighlighting("${getTestName(false)}.svelte")
  }

  // Quick fix tests - require inline testing for intention verification

  fun testGenericsRequiresTypeScriptQuickFix() {
    myFixture.configureByText("Test.svelte", """
      <script generics="T">
        export let value;
      </script>
    """.trimIndent())

    val intention = myFixture.findSingleIntention("Add lang=\"ts\" to script tag")
    myFixture.launchAction(intention)

    myFixture.checkResult("""
      <script generics="T" lang="ts">
        export let value;
      </script>
    """.trimIndent())
  }

  fun testGenericsQuickFixUpdatesExistingLang() {
    myFixture.configureByText("Test.svelte", """
      <script lang="js" generics="T">
        export let value;
      </script>
    """.trimIndent())

    val intention = myFixture.findSingleIntention("Add lang=\"ts\" to script tag")
    myFixture.launchAction(intention)

    myFixture.checkResult("""
      <script lang="ts" generics="T">
        export let value;
      </script>
    """.trimIndent())
  }

  fun testRemoveGenericsFromModuleScriptQuickFix() {
    myFixture.configureByText("Test.svelte", """
      <script context="module" lang="ts" generics="T">
        export const shared = 'value';
      </script>
    """.trimIndent())

    val intention = myFixture.findSingleIntention("Remove generics attribute")
    myFixture.launchAction(intention)

    myFixture.checkResult("""
      <script context="module" lang="ts">
        export const shared = 'value';
      </script>
    """.trimIndent())
  }

  fun testRemoveEmptyGenericsQuickFix() {
    myFixture.configureByText("Test.svelte", """
      <script lang="ts" generics="">
        export let value;
      </script>
    """.trimIndent())

    myFixture.doHighlighting()
    val quickFix = myFixture.getAllQuickFixes().find { it.text == "Remove empty generics attribute" }
    assertNotNull("Quick fix 'Remove empty generics attribute' should be available", quickFix)
    myFixture.launchAction(quickFix!!)

    myFixture.checkResult("""
      <script lang="ts">
        export let value;
      </script>
    """.trimIndent())
  }
}
