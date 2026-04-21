package dev.blachut.svelte.lang.codeInsight

import com.intellij.lang.javascript.JSTestUtils
import com.intellij.lang.javascript.modules.JSSymlinksImportTestBase
import com.intellij.openapi.vfs.VirtualFile
import dev.blachut.svelte.lang.getSvelteTestDataPath

class SvelteComponentAutoImportTest : JSSymlinksImportTestBase() {
  override fun getBasePath(): String = getSvelteTestDataPath() + "/dev/blachut/svelte/lang/codeInsight/autoImport/"

  override fun setUp() {
    super.setUp()
    myFixture.enableInspections(*SvelteHighlightingTest.configureDefaultLocalInspectionTools().toTypedArray())
  }

  fun testAutoImportFromWorkspaceDependency() {
    configureWorkspaceProject()

    findAndInvokeSingleImportAction()

    assertTrue(myFixture.file.text.contains("import WorkspaceButton from"))
    myFixture.checkHighlighting(false, false, false)
    assertComponentNavigationTarget("apps/a/WorkspaceButton.svelte")
  }

  fun testNoAutoImportFromUndeclaredWorkspaceDependency() {
    configureWorkspaceProject(hasDependency = false)

    checkImportText()
  }

  fun testAutoImportFromTypeScriptProjectReference() {
    configureTypeScriptProject()

    findAndInvokeSingleImportAction()

    assertTrue(myFixture.file.text.contains("import WorkspaceButton from"))
    myFixture.checkHighlighting(false, false, false)
    assertComponentNavigationTarget("packages/ui-lib/src/WorkspaceButton.svelte")
  }

  fun testAutoImportFromTypeScriptIncludedDirectory() {
    configureIncludedTypeScriptProject()

    findAndInvokeSingleImportAction()

    assertTrue(myFixture.file.text.contains("import WorkspaceButton from \"../../shared/WorkspaceButton.svelte\";"))
    myFixture.checkHighlighting(false, false, false)
    assertComponentNavigationTarget("shared/WorkspaceButton.svelte")
  }

  private fun configureWorkspaceProject(hasDependency: Boolean = true) {
    monorepoStructure(
      testDirectory = copyProject("workspaceAutoImport"),
    ) {
      root {
        dependOn("apps/a")
        dependOn("apps/dependsOnA")
        dependOn("apps/notDependsOnA")
      }
    }
    myFixture.configureFromTempProjectFile(if (hasDependency) "apps/dependsOnA/Usage.svelte" else "apps/notDependsOnA/Usage.svelte")
  }

  private fun configureTypeScriptProject() {
    monorepoStructure(
      testDirectory = copyProject("typeScriptProjectReferenceAutoImport"),
    ) {
      module("packages/consumer") {
        dependOn("packages/ui-lib")
      }
    }
    myFixture.configureFromTempProjectFile("packages/consumer/src/Usage.svelte")
  }

  private fun configureIncludedTypeScriptProject() {
    copyProject("typeScriptIncludedDirectoryAutoImport")
    myFixture.configureFromTempProjectFile("app/src/Usage.svelte")
  }

  private fun assertComponentNavigationTarget(pathSuffix: String) {
    val target = JSTestUtils.getGotoDeclarationTarget(myFixture)
    assertNotNull(target)
    assertTrue(target!!.containingFile.virtualFile.path.endsWith(pathSuffix))
  }

  private fun findAndInvokeSingleImportAction() {
    val actions = myFixture.availableIntentions.filter { it.text.startsWith("Insert '") && it.text.contains("import ") }
    assertEquals("Expected a single import action, but got: ${actions.joinToString { it.text }}", 1, actions.size)
    myFixture.launchAction(actions.single())
  }

  private fun copyProject(name: String): VirtualFile {
    return myFixture.tempDirFixture.copyAll(basePath + name, "")
  }
}
