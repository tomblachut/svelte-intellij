package dev.blachut.svelte.lang.stubs

import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import com.intellij.psi.stubs.StubSerializer
import dev.blachut.svelte.lang.SvelteLangMode
import dev.blachut.svelte.lang.parsing.html.SvelteHtmlFileElementType
import dev.blachut.svelte.lang.psi.SvelteFileStub

/**
 * Serializer for [SvelteFileStub] that persists the language mode.
 */
internal class SvelteFileStubSerializer : StubSerializer<SvelteFileStub> {
  override fun getExternalId(): String =
    SvelteHtmlFileElementType.FILE.toString()

  override fun serialize(stub: SvelteFileStub, dataStream: StubOutputStream) {
    dataStream.writeVarInt(stub.langMode.ordinal)
  }

  override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): SvelteFileStub {
    val ordinal = dataStream.readVarInt()
    val langMode = SvelteLangMode.entries.getOrElse(ordinal) { SvelteLangMode.DEFAULT }
    return SvelteFileStub(langMode)
  }

  override fun indexStub(stub: SvelteFileStub, sink: IndexSink) {
    // No indexing needed for file-level lang mode
  }
}
