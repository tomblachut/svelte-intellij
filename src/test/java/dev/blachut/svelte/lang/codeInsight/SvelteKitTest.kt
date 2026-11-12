package dev.blachut.svelte.lang.codeInsight

import com.intellij.lang.javascript.inspections.JSUnusedGlobalSymbolsInspection
import com.intellij.lang.javascript.inspections.JSUnusedLocalSymbolsInspection
import com.intellij.lang.javascript.psi.ecma6.TypeScriptModule
import com.intellij.lang.javascript.psi.resolve.JSResolveResult
import com.intellij.openapi.application.readAction
import com.intellij.psi.PsiPolyVariantReference
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import dev.blachut.svelte.lang.copyBundledSvelteKit
import dev.blachut.svelte.lang.svelteCodeInsightFixture
import dev.blachut.svelte.lang.svelteKitPackageJson
import dev.blachut.svelte.lang.svelteProjectFixture
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInfo

@TestApplication
class SvelteKitTest {
  companion object {
    private const val BASE_PATH = "dev/blachut/svelte/lang/codeInsight/svelteKit"
  }

  private val projectFixture = svelteProjectFixture()
  private val myFixture by svelteCodeInsightFixture(projectFixture)

  private lateinit var testNameLowercased: String

  @BeforeEach
  fun setUp(testInfo: TestInfo) {
    testNameLowercased = PlatformTestUtil.getTestName(testInfo.testMethod.get().name, true)
  }

  @Test
  fun testResolveAppModules() {
    val base = BASE_PATH + "/" + testNameLowercased
    myFixture.copyDirectoryToProject(base, "")
    myFixture.copyBundledSvelteKit()
    myFixture.configureFromTempProjectFile("src/routes/+page.svelte")

    timeoutRunBlocking { readAction {
      val ref = myFixture.getReferenceAtCaretPositionWithAssertion() as PsiPolyVariantReference

      assertInstanceOf(TypeScriptModule::class.java, JSResolveResult.resolve(ref.multiResolve(false)))
    } }
  }

  @Test
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

  @Test
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

  @Test
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

  @Test
  fun testSvelteKitUniversalHooks() {
    myFixture.addFileToProject("package.json", svelteKitPackageJson)
    myFixture.enableInspections(JSUnusedLocalSymbolsInspection::class.java)
    myFixture.enableInspections(JSUnusedGlobalSymbolsInspection::class.java)

    myFixture.configureByText("hooks.ts", """
      export function reroute({ url }: any) { return url.pathname; }
      export function transport() { return {}; }
      export function <warning descr="Unused function notAHook">notAHook</warning>() {}
    """.trimIndent())
    myFixture.testHighlighting()
  }

  @Test
  fun testSvelteKitServerHooks() {
    myFixture.addFileToProject("package.json", svelteKitPackageJson)
    myFixture.enableInspections(JSUnusedLocalSymbolsInspection::class.java)
    myFixture.enableInspections(JSUnusedGlobalSymbolsInspection::class.java)

    myFixture.configureByText("hooks.server.ts", """
      export function handle({ event, resolve }: any) { return resolve(event); }
      export function handleFetch({ request, fetch }: any) { return fetch(request); }
      export function handleError({ error }: any) { console.error(error); }
      export function handleValidationError({ message }: any) { return { message }; }
      export function init() { console.log('server init'); }
      export function <warning descr="Unused function notAHook">notAHook</warning>() {}
    """.trimIndent())
    myFixture.testHighlighting()
  }

}
