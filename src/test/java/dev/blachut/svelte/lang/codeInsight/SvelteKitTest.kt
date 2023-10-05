package dev.blachut.svelte.lang.codeInsight

import com.intellij.lang.javascript.inspections.JSUnusedGlobalSymbolsInspection
import com.intellij.lang.javascript.inspections.JSUnusedLocalSymbolsInspection
import com.intellij.lang.javascript.psi.ecma6.TypeScriptModule
import com.intellij.lang.javascript.psi.resolve.JSResolveResult
import com.intellij.psi.PsiPolyVariantReference
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import dev.blachut.svelte.lang.copyBundledSvelteKit
import dev.blachut.svelte.lang.getSvelteTestDataPath

class SvelteKitTest : BasePlatformTestCase() {
    override fun getTestDataPath(): String = getSvelteTestDataPath()
    override fun getBasePath(): String = "dev/blachut/svelte/lang/codeInsight/svelteKit"

    fun testResolveAppModules() {
        val base = basePath + "/" + getTestName(true)
        myFixture.copyDirectoryToProject(base, "")
        myFixture.copyBundledSvelteKit()
        myFixture.configureFromTempProjectFile("src/routes/+page.svelte")

        val ref = myFixture.getReferenceAtCaretPositionWithAssertion() as PsiPolyVariantReference

        assertInstanceOf(JSResolveResult.resolve(ref.multiResolve(false)), TypeScriptModule::class.java)
    }

    fun testImplicitlyUsedExportsDeosntApplyToSvelteFiles() {
        myFixture.enableInspections(JSUnusedLocalSymbolsInspection::class.java)
        myFixture.enableInspections(JSUnusedGlobalSymbolsInspection::class.java)

        myFixture.configureByText("index.svelte", """
            <script context="module" lang="ts">
                export const <warning>prerender</warning> = true;
            </script>
            <script lang="ts">
                export const <warning>hydrate</warning> = true;
            </script>
        """.trimIndent())
        myFixture.testHighlighting()
    }
}
