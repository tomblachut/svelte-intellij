// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package dev.blachut.svelte.lang.codeInsight

import com.intellij.lang.javascript.TrackFailedTestExtension
import com.intellij.lang.javascript.inspections.JSUnusedGlobalSymbolsInspection
import com.intellij.lang.javascript.inspections.JSUnusedLocalSymbolsInspection
import com.intellij.openapi.application.EDT
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.codeInsightFixture
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.moduleFixture
import com.intellij.testFramework.junit5.fixture.projectFixture
import com.intellij.testFramework.junit5.fixture.tempPathFixture
import dev.blachut.svelte.lang.configureBundledSvelte
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import kotlin.time.Duration.Companion.minutes

@TestApplication
@TestDataPath($$"$PROJECT_ROOT/plugins/svelte/src/test/resources/dev/blachut/svelte/lang/codeInsight/componentProps")
class SvelteComponentPropsImplicitUsageTest {
  private val tempDirFixture = tempPathFixture()
  private val projectFixture = projectFixture(tempDirFixture, openAfterCreation = true)
  private val moduleFixture = projectFixture.moduleFixture(tempDirFixture, addPathToSourceRoot = true)
  private val codeInsightFixture by codeInsightFixture(projectFixture, tempDirFixture)

  @BeforeEach
  fun setUp() {
    moduleFixture.get()
  }

  companion object {
    @JvmField
    @RegisterExtension
    val trackFailedTest: TrackFailedTestExtension = TrackFailedTestExtension(
      "a hoisted props object covers each props property",
      "new of any class keeps the report",
    )
  }

  @Test
  fun `mount covers each props property`() =
    doTest("mountProps")

  @Test
  fun `mount covers each props property with the option on`() =
    doTest("mountProps", reportUnusedProperties = true)

  @Test
  fun `hydrate covers each props property`() =
    doTest("hydrateProps")

  // Svelte 4 creates a component with `new`.
  @Test
  fun `new of a component covers each props property`() =
    doTest("newComponentProps")

  /**
   * onSelect is part of props, shouldn't be unused
   */
  @Test
  fun `a hoisted props object covers each props property`() =
    doTest("hoistedProps")

  /**
   * onSelect is effectively unused but there's no warning
   */
  @Test
  fun `new of any class keeps the report`() =
    doTest("newOfAnyClass")

  /**
   * Highlights `src/main.ts` of the project in the [project] directory of the test data.
   *
   * Each project holds the Svelte 5 typings, so the shape matches a real install. The type of `mount`
   * covers the option object of the call. It does not cover the props object of that option.
   */
  private fun doTest(
    project: String,
    reportUnusedProperties: Boolean = false,
  ) {
    timeoutRunBlocking(timeout = 1.minutes) {
      withContext(Dispatchers.EDT) {
        codeInsightFixture.copyDirectoryToProject(project, "")
        codeInsightFixture.configureBundledSvelte()
      }
      val globalSymbols = JSUnusedGlobalSymbolsInspection()
      globalSymbols.myReportUnusedProperties = reportUnusedProperties
      codeInsightFixture.enableInspections(JSUnusedLocalSymbolsInspection(), globalSymbols)
      codeInsightFixture.testHighlighting(true, false, true, "src/main.ts")
    }
  }
}
