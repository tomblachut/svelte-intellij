package dev.blachut.svelte.lang.service

import com.intellij.lang.javascript.typescript.service.TypeScriptServiceTestBase
import com.intellij.platform.lsp.tests.checkLspHighlighting
import dev.blachut.svelte.lang.checkCompletionContains
import dev.blachut.svelte.lang.getRelativeSvelteTestDataPath
import org.junit.Test

class SvelteServiceCompletionTest : SvelteServiceTestBase() {
  override fun getBasePath(): String = getRelativeSvelteTestDataPath() + "/dev/blachut/svelte/lang/service/completion"

  @Test
  fun testKitDataProp() {
    defaultCompletionTest(directory = true)
    val lookupElements = myFixture.completeBasic()
    lookupElements.checkCompletionContains("somePost", "uniqueName")
  }

  @Test
  fun testPropertiesOnNullableReferences() { // WEB-63103
    myFixture.addFileToProject("tsconfig.json", tsconfig)

    myFixture.addFileToProject("api.ts", """
      declare const uniqueSymbol: unique symbol;
      
      interface UniqueInterface {
        readonly [uniqueSymbol]: unknown;
      }
      
      interface ActionFailure<T extends Record<string, unknown> | undefined = undefined>
        extends UniqueInterface {
        status: number;
        data: T;
      }
      
      type OptionalUnion<
        U extends Record<string, any>, // not unknown, else interfaces don't satisfy this constraint
        A extends keyof U = U extends U ? keyof U : never
      > = U extends unknown ? { [P in Exclude<A, keyof U>]?: never } & U : never;
      
      type UnpackValidationError<T> = T extends ActionFailure<infer X>
        ? X
        : T extends void
          ? undefined // needs to be undefined, because void will corrupt union type
          : T;
      
      export type Expand<T> = T extends infer O ? { [K in keyof O]: O[K] } : never;
      
      type AwaitedActions<T extends Record<string, (...args: any) => any>> = OptionalUnion<
        {
          [Key in keyof T]: UnpackValidationError<Awaited<ReturnType<T[Key]>>>;
        }[keyof T]
      >;
      
      const actions = {
        submit: async () => {
          const data = {};
          return { success: true, data };
        }
      };
      
      export type ActionData = Expand<AwaitedActions<typeof actions>> | null;
    """.trimIndent())

    myFixture.configureByText("Usage.svelte", """
      <script lang="ts">
        import type { ActionData } from './api';
      
        export let form: ActionData;
      
        <error>form</error>.<caret><error>d</error>;
      </script>
    """.trimIndent())

    myFixture.checkLspHighlighting()
    assertCorrectService()

    val elements = myFixture.completeBasic()
    myFixture.type('\t')

    // todo missing support for additionalTextEdits in LspCompletionSupport, replace with below
    myFixture.checkResult("""
      <script lang="ts">
        import type { ActionData } from './api';
      
        export let form: ActionData;
      
        form?.data;
      </script>
    """.trimIndent(), true)
    TypeScriptServiceTestBase.assertHasServiceItems(elements, true)
  }

  private fun defaultCompletionTest(directory: Boolean = false) {
    configureDefault(directory)

    myFixture.checkLspHighlighting()
    assertCorrectService()
  }
}
