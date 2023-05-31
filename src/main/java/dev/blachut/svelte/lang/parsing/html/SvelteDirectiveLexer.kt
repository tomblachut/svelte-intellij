package dev.blachut.svelte.lang.parsing.html

import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lexer.LexerBase
import com.intellij.psi.tree.IElementType
import com.intellij.psi.xml.XmlTokenType
import kotlin.math.min

class SvelteDirectiveLexer : LexerBase() {
  private var myBuffer: CharSequence = ""

  private var myEndOffset = 0

  private var myPosition = 0
  private var myState = State.INITIAL.value
  private var myTokenType: IElementType? = null
  private var myTokenEnd = 0

  override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
    myBuffer = buffer
    myPosition = startOffset
    myEndOffset = endOffset
    myState = initialState

    advance()
  }

  override fun getState(): Int = myState

  override fun getTokenType(): IElementType? {
    return myTokenType
  }

  override fun getTokenStart(): Int {
    return myPosition
  }

  override fun getTokenEnd(): Int {
    return myTokenEnd
  }

  override fun advance() {
    myPosition = myTokenEnd

    if (myPosition == myEndOffset) {
      myTokenType = null
      return
    }

    when (myState) {
      State.INITIAL.value -> {
        val colonIndex = myBuffer.indexOf(':', myPosition)
        if (colonIndex > 0) { // : can't be first character
          myTokenType = JSTokenTypes.IDENTIFIER
          myTokenEnd = colonIndex + 1
          myState = State.NAME.value
        }
        else {
          myTokenType = XmlTokenType.XML_NAME
          myTokenEnd = myEndOffset
        }
      }
      State.NAME.value, State.MODIFIER.value -> when {
        myBuffer[myPosition] == '.' -> {
          myTokenType = JSTokenTypes.DOT
          myTokenEnd = myPosition + 1
        }
        myBuffer[myPosition] == '|' -> {
          myTokenType = JSTokenTypes.OR
          myTokenEnd = myPosition + 1
          myState = State.MODIFIER.value
        }
        else -> {
          val pipeIndex = myBuffer.indexOf('|', myPosition).let { if (it == -1) myEndOffset else it }
          val dotIndex = myBuffer.indexOf('.', myPosition).let { if (it == -1) myEndOffset else it }
          val firstIndex = min(dotIndex, pipeIndex)

          myTokenType = when (myState) {
            State.NAME.value -> JSTokenTypes.IDENTIFIER
            else -> XmlTokenType.XML_NAME
          }
          myTokenEnd = firstIndex
        }
      }
    }
  }

  override fun getBufferSequence(): CharSequence = myBuffer

  override fun getBufferEnd(): Int = myEndOffset

  private enum class State(val value: Int) {
    INITIAL(0), NAME(1), MODIFIER(2)
  }
}
