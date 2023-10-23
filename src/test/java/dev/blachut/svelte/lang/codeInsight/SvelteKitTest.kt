package dev.blachut.svelte.lang.codeInsight

import com.intellij.lang.javascript.inspections.JSUnusedGlobalSymbolsInspection
import com.intellij.lang.javascript.inspections.JSUnusedLocalSymbolsInspection
import com.intellij.lang.javascript.psi.ecma6.TypeScriptModule
import com.intellij.lang.javascript.psi.resolve.JSResolveResult
import com.intellij.psi.PsiPolyVariantReference
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import dev.blachut.svelte.lang.copyBundledSvelteKit
import dev.blachut.svelte.lang.getSvelteTestDataPath
import dev.blachut.svelte.lang.svelteKitPackageJson

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

  fun testImplicitlyUsedExportsDontApplyToSvelteFiles() {
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

  fun testImplicitlyUsedSnapshot() {
    myFixture.addFileToProject("package.json", svelteKitPackageJson)
    myFixture.enableInspections(JSUnusedLocalSymbolsInspection::class.java)
    myFixture.enableInspections(JSUnusedGlobalSymbolsInspection::class.java)

    myFixture.configureByText("+page.svelte", """
      <script lang="ts">
          let comment = '';

          export const snapshot = {
            capture: () => comment,
            restore: (value) => comment = value
          };
          
          export const shot = {
            <warning>capture</warning>: () => comment,
            <warning>restore</warning>: (value) => comment = value
          };
      </script>
    """.trimIndent())
    myFixture.testHighlighting()
  }

  fun testImplicitlyUsedLoadFunction() {
    myFixture.addFileToProject("package.json", svelteKitPackageJson)
    myFixture.enableInspections(JSUnusedLocalSymbolsInspection::class.java)
    myFixture.enableInspections(JSUnusedGlobalSymbolsInspection::class.java)

    myFixture.configureByText("+page.ts", """
      export async function load(event: any) {
        return { url: event.url };
      }
      
      export async function <warning>_load</warning>(event: any) {
        return { url: event.url };
      }
      
      async function <warning>loadNonExported</warning>(event: any) {
        return { url: event.url };
      }
    """.trimIndent())
    myFixture.testHighlighting()
  }

}
