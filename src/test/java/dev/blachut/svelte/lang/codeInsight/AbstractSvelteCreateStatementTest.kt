package dev.blachut.svelte.lang.codeInsight

import com.intellij.openapi.project.Project
import com.intellij.testFramework.junit5.fixture.TestFixture
import dev.blachut.svelte.lang.svelteCodeInsightFixture

/**
 * The common tests of the create-statement intentions in a Svelte file.
 * Each concrete class passes its own [projectFixture] from `svelteProjectFixture()` in its companion,
 * so that the tests of the class share one project and one module.
 */
abstract class AbstractSvelteCreateStatementTest(projectFixture: TestFixture<Project>) {
    protected val myFixture by svelteCodeInsightFixture(projectFixture)

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
