package dev.blachut.svelte.lang.codeInsight

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.Assert

class SvelteCompletionTest : BasePlatformTestCase() {

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
            Assert.assertTrue("Actual: ${items.map { it.lookupString }}", matched.size == variants.size)
        } else {
            Assert.assertTrue("Actual: ${items.map { it.lookupString }}", matched.isEmpty())
        }
    }

    private fun hasElements(items: Array<LookupElement>, vararg variants: String) {
        checkElements(items, true, *variants)
    }

    private fun noElements(items: Array<LookupElement>, vararg variants: String) {
        checkElements(items, false, *variants)
    }

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
                    import App from "./Hello1.svelte";
                </script>
                <App <caret>
                """.trimIndent())
        val items = myFixture.completeBasic()
        hasElements(items, "slot", "hello11")
        noElements(items, "hello11NotAvailable", "hello22NotAvailable", "hello22")
    }

    fun testInterpolation() {
        myFixture.configureByText("Test.svelte",
            """
                <script>
                    export let hello11;
                    let hello11Local = 10;
                </script>
                <div>{h<caret>}</div>
                """.trimIndent()
        )
        val items = myFixture.completeBasic()
        hasElements(items, "hello11", "hello11Local")
    }

    fun testSimpleSvelteNamespace() {
        myFixture.configureByText("Hello.svelte", "<h1>Test</h1>")
        myFixture.configureByText("foo.svelte", "<svelte:<caret>")
        hasElements(myFixture.completeBasic(), "body", "self")
    }

    fun testInterpolationInAttribute() {
        myFixture.configureByText("Test.svelte",
            """
                <script>
                    export let hello11 = "src"
                    let hello11Local = "src";
                </script>
                <img src={<caret>}>
                """.trimIndent()
        )
        val items = myFixture.completeBasic()
        hasElements(items, "hello11", "hello11Local")
    }

    fun testMustache() {
        myFixture.configureByText("Hello.svelte",
            """
                <script></script>
                {#if <caret>}
                {/if}
                """.trimIndent())
        hasElements(myFixture.completeBasic(), "true", "false")
    }
}
