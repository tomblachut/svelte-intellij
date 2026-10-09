// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package dev.blachut.svelte.lang.codeInsight

import com.intellij.javascript.testFramework.web.checkImports
import com.intellij.javascript.testFramework.web.moveFiles
import com.intellij.javascript.testFramework.web.renameFile
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import dev.blachut.svelte.lang.SvelteCodeInsightTestCase
import dev.blachut.svelte.lang.SvelteTestModule

/**
 * Moves or renames the files that `src/pages/index.svelte` imports, or moves the page itself.
 * Each test then checks the module name and the resolved file of each import in the page.
 * The `relativeImport` project has relative imports.
 * The `pathAlias` project adds the `@components` path alias and imports through it.
 * The `tsconfig.json` sets `allowJs`, as the Astro base config does, so the three framework tests check the same case.
 */
class SvelteFileRefactoringTest : SvelteCodeInsightTestCase("refactoring/file") {
  fun testMoveComponentRelativeImport() =
    doMoveComponentsTest("relativeImport",
                         "../components/sub/Hello.svelte -> src/components/sub/Hello.svelte",
                         "../components/sub/animals.js -> src/components/sub/animals.ts")

  fun testMoveComponentPathAlias() =
    doMoveComponentsTest("pathAlias",
                         "@components/sub/Hello.svelte -> src/components/sub/Hello.svelte",
                         "@components/sub/animals.js -> src/components/sub/animals.ts")

  fun testMovePageRelativeImport() =
    doMovePageTest("relativeImport",
                   "../../components/Hello.svelte -> src/components/Hello.svelte",
                   "../../components/animals.js -> src/components/animals.ts")

  fun testMovePagePathAlias() =
    doMovePageTest("pathAlias",
                   "@components/Hello.svelte -> src/components/Hello.svelte",
                   "@components/animals.js -> src/components/animals.ts")

  fun testRenameComponentRelativeImport() =
    doRenameComponentsTest("relativeImport",
                           "../components/Greeting.svelte -> src/components/Greeting.svelte",
                           "../components/pets.js -> src/components/pets.ts")

  fun testRenameComponentPathAlias() =
    doRenameComponentsTest("pathAlias",
                           "@components/Greeting.svelte -> src/components/Greeting.svelte",
                           "@components/pets.js -> src/components/pets.ts")

  private fun doMoveComponentsTest(project: String, vararg expectedImports: String) =
    doFileRefactoringTest(project, "src/pages/index.svelte", expectedImports) {
      moveFiles("src/components/sub", "src/components/Hello.svelte", "src/components/animals.ts")
    }

  private fun doMovePageTest(project: String, vararg expectedImports: String) =
    doFileRefactoringTest(project, "src/pages/blog/index.svelte", expectedImports) {
      moveFiles("src/pages/blog", "src/pages/index.svelte")
    }

  private fun doRenameComponentsTest(project: String, vararg expectedImports: String) =
    doFileRefactoringTest(project, "src/pages/index.svelte", expectedImports) {
      renameFile("src/components/Hello.svelte", "Greeting.svelte")
      renameFile("src/components/animals.ts", "pets.ts")
    }

  private fun doFileRefactoringTest(
    project: String,
    importingFile: String,
    expectedImports: Array<out String>,
    refactoring: CodeInsightTestFixture.() -> Unit,
  ) =
    doConfiguredTest(SvelteTestModule.SVELTE_5, dir = true, dirName = project, configureFileName = "src/pages/index.svelte") {
      refactoring()
      checkImports(importingFile, *expectedImports)
    }
}
