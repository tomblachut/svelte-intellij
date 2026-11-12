// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package dev.blachut.svelte.lang.codeInsight

import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.junit5.TestApplication
import dev.blachut.svelte.lang.getSvelteTestDataPath
import dev.blachut.svelte.lang.inspections.SvelteEmptyGenericsInspection
import dev.blachut.svelte.lang.inspections.SvelteGenericsOnInstanceScriptOnlyInspection
import dev.blachut.svelte.lang.inspections.SvelteGenericsRequiresTypeScriptInspection
import dev.blachut.svelte.lang.svelteCodeInsightFixture
import dev.blachut.svelte.lang.svelteProjectFixture
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInfo


/**
 * Tests for inspections related to the `generics` attribute on script tags.
 */
@TestApplication
class SvelteGenericsInspectionTest {
  companion object {
    private val projectFixture = svelteProjectFixture()
  }

  private val svelteTestDataPath: String
    get() = getSvelteTestDataPath()

  private val myFixture by svelteCodeInsightFixture(projectFixture, "$svelteTestDataPath/dev/blachut/svelte/lang/codeInsight/inspections/generics")

  private lateinit var testName: String

  @BeforeEach
  fun setUp(testInfo: TestInfo) {
    testName = PlatformTestUtil.getTestName(testInfo.testMethod.get().name, false)
    myFixture.enableInspections(
      SvelteGenericsRequiresTypeScriptInspection(),
      SvelteGenericsOnInstanceScriptOnlyInspection(),
      SvelteEmptyGenericsInspection()
    )
  }

  // File-based tests for SvelteEmptyGenericsInspection
  @Test
  fun testEmptyGenerics(): Unit = doTest()
  @Test
  fun testWhitespaceOnlyGenerics(): Unit = doTest()

  // File-based tests for SvelteGenericsRequiresTypeScriptInspection
  @Test
  fun testGenericsRequiresTypeScript(): Unit = doTest()
  @Test
  fun testGenericsWithWrongLang(): Unit = doTest()

  // File-based tests for SvelteGenericsOnInstanceScriptOnlyInspection
  @Test
  fun testGenericsOnModuleScript(): Unit = doTest()

  // Valid cases (no errors expected)
  @Test
  fun testValidGenerics(): Unit = doTest()
  @Test
  fun testValidGenericsWithModuleScript(): Unit = doTest()

  private fun doTest() {
    myFixture.testHighlighting("$testName.svelte")
  }

  // Quick fix tests - require inline testing for intention verification

  @Test
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

  @Test
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

  @Test
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

  @Test
  fun testRemoveEmptyGenericsQuickFix() {
    myFixture.configureByText("Test.svelte", """
      <script lang="ts" generics="">
        export let value;
      </script>
    """.trimIndent())

    myFixture.doHighlighting()
    val quickFix = myFixture.getAllQuickFixes().find { it.text == "Remove empty generics attribute" }
    assertNotNull(quickFix, "Quick fix 'Remove empty generics attribute' should be available")
    myFixture.launchAction(quickFix!!)

    myFixture.checkResult("""
      <script lang="ts">
        export let value;
      </script>
    """.trimIndent())
  }
}
