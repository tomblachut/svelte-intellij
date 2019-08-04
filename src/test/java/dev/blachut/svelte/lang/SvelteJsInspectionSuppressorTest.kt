package dev.blachut.svelte.lang

import com.google.common.collect.Sets
import com.intellij.codeInspection.InspectionEP
import com.intellij.codeInspection.InspectionProfileEntry
import com.intellij.codeInspection.LocalInspectionEP
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixture4TestCase
import org.junit.Before
import org.junit.ComparisonFailure
import org.junit.Test

internal class SvelteJsInspectionSuppressorTest : LightPlatformCodeInsightFixture4TestCase() {

    private val enabledInspections = Sets.newHashSet("UnnecessaryLabelJS", "JSUnusedAssignment")

    @Before
    fun setup() {
        val inspections = LocalInspectionEP.LOCAL_INSPECTION.extensions()
            .map(InspectionEP::instantiateTool)
            .filter { enabledInspections.contains(it.shortName) }
            .toArray { arrayOfNulls<InspectionProfileEntry>(it) }
        myFixture.enableInspections(*inspections)
    }

    @Test
    fun testSvelteLabelWarningSuppression() {
        // language=Svelte
        runInspections("<script>$: alert('ok');</script>")
    }

    @Test
    fun testSvelteLabelAssignmentWarningSuppression() {
        // language=Svelte
        runInspections("<script>let item; $: item = 1; item = 2</script>")
    }

    @Test(expected = ComparisonFailure::class)
    fun testLabelInspectionOutsideSvelteLabel() {
        // language=Svelte
        runInspections("<script>label: alert('ok')</script>")
    }

    @Test(expected = ComparisonFailure::class)
    fun testAssignmentInspectionOutsideSvelteLabel() {
        // language=Svelte
        runInspections("<script>let item; item = 1; item = 2</script>")
    }

    private fun runInspections(code: String) {
        myFixture.configureByText("file.svelte", code)
        myFixture.checkHighlighting()
    }
}
