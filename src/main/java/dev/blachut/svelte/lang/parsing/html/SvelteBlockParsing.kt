package dev.blachut.svelte.lang.parsing.html

import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiBuilder.Marker
import com.intellij.lang.html.HtmlParsing
import com.intellij.openapi.util.NlsContexts
import com.intellij.psi.tree.IElementType
import dev.blachut.svelte.lang.SvelteBundle
import dev.blachut.svelte.lang.psi.*
import org.jetbrains.annotations.Nls

object SvelteBlockParsing {
  fun startBlock(tagToken: IElementType, tagMarker: Marker, fragmentMarker: Marker): SvelteBlock {
    val parsingDefinition = when (tagToken) {
      SvelteTagElementTypes.IF_START -> IF_BLOCK_DEFINITION
      SvelteTagElementTypes.EACH_START -> EACH_BLOCK_DEFINITION
      SvelteTagElementTypes.AWAIT_START -> AWAIT_BLOCK_DEFINITION
      SvelteTagElementTypes.KEY_START -> KEY_BLOCK_DEFINITION
      else -> throw IllegalArgumentException("Expected start tag token")
    }

    val outerMarker = tagMarker.precede()
    val innerMarker = tagMarker.precede()

    return SvelteBlock(parsingDefinition, outerMarker, innerMarker, fragmentMarker)
  }

  private val IF_BLOCK_DEFINITION = BlockParsingDefinition(
    blockToken = SvelteElementTypes.IF_BLOCK,
    primaryBranchToken = SvelteElementTypes.IF_TRUE_BRANCH,
    innerTagToBranchTokens = mapOf(SvelteTagElementTypes.ELSE_CLAUSE to SvelteElementTypes.IF_ELSE_BRANCH),
    endTag = SvelteTagElementTypes.IF_END,
    missingEndTagMessage = SvelteBundle.message("svelte.parsing.if.is.not.closed")
  )

  private val EACH_BLOCK_DEFINITION = BlockParsingDefinition(
    blockToken = SvelteElementTypes.EACH_BLOCK,
    primaryBranchToken = SvelteElementTypes.EACH_LOOP_BRANCH,
    innerTagToBranchTokens = mapOf(SvelteTagElementTypes.ELSE_CLAUSE to SvelteElementTypes.EACH_ELSE_BRANCH),
    endTag = SvelteTagElementTypes.EACH_END,
    missingEndTagMessage = SvelteBundle.message("svelte.parsing.each.is.not.closed")
  )

  private val AWAIT_BLOCK_DEFINITION = BlockParsingDefinition(
    blockToken = SvelteElementTypes.AWAIT_BLOCK,
    primaryBranchToken = SvelteElementTypes.AWAIT_MAIN_BRANCH,
    innerTagToBranchTokens = mapOf(
      SvelteTagElementTypes.THEN_CLAUSE to SvelteElementTypes.AWAIT_THEN_BRANCH,
      SvelteTagElementTypes.CATCH_CLAUSE to SvelteElementTypes.AWAIT_CATCH_BRANCH
    ),
    endTag = SvelteTagElementTypes.AWAIT_END,
    missingEndTagMessage = SvelteBundle.message("svelte.parsing.await.is.not.closed")
  )

  private val KEY_BLOCK_DEFINITION = BlockParsingDefinition(
    blockToken = SvelteElementTypes.KEY_BLOCK,
    primaryBranchToken = SvelteElementTypes.KEY_PRIMARY_BRANCH,
    innerTagToBranchTokens = mapOf(),
    endTag = SvelteTagElementTypes.KEY_END,
    missingEndTagMessage = SvelteBundle.message("svelte.parsing.key.is.not.closed")
  )
}

data class SvelteBlock(
  private val parsingDefinition: BlockParsingDefinition,
  private val outerMarker: Marker,
  private var innerMarker: Marker,
  private var fragmentMarker: Marker
) : HtmlParsing.HtmlParserStackItem {
  private var lastInnerElement = parsingDefinition.primaryBranchToken

  fun isMatchingInnerTag(token: IElementType) = parsingDefinition.innerTagToBranchTokens.containsKey(token)

  fun isMatchingEndTag(token: IElementType) = token === parsingDefinition.endTag

  fun handleInnerTag(token: IElementType, resultMarker: Marker, nextFragmentMarker: Marker) {
    fragmentMarker.doneBefore(SvelteElementTypes.FRAGMENT, resultMarker)
    innerMarker.doneBefore(lastInnerElement, resultMarker)
    lastInnerElement = parsingDefinition.innerTagToBranchTokens[token]
                       ?: throw IllegalArgumentException("Expected matching inner clause")
    innerMarker = resultMarker.precede()
    fragmentMarker = nextFragmentMarker
  }

  override fun done(builder: PsiBuilder,
                    beforeMarker: Marker?,
                    incomplete: Boolean) {
    if (beforeMarker == null) {
      fragmentMarker.done(SvelteElementTypes.FRAGMENT)
      innerMarker.done(lastInnerElement)
      if (incomplete) {
        builder.mark().error(parsingDefinition.missingEndTagMessage)
      }
      outerMarker.done(parsingDefinition.blockToken)
    }
    else {
      fragmentMarker.doneBefore(SvelteElementTypes.FRAGMENT, beforeMarker)
      innerMarker.doneBefore(lastInnerElement, beforeMarker)
      if (incomplete) {
        builder.mark().error(parsingDefinition.missingEndTagMessage)
      }
      outerMarker.done(parsingDefinition.blockToken)
    }
  }
}

data class BlockParsingDefinition(
  val blockToken: SvelteElementType,
  val primaryBranchToken: SvelteElementType,
  val innerTagToBranchTokens: Map<SvelteJSBlockLazyElementType, SvelteElementType>,
  val endTag: SvelteJSElementType,
  @Nls @NlsContexts.ParsingError val missingEndTagMessage: String
)
