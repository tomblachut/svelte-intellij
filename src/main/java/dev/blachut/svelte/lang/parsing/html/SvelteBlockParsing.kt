package dev.blachut.svelte.lang.parsing.html

import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiBuilder.Marker
import com.intellij.lang.html.HtmlParsing
import com.intellij.openapi.util.NlsContexts
import com.intellij.psi.tree.IElementType
import dev.blachut.svelte.lang.SvelteBundle
import dev.blachut.svelte.lang.psi.AwaitStartType
import dev.blachut.svelte.lang.psi.CatchClauseType
import dev.blachut.svelte.lang.psi.EachStartType
import dev.blachut.svelte.lang.psi.ElseClauseType
import dev.blachut.svelte.lang.psi.IfStartType
import dev.blachut.svelte.lang.psi.KeyStartType
import dev.blachut.svelte.lang.psi.SnippetStartType
import dev.blachut.svelte.lang.psi.SvelteElementType
import dev.blachut.svelte.lang.psi.SvelteElementTypes
import dev.blachut.svelte.lang.psi.SvelteJSElementType
import dev.blachut.svelte.lang.psi.SvelteTagElementTypes
import dev.blachut.svelte.lang.psi.ThenClauseType
import org.jetbrains.annotations.Nls

object SvelteBlockParsing {
  fun startBlock(tagToken: IElementType, tagMarker: Marker, fragmentMarker: Marker): SvelteBlock {
    val parsingDefinition = when (tagToken) {
      is IfStartType -> IF_BLOCK_DEFINITION
      is EachStartType -> EACH_BLOCK_DEFINITION
      is AwaitStartType -> AWAIT_BLOCK_DEFINITION
      is KeyStartType -> KEY_BLOCK_DEFINITION
      is SnippetStartType -> SNIPPET_BLOCK_DEFINITION
      else -> throw IllegalArgumentException("Expected start tag token")
    }

    val outerMarker = tagMarker.precede()
    val innerMarker = tagMarker.precede()

    return SvelteBlock(parsingDefinition, outerMarker, innerMarker, fragmentMarker)
  }

  private val IF_BLOCK_DEFINITION = BlockParsingDefinition(
    blockToken = SvelteElementTypes.IF_BLOCK,
    primaryBranchToken = SvelteElementTypes.IF_TRUE_BRANCH,
    getBranchToken = { tag -> if (tag is ElseClauseType) SvelteElementTypes.IF_ELSE_BRANCH else null },
    endTag = SvelteTagElementTypes.IF_END,
    missingEndTagMessage = SvelteBundle.message("svelte.parsing.if.is.not.closed")
  )

  private val EACH_BLOCK_DEFINITION = BlockParsingDefinition(
    blockToken = SvelteElementTypes.EACH_BLOCK,
    primaryBranchToken = SvelteElementTypes.EACH_LOOP_BRANCH,
    getBranchToken = { tag -> if (tag is ElseClauseType) SvelteElementTypes.EACH_ELSE_BRANCH else null },
    endTag = SvelteTagElementTypes.EACH_END,
    missingEndTagMessage = SvelteBundle.message("svelte.parsing.each.is.not.closed")
  )

  private val AWAIT_BLOCK_DEFINITION = BlockParsingDefinition(
    blockToken = SvelteElementTypes.AWAIT_BLOCK,
    primaryBranchToken = SvelteElementTypes.AWAIT_MAIN_BRANCH,
    getBranchToken = { tag ->
      when (tag) {
        is ThenClauseType -> SvelteElementTypes.AWAIT_THEN_BRANCH
        is CatchClauseType -> SvelteElementTypes.AWAIT_CATCH_BRANCH
        else -> null
      }
    },
    endTag = SvelteTagElementTypes.AWAIT_END,
    missingEndTagMessage = SvelteBundle.message("svelte.parsing.await.is.not.closed")
  )

  private val KEY_BLOCK_DEFINITION = BlockParsingDefinition(
    blockToken = SvelteElementTypes.KEY_BLOCK,
    primaryBranchToken = SvelteElementTypes.KEY_PRIMARY_BRANCH,
    getBranchToken = { null },
    endTag = SvelteTagElementTypes.KEY_END,
    missingEndTagMessage = SvelteBundle.message("svelte.parsing.key.is.not.closed")
  )

  private val SNIPPET_BLOCK_DEFINITION = BlockParsingDefinition(
    blockToken = SvelteElementTypes.SNIPPET_BLOCK,
    primaryBranchToken = SvelteElementTypes.SNIPPET_PRIMARY_BRANCH,
    getBranchToken = { null },
    endTag = SvelteTagElementTypes.SNIPPET_END,
    missingEndTagMessage = SvelteBundle.message("svelte.parsing.snippet.is.not.closed")
  )
}

data class SvelteBlock(
  private val parsingDefinition: BlockParsingDefinition,
  private val outerMarker: Marker,
  private var innerMarker: Marker,
  private var fragmentMarker: Marker
) : HtmlParsing.HtmlParserStackItem {
  private var lastInnerElement = parsingDefinition.primaryBranchToken

  fun isMatchingInnerTag(token: IElementType) = parsingDefinition.getBranchToken(token) != null

  fun isMatchingEndTag(token: IElementType) = token === parsingDefinition.endTag

  fun handleInnerTag(token: IElementType, resultMarker: Marker, nextFragmentMarker: Marker) {
    fragmentMarker.doneBefore(SvelteElementTypes.FRAGMENT, resultMarker)
    innerMarker.doneBefore(lastInnerElement, resultMarker)
    lastInnerElement = parsingDefinition.getBranchToken(token)
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
  val getBranchToken: (IElementType) -> SvelteElementType?,
  val endTag: SvelteJSElementType,
  @Nls @NlsContexts.ParsingError val missingEndTagMessage: String
)
