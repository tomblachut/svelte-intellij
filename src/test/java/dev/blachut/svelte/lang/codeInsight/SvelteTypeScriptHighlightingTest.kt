package dev.blachut.svelte.lang.codeInsight

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.vite.createAndSetViteConfig
import dev.blachut.svelte.lang.getSvelteTestDataPath
import junit.framework.TestCase

class SvelteTypeScriptHighlightingTest : BasePlatformTestCase() {
  override fun getTestDataPath(): String = getSvelteTestDataPath()
  override fun getBasePath(): String = "dev/blachut/svelte/lang/codeInsight/tsHighlighting"

  override fun setUp() {
    super.setUp()
    myFixture.enableInspections(*SvelteHighlightingTest.configureDefaultLocalInspectionTools().toTypedArray())
  }

  fun testSimpleWithType() {
    myFixture.configureByText("Usage.svelte", """
      <script lang="ts">
        export let testName: string;
        console.log(${"$$"}props);
        declare let test:number;
        if (test) {
            test = <error>"hello"</error>;
        } else {
            test = 2;
        }
        console.log(test);
      </script>
      <title>{testName}</title>
    """.trimIndent())
    myFixture.testHighlighting()
  }

  fun testStoreImport() {
    myFixture.configureByText("hello.ts", "export const count = 1")
    myFixture.configureByText("Usage.svelte", """
    <script lang="ts">
      import {count} from "./hello";

      $: ${"$"}count && console.log('changed');
    </script>
    <title>test</title>
    """.trimIndent())
    myFixture.testHighlighting()
  }

  fun testTsconfigDirectAncestorDiscovery() {
    val base = basePath + "/" + getTestName(true)
    myFixture.configureByFiles("$base/src/App.svelte", "$base/tsconfig.json")

    myFixture.testHighlighting()
  }

  fun testTsconfigIndirectAncestorDiscovery() {
    val base = basePath + "/" + getTestName(true)
    myFixture.configureByFiles("$base/src/nested/Child.svelte", "$base/tsconfig.json")

    myFixture.testHighlighting()
  }

  fun testSvelteKitPathMapping() {
    myFixture.copyDirectoryToProject(basePath + "/" + getTestName(true), "")
    myFixture.configureFromTempProjectFile("src/routes/+page.svelte")
    myFixture.testHighlighting()
    TestCase.assertNotNull(myFixture.findSingleIntention("Insert 'import Counter from \"\$lib/Counter.svelte\"'"))
  }

  fun testTsOverSvelte() {
    myFixture.copyDirectoryToProject(basePath + "/" + getTestName(true), "")
    myFixture.configureFromTempProjectFile("+page.svelte")
    myFixture.testHighlighting()
  }

  fun testViteAlias() {
    val root = myFixture.copyDirectoryToProject(basePath + "/" + getTestName(true), "")
    createAndSetViteConfig(project, testRootDisposable, "aliasPath", "dir", null, root.path)
    myFixture.configureFromTempProjectFile(getTestName(false) + ".svelte")
    myFixture.testHighlighting()
    TestCase.assertNotNull(myFixture.findSingleIntention("Insert 'import Other from \"aliasPath/Other.svelte\"'"))
  }

}
