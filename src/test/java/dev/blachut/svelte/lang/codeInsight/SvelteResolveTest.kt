// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package dev.blachut.svelte.lang.codeInsight

import com.intellij.lang.javascript.psi.JSTagEmbeddedContent
import com.intellij.psi.util.contextOfType
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase

class SvelteResolveTest : BasePlatformTestCase() {
    override fun getTestDataPath(): String = "src/test/resources"
    override fun getBasePath(): String = "dev/blachut/svelte/lang/resolve"

    fun testBlock() {
        myFixture.configureByText(
            "Example.svelte", """
            <script>
                let promise = Promise.resolve();
            </script>

            {#await promise then value}
                <h1>{<caret>value}</h1>
            {/await}
            """.trimIndent()
        )
        val reference = myFixture.getReferenceAtCaretPosition()
        TestCase.assertNotNull(reference)

        val variable = reference!!.resolve()
        TestCase.assertNotNull(variable)
        TestCase.assertEquals(variable?.text, "value")
    }

    fun testBranchIsolation() {
        myFixture.configureByText(
            "Example.svelte", """
            <script>
                let promise = Promise.resolve();
            </script>

            {#await promise then value}
                <h1>{value}</h1>
            {:catch error}
                <h1>{<caret>value}</h1>
            {/await}
            """.trimIndent()
        )
        val reference = myFixture.getReferenceAtCaretPosition()
        TestCase.assertNotNull(reference)

        val variable = reference!!.resolve()
        TestCase.assertNull(variable)
    }

    fun testEachKeyExpression() {
        myFixture.configureByText(
            "Example.svelte", """
            <script>
                let items = [
                    {name: 'alice', id: 'a'},
                    {name: 'bob', id: 'b'},
                ];
            </script>

            {#each items as {name, id} (<caret>id)}
                <article>{name}</article>
            {/each}
            """.trimIndent()
        )
        val reference = myFixture.getReferenceAtCaretPosition()
        TestCase.assertNotNull(reference)

        val variable = reference!!.resolve()
        TestCase.assertNotNull(variable)
        TestCase.assertEquals(variable?.text, "id")
    }

    fun testVariableShadowing() {
        myFixture.configureByText(
            "Example.svelte", """
            <script>
                let then = Promise.resolve();
            </script>

            {#await <caret>then then then}
                <h1>{then}</h1>
            {/await}
            """.trimIndent()
        )
        val reference = myFixture.getReferenceAtCaretPosition()
        TestCase.assertNotNull(reference)

        val variable = reference!!.resolve()
        TestCase.assertNotNull(variable)
        TestCase.assertNotNull(variable!!.contextOfType<JSTagEmbeddedContent>())
    }
}
