// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package dev.blachut.svelte.lang.service

import com.intellij.lang.javascript.JSNavigationTest
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.mock.MockDocument
import com.intellij.openapi.Disposable
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.util.registry.RegistryManager
import com.intellij.platform.lsp.tests.checkLspHighlighting
import com.intellij.platform.lsp.tests.waitForDiagnosticsFromLspServer
import com.intellij.testFramework.ExpectedHighlightingData
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl
import dev.blachut.svelte.lang.codeInsight.SvelteHighlightingTest
import dev.blachut.svelte.lang.service.settings.getSvelteServiceSettings
import junit.framework.TestCase
import org.junit.Test

class SvelteServiceTest : SvelteServiceTestBase() {
  override fun setUp() {
    super.setUp()
    myFixture.enableInspections(*SvelteHighlightingTest.configureDefaultLocalInspectionTools().toTypedArray())
  }

  @Test
  fun testServiceWorks() {
    addTypeScriptCommonFiles()
    myFixture.configureByText("Hello.svelte", """
      <script lang="ts">
        let <error descr="Svelte: Type 'number' is not assignable to type 'string'.">local</error>: string = 1;
        local;
        
        function acceptNumber(num: number): number { return num; }
        
        acceptNumber(<error descr="Svelte: Argument of type 'boolean' is not assignable to parameter of type 'number'.">true</error>);
      </script>
      
      <!-- todo remove duplicate internal warning -->
      {acceptNumber(<error descr="Svelte: Argument of type 'boolean' is not assignable to parameter of type 'number'.">true</error>)}
      
      <input <warning descr="Svelte: A11y: Avoid using autofocus">autofocus</warning>>
    """.trimIndent())
    myFixture.checkLspHighlighting()
    assertCorrectService()
  }

  @Test
  fun testSyntaxError() {
    // This test checks if we properly process Diagnostics without code.
    // It's important because exceptions in our highlighting are swallowed but appear to affect performance.
    // Would be good to actually completely hide this annotation, but then I'm not sure how to still verify the above.
    addTypeScriptCommonFiles()
    myFixture.configureByText("Hello.svelte", """
      <script lang="ts"><EOLError descr="Svelte: [svelte-preprocess] Encountered type error"></EOLError>
        let hello = "hello"<error descr="Svelte: ',' expected."><error descr="Newline or semicolon expected">w</error>rong</error>;
        console.log(hello);
      </script>
    """.trimIndent())
    myFixture.checkLspHighlighting()
    assertCorrectService()
  }

  @Test
  fun testStyleLangNoCrash() {
    // Svelte LS will print long error "Cannot find module 'sass'" with require stack.
    // Description is not important, in 2023.3 the LS crashed instead of showing any errors.
    addTypeScriptCommonFiles()
    myFixture.configureByText("Hello.svelte", """
      <style lang="scss"><EOLError></EOLError>
        div {
          color: red;
        }
      </style>
    """.trimIndent())
    myFixture.checkLspHighlighting()
    assertCorrectService()
  }

  @Test
  fun testNotificationsForTSFileChanges() {
    addTypeScriptCommonFiles()
    myFixture.configureByText("helper.ts", "") // empty file will trigger "not a module" error
    val helperDocument = myFixture.editor.document
    myFixture.configureByText("Hello.svelte", """
      <script lang="ts">
        import * as ns from <error>"./helper"</error>;
        
        const <error descr="Svelte: Type 'boolean' is not assignable to type 'string'.">expectError</error>: string = true;
        console.log(ns, expectError);
      </script>
    """.trimIndent())
    myFixture.checkLspHighlighting()
    assertCorrectService()

    WriteCommandAction.runWriteCommandAction(project) {
      helperDocument.insertString(0, "export {};")
    }

    val checkDoc = MockDocument()
    checkDoc.replaceText("""
      <script lang="ts">
        import * as ns from "./helper";
        
        const <error descr="Svelte: Type 'boolean' is not assignable to type 'string'.">expectError</error>: string = true;
        console.log(ns, expectError);
      </script>
    """.trimIndent(), 0)

    val data = ExpectedHighlightingData(checkDoc, true, true, false)
    data.init()
    waitForDiagnosticsFromLspServer(project, file.virtualFile)
    (myFixture as CodeInsightTestFixtureImpl).collectAndCheckHighlighting(data)
  }

  @Test
  fun testTypeCheckingForProps() {
    addTypeScriptCommonFiles()
    myFixture.addFileToProject("Child.svelte", """
      <script lang="ts">
        export let numA: number = 1;
        let numBPrivate: number;
        
        export { numBPrivate as numB }
      </script>

      <p>{numA}</p>
      <p>{numBPrivate}</p>
    """.trimIndent())
    myFixture.configureByText("Hello.svelte", """
      <script lang="ts">
        import Child from "./Child.svelte";
      </script>
      
      <Child <error descr="Svelte: Type 'string' is not assignable to type 'number'.">numA</error>="1" numB={10} />
      <Child <error descr="Svelte: Type 'boolean' is not assignable to type 'number'.">numB</error>={true} />
      
      <Child numA={undefined} numB={1} />
      <Child <error descr="Svelte: Type 'null' is not assignable to type 'number | undefined'.">numA</error>={null} numB={1} />
      <<error descr="Svelte: Property 'numB' is missing in type '{}' but required in type '{ numA?: number | undefined; numB: number; }'.">Child</error> />
      
      <Child <error descr="Svelte: Object literal may only specify known properties, and '\"numBPrivate\"' does not exist in type '{ numA?: number | undefined; numB: number; }'.">numBPrivate</error>={undefined} numB={1} />
    """.trimIndent())
    myFixture.checkLspHighlighting()
    assertCorrectService()
  }

  @Test
  fun testTypeNarrowing() {
    addTypeScriptCommonFiles()
    myFixture.configureByText("Hello.svelte", """
      <script lang="ts">
        type SuccessModel = {
          success: true;
          successMessage: string;
        };
      
        type ErrorModel = {
          success: false;
          errorMessage: string;
        };
      
        type Model = SuccessModel | ErrorModel;
      
        function getModel(): Model {
          return {success: true, successMessage: "hello"};
        }
      
        const model = getModel();
      </script>
      
      {#if model.success}
        <p>{model.successMessage}</p>
      {:else}
        <p>{model.errorMessage}</p>
      {/if}
    """.trimIndent())
    myFixture.checkLspHighlighting()
    assertCorrectService()
  }

  @Test
  fun testTypesFromSeparateScriptTags() { // WEB-54516
    addTypeScriptCommonFiles()
    myFixture.configureByText("Foo.svelte", """
      <script context="module" lang="ts">
        export interface User {
          foo: number;
        }
      
        export let defaultUser: User = {
          foo: 5,
        }
      </script>
      
      <script lang="ts">
        export let user: User;
        defaultUser = user;
      </script>
    """.trimIndent())
    myFixture.checkLspHighlighting()
    assertCorrectService()
  }

  @Test
  fun testReactiveDeclarationDestructuredObjectJS() {
    addTypeScriptCommonFiles()
    myFixture.configureByText("Foo.svelte", """
      <script>
        $: ({ foo1 } = { foo1: 1 });
      
        foo1;
        <error descr="Svelte: Cannot find name 'foo2'.">foo2</error>;
      </script>
      
      <p>{foo1}</p>
      <p>{<error descr="Svelte: Cannot find name 'foo2'.">foo2</error>}</p>
    """.trimIndent())
    myFixture.checkLspHighlighting()
    assertCorrectService()
  }

  @Test
  fun testReactiveDeclarationDestructuredObjectTS() {
    addTypeScriptCommonFiles()
    myFixture.configureByText("Foo.svelte", """
      <script lang="ts">
        $: ({ foo1 } = { foo1: 1 });
      
        foo1;
        <error descr="Svelte: Cannot find name 'foo2'.">foo2</error>;
      </script>
      
      <p>{foo1}</p>
      <p>{<error descr="Svelte: Cannot find name 'foo2'.">foo2</error>}</p>
    """.trimIndent())
    myFixture.checkLspHighlighting()
    assertCorrectService()
  }

  @Test
  fun testReactiveDeclarationDestructuredArrayJS() {
    addTypeScriptCommonFiles()
    myFixture.configureByText("Foo.svelte", """
      <script>
        $: [foo1] = [1];
      
        foo1;
        <error descr="Svelte: Cannot find name 'foo2'."><error descr="Unresolved variable or type foo2">foo2</error></error>;
      </script>
      
      <p>{foo1}</p>
      <p>{<error descr="Svelte: Cannot find name 'foo2'."><error descr="Unresolved variable or type foo2">foo2</error></error>}</p>
    """.trimIndent())
    myFixture.checkLspHighlighting()
    assertCorrectService()
  }

  @Test
  fun testReactiveDeclarationDestructuredArrayTS() {
    addTypeScriptCommonFiles()
    myFixture.configureByText("Foo.svelte", """
      <script lang="ts">
        $: [foo1] = [1];
      
        foo1;
        <error descr="Svelte: Cannot find name 'foo2'.">foo2</error>;
      </script>
      
      <p>{foo1}</p>
      <p>{<error descr="Svelte: Cannot find name 'foo2'."><error descr="Unresolved variable or type foo2">foo2</error></error>}</p>
    """.trimIndent())
    myFixture.checkLspHighlighting()
    assertCorrectService()
  }

  @Test
  fun testDestructuredAssignmentAssignability() { // WEB-60202
    addTypeScriptCommonFiles()
    myFixture.configureByText("Foo.svelte", """
      <script lang="ts">
        class Account {
        }
      
        interface TransactionInit {
          sourceAccount?: Account;
        }
      
        export let init: TransactionInit;
      
        let sourceAccount: Account | undefined;
        $: ({ sourceAccount } = init);
      </script>
      
      {sourceAccount}
    """.trimIndent())
    myFixture.checkLspHighlighting()
    assertCorrectService()
  }

  @Test
  fun testImportFromModuleScriptJS() {
    addTypeScriptCommonFiles()
    myFixture.addFileToProject("Helper.svelte", """
      <script context="module">
        export class Inner {
          foo = true;
        }
      </script>
    """.trimIndent())
    myFixture.configureByText("Foo.svelte", """
      <script>
        import { Inner, Inner as Renamed, <error descr="Svelte: Module '\"./Helper.svelte\"' has no exported member 'Wrong'. Did you mean to use 'import Wrong from \"./Helper.svelte\"' instead?">Wrong</error> } from "./Helper.svelte";
      
        new Inner;
        new Renamed;
        new Wrong;
      </script>
    """.trimIndent())
    myFixture.checkLspHighlighting()
    assertCorrectService()
  }

  @Test
  fun testImportFromModuleScriptTS() {
    addTypeScriptCommonFiles()
    myFixture.addFileToProject("Helper.svelte", """
      <script context="module" lang="ts">
        export class Inner {
          foo = true;
        }

        export interface Foo {
          bar: number;
        }
      </script>
    """.trimIndent())
    myFixture.configureByText("Foo.svelte", """
      <script lang="ts">
        import { Inner, Inner as Renamed, <error descr="Svelte: Module '\"./Helper.svelte\"' has no exported member 'Wrong'. Did you mean to use 'import Wrong from \"./Helper.svelte\"' instead?">Wrong</error>, type Foo } from "./Helper.svelte";
      
        new Inner;
        new Renamed;
        new Wrong;
      
        const x: Foo = {bar: 42};
      </script>
      
      {x}
    """.trimIndent())
    myFixture.checkLspHighlighting()
    assertCorrectService()
  }

  @Test
  fun testFunctionDeclarationGTDU() {
    addTypeScriptCommonFiles()
    myFixture.configureByText("Hello.svelte", """
      <script lang="ts">
        function <caret>handleClick() {
          console.log("clicked!");
        }
      
        handleClick();
      </script>
      
      <button on:click={handleClick}>Hello</button>
    """.trimIndent())
    myFixture.checkLspHighlighting()
    assertCorrectService()

    JSNavigationTest.doTestGTDU(myFixture, true)
  }

  @Test
  fun testFunctionReferenceGTDU() {
    addTypeScriptCommonFiles()
    myFixture.configureByText("Hello.svelte", """
      <script lang="ts">
        function handleClick() {
          console.log("clicked!");
        }
      
        handleClick();
      </script>
      
      <button on:click={<caret>handleClick}>Hello</button>
    """.trimIndent())
    myFixture.checkLspHighlighting()
    assertCorrectService()

    JSNavigationTest.doTestGTDU(myFixture, false)
  }

  @Test
  fun testReactiveDeclarationReferenceGTDU() {
    addTypeScriptCommonFiles()
    // todo hide internal errors for destructured reactive declaration references
    myFixture.configureByText("Hello.svelte", """
      <script lang="ts">
        $: ({ foo } = { foo: 1 });
      </script>
      
      <p>Foo: {<caret>foo}</p>
    """.trimIndent())
    myFixture.checkLspHighlighting()
    assertCorrectService()

    JSNavigationTest.doTestGTDU(myFixture, false)
  }

  @Test
  fun testTypeScriptPluginWorks() {
    addTypeScriptCommonFiles()
    myFixture.configureByText("Foo.svelte", """
      <script lang="ts">
        export let prop1 = 5;
      </script>
      
      {prop1}
    """.trimIndent())
    myFixture.checkLspHighlighting()

    myFixture.configureByText("usage.ts", """
      import Foo from "./Foo.svelte";

      new Foo({props: {<error descr="TS2322: Type 'string' is not assignable to type 'number'.">prop1</error>: "foo"}});
      let <error descr="TS2322: Type 'number' is not assignable to type 'typeof SvelteComponent'.">foo</error>: typeof Foo = 5;
      console.log(foo);
    """.trimIndent())
    myFixture.checkHighlighting() // no checkLspHighlighting for ts server protocol
    assertCorrectServiceForTsFile()

    // typescript-plugin-svelte for now doesn't check the path, only file name
    myFixture.configureByText("+page.server.ts", """
      export async function load(event<error descr="TS2307: Cannot find module './${"$"}types.js' or its corresponding type declarations. (this likely means that SvelteKit's type generation didn't run yet - try running it by executing 'npm run dev' or 'npm run build')">)</error> {
        helper(event);
        return {};
      }
      
      export async function <warning descr="TS71001: Invalid export 'misspelledLoad' (valid exports are prerender, ssr, csr, trailingSlash, config, actions, load, entries, or anything with a '_' prefix)">misspelledLoad</warning>(event: any) {
        helper(event);
        return {};
      }
      
      function helper(<error descr="TS7006: Parameter 'implicitAny' implicitly has an 'any' type.">implicitAny</error>) {
        return implicitAny;
      }
    """.trimIndent())
    myFixture.checkHighlighting() // no checkLspHighlighting for ts server protocol


    // todo suppress "Method expression is not of Function type"
    myFixture.configureByText("anotherUsage.js", """
      import Foo from "./Foo.svelte";

      new <weak_warning>Foo</weak_warning>({props: {<error descr="TS2322: Type 'string' is not assignable to type 'number'.">prop1</error>: "foo"}});
    """.trimIndent())
    myFixture.checkHighlighting() // no checkLspHighlighting for ts server protocol
    assertCorrectServiceForTsFile()
  }

  @Test
  fun testTypeScriptPluginWorksWithLegacyToolWindow() {
    RegistryManager.getInstance().get("ts.tool.window.show").setValue(true, testRootDisposable)

    addTypeScriptCommonFiles()
    myFixture.configureByText("Foo.svelte", """
      <script lang="ts">
        export let prop1 = 5;
      </script>
      
      {prop1}
    """.trimIndent())
    myFixture.checkLspHighlighting()

    myFixture.configureByText("usage.ts", """
      import Foo from "./Foo.svelte";

      new Foo({props: {<error descr="TS2322: Type 'string' is not assignable to type 'number'.">prop1</error>: "foo"}});
      let <error descr="TS2322: Type 'number' is not assignable to type 'typeof Foo__SvelteComponent_'.">foo</error>: typeof Foo = 5;
      console.log(foo);
    """.trimIndent())
    myFixture.checkHighlighting() // no checkLspHighlighting for ts server protocol
    assertCorrectServiceForTsFile()
  }

  @Test
  fun testTypeScriptServiceResolve() {
    addTypeScriptCommonFiles()
    myFixture.addFileToProject("randomFile.ts", """
      export async function run() {
        return {
          /**
           * bug-maker
           */
          exposedStuff: true,
        };
      }
    """.trimIndent())

    myFixture.configureByText("Foo.svelte", """
      <script lang="ts">
        /**
         * docs!
         */
        export function exposedStuff() {
          console.log("hello!");
        }
      </script>
      
      <button on:click|preventDefault={exposedStuff} />
    """.trimIndent())
    myFixture.checkLspHighlighting()

    checkTypeScriptServiceResolve("usageTS.ts", """
      import Foo from "./Foo.svelte";

      let foo = new Foo({target: document}); // ts
      foo.<caret>exposedStuff
    """.trimIndent())

    checkTypeScriptServiceResolve("usageJS.js", """
      import Foo from "./Foo.svelte";

      // noinspection JSValidateTypes
      let foo = new Foo({target: document}); // js
      foo.<caret>exposedStuff
    """.trimIndent())
  }

  @Test
  fun testA11yWarningsEnabled() {
    addTypeScriptCommonFiles()
    myFixture.configureByText("Hello.svelte", """
      <script lang="ts">
      	const <error descr="Svelte: Type 'boolean' is not assignable to type 'string'.">expectError</error>: string = true;
        console.log(expectError);
      </script>
      
      <warning descr="Svelte: A11y: visible, non-interactive elements with an on:click event must be accompanied by a keyboard event handler. Consider whether an interactive element such as <button type=\"button\"> or <a> might be more appropriate. See https://svelte.dev/docs/accessibility-warnings#a11y-click-events-have-key-events for more details."><div role="button" tabindex="0" on:click={() => alert("hello")}>
        Hello
      </div</warning>>
    """.trimIndent())
    myFixture.checkLspHighlighting()
    assertCorrectService()
  }

  @Test
  fun testA11yWarningsDisabled() {
    val settings = getSvelteServiceSettings(project)
    settings.showA11yWarnings = false
    disposeOnTearDown(Disposable { settings.showA11yWarnings = true })

    addTypeScriptCommonFiles()
    myFixture.configureByText("Hello.svelte", """
      <script lang="ts">
      	const <error descr="Svelte: Type 'boolean' is not assignable to type 'string'.">expectError</error>: string = true;
        console.log(expectError);
      </script>
      
      <div role="button" tabindex="0" on:click={() => alert("hello")}>
      	Hello
      </div>
    """.trimIndent())
    myFixture.checkLspHighlighting()
    assertCorrectService()
  }

  private fun checkTypeScriptServiceResolve(fileName: String, text: String) {
    myFixture.configureByText(fileName, text)
    myFixture.checkHighlighting() // no checkLspHighlighting for ts server protocol
    assertCorrectServiceForTsFile()
    val ref = myFixture.getReferenceAtCaretPositionWithAssertion()
    ref as JSReferenceExpression
    TestCase.assertEquals(1, ref.multiResolve(false).size)
    val target = ref.resolve()!!
    TestCase.assertEquals("Foo.svelte", target.containingFile?.name)
    // we'd love to double check what's the target of navigation but Svelte service works differently in prod and in tests
    //target as JSFunctionDeclaration
    //TestCase.assertEquals("exposedStuff", target.name)
  }

}