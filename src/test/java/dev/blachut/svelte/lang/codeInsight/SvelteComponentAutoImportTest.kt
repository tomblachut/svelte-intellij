package dev.blachut.svelte.lang.codeInsight

import com.intellij.lang.javascript.JSTestUtils
import com.intellij.lang.javascript.modules.JSSymlinksImportFixture.Companion.jsSymlinksImportFixture
import com.intellij.lang.javascript.modules.JSTempDirFixtureOptions
import com.intellij.openapi.application.runReadActionBlocking
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.runInEdtAndGet
import dev.blachut.svelte.lang.getSvelteTestDataPath
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@TestApplication
class SvelteComponentAutoImportTest {
  private val symlinks by jsSymlinksImportFixture(
    JSTempDirFixtureOptions(getSvelteTestDataPath() + "/dev/blachut/svelte/lang/codeInsight/autoImport/"))

  @BeforeEach
  fun enableSvelteInspections() {
    symlinks.fixture.enableInspections(*configureDefaultLocalInspectionTools().toTypedArray())
  }

  @Test
  fun testAutoImportFromWorkspaceDependency() {
    configureWorkspaceProject()

    findAndInvokeSingleImportAction()

    assertTrue(runReadActionBlocking { symlinks.fixture.file.text }.contains("import WorkspaceButton from"))
    symlinks.fixture.checkHighlighting(false, false, false)
    assertComponentNavigationTarget("apps/a/WorkspaceButton.svelte")
  }

  @Test
  fun testNoAutoImportFromUndeclaredWorkspaceDependency() {
    configureWorkspaceProject(hasDependency = false)

    symlinks.checkImportText()
  }

  @Test
  fun testAutoImportFromTypeScriptProjectReference() {
    configureTypeScriptProject()

    findAndInvokeSingleImportAction()

    assertTrue(runReadActionBlocking { symlinks.fixture.file.text }.contains("import WorkspaceButton from"))
    symlinks.fixture.checkHighlighting(false, false, false)
    assertComponentNavigationTarget("packages/ui-lib/src/WorkspaceButton.svelte")
  }

  @Test
  fun testAutoImportFromTypeScriptIncludedDirectory() {
    configureIncludedTypeScriptProject()

    findAndInvokeSingleImportAction()

    assertTrue(runReadActionBlocking { symlinks.fixture.file.text }
                 .contains("import WorkspaceButton from \"../../shared/WorkspaceButton.svelte\";"))
    symlinks.fixture.checkHighlighting(false, false, false)
    assertComponentNavigationTarget("shared/WorkspaceButton.svelte")
  }

  private fun configureWorkspaceProject(hasDependency: Boolean = true) {
    symlinks.monorepoStructure(
      testDirectory = copyProject("workspaceAutoImport"),
    ) {
      root {
        dependOn("apps/a")
        dependOn("apps/dependsOnA")
        dependOn("apps/notDependsOnA")
      }
    }
    symlinks.fixture.configureFromTempProjectFile(if (hasDependency) "apps/dependsOnA/Usage.svelte" else "apps/notDependsOnA/Usage.svelte")
  }

  private fun configureTypeScriptProject() {
    symlinks.monorepoStructure(
      testDirectory = copyProject("typeScriptProjectReferenceAutoImport"),
    ) {
      module("packages/consumer") {
        dependOn("packages/ui-lib")
      }
    }
    symlinks.fixture.configureFromTempProjectFile("packages/consumer/src/Usage.svelte")
  }

  private fun configureIncludedTypeScriptProject() {
    copyProject("typeScriptIncludedDirectoryAutoImport")
    symlinks.fixture.configureFromTempProjectFile("app/src/Usage.svelte")
  }

  private fun assertComponentNavigationTarget(pathSuffix: String) {
    val targetPath = runInEdtAndGet { JSTestUtils.getGotoDeclarationTarget(symlinks.fixture)?.containingFile?.virtualFile?.path }
    assertNotNull(targetPath)
    assertTrue(targetPath!!.endsWith(pathSuffix))
  }

  private fun findAndInvokeSingleImportAction() {
    val actions = symlinks.fixture.availableIntentions.filter { it.text.startsWith("Insert '") && it.text.contains("import ") }
    assertEquals(1, actions.size, "Expected a single import action, but got: ${actions.joinToString { it.text }}")
    symlinks.fixture.launchAction(actions.single())
  }

  private fun copyProject(name: String): VirtualFile {
    return symlinks.fixture.tempDirFixture.copyAll(symlinks.testDataPath + name, "")
  }
}
