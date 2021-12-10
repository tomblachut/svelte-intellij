package dev.blachut.svelte.lang.codeInsight

import com.intellij.testFramework.fixtures.BasePlatformTestCase

abstract class AbstractSvelteCreateStatementTest : BasePlatformTestCase() {
    abstract fun testScriptMissing()

    abstract fun testScriptCollapsed()

    abstract fun testScriptEmpty()

    abstract fun testScriptBlank()

    abstract fun testScriptNonEmpty()

    abstract fun testNonConventionalScriptOrder()

    abstract fun testInsideScriptStillWorksJS()

    abstract fun testInsideScriptStillWorksTS()

    abstract fun testScriptMissingWithAdjacentModuleScript()

    abstract fun testInsideModuleScriptWithAdjacentInstanceScript()

}
