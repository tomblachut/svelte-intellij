package dev.blachut.svelte.lang.psi

import com.intellij.lang.ASTNode
import com.intellij.lang.javascript.JSElementTypes
import com.intellij.lang.javascript.psi.JSDefinitionExpression
import com.intellij.lang.javascript.psi.JSElementType
import com.intellij.lang.javascript.psi.JSElementVisitor
import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.lang.javascript.psi.JSExpressionStatement
import com.intellij.lang.javascript.psi.JSLabeledStatement
import com.intellij.lang.javascript.psi.ecma6.TypeScriptTypeParameterList
import com.intellij.lang.javascript.psi.ecma6.TypeScriptTypeParameterListOwner
import com.intellij.lang.javascript.psi.impl.JSEmbeddedContentImpl
import com.intellij.lang.javascript.psi.stubs.JSEmbeddedContentStub
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveState
import com.intellij.psi.scope.PsiScopeProcessor
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.contextOfType
import com.intellij.psi.util.parents
import com.intellij.psi.xml.XmlTag
import dev.blachut.svelte.lang.codeInsight.SvelteReactiveDeclarationsUtil
import dev.blachut.svelte.lang.parsing.html.SvelteGenericsAttributeEmbeddedContentProvider.Companion.GENERICS_ATTRIBUTE_NAME

/**
 * Svelte script embedded content implementation.
 *
 * Implements TypeScriptTypeParameterListOwner to expose generic type parameters
 * from the `generics` attribute to the TypeScript type system.
 *
 */
class SvelteJSEmbeddedContentImpl : JSEmbeddedContentImpl,
                                    TypeScriptTypeParameterListOwner {
  constructor(node: ASTNode) : super(node)
  constructor(stub: JSEmbeddedContentStub, type: JSElementType<JSEmbeddedContent>) : super(stub, type)

  override fun processDeclarations(
    processor: PsiScopeProcessor,
    state: ResolveState,
    lastParent: PsiElement?,
    place: PsiElement
  ): Boolean {
    if (!super.processDeclarations(processor, state, lastParent, place)) {
      return false
    }

    if (!processReactiveStatements(processor, state, lastParent, place)) {
      return false
    }

    // Process generic type parameters from the generics attribute
    val typeParamList = findScriptGenericsTypeParameterList(this)
    if (typeParamList != null) {
      // Don't include if we're inside the param list itself (avoid infinite recursion)
      if (place.parents(true).none { it == typeParamList }) {
        if (!typeParamList.processDeclarations(processor, state, lastParent, place)) {
          return false
        }
      }
    }

    return true
  }

  /**
   * Expose type parameter list to TypeScript type system.
   * This enables TypeScript to resolve generic type parameters in the script content.
   */
  override fun getTypeParameterList(): TypeScriptTypeParameterList? =
    findScriptGenericsTypeParameterList(this)

  @Suppress("UNUSED_PARAMETER")
  private fun processReactiveStatements(
    processor: PsiScopeProcessor,
    state: ResolveState,
    lastParent: PsiElement?,
    place: PsiElement
  ): Boolean {
    var result = true

    acceptChildren(object : JSElementVisitor() {
      override fun visitJSLabeledStatement(labeledStatement: JSLabeledStatement) {
        if (result && labeledStatement.label == SvelteReactiveDeclarationsUtil.REACTIVE_LABEL && labeledStatement.statement is JSExpressionStatement) {
          val expression = (labeledStatement.statement as JSExpressionStatement).expression ?: return

          val definition =
            expression.node.findChildByType(JSElementTypes.DEFINITION_EXPRESSION)?.psi as JSDefinitionExpression?
            ?: return

          if (!processor.execute(definition, ResolveState.initial())) {
            result = false
          }
        }
      }
    })

    return result
  }

  companion object {
    /**
     * Navigate from script content to the generic type parameter list
     * defined in the generics attribute.
     */
    fun findScriptGenericsTypeParameterList(place: PsiElement): TypeScriptTypeParameterList? {
      val scriptTag = place.contextOfType<XmlTag>()
                        ?.takeIf { it.name.equals("script", ignoreCase = true) }
                      ?: return null

      val genericsAttr = scriptTag.getAttribute(GENERICS_ATTRIBUTE_NAME) ?: return null
      return PsiTreeUtil.findChildOfType(genericsAttr.valueElement, TypeScriptTypeParameterList::class.java)
    }
  }
}
