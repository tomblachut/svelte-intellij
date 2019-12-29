package dev.blachut.svelte.lang.parsing

import com.intellij.lexer.EmbeddedTokenTypesProvider
import com.intellij.lexer.Lexer
import com.intellij.mock.MockApplication
import com.intellij.openapi.extensions.DefaultPluginDescriptor
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.extensions.PluginDescriptor
import com.intellij.openapi.extensions.impl.ExtensionPointImpl
import com.intellij.openapi.extensions.impl.ExtensionsAreaImpl
import com.intellij.testFramework.LexerTestCase
import dev.blachut.svelte.lang.parsing.html.SvelteHtmlLexer

class SvelteLexerTest : LexerTestCase() {
    override fun getDirPath(): String = "src/test/resources/dev/blachut/svelte/lang/parsing"
    override fun getExpectedFileExtension(): String = ".tokens"

    override fun getPathToTestDataFile(extension: String?): String {
        return dirPath + "/" + getTestName(false) + extension
    }

    override fun setUp() {
        super.setUp()
        val app = MockApplication.setUp(testRootDisposable)
        registerExtensionPoint(app.extensionArea, EmbeddedTokenTypesProvider.EXTENSION_POINT_NAME, EmbeddedTokenTypesProvider::class.java)
    }

    override fun createLexer(): Lexer {
        return SvelteHtmlLexer()
    }

    fun testIf() = doTest()
//    fun testIfElseIf() = doTest()
//    fun testEachAssets() = doTest()
//    fun testExpression() = doTest()
//    fun testIncompleteExpression() = doTest()
//    fun testWhitespace() = doTest()
//
//    fun testEachAsAsAsAs() = doTest()
//    fun testAwaitThenThenThen() = doTest()
//    fun testEachAmbiguousAs() = doTest()

    private fun doTest() = doFileTest("svelte")

    private fun <T> registerExtensionPoint(extensionArea: ExtensionsAreaImpl,
                                           extensionPointName: ExtensionPointName<T>,
                                           extensionClass: Class<T>): ExtensionPointImpl<T>? {
        // todo get rid of it - registerExtensionPoint should be not called several times
        val name = extensionPointName.name
        return if (extensionArea.hasExtensionPoint(name)) {
            extensionArea.getExtensionPoint(name)
        } else {
            extensionArea.registerPoint(name, extensionClass, getPluginDescriptor())
        }
    }

    private var myPluginDescriptor: PluginDescriptor? = null

    // easy debug of not disposed extension
    private fun getPluginDescriptor(): PluginDescriptor {
        var pluginDescriptor = myPluginDescriptor
        if (pluginDescriptor == null) {
            pluginDescriptor = DefaultPluginDescriptor(javaClass.name + "." + name)
            myPluginDescriptor = pluginDescriptor
        }
        return pluginDescriptor
    }
}

