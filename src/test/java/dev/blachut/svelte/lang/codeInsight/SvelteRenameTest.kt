package dev.blachut.svelte.lang.codeInsight

import com.intellij.lang.javascript.JSAbstractRenameTest
import dev.blachut.svelte.lang.getSvelteTestDataPath

class SvelteRenameTest : JSAbstractRenameTest() {
  override fun getTestDataPath(): String = getSvelteTestDataPath() + "/dev/blachut/svelte/lang/codeInsight/rename"

  fun testStoreSubscriptionJS() {
    val name = getTestName(false)
    doTestForFilesWithCheckAll("betterCount", "$name.svelte", "$name.js")
  }

  fun testStoreSubscriptionTS() {
    val name = getTestName(false)
    doTestForFilesWithCheckAll("betterCount", "$name.svelte", "$name.ts")
  }

  fun testStoreDeclaration() {
    val name = getTestName(false)
    doTestForFilesWithCheckAll("betterCount", "$name.ts", "${name}JS.svelte", "${name}TS.svelte")
  }
}