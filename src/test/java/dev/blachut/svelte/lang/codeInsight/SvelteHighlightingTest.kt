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
import com.intellij.lang.javascript.modules.ES6CheckImportInspection
import com.intellij.lang.javascript.modules.TypeScriptCheckImportInspection
import com.intellij.lang.typescript.inspections.*
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.xml.util.XmlDuplicatedIdInspection
import com.intellij.xml.util.XmlInvalidIdInspection
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
                    const spread = [];
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

    companion object {
        fun configureDefaultLocalInspectionTools(): List<InspectionProfileEntry> {
            val l = mutableListOf<LocalInspectionTool>()
            l.add(RequiredAttributesInspection())
            l.add(JSConstantReassignmentInspection())
            l.add(ES6UnusedImportsInspection())
            l.add(JSUnresolvedFunctionInspection())
            l.add(JSUnresolvedVariableInspection())
            l.add(JSValidateTypesInspection())
            l.add(JSIncompatibleTypesComparisonInspection())
            val functionSignaturesInspection = JSCheckFunctionSignaturesInspection()
            functionSignaturesInspection.myCheckGuessedTypes = true
            l.add(functionSignaturesInspection)
            l.add(JSValidateJSDocInspection())
            l.add(JSUndeclaredVariableInspection())
            l.add(XmlDuplicatedIdInspection())
            l.add(ES6CheckImportInspection())
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
            l.add(JSReferencingArgumentsOutsideOfFunctionInspection())
            l.add(JSAnnotatorInspection())
            l.add(JSUnusedAssignmentInspection())
            l.add(UnreachableCodeJSInspection())
            l.add(TypeScriptValidateTypesInspection())
            l.add(TypeScriptValidateJSTypesInspection())
            l.add(TypeScriptUnresolvedFunctionInspection())
            l.add(TypeScriptUnresolvedVariableInspection())
            l.add(TypeScriptAccessibilityCheckInspection())
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
