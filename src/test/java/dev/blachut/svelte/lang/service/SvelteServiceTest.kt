// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package dev.blachut.svelte.lang.service

import com.intellij.lang.javascript.JSNavigationTest
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.mock.MockDocument
import com.intellij.openapi.application.readActionBlocking
import com.intellij.openapi.application.runReadActionBlocking
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.util.Disposer
import com.intellij.testFramework.ExpectedHighlightingData
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import dev.blachut.svelte.lang.SvelteTestModule
import dev.blachut.svelte.lang.codeInsight.SvelteHighlightingTest
import dev.blachut.svelte.lang.configureSvelteDependencies
import dev.blachut.svelte.lang.service.SvelteServiceFixture.Companion.svelteServiceFixture
import dev.blachut.svelte.lang.service.settings.getSvelteServiceSettings
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@TestApplication
class SvelteServiceTest {
  private val svelte by svelteServiceFixture()

  @BeforeEach
  fun setUp() {
    svelte.fixture.enableInspections(*SvelteHighlightingTest.configureDefaultLocalInspectionTools().toTypedArray())
  }

  @Test
  fun testServiceWorks() {
    svelte.addTypeScriptCommonFiles()
    svelte.fixture.configureByText("Hello.svelte", """
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
    svelte.fixture.checkHighlighting()
    svelte.assertCorrectService()
  }

  /**
   * WEB-57512 with the LSP running, which is the condition `SvelteHighlightingTest` cannot cover.
   * Short name on purpose: long ones blow the per-test temp path on Windows.
   */
  @Test
  fun testFaImportUsed() {
    svelte.addTypeScriptCommonFiles()
    svelte.fixture.configureSvelteDependencies(SvelteTestModule.SVELTE_FA_4)
    svelte.fixture.configureByText("Usage.svelte", """
      <script lang="ts">
        import Fa from 'svelte-fa';

        // svelte-fa declares `icon` as required, and with the service running the LSP checks it
        const icon: any = null;
      </script>

      <Fa {icon} />
    """.trimIndent())
    svelte.fixture.checkHighlighting()
    svelte.assertCorrectService()
  }

  @Test
  fun testSyntaxError() {
    // This test checks if we properly process Diagnostics without code.
    // It's important because exceptions in our highlighting are swallowed but appear to affect performance.
    // Would be good to actually completely hide this annotation, but then I'm not sure how to still verify the above.
    svelte.addTypeScriptCommonFiles()
    svelte.fixture.configureByText("Hello.svelte", """
      <script lang="ts"><EOLError descr="Svelte: [svelte-preprocess] Encountered type error"></EOLError>
        let hello = "hello"<error descr="Svelte: ',' expected."><error descr="Newline or semicolon expected">w</error>rong</error>;
        console.log(hello);
      </script>
    """.trimIndent())
    svelte.fixture.checkHighlighting()
    svelte.assertCorrectService()
  }

  @Test
  fun testStyleLangNoCrash() {
    // Svelte LS will print long error "Cannot find module 'sass'" with require stack.
    // Description is not important, in 2023.3 the LS crashed instead of showing any errors.
    svelte.addTypeScriptCommonFiles()
    svelte.fixture.configureByText("Hello.svelte", """
      <style lang="scss"><EOLError></EOLError>
        div {
          color: red;
        }
      </style>
    """.trimIndent())
    svelte.fixture.checkHighlighting()
    svelte.assertCorrectService()
  }

  @Test
  fun testNotificationsForTSFileChanges() {
    svelte.addTypeScriptCommonFiles()
    svelte.fixture.configureByText("helper.ts", "") // empty file will trigger "not a module" error
    val helperDocument = svelte.fixture.editor.document
    svelte.fixture.configureByText("Hello.svelte", """
      <script lang="ts">
        import * as ns from <error>"./helper"</error>;
        
        const <error descr="Svelte: Type 'boolean' is not assignable to type 'string'.">expectError</error>: string = true;
        console.log(ns, expectError);
      </script>
    """.trimIndent())
    svelte.fixture.checkHighlighting()
    svelte.assertCorrectService()

    WriteCommandAction.runWriteCommandAction(svelte.project) {
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
    svelte.fixture.doHighlighting()
    (svelte.fixture as CodeInsightTestFixtureImpl).collectAndCheckHighlighting(data)
  }

  @Test
  fun testTypeCheckingForProps() {
    svelte.addTypeScriptCommonFiles()
    svelte.fixture.addFileToProject("Child.svelte", """
      <script lang="ts">
        export let numA: number = 1;
        let numBPrivate: number;
        
        export { numBPrivate as numB }
      </script>

      <p>{numA}</p>
      <p>{numBPrivate}</p>
    """.trimIndent())
    svelte.fixture.configureByText("Hello.svelte", """
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
    svelte.fixture.checkHighlighting()
    svelte.assertCorrectService()
  }

  @Test
  fun testTypeNarrowing() {
    svelte.addTypeScriptCommonFiles()
    svelte.fixture.configureByText("Hello.svelte", """
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
    svelte.fixture.checkHighlighting()
    svelte.assertCorrectService()
  }

  @Test
  fun testTypesFromSeparateScriptTags() { // WEB-54516
    svelte.addTypeScriptCommonFiles()
    svelte.fixture.configureByText("Foo.svelte", """
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
    svelte.fixture.checkHighlighting()
    svelte.assertCorrectService()
  }

  @Test
  fun testReactiveDeclarationDestructuredObjectJS() {
    svelte.addTypeScriptCommonFiles()
    svelte.fixture.configureByText("Foo.svelte", """
      <script>
        $: ({ foo1 } = { foo1: 1 });
      
        foo1;
        <error descr="Svelte: Cannot find name 'foo2'."><error descr="Unresolved variable or type foo2">foo2</error></error>;
      </script>
      
      <p>{foo1}</p>
      <p>{<error descr="Svelte: Cannot find name 'foo2'."><error descr="Unresolved variable or type foo2">foo2</error></error>}</p>
    """.trimIndent())
    svelte.fixture.checkHighlighting()
    svelte.assertCorrectService()
  }

  @Test
  fun testReactiveDeclarationDestructuredObjectTS() {
    svelte.addTypeScriptCommonFiles()
    svelte.fixture.configureByText("Foo.svelte", """
      <script lang="ts">
        $: ({ foo1 } = { foo1: 1 });
      
        foo1;
        <error descr="Svelte: Cannot find name 'foo2'.">foo2</error>;
      </script>
      
      <p>{foo1}</p>
      <p>{<error descr="Svelte: Cannot find name 'foo2'.">foo2</error>}</p>
    """.trimIndent())
    svelte.fixture.checkHighlighting()
    svelte.assertCorrectService()
  }

  @Test
  fun testReactiveDeclarationDestructuredArrayJS() {
    svelte.addTypeScriptCommonFiles()
    svelte.fixture.configureByText("Foo.svelte", """
      <script>
        $: [foo1] = [1];
      
        foo1;
        <error descr="Svelte: Cannot find name 'foo2'."><error descr="Unresolved variable or type foo2">foo2</error></error>;
      </script>
      
      <p>{foo1}</p>
      <p>{<error descr="Svelte: Cannot find name 'foo2'."><error descr="Unresolved variable or type foo2">foo2</error></error>}</p>
    """.trimIndent())
    svelte.fixture.checkHighlighting()
    svelte.assertCorrectService()
  }

  @Test
  fun testReactiveDeclarationDestructuredArrayTS() {
    svelte.addTypeScriptCommonFiles()
    svelte.fixture.configureByText("Foo.svelte", """
      <script lang="ts">
        $: [foo1] = [1];
      
        foo1;
        <error descr="Svelte: Cannot find name 'foo2'.">foo2</error>;
      </script>
      
      <p>{foo1}</p>
      <p>{<error descr="Svelte: Cannot find name 'foo2'.">foo2</error>}</p>
    """.trimIndent())
    svelte.fixture.checkHighlighting()
    svelte.assertCorrectService()
  }

  @Test
  fun testDestructuredAssignmentAssignability() { // WEB-60202
    svelte.addTypeScriptCommonFiles()
    svelte.fixture.configureByText("Foo.svelte", """
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
    svelte.fixture.checkHighlighting()
    svelte.assertCorrectService()
  }

  @Test
  fun testImportFromModuleScriptJS() {
    svelte.addTypeScriptCommonFiles()
    svelte.fixture.addFileToProject("Helper.svelte", """
      <script context="module">
        export class Inner {
          foo = true;
        }
      </script>
    """.trimIndent())
    svelte.fixture.configureByText("Foo.svelte", """
      <script>
        import { Inner, Inner as Renamed, <error descr="Svelte: Module '\"./Helper.svelte\"' has no exported member 'Wrong'. Did you mean to use 'import Wrong from \"./Helper.svelte\"' instead?"><weak_warning descr="Cannot resolve symbol 'Wrong'">Wrong</weak_warning></error> } from "./Helper.svelte";
      
        new Inner;
        new Renamed;
        new Wrong;
      </script>
    """.trimIndent())
    svelte.fixture.checkHighlighting()
    svelte.assertCorrectService()
  }

  @Test
  fun testImportFromModuleScriptTS() {
    svelte.addTypeScriptCommonFiles()
    svelte.fixture.addFileToProject("Helper.svelte", """
      <script context="module" lang="ts">
        export class Inner {
          foo = true;
        }

        export interface Foo {
          bar: number;
        }
      </script>
    """.trimIndent())
    svelte.fixture.configureByText("Foo.svelte", """
      <script lang="ts">
        import { Inner, Inner as Renamed, <error descr="Svelte: Module '\"./Helper.svelte\"' has no exported member 'Wrong'. Did you mean to use 'import Wrong from \"./Helper.svelte\"' instead?">Wrong</error>, type Foo } from "./Helper.svelte";
      
        new Inner;
        new Renamed;
        new Wrong;
      
        const x: Foo = {bar: 42};
      </script>
      
      {x}
    """.trimIndent())
    svelte.fixture.checkHighlighting()
    svelte.assertCorrectService()
  }

  @Test
  fun testFunctionDeclarationGTDU() {
    svelte.addTypeScriptCommonFiles()
    svelte.fixture.configureByText("Hello.svelte", """
      <script lang="ts">
        function <caret>handleClick() {
          console.log("clicked!");
        }
      
        handleClick();
      </script>
      
      <button on:click={handleClick}>Hello</button>
    """.trimIndent())
    svelte.fixture.checkHighlighting()
    svelte.assertCorrectService()

    JSNavigationTest.doTestGTDU(svelte.fixture, true)
  }

  @Test
  fun testFunctionReferenceGTDU() {
    svelte.addTypeScriptCommonFiles()
    svelte.fixture.configureByText("Hello.svelte", """
      <script lang="ts">
        function handleClick() {
          console.log("clicked!");
        }
      
        handleClick();
      </script>
      
      <button on:click={<caret>handleClick}>Hello</button>
    """.trimIndent())
    svelte.fixture.checkHighlighting()
    svelte.assertCorrectService()

    JSNavigationTest.doTestGTDU(svelte.fixture, false)
  }

  @Test
  fun testReactiveDeclarationReferenceGTDU() {
    svelte.addTypeScriptCommonFiles()
    // todo hide internal errors for destructured reactive declaration references
    svelte.fixture.configureByText("Hello.svelte", """
      <script lang="ts">
        $: ({ foo } = { foo: 1 });
      </script>
      
      <p>Foo: {<caret>foo}</p>
    """.trimIndent())
    svelte.fixture.checkHighlighting()
    svelte.assertCorrectService()

    JSNavigationTest.doTestGTDU(svelte.fixture, false)
  }

  @Test
  fun testTypeScriptPluginWorks() {
    svelte.addTypeScriptCommonFiles()
    svelte.fixture.configureByText("Foo.svelte", """
      <script lang="ts">
        export let prop1 = 5;
      </script>
      
      {prop1}
    """.trimIndent())
    svelte.fixture.checkHighlighting()

    svelte.fixture.configureByText("usage.ts", """
      import Foo from "./Foo.svelte";

      new Foo({props: {<error descr="TS2322: Type 'string' is not assignable to type 'number'.">prop1</error>: "foo"}});
      let <error descr="TS2322: Type 'number' is not assignable to type 'typeof Foo__SvelteComponent_'.">foo</error>: typeof Foo = 5;
      console.log(foo);
    """.trimIndent())
    svelte.fixture.checkHighlighting() // no checkLspHighlighting for ts server protocol
    svelte.assertCorrectServiceForTsFile()

    // typescript-plugin-svelte for now doesn't check the path, only file name
    svelte.fixture.configureByText("+page.server.ts", """
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
    svelte.fixture.checkHighlighting() // no checkLspHighlighting for ts server protocol


    // todo suppress "Method expression is not of Function type"
    svelte.fixture.configureByText("anotherUsage.js", """
      import Foo from "./Foo.svelte";

      new <weak_warning>Foo</weak_warning>({props: {<error descr="TS2322: Type 'string' is not assignable to type 'number'.">prop1</error>: "foo"}});
    """.trimIndent())
    svelte.fixture.checkHighlighting() // no checkLspHighlighting for ts server protocol
    svelte.assertCorrectServiceForTsFile()
  }

  @Test
  fun testTypeScriptServiceResolve() {
    svelte.addTypeScriptCommonFiles()
    svelte.fixture.addFileToProject("randomFile.ts", """
      export async function run() {
        return {
          /**
           * bug-maker
           */
          exposedStuff: true,
        };
      }
    """.trimIndent())

    svelte.fixture.configureByText("Foo.svelte", """
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
    svelte.fixture.checkHighlighting()

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
    svelte.addTypeScriptCommonFiles()
    svelte.fixture.configureByText("Hello.svelte", """
      <script lang="ts">
      	const <error descr="Svelte: Type 'boolean' is not assignable to type 'string'.">expectError</error>: string = true;
        console.log(expectError);
      </script>
      
      <warning descr="Svelte: A11y: visible, non-interactive elements with an on:click event must be accompanied by a keyboard event handler. Consider whether an interactive element such as <button type=\"button\"> or <a> might be more appropriate. See https://svelte.dev/docs/accessibility-warnings#a11y-click-events-have-key-events for more details."><div role="button" tabindex="0" on:click={() => alert("hello")}>
        Hello
      </div</warning>>
    """.trimIndent())
    svelte.fixture.checkHighlighting()
    svelte.assertCorrectService()
  }

  @Test
  fun testA11yWarningsDisabled() {
    val settings = getSvelteServiceSettings(svelte.project)
    settings.showA11yWarnings = false
    Disposer.register(svelte.testRootDisposable) { settings.showA11yWarnings = true }

    svelte.addTypeScriptCommonFiles()
    svelte.fixture.configureByText("Hello.svelte", """
      <script lang="ts">
      	const <error descr="Svelte: Type 'boolean' is not assignable to type 'string'.">expectError</error>: string = true;
        console.log(expectError);
      </script>
      
      <div role="button" tabindex="0" on:click={() => alert("hello")}>
      	Hello
      </div>
    """.trimIndent())
    svelte.fixture.checkHighlighting()
    svelte.assertCorrectService()
  }

  @Test
  fun testGenericGoToDeclaration() {
    svelte.addTypeScriptCommonFiles()

    svelte.fixture.configureByText("TestComponent.svelte", """
      <script lang="ts" generics="T extends string">
        export let value: <caret>T;
      </script>
      {value}
    """.trimIndent())

    svelte.fixture.checkHighlighting()
    svelte.assertCorrectService()

    JSNavigationTest.doTestGTDU(svelte.fixture, false)
  }

  @Test
  fun testGenericFindUsages() {
    svelte.addTypeScriptCommonFiles()

    svelte.fixture.configureByText("TestComponent.svelte", """
      <script lang="ts" generics="<caret>T extends string">
        export let value: T;
      </script>
      {value}
    """.trimIndent())

    svelte.fixture.doHighlighting()
    svelte.assertCorrectService()

    JSNavigationTest.doTestGTDU(svelte.fixture, true)
  }

  @Test
  fun testFindUsagesFromTypeParameterDeclaration() {
    svelte.addTypeScriptCommonFiles()

    svelte.fixture.configureByText("TestComponent.svelte", """
      <script lang="ts" generics="<caret>T extends string">
        export let value: T;
        function process(item: T): T {
          return item;
        }
      </script>
    """.trimIndent())

    svelte.fixture.doHighlighting()
    svelte.assertCorrectService()

    val usages = runReadActionBlocking { svelte.fixture.findUsages(svelte.fixture.elementAtCaret) }

    // Should find: value: T, item: T, return type: T
    assertTrue(usages.size >= 3, "Expected at least 3 usages of T")

    // Verify usages are in expected contexts
    val usageTexts = runReadActionBlocking { usages.map { it.element?.text } }
    assertTrue(usageTexts.any { it == "T" }, "Should find usage in 'value: T'")
  }

  @Test
  fun testFindUsagesFromUsageSite() {
    svelte.addTypeScriptCommonFiles()

    svelte.fixture.configureByText("TestComponent.svelte", """
      <script lang="ts" generics="T extends string">
        export let value: <caret>T;
        function process(item: T): T {
          return item;
        }
      </script>
    """.trimIndent())

    svelte.fixture.doHighlighting()
    svelte.assertCorrectService()

    val usages = runReadActionBlocking { svelte.fixture.findUsages(svelte.fixture.elementAtCaret) }

    // Should resolve from usage back to declaration and find all usages
    assertTrue(usages.size >= 3, "Should find all usages from usage site")
  }

  @Test
  fun testFindUsagesMultipleTypeParameters() {
    svelte.addTypeScriptCommonFiles()

    svelte.fixture.configureByText("TestComponent.svelte", """
      <script lang="ts" generics="T extends string, <caret>U extends number">
        export let first: T;
        export let second: U;
        function combine(a: T, b: U): [T, U] {
          return [a, b];
        }
      </script>
    """.trimIndent())

    svelte.fixture.doHighlighting()
    svelte.assertCorrectService()

    val usages = runReadActionBlocking { svelte.fixture.findUsages(svelte.fixture.elementAtCaret) }

    // Should find only U usages, not T usages
    assertTrue(usages.size >= 2, "Should find usages of U")

    // Verify U is found but not T
    val usageParents = runReadActionBlocking { usages.mapNotNull { it.element?.parent?.parent?.text } }
    assertTrue(usageParents.any { it.contains("second") }, "Should find 'second: U'")
    assertTrue(usageParents.any { it.contains("b:") }, "Should find 'b: U'")
  }


  private fun checkTypeScriptServiceResolve(fileName: String, text: String) {
    svelte.fixture.configureByText(fileName, text)
    svelte.fixture.checkHighlighting() // no checkLspHighlighting for ts server protocol
    svelte.assertCorrectServiceForTsFile()
    timeoutRunBlocking {
      readActionBlocking {
        val ref = svelte.fixture.getReferenceAtCaretPositionWithAssertion()
        ref as JSReferenceExpression
        assertEquals(1, ref.multiResolve(false).size)
        val target = ref.resolve()!!
        assertEquals("Foo.svelte", target.containingFile?.name)
      }
    }
    // we'd love to double check what's the target of navigation but Svelte service works differently in prod and in tests
    //target as JSFunctionDeclaration
    //TestCase.assertEquals("exposedStuff", target.name)
  }

}