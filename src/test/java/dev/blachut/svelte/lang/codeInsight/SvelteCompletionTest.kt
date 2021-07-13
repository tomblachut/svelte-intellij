package dev.blachut.svelte.lang.codeInsight

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.testFramework.UsefulTestCase
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class SvelteCompletionTest : BasePlatformTestCase() {
    fun testContextModuleAttribute() {
        myFixture.configureByText("foo.svelte", "<script c<caret>></script>")
        hasElements(myFixture.completeBasic(), "context=\"module\"")
    }

    fun testExistContextAttribute() {
        myFixture.configureByText("foo.svelte", "<script context=\"module\" c<caret>></script>")
        noElements(myFixture.completeBasic(), "context=\"module\"")
    }

    fun testStyleLang() {
        myFixture.configureByText("foo.svelte", "<style lang=\"<caret>\"></style>")
        hasElements(myFixture.completeBasic(), "css")
    }

    fun testSimpleTagAttribute() {
        myFixture.configureByText("foo.svelte", "<div c<caret>></div>")
        noElements(myFixture.completeBasic(), "context=\"module\"")
    }

    fun testSimpleTag() {
        myFixture.configureByText("Hello.svelte", "<h1>Test</h1>")
        myFixture.configureByText("foo.svelte", "<<caret>")
        hasElements(myFixture.completeBasic(), "div", "main", "svelte:body", "svelte:self")
    }

    fun testSimpleTagNested() {
        myFixture.configureByText("Hello.svelte", "<h1>Test</h1>")
        myFixture.configureByText("foo.svelte", "<div><<caret></div>")
        hasElements(myFixture.completeBasic(), "div", "svelte:body", "svelte:self")
    }

    fun testSimpleSvelteNamespace() {
        myFixture.configureByText("Hello.svelte", "<h1>Test</h1>")
        myFixture.configureByText("foo.svelte", "<svelte:<caret>")
        hasElements(myFixture.completeBasic(), "body", "self")
    }

    fun testComponentImportNoScript() {
        myFixture.configureByText("Hello.svelte", "<h1>Test</h1>")
        myFixture.configureByText("Usage.svelte", "<h1><Hel<caret></h1>")
        myFixture.completeBasic()
        myFixture.checkResult(
            """
                <script>
                    import Hello from "./Hello.svelte";
                </script>

                <h1><Hello</h1>
                """.trimIndent())
    }

    fun testComponentImportWithScript() {
        myFixture.configureByText("Hello.svelte", "<h1>Test</h1>")
        myFixture.configureByText("Usage.svelte",
            """
                <h1><Hel<caret></h1>
                <script>
                    console.log("hello")
                </script>
                """.trimIndent())
        myFixture.completeBasic()
        myFixture.checkResult(
            """
                <h1><Hello</h1>
                <script>
                    import Hello from "./Hello.svelte";
                    console.log("hello")
                </script>
                """.trimIndent())
    }

    fun testComponentsTopLevel() {
        myFixture.configureByText("Hello1.svelte", "<h1>Test</h1>")
        myFixture.configureByText("Hello2.svelte", "<h1>Test</h1>")
        myFixture.configureByText("Usage.svelte", "<Hel<caret>")
        hasElements(myFixture.completeBasic(), "Hello1", "Hello2")
    }

    fun testComponentExportedAttributes() {
        myFixture.configureByText("Hello1.svelte",
            """
                <script>
                    export let hello11;
                    let hello11NotAvailable = 10;
                </script>
                """.trimIndent())
        myFixture.configureByText("Hello2.svelte",
            """
                <script>
                    export let hello22;
                    let hello22NotAvailable = 10;
                </script>
                """.trimIndent())

        myFixture.configureByText("Usage.svelte",
                                  """
                <script>
                    import Hello1 from "./Hello1.svelte";
                </script>
                <Hello1 <caret>
                """.trimIndent())
        val items = myFixture.completeBasic()
        hasElements(items, "slot", "hello11")
        noElements(items, "hello11NotAvailable", "hello22NotAvailable", "hello22")
    }

    fun testReactiveStatement() {
        myFixture.configureByText("Test.svelte", """
            <script>
                export let hello11 = "src"
                let hello11Local = "src";
                $: hello11Reactive = 20;

                $: true && hello<caret>
            </script>
        """.trimIndent())
        val items = myFixture.completeBasic()
        hasElements(items, "hello11", "hello11Local", "hello11Reactive")
        UsefulTestCase.assertSize(3, items)
    }

    fun testInterpolation() {
        myFixture.configureByText("Test.svelte", """
            <script>
                export let hello11;
                let hello11Local = 10;
                $: hello11Reactive = 20;
            </script>
            <div>{h<caret>}</div>
        """.trimIndent())
        val items = myFixture.completeBasic()
        hasElements(items, "hello11", "hello11Local", "hello11Reactive")
    }

    fun testInterpolationInAttribute() {
        myFixture.configureByText("Test.svelte", """
            <script>
                export let hello11 = "src"
                let hello11Local = "src";
                $: hello11Reactive = 20;
            </script>
            <img src={hello<caret>}>
        """.trimIndent())
        val items = myFixture.completeBasic()
        hasElements(items, "hello11", "hello11Local", "hello11Reactive")
        UsefulTestCase.assertSize(3, items)
    }

    fun testKeywords() {
        myFixture.configureByText("Test.svelte", """
             <div>{<caret>}</div>
        """.trimIndent())
        val items = myFixture.completeBasic()
        hasElements(items, "#if", "#await", "@html")
    }

    fun testKeywords2() {
        myFixture.configureByText("Test.svelte", """
             <div>{#aw<caret>}</div>
        """.trimIndent())
        val items = myFixture.completeBasic()
        assertNull(items)
        myFixture.checkResult("""
             <div>{#await <caret>}</div>
        """.trimIndent())
    }

    fun testMustache() {
        myFixture.configureByText("Hello.svelte",
            """
                <script></script>
                {#if <caret>}
                {/if}
                """.trimIndent()
        )
        hasElements(myFixture.completeBasic(), "true", "false")
    }

    fun testDirectivesOnElement() {
        myFixture.configureByText("Usage.svelte", "<h1 <caret>>Test</h1>")
        val items = myFixture.completeBasic()
        hasElements(items, "bind:", "on:", "class:", "use:", "transition:", "in:", "out:", "animate:", "let:")
    }

    fun testDirectivesOnComponent() {
        myFixture.configureByText("Usage.svelte", "<Box <caret>>Test</Box>")
        val items = myFixture.completeBasic()
        hasElements(items, "bind:", "on:", "let:")
        noElements(items, "class:", "use:", "transition:", "in:", "out:", "animate:")
    }

    fun testBindDirective() {
        myFixture.configureByText("Usage.svelte",
            """
                <script>
                    let localVariable = "localVariableValue"
                </script>
                <h1 bind:<caret>>Hello</h1>
                """.trimIndent()
        )
        val items = myFixture.completeBasic()
        hasElements(items, "title")
        noElements(items, "onclick", "localVariable")
    }

    fun testOnDirective() {
        myFixture.configureByText("Usage.svelte",
            """
                <script>
                    let localVariable = "localVariableValue"
                </script>
                <h1 on:<caret>>Hello</h1>
                """.trimIndent()
        )
        val items = myFixture.completeBasic()
        hasElements(items, "click")
        noElements(items, "onclick", "localVariable")
    }

    fun testClassDirective() {
        myFixture.configureByText("Usage.svelte",
            """
                <script>
                    let localVariable = "localVariableValue"
                </script>
                <h1 class:<caret>>Hello</h1>
                <style>
                    .localClass {}
                </style>
                """.trimIndent()
        )
        val items = myFixture.completeBasic()
        hasElements(items, "localVariable", "localClass")
    }

    fun testUseDirective() {
        myFixture.configureByText("Usage.svelte",
            """
                <script>
	                function action() {}
                </script>
                <h1 use:<caret>>Hello</h1>
                """.trimIndent()
        )
        val items = myFixture.completeBasic()
        hasElements(items, "action")
    }

    fun testTransitionDirective() {
        myFixture.configureByText("Usage.svelte",
            """
                <script>
	                function fade() {}
                </script>
                <h1 transition:<caret>>Hello</h1>
                """.trimIndent()
        )
        val items = myFixture.completeBasic()
        hasElements(items, "fade")
    }

    fun testInDirective() {
        myFixture.configureByText("Usage.svelte",
            """
                <script>
	                function fade() {}
                </script>
                <h1 in:<caret>>Hello</h1>
                """.trimIndent()
        )
        val items = myFixture.completeBasic()
        hasElements(items, "fade")
    }

    fun testOutDirective() {
        myFixture.configureByText("Usage.svelte",
            """
                <script>
	                function fade() {}
                </script>
                <h1 out:<caret>>Hello</h1>
                """.trimIndent()
        )
        val items = myFixture.completeBasic()
        hasElements(items, "fade")
    }

    fun testAnimateDirective() {
        myFixture.configureByText("Usage.svelte",
            """
                <script>
	                function fade() {}
                </script>
                <h1 animate:<caret>>Hello</h1>
                """.trimIndent()
        )
        val items = myFixture.completeBasic()
        hasElements(items, "fade")
    }

    fun testPrefetchAttribute() {
        myFixture.configureByText("Test.svelte", """
            <a svp<caret>>Hello</a>
        """.trimIndent())
        myFixture.completeBasic()
        myFixture.checkResult("""
            <a sveltekit:prefetch<caret>>Hello</a>
        """.trimIndent())
    }

    private fun checkElements(items: Array<LookupElement>, expected: Boolean, vararg variants: String) {
        val toCheck = setOf(*variants)
        val matched = mutableSetOf<String>()
        for (e in items) {
            val lookupString = e.lookupString
            if (toCheck.contains(lookupString)) {
                matched.add(lookupString)
            }
            if (matched.size == variants.size) break
        }
        if (expected) {
            UsefulTestCase.assertTrue("Actual: ${items.map { it.lookupString }}", matched.size == variants.size)
        }
        else {
            UsefulTestCase.assertTrue("Actual: ${items.map { it.lookupString }}", matched.isEmpty())
        }
    }

    private fun hasElements(items: Array<LookupElement>, vararg variants: String) {
        checkElements(items, true, *variants)
    }

    private fun noElements(items: Array<LookupElement>, vararg variants: String) {
        checkElements(items, false, *variants)
    }

}
