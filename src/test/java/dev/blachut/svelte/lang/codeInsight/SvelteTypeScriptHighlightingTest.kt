package dev.blachut.svelte.lang.codeInsight

import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.vite.createAndSetViteConfig
import dev.blachut.svelte.lang.svelteCodeInsightFixture
import dev.blachut.svelte.lang.svelteProjectFixture
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInfo

@TestApplication
class SvelteTypeScriptHighlightingTest {
  companion object {
    private const val BASE_PATH = "dev/blachut/svelte/lang/codeInsight/tsHighlighting"
    private val projectFixture = svelteProjectFixture()
  }

  private val myFixture by svelteCodeInsightFixture(projectFixture)

  private lateinit var testName: String
  private lateinit var testNameLowercased: String

  @BeforeEach
  fun setUp(testInfo: TestInfo) {
    testName = PlatformTestUtil.getTestName(testInfo.testMethod.get().name, false)
    testNameLowercased = PlatformTestUtil.getTestName(testInfo.testMethod.get().name, true)
    myFixture.enableInspections(*configureDefaultLocalInspectionTools().toTypedArray())
  }

  @Test
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

  @Test
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

  @Test
  fun testTsconfigDirectAncestorDiscovery() {
    val base = BASE_PATH + "/" + testNameLowercased
    myFixture.configureByFiles("$base/src/App.svelte", "$base/tsconfig.json")

    myFixture.testHighlighting()
  }

  @Test
  fun testTsconfigIndirectAncestorDiscovery() {
    val base = BASE_PATH + "/" + testNameLowercased
    myFixture.configureByFiles("$base/src/nested/Child.svelte", "$base/tsconfig.json")

    myFixture.testHighlighting()
  }

  @Test
  fun testSvelteKitPathMapping() {
    myFixture.copyDirectoryToProject(BASE_PATH + "/" + testNameLowercased, "")
    myFixture.configureFromTempProjectFile("src/routes/+page.svelte")
    myFixture.testHighlighting()
    assertNotNull(myFixture.findSingleIntention("Insert 'import Counter from \"\$lib/Counter.svelte\"'"))
  }

  @Test
  fun testTsOverSvelte() {
    myFixture.copyDirectoryToProject(BASE_PATH + "/" + testNameLowercased, "")
    myFixture.configureFromTempProjectFile("+page.svelte")
    myFixture.testHighlighting()
  }

  @Test
  fun testViteAlias() {
    val root = myFixture.copyDirectoryToProject(BASE_PATH + "/" + testNameLowercased, "")
    createAndSetViteConfig(myFixture.project, myFixture.testRootDisposable, "aliasPath", "dir", null, root.path)
    myFixture.configureFromTempProjectFile(testName + ".svelte")
    myFixture.testHighlighting()
    assertNotNull(myFixture.findSingleIntention("Insert 'import Other from \"aliasPath/Other.svelte\"'"))
  }

  @Test
  fun testViteAliasWithExtension() {
    val root = myFixture.copyDirectoryToProject(BASE_PATH + "/" + testNameLowercased, "")
    createAndSetViteConfig(myFixture.project, myFixture.testRootDisposable, "aliasPath", "dir", listOf(".svelte"), root.path)
    myFixture.configureFromTempProjectFile(testName + ".svelte")
    myFixture.testHighlighting()
    assertNotNull(myFixture.findSingleIntention("Insert 'import Other from \"aliasPath/Other.svelte\"'"))
  }

}
