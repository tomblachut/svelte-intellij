package dev.blachut.svelte.lang.codeInsight

import com.intellij.codeInsight.daemon.impl.analysis.XmlUnboundNsPrefixInspection
import com.intellij.codeInspection.InspectionProfileEntry
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.htmlInspections.HtmlUnknownAttributeInspection
import com.intellij.codeInspection.htmlInspections.HtmlUnknownBooleanAttributeInspection
import com.intellij.codeInspection.htmlInspections.HtmlUnknownTagInspection
import com.intellij.codeInspection.htmlInspections.RequiredAttributesInspection
import com.intellij.lang.javascript.inspection.JSObjectNullOrUndefinedInspection
import com.intellij.lang.javascript.inspection.JSSuspiciousTypeGuardInspection
import com.intellij.lang.javascript.inspection.JSUnusedAssignmentInspection
import com.intellij.lang.javascript.inspections.*
import com.intellij.lang.javascript.modules.TypeScriptCheckImportInspection
import com.intellij.lang.typescript.inspections.*
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.xml.util.XmlDuplicatedIdInspection
import com.intellij.xml.util.XmlInvalidIdInspection
import com.sixrr.inspectjs.assignment.SillyAssignmentJSInspection
import com.sixrr.inspectjs.confusing.CommaExpressionJSInspection
import com.sixrr.inspectjs.confusing.PointlessBooleanExpressionJSInspection
import com.sixrr.inspectjs.validity.UnreachableCodeJSInspection
import dev.blachut.svelte.lang.inspections.SvelteUnresolvedComponentInspection

class SvelteHighlightingTest : BasePlatformTestCase() {

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
        myFixture.checkResult(
            """
                <script>
                    import Foo from "./Foo.svelte";
                </script>

                <Foo />
                """.trimIndent()
        )
        myFixture.testHighlighting() // no errors after import
    }

    fun testNamespacedComponent() {
        // TODO Support namespaced components WEB-61636
        myFixture.configureByText("Usage.svelte", """
            <<error descr=""><error descr="Cannot resolve symbol 'Unresolved'">Unresolved</error></error> test="" />
            <Also.Unresolved test="" />
        """.trimIndent())
        myFixture.testHighlighting()
    }

    fun testComponentAttribute() {
        configureComponentWithAttribute("Foo.svelte")
        myFixture.configureByText("Usage.svelte",
            """
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
        """.trimIndent())
        myFixture.testHighlighting()
    }

    fun testHandler() {
        myFixture.configureByText("Foo.svelte",
            """
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
        myFixture.configureByText("Foo.svelte",
            """
                <script>
                    let withHtml="<div>hello</div>"
                </script>
                <p>{@html withHtml}</p>
                """.trimIndent())
        myFixture.testHighlighting()
    }

    fun testUnknownTag() {
        myFixture.configureByText("Foo.svelte", "<<warning>unknown</warning> />")
        myFixture.testHighlighting()
    }

    fun testUnusedImport() {
        configureComponentWithAttribute("Foo.svelte")
        myFixture.configureByText("Usage.svelte",
            """
                <script>
                    <warning>import Usage from "./Usage.svelte";</warning>
                </script>
                """.trimIndent())
        myFixture.testHighlighting()
    }

    fun testUnusedVariable() {
        myFixture.configureByText("Foo.svelte",
            """
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
        myFixture.configureByText("Foo.svelte",
            """
                <script>
                    import Foo from './Foo.svelte';
                    const spread = {};
                </script>
                <Foo {...spread} />
                <Foo {...<error>unknownSpread</error>} />
                """.trimIndent()
        )
        myFixture.testHighlighting()
    }

    fun testConditionIfBlock() {
        myFixture.configureByText("Foo.svelte",
            """
                <script>const test = true;</script>
                {#if test || <error>test2</error>}
                <button>
                test
                </button>
                {/if}
                """.trimIndent())
        myFixture.testHighlighting()
    }

    fun testConditionIfElse() {
        myFixture.configureByText("Foo.svelte",
            """
                <script>const test = true;</script>
                {#if test}
                <button>
                test
                </button>
                {:else}
                <br>
                {/if}
                """.trimIndent())
        myFixture.testHighlighting()
    }

    fun testConditionIfElseIf() {
        myFixture.configureByText("Foo.svelte",
            """
                <script>const test = true;</script>
                {#if test}
                <button>
                test
                </button>
                {:else if test}
                <br>
                {/if}
                """.trimIndent())
        myFixture.testHighlighting()
    }

    fun testEach() {
        myFixture.configureByText("Foo.svelte",
            """
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
        myFixture.configureByText("Foo.svelte",
            """
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
        myFixture.configureByText("Foo.svelte",
            """
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
        myFixture.configureByText("Foo.svelte",
            """
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

    fun testEventsWithModifiers() {
        myFixture.configureByText("Foo.svelte",
            """
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
        myFixture.configureByText("Foo.svelte",
            """
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

    fun testStoreShorthandAssignment() {
        myFixture.configureByText("Foo.svelte",
            """
                <script>
                    import { writable } from 'svelte/store';
                    const store = writable();
                    console.log(${"$"}store); // TODO required before fixing JSUnusedAssignment inspection
                    ${"$"}store = 1;
                    console.log(${"$"}store);
                    <error>store</error> = writable();
                </script>
                {${"$"}store}
                """.trimIndent())
        myFixture.testHighlighting()
    }

    fun testArrayReassignment() {
        // TODO check if resolved variable is declared directly in instance script
        myFixture.configureByText("Foo.svelte",
            """
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

    fun testNoRedundancyFromReactiveStatement() {
        myFixture.configureByText("Foo.svelte", """
            <script lang="ts">
                let init = { foo: true };

                ${'$'}: {
                    init = { foo: true };
                }

                init.foo;
            </script>
            
            {init.foo}
        """.trimIndent())
        myFixture.testHighlighting()
    }

    fun testReactiveDeclarationReference() {
        myFixture.configureByText("Foo.svelte", """
            <script lang="ts">
                ${'$'}: doubled = 2;
                ${'$'}: quadrupled = doubled * 2;
            </script>

            <p>{doubled}</p>
        """.trimIndent())
        myFixture.testHighlighting()
    }

    fun testBindDirective() {
        myFixture.configureByText("Foo.svelte",
                                  """
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
        myFixture.configureByText("Foo.svelte",
            """
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
        myFixture.configureByText("Foo.svelte",
            """
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
        myFixture.configureByText("Foo.svelte",
            """
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

    fun testLetDirectiveShorthand() {
        myFixture.configureByText("Foo.svelte",
            """
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


    companion object {
        fun configureDefaultLocalInspectionTools(): List<InspectionProfileEntry> {
            val l = mutableListOf<LocalInspectionTool>()
            l.add(RequiredAttributesInspection())
            l.add(JSConstantReassignmentInspection())
            l.add(ES6UnusedImportsInspection())
            l.add(JSUnresolvedReferenceInspection())
            l.add(JSValidateTypesInspection())
            l.add(JSIncompatibleTypesComparisonInspection())
            l.add(SillyAssignmentJSInspection())
            l.add(CommaExpressionJSInspection())
            val functionSignaturesInspection = JSCheckFunctionSignaturesInspection()
            functionSignaturesInspection.myCheckGuessedTypes = true
            l.add(functionSignaturesInspection)
            l.add(JSValidateJSDocInspection())
            l.add(JSUndeclaredVariableInspection())
            l.add(XmlDuplicatedIdInspection())
            l.add(XmlInvalidIdInspection())
            l.add(HtmlUnknownTagInspection())
            l.add(HtmlUnknownBooleanAttributeInspection())
            l.add(HtmlUnknownAttributeInspection())
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
            l.add(TypeScriptValidateJSTypesInspection())
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
