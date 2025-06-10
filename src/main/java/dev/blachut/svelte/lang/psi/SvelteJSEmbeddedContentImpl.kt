package dev.blachut.svelte.lang.psi

import com.intellij.lang.ASTNode
import com.intellij.lang.javascript.JSStubElementTypes
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.impl.JSEmbeddedContentImpl
import com.intellij.lang.javascript.psi.stubs.JSEmbeddedContentStub
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveState
import com.intellij.psi.scope.PsiScopeProcessor
import dev.blachut.svelte.lang.codeInsight.SvelteReactiveDeclarationsUtil

class SvelteJSEmbeddedContentImpl : JSEmbeddedContentImpl {
  constructor(node: ASTNode) : super(node)
  constructor(stub: JSEmbeddedContentStub, type: JSElementType<JSEmbeddedContent>) : super(stub, type)

  override fun processDeclarations(
    processor: PsiScopeProcessor,
    state: ResolveState,
    lastParent: PsiElement?,
    place: PsiElement
  ): Boolean {
    return super.processDeclarations(processor, state, lastParent, place) &&
           processReactiveStatements(processor, state, lastParent, place)
  }

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
            expression.node.findChildByType(JSStubElementTypes.DEFINITION_EXPRESSION)?.psi as JSDefinitionExpression?
            ?: return

          if (!processor.execute(definition, ResolveState.initial())) {
            result = false
          }
        }
      }
    })

    return result
  }
}
