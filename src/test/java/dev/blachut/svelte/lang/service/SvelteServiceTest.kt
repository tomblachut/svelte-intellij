// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package dev.blachut.svelte.lang.service

import com.intellij.lang.javascript.JSNavigationTest
import com.intellij.platform.lsp.tests.checkLspHighlighting
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
    """)
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
        import { Inner, Inner as Renamed } from "./Helper.svelte";
      
        new Inner;
        new Renamed;
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
        import { Inner, Inner as Renamed, type Foo } from "./Helper.svelte";
      
        new Inner;
        new Renamed;
      
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
    """)
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
    """)
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
    """)
    myFixture.checkLspHighlighting()
    assertCorrectService()

    JSNavigationTest.doTestGTDU(myFixture, false)
  }

}