package dev.blachut.svelte.lang.codeInsight

import com.intellij.lang.javascript.JSAbstractFindUsagesTest
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlTag
import com.intellij.testFramework.UsefulTestCase
import dev.blachut.svelte.lang.SvelteTestScenario
import dev.blachut.svelte.lang.configureBundledSvelte
import dev.blachut.svelte.lang.doTestWithLangFromTestNameSuffix
import dev.blachut.svelte.lang.getSvelteTestDataPath


class SvelteFindUsagesTest : JSAbstractFindUsagesTest() {
  override fun getBasePath(): String = ""
  override fun getTestDataPath(): String = getSvelteTestDataPath()

  fun testStoreImportedJS() = doTestWithLangFromTestNameSuffix(storeImported)

  fun testStoreImportedTS() = doTestWithLangFromTestNameSuffix(storeImported)

  private val storeImported = SvelteTestScenario { langExt, _ ->
    myFixture.configureBundledSvelte()
    val count = "\$count" // to trick Kotlin
    myFixture.configureByText("Foo.svelte", """
      <script lang="$langExt" module>
        import { count } from "./stores";

        count.set(5);
        console.log($count.toFixed()); // bug, todo prevent resolve
      </script>
      
      <script lang="$langExt">
        count.set(5);
        console.log($count.toFixed());
        
        $: $count.toFixed();
        
	      console.log({ $count }); // the highlight is missing, todo fix
      </script>

      <main>{$count}</main>
      <section>{count}</section>
    """.trimIndent())
    myFixture.configureByText("stores.$langExt", """
      import { writable } from "svelte/store";
    
      export const <caret>count = writable(5);
    """.trimIndent())
    doTest()
  }

  private fun doTest() {
    val usages = doFindUsages()
    UsefulTestCase.assertSize(8, usages) // 8 + 1 false positive - 1 false negative
  }

  fun testFindUsagesOfCssClassIncludesSvelteElement() {
    myFixture.configureByText("Foo.svelte", """
      <svelte:element this={'div'} class="my-class"></svelte:element>
      <style>
        .my-cl<caret>ass { color: red; }
      </style>
    """.trimIndent())
    val usages = myFixture.findUsages(myFixture.elementAtCaret)
    val svelteElementUsages = usages.filter { usage ->
      PsiTreeUtil.getParentOfType(usage.element, XmlTag::class.java)?.name == "svelte:element"
    }
    assertEquals("Expected exactly one usage inside <svelte:element>", 1, svelteElementUsages.size)
  }

  fun testFindUsagesOfCssClassInMultiClassAttribute() {
    myFixture.configureByText("Foo.svelte", """
      <svelte:element this={'div'} class="my-class other-class"></svelte:element>
      <style>
        .my-cl<caret>ass { color: red; }
        .other-class { color: blue; }
      </style>
    """.trimIndent())
    val usages = myFixture.findUsages(myFixture.elementAtCaret)
    val svelteElementUsages = usages.filter { usage ->
      PsiTreeUtil.getParentOfType(usage.element, XmlTag::class.java)?.name == "svelte:element"
    }
    assertEquals("Expected exactly one usage of .my-class (not .other-class)", 1, svelteElementUsages.size)
  }
}