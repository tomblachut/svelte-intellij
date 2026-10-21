package dev.blachut.svelte.lang.format

import com.intellij.openapi.project.Project
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.codeInsightModuleFixture
import com.intellij.psi.formatter.FormatterTestUtil
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.projectFixture
import dev.blachut.svelte.lang.getSvelteTestDataPath
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInfo
import java.io.File

@TestApplication
class SvelteFormatterTest {
  companion object {
    private val projectFixture = projectFixture(openAfterCreation = true)
  }

  private val myFixture by codeInsightModuleFixture(projectFixture)

  private val project: Project get() = myFixture.project

  private lateinit var testName: String

  @BeforeEach
  fun setUp(testInfo: TestInfo) {
    FormatterTestUtil.assertNoProjectCodeStyle(project)
    testName = PlatformTestUtil.getTestName(testInfo.testMethod.get().name, false)
  }

  private fun doTest() {
    FormatterTestUtil.doTest(project, getSvelteTestDataPath() + File.separatorChar + "dev/blachut/svelte/lang/format", "svelte", testName)
  }

  @Test
  fun testNoSvelteBlocks() = doTest()
  @Test
  fun testNestedBlocks() = doTest()
  @Test
  fun testNestedBlocksFlat() = doTest()
  @Test
  fun testIndentedExpressions() = doTest()
  @Test
  fun testOneLineBlock() = doTest()

  @Test
  fun testScriptContents() = doTest()
  @Test
  fun testScriptContentsSingleLine() = doTest()
  @Test
  fun testScriptStyleEmpty() = doTest()

  // WEB-77758: `//` and `/* */` comments inside start tags, see
  // https://github.com/sveltejs/svelte/pull/17671.
  // TagAttributes is the comment free baseline of TagComments: the two have to be indented alike.
  @Test
  fun testTagAttributes() = doTest()
  @Test
  fun testTagComments() = doTest()

  @Test
  fun testMultilineExpression() = doTest()
  fun todoMultilineProp() = doTest()

  //fun testScriptGenericsFormat() = doTest()
  //fun testScriptGenericsMultiline() = doTest()
  //fun testScriptGenericsNested() = doTest()
  //fun testScriptGenericsUnionIntersection() = doTest()
}
