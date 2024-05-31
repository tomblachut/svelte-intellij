package dev.blachut.svelte.lang.codeInsight

import com.intellij.codeInsight.daemon.impl.analysis.XmlUnboundNsPrefixInspection
import com.intellij.codeInspection.InspectionProfileEntry
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.htmlInspections.*
import com.intellij.htmltools.codeInspection.htmlInspections.HtmlRequiredAltAttributeInspection
import com.intellij.htmltools.codeInspection.htmlInspections.HtmlRequiredTitleElementInspection
import com.intellij.lang.javascript.JSTestUtils
import com.intellij.lang.javascript.inspection.JSObjectNullOrUndefinedInspection
import com.intellij.lang.javascript.inspection.JSSuspiciousTypeGuardInspection
import com.intellij.lang.javascript.inspection.JSUnusedAssignmentInspection
import com.intellij.lang.javascript.inspections.*
import com.intellij.lang.javascript.modules.TypeScriptCheckImportInspection
import com.intellij.lang.typescript.inspection.TypeScriptMissingConfigOptionInspection
import com.intellij.lang.typescript.inspections.*
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.xml.util.CheckEmptyTagInspection
import com.intellij.xml.util.XmlDuplicatedIdInspection
import com.intellij.xml.util.XmlInvalidIdInspection
import com.sixrr.inspectjs.assignment.SillyAssignmentJSInspection
import com.sixrr.inspectjs.confusing.CommaExpressionJSInspection
import com.sixrr.inspectjs.confusing.PointlessBooleanExpressionJSInspection
import com.sixrr.inspectjs.control.UnnecessaryLabelJSInspection
import com.sixrr.inspectjs.validity.UnreachableCodeJSInspection
import dev.blachut.svelte.lang.SvelteTestScenario
import dev.blachut.svelte.lang.configureBundledSvelte
import dev.blachut.svelte.lang.doTestWithLangFromTestNameSuffix
import dev.blachut.svelte.lang.getSvelteTestDataPath
import dev.blachut.svelte.lang.inspections.SvelteUnresolvedComponentInspection

class SvelteHighlightingTest : BasePlatformTestCase() {
  override fun getTestDataPath(): String = getSvelteTestDataPath()

  override fun setUp() {
    super.setUp()
    myFixture.enableInspections(*configureDefaultLocalInspectionTools().toTypedArray())
  }

  private fun configureComponentWithAttribute(name: String) {
    myFixture.configureByText(name, "<script>export var test=\"\"</script>")
  }

  fun testUnresolvedComponent() {
    configureComponentWithAttribute("Foo.svelte")
    myFixture.configureByText("Usage.svelte", "<<error>Fo<caret>o</error> />")
    myFixture.testHighlighting()
    myFixture.launchAction(myFixture.findSingleIntention("Insert 'import Foo from \"./Foo.svelte\"'"))
    myFixture.checkResult("""
      <script>
          import Foo from "./Foo.svelte";
      </script>

      <Foo />
      """.trimIndent())
    myFixture.testHighlighting() // no errors after import
  }

  fun testNamespacedComponent() {
    // TODO Support namespaced components WEB-61636
    myFixture.configureByText("Usage.svelte", """
      <<error descr="Cannot resolve symbol 'Unresolved'"><error descr="Unresolved Svelte component">Unresolved</error></error> test="" />
      <Also.Unresolved test="" />
      <Also.Unresolved test="" />
    """.trimIndent())
    myFixture.testHighlighting()
  }

  fun testComponentAttribute() {
    configureComponentWithAttribute("Foo.svelte")
    myFixture.configureByText("Usage.svelte", """
      <script>
      import Usage from "./Usage.svelte";
      </script>
      <Usage test="" />
      <div class="someName" <warning>unknowAttr</warning>="">test</div>
    """.trimIndent())
    myFixture.testHighlighting()
  }

  fun testShorthandAttributeRequired() {
    configureComponentWithAttribute("Foo.svelte")
    myFixture.configureByText("Usage.svelte", """
      <script>
        let src = 'tutorial/image.gif';
        let alt = 'Rick Astley';
      </script>

      <img {src} {alt}>
      <img bind:src bind:alt> <!-- not really valid, but we also expect another error from LS -->
    """.trimIndent())
    myFixture.testHighlighting()
  }

  fun testNoWrongAttributeValueForDynamicExpressions() {
    myFixture.configureByText("Test.svelte", """
      <script>
        let flag = false;
      </script>

      <button disabled="{flag}">Hello</button>
      <button disabled={flag}>Hello</button>
      <button disabled={false}>Hello</button>
      <button disabled>Hello</button>
      <button disabled="<warning descr="Wrong attribute value">true</warning>">Hello</button>
      <button disabled="disabled">Hello</button> <!-- valid according to HTML, todo invalid according to Svelte -->
    """.trimIndent())
    myFixture.testHighlighting()
  }

  fun testNoInvalidIdReference() {
    myFixture.configureByText("Test.svelte", """
      <script lang="ts">
        let id = "hello";
        $: renamedId = id;
      </script>
      
      <input id={id}>
      <label for={renamedId}></label>
      <label for="constButDefinedInAnotherComponent"></label>
    """.trimIndent())
    myFixture.testHighlighting()
  }

  fun testNoDuplicateIdReference() {
    myFixture.configureByText("Test.svelte", """
      {#if true}
        <div id="id"></div>
      {:else}
        <div id="id"></div>
      {/if}
    """.trimIndent())
    myFixture.testHighlighting()
  }

  fun testEmptyTagWarningHidden() {
    myFixture.configureByText("Test.svelte", """
      <div />
    """.trimIndent())
    myFixture.testHighlighting()
  }

  fun testGenerics() {
    myFixture.configureByText("Test.svelte", """
      <script lang="ts" generics="Todo extends string">
        export let something: <error descr="Unresolved type Todo">Todo</error>;
      </script>
      
      {something}
    """.trimIndent())
    myFixture.testHighlighting()
  }

  fun testHandler() {
    myFixture.configureByText("Foo.svelte", """
      <script>
        function handleClick() {}
      </script>
      <button on:click={handleClick}>hello</button>
      <svelte:window on:keydown={handleClick}/>
      <button on:click={<error>unknownHandleClick</error>}>hello</button>
    """.trimIndent())
    myFixture.testHighlighting()
  }

  fun testHtmlDirectiveTag() {
    myFixture.configureByText("Foo.svelte", """
      <script>
        let withHtml="<div>hello</div>"
      </script>
      <p>{@html withHtml}</p>
    """.trimIndent())
    myFixture.testHighlighting()
  }

  fun testUnknownTag() {
    myFixture.configureByText("Foo.svelte", """
      <<warning>unknown</warning> />
    """.trimIndent())
    myFixture.testHighlighting()
  }

  fun testUnusedImport() {
    configureComponentWithAttribute("Foo.svelte")
    myFixture.configureByText("Usage.svelte", """
    <script>
      <warning>import Usage from "./Usage.svelte";</warning>
    </script>
    """.trimIndent())
    myFixture.testHighlighting()
  }

  fun testUnusedVariable() {
    myFixture.configureByText("Foo.svelte", """
    <script>
      function handleClick() {
        var <warning>unusedVar</warning> = '1'
        function <warning>unusedFunction</warning>() {}
      }
    </script>
    <button on:click={handleClick}>hello</button>
    """.trimIndent())
    myFixture.testHighlighting()
  }

  fun testSpread() {
    myFixture.configureByText("Foo.svelte", """
      <script>
        import Foo from './Foo.svelte';
        const spread = {};
      </script>
      <Foo {...spread} />
      <Foo {...<error>unknownSpread</error>} />
    """.trimIndent())
    myFixture.testHighlighting()
  }

  fun testConditionIfBlock() {
    myFixture.configureByText("Foo.svelte", """
      <script>
        const test = true;
      </script>
      {#if test || <error>test2</error>}
        <button>test</button>
      {/if}
    """.trimIndent())
    myFixture.testHighlighting()
  }

  fun testConditionIfElse() {
    myFixture.configureByText("Foo.svelte", """
      <script>
        const test = true;
      </script>
      {#if test}
        <button>test</button>
      {:else}
        <br>
      {/if}
    """.trimIndent())
    myFixture.testHighlighting()
  }

  fun testConditionIfElseIf() {
    myFixture.configureByText("Foo.svelte", """
      <script>
        const test = true;
      </script>
      {#if test}
        <button>test</button>
      {:else if test}
        <br>
      {/if}
    """.trimIndent())
    myFixture.testHighlighting()
  }

  fun testEach() {
    myFixture.configureByText("Foo.svelte", """
      <script>
        const cats = [{id:111, name:"test"}];
      </script>
      <ul>
        {#each cats as cat}
          <li><a target="_blank" href="https://www.youtube.com/watch?v={cat.id}">
            {cat.name}
            {cat.<weak_warning>unknownName</weak_warning>}
          </a></li>
        {/each}
      </ul>
    """.trimIndent())
    myFixture.testHighlighting()
  }

  fun testEachWithId() {
    configureComponentWithAttribute("Foo.svelte")
    myFixture.configureByText("Foo.svelte", """
      <script>
        import Foo from './Foo.svelte';
        const cats = [{id:111, name:"test"}];
      </script>
      {#each cats as cat (cat.id)}
        <Foo test={cat.name} />
      {/each}
    """.trimIndent())
    myFixture.testHighlighting()
  }

  fun testAwaitPromise() {
    myFixture.configureByText("Foo.svelte", """
      <script>
        async function myF() {return 1}
        let promise11 = myF()
      </script>
      {#await promise11}
        <p>...waiting</p>
      {:then num11}
        <p>The number is {num11}</p>
      {:catch error}
        <p style="color: red">{error.message}</p>
      {/await}
    """.trimIndent())
    myFixture.testHighlighting()
  }

  fun testShortAwaitPromise() {
    myFixture.configureByText("Foo.svelte", """
      <script>
        async function myF() {return 1}
        let promise11 = myF()
      </script>
      {#await promise11 then value11}
        <p>the value is {value11}</p>
      {/await}
    """.trimIndent())
    myFixture.testHighlighting()
  }

  fun testSnippet() {
    myFixture.configureByText("Hello.svelte", """
      <script lang="ts">
        figure<error descr="Invalid number of arguments, expected 1..2">()</error>;
      </script>
      
      {#snippet figure(image, unused = true)}
        <img src={image.src} alt={image.caption} />
      {/snippet}
      
      {#snippet <warning descr="Unused function another">another</warning>()}<div></div>{/snippet}
      
      <button>
        {@render figure({})}
        {@render figure<weak_warning descr="Invalid number of arguments, expected 1..2">()</weak_warning>}
      </button>
    """.trimIndent())
    myFixture.testHighlighting()
  }

  fun testBlockKeywords() {
    myFixture.configureByText("Foo.svelte", """
      {#each [] <info descr="null">as</info> <info descr="identifiers//parameter">local</info>}{/each}
      {#await {} <info descr="null">then</info> <info descr="identifiers//parameter">local</info>}{/await}
      
      {@<info descr="null">debug</info> <info descr="identifiers//global variable">window</info>}
      {@<info descr="null">html</info> ''}
      {@<info descr="null">const</info> <info descr="identifiers//local variable">x</info> = 1}
    """.trimIndent())
    myFixture.testHighlighting(false, true, false)
  }

  fun testEventsWithModifiers() {
    myFixture.configureByText("Foo.svelte", """
      <script>
        function handleClick() {}
      </script>
      <button on:click|once={handleClick}>
        Click me
      </button>
      <button on:click|once|capture={handleClick}>
        Click me1
      </button>
    """.trimIndent())
    myFixture.testHighlighting()
  }

  fun testMultipleSelect() {
    myFixture.configureByText("Foo.svelte", """
    <script>
      const flavours = [];
    </script>
    <select multiple bind:value={flavours}>
      {#each <error>menu1</error> as flavour}
        <option value={flavour}>
          {flavour}
        </option>
      {/each}
    </select>
    """.trimIndent())
    myFixture.testHighlighting()
  }

  fun testStoreEvaluationLocalJS() = doTestWithLangFromTestNameSuffix(storeEvaluationLocal)

  fun testStoreEvaluationLocalTS() = doTestWithLangFromTestNameSuffix(storeEvaluationLocal)

  private val storeEvaluationLocal = SvelteTestScenario { langExt, langWarning ->
    myFixture.configureBundledSvelte()
    myFixture.configureByText("helpers.ts", """
      import type { Readable } from "svelte/store";
      
      export function acceptNumber(x: number): number {
        return x;
      }
      
      export function acceptStore<T>(x: Readable<T>): Readable<T> {
        return x;
      }
    """.trimIndent())
    val count = "\$count" // to trick Kotlin
    myFixture.configureByText("Foo.svelte", """
      <script lang="$langExt">
        import { writable } from 'svelte/store';
        import { acceptNumber, acceptStore } from "./helpers";
      
        const count = writable(5);
      
        acceptNumber(<$langWarning descr="Argument type Writable<number> is not assignable to parameter type number">count</$langWarning>);
        acceptStore(count);
        acceptNumber($count);
        acceptStore(<$langWarning descr="Argument type number is not assignable to parameter type Readable<unknown>">$count</$langWarning>);
        
	      // noinspection JSCheckFunctionSignatures // todo there's an eval bug only in JS
        acceptNumber({ $count: $count }.$count);
        acceptNumber({ <error descr="Unresolved variable or type $count">$count</error> }.$count); // todo should be resolved, hidden by LSP
      
        count.<$langWarning descr="Unresolved function or method toFixed()">toFixed</$langWarning>();
        $count.toFixed();
      </script>
      
      <div>{acceptNumber(<weak_warning descr="Argument type Writable<number> is not assignable to parameter type number">count</weak_warning>)}</div>
      <div>{acceptStore(count)}</div>
      <div>{acceptNumber($count)}</div>
      <div>{acceptStore(<weak_warning descr="Argument type number is not assignable to parameter type Readable<unknown>">$count</weak_warning>)}</div>
      
      <!-- todo prod IDE evaluates argument to `any | Writable<number>`, which is also wrong, but hides the below error -->
      <div>{acceptNumber(<weak_warning descr="Argument type Writable<number> is not assignable to parameter type number">{ $count: $count }.$count</weak_warning>)}</div>
      <div>{acceptNumber({ <error descr="Unresolved variable or type $count">$count</error> }.$count)}</div>
    """.trimIndent())
    myFixture.testHighlighting()
  }

  fun testStoreEvaluationImportedJS() = doTestWithLangFromTestNameSuffix(storeEvaluationImported)

  fun testStoreEvaluationImportedTS() = doTestWithLangFromTestNameSuffix(storeEvaluationImported)

  private val storeEvaluationImported = SvelteTestScenario { langExt, langWarning ->
    myFixture.configureBundledSvelte()
    myFixture.configureByText("helpers.ts", """
      import type { Readable } from "svelte/store";
      import { writable } from "svelte/store";

      export const count = writable(5);
                  
      export function acceptNumber(x: number): number {
        return x;
      }
      
      export function acceptStore<T>(x: Readable<T>): Readable<T> {
        return x;
      }
    """.trimIndent())
    val count = "\$count" // to trick Kotlin
    myFixture.configureByText("Foo.svelte", """
      <script lang="$langExt" context="module">
        import { acceptNumber, acceptStore, count } from "./helpers";

        count.set(5);
        console.log($count.toFixed()); // bug, todo prevent resolve, has error from LSP
      </script>
      
      <script lang="$langExt">
        acceptNumber(<$langWarning descr="Argument type Writable<number> is not assignable to parameter type number">count</$langWarning>);
        acceptStore(count);
        acceptNumber($count);
        acceptStore(<$langWarning descr="Argument type number is not assignable to parameter type Readable<unknown>">$count</$langWarning>);
        
	      // noinspection JSCheckFunctionSignatures // todo there's an eval bug only in JS
        acceptNumber({ $count: $count }.$count);
        acceptNumber({ <error descr="Unresolved variable or type $count">$count</error> }.$count); // todo should be resolved, hidden by LSP
      
        count.<$langWarning descr="Unresolved function or method toFixed()">toFixed</$langWarning>();
        $count.toFixed();
      </script>
      
      <div>{acceptNumber(<weak_warning descr="Argument type Writable<number> is not assignable to parameter type number">count</weak_warning>)}</div>
      <div>{acceptStore(count)}</div>
      <div>{acceptNumber($count)}</div>
      <div>{acceptStore(<weak_warning descr="Argument type number is not assignable to parameter type Readable<unknown>">$count</weak_warning>)}</div>
      
      <!-- todo prod IDE evaluates argument to `any | Writable<number>`, which is also wrong, but hides the below error -->
      <div>{acceptNumber(<weak_warning descr="Argument type Writable<number> is not assignable to parameter type number">{ $count: $count }.$count</weak_warning>)}</div>
      <div>{acceptNumber({ <error descr="Unresolved variable or type $count">$count</error> }.$count)}</div>
    """.trimIndent())
    myFixture.testHighlighting()
  }

  fun testStoreHighlightUsagesLocalJS() = doTestWithLangFromTestNameSuffix(storeHighlightUsagesLocal)

  fun testStoreHighlightUsagesLocalTS() = doTestWithLangFromTestNameSuffix(storeHighlightUsagesLocal)

  /**
   * @see [SvelteFindUsagesTest]
   */
  private val storeHighlightUsagesLocal = SvelteTestScenario { langExt, _ ->
    myFixture.configureBundledSvelte()
    val count = "\$count" // to trick Kotlin
    myFixture.configureByText("Foo.svelte", """
      <script lang="$langExt">
        import { writable } from "svelte/store";
      
        const <highlight><caret>count</highlight> = writable(5);
      
        <highlight>count</highlight>.set(5);
        console.log($<highlight>count</highlight>.toFixed());
        
        $: $<highlight>count</highlight>.toFixed();
        
	      console.log({ $count }); // the highlight is missing, todo fix
      </script>

      <div>{$<highlight>count</highlight>}</div>
      <div>{<highlight>count</highlight>}</div>
    """.trimIndent())
    JSTestUtils.checkHighlightUsages(myFixture, false)
  }

  fun testStoreHighlightUsagesImportedJS() = doTestWithLangFromTestNameSuffix(storeHighlightUsagesImported)

  fun testStoreHighlightUsagesImportedTS() = doTestWithLangFromTestNameSuffix(storeHighlightUsagesImported)

  /**
   * @see [SvelteFindUsagesTest]
   */
  private val storeHighlightUsagesImported = SvelteTestScenario { langExt, _ ->
    myFixture.configureBundledSvelte()
    myFixture.configureByText("stores.$langExt", """
      import { writable } from "svelte/store";
    
      export const count = writable(5);
    """.trimIndent())
    val count = "\$count" // to trick Kotlin
    myFixture.configureByText("Foo.svelte", """
      <script lang="$langExt">
        import { <highlight>count</highlight> } from "./stores";
      
        <highlight>count</highlight>.set(5);
        console.log($<highlight><caret>count</highlight>.toFixed());
        
        $: $<highlight>count</highlight>.toFixed();
        
	      console.log({ $count }); // the highlight is missing, todo fix
      </script>

      <div>{$<highlight>count</highlight>}</div>
      <div>{<highlight>count</highlight>}</div>
    """.trimIndent())
    JSTestUtils.checkHighlightUsages(myFixture, false)
  }

  fun testStoreUnusedImportsJS() = doTestWithLangFromTestNameSuffix(storeUnusedImports)

  fun testStoreUnusedImportsTS() = doTestWithLangFromTestNameSuffix(storeUnusedImports)

  private val storeUnusedImports = SvelteTestScenario { langExt, _ ->
    myFixture.configureBundledSvelte()
    myFixture.configureByText("stores.$langExt", """
      import { writable } from "svelte/store";
    
      export const count = writable(5);
    """.trimIndent())
    myFixture.configureByText("Foo.svelte", """
      <script lang="$langExt">
        import {
          <warning>count</warning>,
          count as aliasedCount,
          count as aliasedCountInScript,
          count as aliasedCountNoDollar,
        } from './stores';
      
        ${'$'}aliasedCountInScript;
      </script>
      
      <div>{${'$'}aliasedCount}</div>
      <div>{aliasedCountNoDollar}</div>
    """.trimIndent())
    myFixture.testHighlighting()
  }

  fun testStoreShorthandAssignmentJS() = doTestWithLangFromTestNameSuffix(storeShorthandAssignment)

  fun testStoreShorthandAssignmentTS() = doTestWithLangFromTestNameSuffix(storeShorthandAssignment)

  private val storeShorthandAssignment = SvelteTestScenario { langExt, _ ->
    myFixture.configureBundledSvelte()
    val store = "\$store" // to trick Kotlin
    myFixture.configureByText("Foo.svelte", """
      <script lang="$langExt">
        import { writable } from "svelte/store";
        const store = writable();
        console.log($store); // TODO required before fixing JSUnusedAssignment inspection
        $store = 1;
        console.log($store);
        <error>store</error> = writable();
      </script>
      {$store}
    """.trimIndent())
    myFixture.testHighlighting()
  }

  fun testArrayReassignment() {
    // TODO check if resolved variable is declared directly in instance script
    myFixture.configureByText("Foo.svelte", """
      <script>
        let users = [];

        function addUser(user) {
            users.push(user);
            users = users;
        }
      </script>
      <button on:click={addUser}>Hello</button>
    """.trimIndent())
    myFixture.testHighlighting()
  }

  fun testNullablePropFallback() {
    myFixture.configureByText("Foo.svelte", """
      <script>
        export let nullable = null;

        if (nullable) {
            nullable = {};
        }
      </script>
    """.trimIndent())
    myFixture.testHighlighting()
  }

  fun testPropIsInitialised() {
    myFixture.configureByText("Foo.svelte", """
      <script lang="ts">
        export let data: { foo: true };
      
        data.foo;
      </script>
      
      {data.foo}
    """.trimIndent())
    myFixture.testHighlighting()
  }

  fun testReactiveLabelKeywordJS() {
    myFixture.configureByText("Foo.svelte", """
      <script>
        <info descr="reactive">$</info>: {
          break <info descr="reactive">$</info>;
        }
        
        <info descr="label">unrelated</info>: 1;
      </script>
    """.trimIndent())
    myFixture.testHighlighting(false, true, false)
  }

  fun testReactiveLabelKeywordTS() {
    myFixture.configureByText("Foo.svelte", """
      <script lang="ts">
        <info descr="reactive">$</info>: {
          break <info descr="reactive">$</info>;
        }
        
        <info descr="label">unrelated</info>: 1;
      </script>
    """.trimIndent())
    myFixture.testHighlighting(false, true, false)
  }

  fun testNoRedundancyFromReactiveStatement() {
    myFixture.configureByText("Foo.svelte", """
      <script lang="ts">
        let init = { foo: true };

        $: {
            init = { foo: true };
        }

        init.foo;
      </script>
      
      {init.foo}
    """.trimIndent())
    myFixture.testHighlighting()
  }

  fun testReactiveStatementsIsolatedForNullChecks() {
    myFixture.configureByText("Foo.svelte", """
      <script lang="ts">
        let someOtherVar = false;
        let user0: { number: number; } | null = null;
        let user: { number: number; } | null = null;
        let test: number = 0;
        user0.number; // todo report "user0 is null"

        $: if (someOtherVar) {
          if (user) {
            test = user.number;
            user.number.toFixed();
          }
        }
      </script>
    """.trimIndent())
    myFixture.testHighlighting()
  }

  fun testReactiveDeclarationReferenceJS() { // WEB-59293 + WEB-63611
    myFixture.configureByText("Foo.svelte", """
      <script>
        $: doubled = 2;
        $: quadrupled = doubled * 2;
        
        <error descr="Unresolved variable or type reallyUnresolved">reallyUnresolved</error>;

        function update() {
          doubled = 42;
        }
      </script>

      <p on:click={update}>{doubled}</p>
      <p>{<error descr="Unresolved variable or type reallyUnresolved">reallyUnresolved</error>}</p>
    """.trimIndent())
    myFixture.testHighlighting()
  }

  fun testReactiveDeclarationReferenceTS() { // WEB-59293 + WEB-63611
    myFixture.configureByText("Foo.svelte", """
      <script lang="ts">
        $: doubled = 2;
        $: quadrupled = doubled * 2;
        
        <error descr="Unresolved variable or type reallyUnresolved">reallyUnresolved</error>;
        
        function update() {
          doubled = 42;
        }
      </script>

      <p on:click={update}>{doubled}</p>
      <p>{<error descr="Unresolved variable or type reallyUnresolved">reallyUnresolved</error>}</p>
    """.trimIndent())
    myFixture.testHighlighting()
  }

  fun testReactiveDeclarationDestructuredJS() {
    myFixture.configureByText("Foo.svelte", """
      <script>
        $: ({ foo1 } = { foo1: 1 });
        <warning>dollar</warning>: ({ <error descr="Unresolved variable or type foo2">foo2</error> } = { foo2: 1 });
        ({ <error descr="Unresolved variable or type foo3">foo3</error> } = { foo3: 1 });
      
        // todo actually implement resolve on IDE side
        <error descr="Unresolved variable or type foo1">foo1</error>;
        <error descr="Unresolved variable or type foo2">foo2</error>;
      </script>
      
      <p>{<error descr="Unresolved variable or type foo1">foo1</error>}</p>
      <p>{<error descr="Unresolved variable or type foo2">foo2</error>}</p>
    """.trimIndent())
    myFixture.testHighlighting()
  }

  fun testReactiveDeclarationDestructuredTS() {
    myFixture.configureByText("Foo.svelte", """
      <script lang="ts">
        $: ({ foo1 } = { foo1: 1 });
        <warning>dollar</warning>: ({ <error descr="Unresolved variable or type foo2">foo2</error> } = { foo2: 1 });
        ({ <error descr="Unresolved variable or type foo3">foo3</error> } = { foo3: 1 });
      
        // todo actually implement resolve on IDE side
        <error descr="Unresolved variable or type foo1">foo1</error>;
        <error descr="Unresolved variable or type foo2">foo2</error>;
      </script>
      
      <p>{<error descr="Unresolved variable or type foo1">foo1</error>}</p>
      <p>{<error descr="Unresolved variable or type foo2">foo2</error>}</p>
    """.trimIndent())
    myFixture.testHighlighting()
  }

  fun testBindDirective() {
    myFixture.configureByText("Foo.svelte", """
      <script>
        let title = "text";
        let otherTitle = "text2";
      </script>
      <button bind:title>Click me</button>
      <button bind:<error>unknownTitle</error>>Click me</button>
      <button bind:name={otherTitle}>Click me</button>
      <button bind:name={<error>unknownTitle</error>}>Click me</button>
    """.trimIndent())
    myFixture.testHighlighting()
  }

  fun testClassDirective() {
    myFixture.configureByText("Foo.svelte", """
      <script>
        let active = true;
      </script>
      <button class:active>Click me</button>
      <style>
        .active {
          background-color: cadetblue;
        }
      </style>
    """.trimIndent())
    myFixture.testHighlighting()
  }

  fun testUseDirective() {
    myFixture.configureByText("Foo.svelte", """
      <script>
        function action() {}
      </script>
      <button use:action>Click me</button>
      <button use:action={{param: true}}>Click me</button>
      <button use:<error>unknownAction</error>>Click me</button>
      <button use:<error>unknownAction</error>={{param: true}}>Click me</button>
      <button use:<error>unknownAction</error>.nested>Click me</button>
    """.trimIndent())
    myFixture.testHighlighting()
  }

  fun testTransitionDirective() {
    myFixture.configureByText("Foo.svelte", """
      <script>
        function fade() {}
      </script>
      <button transition:fade>Click me</button>
      <button transition:fade|local>Click me</button>
      <button transition:fade={{param: true}}>Click me</button>
      <button transition:fade|local={{param: true}}>Click me</button>
      <button transition:<error>unknownFade</error>>Click me</button>
      <button transition:<error>unknownFade</error>|local>Click me</button>
      <button transition:<error>unknownFade</error>={{param: true}}>Click me</button>
      <button transition:<error>unknownFade</error>|local={{param: true}}>Click me</button>
    """.trimIndent())
    myFixture.testHighlighting()
  }

  fun testStyleDirective() {
    myFixture.configureByText("Foo.svelte", """
      <button style:color="red">Hello</button>
    """.trimIndent())
    myFixture.testHighlighting()
  }

  fun testLetDirectiveShorthand() {
    myFixture.configureByText("Foo.svelte", """
      <div let:opened>Lorem ipsum {opened}</div>
    """.trimIndent())
    myFixture.testHighlighting()
  }

  fun testExpressionInStyleAttribute() {
    myFixture.configureByText("Foo.svelte", """
      <div style="--custom: {'dynamic'}"></div>
      <div style="transform: translate(0, {100}%)"></div>
    """.trimIndent())
    myFixture.testHighlighting()
  }

  fun testStyleTagErrorsOriginatingInInlineStyleSuppressed() {
    myFixture.configureByText("Foo.svelte", """
      <script>
        let bgOpacity = 0.5;
        let myColor = bgOpacity < 0.6 ? "#000" : "#fff";
      </script>
  
      <p style="--myColor: {myColor}; --opacity: {bgOpacity};">This is a paragraph.</p>
  
      <style>
        p {
          color: var(--myColor);
          background: rgba(255, 62, 0, var(--opacity));
        }
      </style>
    """.trimIndent())
    myFixture.testHighlighting()
  }

  fun testConstTagDisallowNestedAssignment() {
    myFixture.configureByText("Foo.svelte", """
      {#if true}
        {@const y = <error descr="Unresolved variable or type x">x</error> = 3}
        {x + y}
      {/if}
    """.trimIndent())
    myFixture.testHighlighting()
  }

  fun testConstTagReassignment() {
    myFixture.configureByText("Foo.svelte", """
      {#if true}
        {@const area = 1}
        <p on:click={() => <error descr="Attempt to assign to const or readonly variable">area</error> = 50}>
          {area}
        </p>
      {/if}
    """.trimIndent())
    myFixture.testHighlighting()
  }

  fun testConstTagUseBeforeDeclare() {
    myFixture.configureByText("Foo.svelte", """
      {#if true}
        <p>{area}</p>
        {@const area = 1 + areaLast}
        {@const areaLast = 2}
      {/if}
    """.trimIndent())
    myFixture.testHighlighting()
  }

  fun testJSOverSvelte() {
    myFixture.configureByText("usage.svelte", """
      <script>console.log("hello")</script>
    """.trimIndent())
    myFixture.configureByText("usage.js", """
      export function hello() {return "hello"}
    """.trimIndent())
    myFixture.configureByText("+page.svelte", """
      <script>
        import {hello} from './usage'
        console.log(hello());
      </script>
    """.trimIndent())
    myFixture.testHighlighting()
  }

  fun testTSResolveToAmbientModule() {
    myFixture.configureByText("friend.d.ts", """
      export {}
      
      declare module "friend" {
        export interface Options {
          orientation?: "p" | "portrait" | "l" | "landscape";
          unit?: "pt" | "px" | "in" | "mm" | "cm" | "ex" | "em" | "pc";
          format?: string | number[];
        }
      
        export interface HTMLOptions {
          callback?: (doc: Builder) => void;
          width?: number;
        }
      
        export class Builder {
          constructor(options?: Options);
          constructor(
            orientation?: "p" | "portrait" | "l" | "landscape",
            unit?: "pt" | "px" | "in" | "mm" | "cm" | "ex" | "em" | "pc",
            format?: string | number[],
          );
          
          save(filename: string, options: { returnPromise: true }): Promise<void>;
          save(filename?: string): Builder;
      
          html(src: string | HTMLElement, options?: HTMLOptions): Promise<any>;
        }
      
        export default Builder;
      }
    """.trimIndent())

    myFixture.configureByText("Usage.svelte", """
      <script lang="ts">
        import {Builder} from "friend"
      
        type Data = { id: string }
      
        export let data: Data
        let content: HTMLElement
      
        function generate() {
          const doc = new Builder({orientation: "l", unit: "cm"});
          doc.html(content, {
            width: 6,
            callback: (doc) => doc.save(data.id),
          })
        }
      </script>
      
      <button on:click={generate}>Generate</button>
    """.trimIndent())
    myFixture.testHighlighting()
  }


  fun testDebugTagNoCommaExpressionWarnings() {
    myFixture.configureByText("Commas.svelte", """
      <script>
        const a = 0, b = 1;
        const x = (a<warning descr="Comma expression">,</warning> b);
      </script>

      {#if true}
        {@const y = (a<warning descr="Comma expression">,</warning> b)}
        {#if a<warning descr="Comma expression">,</warning> b}maybe{/if}
        {a, b<warning descr="Comma expression">,</warning> y}
        {@debug a, b}
        {@debug (a, b, x, y)}
        {@debug a, b, (a, b)} <!-- todo disallow arbitrary expressions in @debug -->
      {/if}
    """.trimIndent())
    myFixture.testHighlighting()
  }

  fun testHeadTag() {
    myFixture.configureByText("head.svelte", """
      <script>
        import Head from "./head.svelte";
      </script>
      <<warning descr="Missing required 'title' element">head</warning>></head>
      <Head></Head>
    """.trimIndent())
    myFixture.testHighlighting()
  }

  fun testAreaTag() {
    myFixture.configureByText("area.svelte", """
      <script>
        import Area from "./area.svelte";
      </script>
      <div>
        <<warning descr="Missing required 'alt' attribute">area</warning>></area>
        <Area></Area>
      </div>
    """.trimIndent())
    myFixture.testHighlighting()
  }

  fun testImportFromModuleScriptJS() {
    myFixture.addFileToProject("Helper.svelte", """
      <script context="module">
        export class Inner {
          foo = true;
        }
      </script>
    """.trimIndent())

    myFixture.configureByText("Foo.svelte", """
      <script>
        import { Inner, Inner as Renamed, <weak_warning descr="Cannot resolve symbol 'Wrong'">Wrong</weak_warning> } from "./Helper.svelte";
      
        new Inner;
        new Renamed;
        new Wrong;
      </script>
    """.trimIndent())
    myFixture.testHighlighting()

    myFixture.configureByText("bar.js", """
      import { Inner, Inner as Renamed, <weak_warning descr="Cannot resolve symbol 'Wrong'">Wrong</weak_warning> } from "./Helper.svelte";
    
      new Inner;
      new Renamed;
      new Wrong;
    """.trimIndent())
    myFixture.testHighlighting()
  }

  fun testImportFromModuleScriptTS() {
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
        import { Inner, Inner as Renamed, <error descr="Cannot resolve symbol 'Wrong'">Wrong</error>, type Foo } from "./Helper.svelte";
      
        new Inner;
        new Renamed;
        new Wrong;
      
        const x: Foo = {bar: 42};
      </script>
      
      {x}
    """.trimIndent())
    myFixture.testHighlighting()

    myFixture.configureByText("bar.ts", """
      import { Inner, Inner as Renamed, <error descr="Cannot resolve symbol 'Wrong'">Wrong</error>, type Foo } from "./Helper.svelte";
    
      new Inner;
      new Renamed;
      new Wrong;
    
      const x: Foo = {bar: 42};
      
      console.log(x);
    """.trimIndent())
    myFixture.testHighlighting()
  }

  companion object {
    fun configureDefaultLocalInspectionTools(): List<InspectionProfileEntry> {
      val l = mutableListOf<LocalInspectionTool>()
      l.add(TypeScriptMissingConfigOptionInspection())
      l.add(RequiredAttributesInspection())
      l.add(HtmlRequiredAltAttributeInspection())
      l.add(HtmlRequiredTitleElementInspection())
      l.add(JSConstantReassignmentInspection())
      l.add(ES6UnusedImportsInspection())
      l.add(JSUnresolvedReferenceInspection())
      l.add(JSValidateTypesInspection())
      l.add(JSIncompatibleTypesComparisonInspection())
      l.add(SillyAssignmentJSInspection())
      l.add(UnnecessaryLabelJSInspection())
      l.add(CommaExpressionJSInspection())
      val functionSignaturesInspection = JSCheckFunctionSignaturesInspection()
      functionSignaturesInspection.myCheckGuessedTypes = true
      l.add(functionSignaturesInspection)
      l.add(JSValidateJSDocInspection())
      l.add(JSUndeclaredVariableInspection())
      l.add(XmlDuplicatedIdInspection())
      l.add(XmlInvalidIdInspection())
      l.add(HtmlUnknownTagInspection())
      l.add(CheckEmptyTagInspection())
      l.add(HtmlUnknownBooleanAttributeInspection())
      l.add(HtmlUnknownAttributeInspection())
      l.add(HtmlWrongAttributeValueInspection())
      l.add(XmlUnboundNsPrefixInspection())
      l.add(JSUnusedLocalSymbolsInspection())
      l.add(JSPotentiallyInvalidConstructorUsageInspection())
      l.add(JSUnnecessarySemicolonInspection())
      l.add(JSLastCommaInArrayLiteralInspection())
      l.add(JSLastCommaInObjectLiteralInspection())
      l.add(JSReferencingMutableVariableFromClosureInspection())
      l.add(JSPotentiallyInvalidUsageOfThisInspection())
      l.add(JSPotentiallyInvalidUsageOfClassThisInspection())
      l.add(JSPotentiallyInvalidTargetOfIndexedPropertyAccess())
      l.add(JSUndefinedPropertyAssignmentInspection())
      l.add(JSClosureCompilerSyntaxInspection())
      l.add(JSCommentMatchesSignatureInspection())
      l.add(JSFileReferencesInspection())
      l.add(JSUnusedGlobalSymbolsInspection())
      l.add(JSAnnotatorInspection())
      l.add(JSUnusedAssignmentInspection())
      l.add(UnreachableCodeJSInspection())
      l.add(TypeScriptValidateTypesInspection())
      l.add(TypeScriptUnresolvedReferenceInspection())
      l.add(TypeScriptCheckImportInspection())
      l.add(TypeScriptDuplicateUnionOrIntersectionTypeInspection())
      l.add(PointlessBooleanExpressionJSInspection())
      l.add(TypeScriptValidateGenericTypesInspection())
      l.add(TypeScriptRedundantGenericTypeInspection())

      // CF inspections
      l.add(JSSuspiciousTypeGuardInspection())
      l.add(JSObjectNullOrUndefinedInspection())
      l.add(SvelteUnresolvedComponentInspection())
      return l
    }
  }
}
