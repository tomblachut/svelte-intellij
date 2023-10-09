// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package dev.blachut.svelte.lang.service

import com.intellij.lang.javascript.JSNavigationTest
import com.intellij.mock.MockDocument
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.platform.lsp.tests.checkLspHighlighting
import com.intellij.platform.lsp.tests.waitForDiagnosticsFromLspServer
import com.intellij.testFramework.ExpectedHighlightingData
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl
import dev.blachut.svelte.lang.codeInsight.SvelteHighlightingTest
import org.junit.Test

class SvelteServiceTest : SvelteServiceTestBase() {
  override fun setUp() {
    super.setUp()
    myFixture.enableInspections(*SvelteHighlightingTest.configureDefaultLocalInspectionTools().toTypedArray())
  }

  @Test
  fun testServiceWorks() {
    myFixture.addFileToProject("tsconfig.json", tsconfig)
    myFixture.configureByText("Hello.svelte", """
      <script lang="ts">
        let <error descr="Svelte: Type 'number' is not assignable to type 'string'.">local</error>: string = 1;
        local;
        
        function acceptNumber(num: number): number { return num; }
        
        acceptNumber(<error descr="Svelte: Argument of type 'boolean' is not assignable to parameter of type 'number'.">true</error>);
      </script>
      
      <!-- todo remove duplicate internal warning -->
      {acceptNumber(<error descr="Svelte: Argument of type 'boolean' is not assignable to parameter of type 'number'."><weak_warning descr="Argument type  boolean  is not assignable to parameter type  number ">true</weak_warning></error>)}
      
      <input <warning descr="Svelte: A11y: Avoid using autofocus">autofocus</warning>>
    """.trimIndent())
    myFixture.checkLspHighlighting()
    assertCorrectService()
  }

  @Test
  fun testNotificationsForTSFileChanges() {
    myFixture.addFileToProject("tsconfig.json", tsconfig)
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
    myFixture.addFileToProject("tsconfig.json", tsconfig)
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
      
      <Child <error descr="Svelte: Type '{ numBPrivate: undefined; numB: number; }' is not assignable to type '{ numA?: number | undefined; numB: number; }'.
        Object literal may only specify known properties, and '\"numBPrivate\"' does not exist in type '{ numA?: number | undefined; numB: number; }'.">numBPrivate</error>={undefined} numB={1} />
    """.trimIndent())
    myFixture.checkLspHighlighting()
    assertCorrectService()
  }

  @Test
  fun testTypeNarrowing() {
    myFixture.addFileToProject("tsconfig.json", tsconfig)
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
    myFixture.addFileToProject("tsconfig.json", tsconfig)
    myFixture.configureByText("Foo.svelte", """
      <script context='module' lang='ts'>
        export interface User {
          foo: number;
        }
      
        export let defaultUser: User = {
          foo: 5,
        }
      </script>
      
      <script lang='ts'>
        export let user: User;
        defaultUser = user;
      </script>
    """.trimIndent())
    myFixture.checkLspHighlighting()
    assertCorrectService()
  }

  @Test
  fun testReactiveDeclarationDestructuredJS() {
    myFixture.addFileToProject("tsconfig.json", tsconfig)
    myFixture.configureByText("Foo.svelte", """
      <script>
        ${'$'}: ({ foo1 } = { foo1: 1 });
      
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
  fun testReactiveDeclarationDestructuredTS() {
    myFixture.addFileToProject("tsconfig.json", tsconfig)
    myFixture.configureByText("Foo.svelte", """
      <script lang="ts">
        ${'$'}: ({ foo1 } = { foo1: 1 });
      
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
    myFixture.addFileToProject("tsconfig.json", tsconfig)
    myFixture.configureByText("Foo.svelte", """
      <script lang="ts">
        class Account {
        }
      
        interface TransactionInit {
          sourceAccount?: Account;
        }
      
        export let init: TransactionInit;
      
        let sourceAccount: Account | undefined;
        ${'$'}: ({ sourceAccount } = init);
      </script>
      
      {sourceAccount}
    """.trimIndent())
    myFixture.checkLspHighlighting()
    assertCorrectService()
  }

  @Test
  fun testImportFromModuleScriptJS() {
    myFixture.addFileToProject("tsconfig.json", tsconfig)
    myFixture.addFileToProject("Helper.svelte", """
      <script context="module">
        export class Inner {
          foo = true;
        }
      </script>
    """.trimIndent())
    myFixture.configureByText("Foo.svelte", """
      <script>
        import { Inner, Inner as Renamed, <error descr="Svelte: Module '\"./Helper.svelte\"' has no exported member 'Wrong'. Did you mean to use 'import Wrong from \"./Helper.svelte\"' instead?"><weak_warning descr="Cannot resolve symbol 'Wrong'">Wrong</weak_warning></error> } from "./Helper.svelte";
      
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
    myFixture.addFileToProject("tsconfig.json", tsconfig)
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
        import { Inner, Inner as Renamed, <error descr="Cannot resolve symbol 'Wrong'"><error descr="Svelte: Module '\"./Helper.svelte\"' has no exported member 'Wrong'. Did you mean to use 'import Wrong from \"./Helper.svelte\"' instead?">Wrong</error></error>, type Foo } from "./Helper.svelte";
      
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
    myFixture.addFileToProject("tsconfig.json", tsconfig)
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
    myFixture.addFileToProject("tsconfig.json", tsconfig)
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
    myFixture.addFileToProject("tsconfig.json", tsconfig)
    // todo hide internal errors for destructured reactive declaration references
    myFixture.configureByText("Hello.svelte", """
      <script lang="ts">
        ${'$'}: ({ foo } = { foo: 1 });
      </script>
      
      <p>Foo: {<caret>foo}</p>
    """.trimIndent())
    myFixture.checkLspHighlighting()
    assertCorrectService()

    JSNavigationTest.doTestGTDU(myFixture, false)
  }

}