package dev.blachut.svelte.lang.psi

import com.intellij.lang.javascript.JSElementTypes
import com.intellij.lang.javascript.psi.JSNamedElement
import com.intellij.lang.javascript.psi.JSParameter
import com.intellij.lang.javascript.psi.JSVariable
import com.intellij.lang.javascript.psi.util.JSDestructuringVisitor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceService
import com.intellij.psi.ResolveState
import com.intellij.psi.impl.source.xml.XmlAttributeImpl
import com.intellij.psi.scope.PsiScopeProcessor
import com.intellij.psi.tree.DefaultRoleFinder
import com.intellij.psi.tree.RoleFinder
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlElement
import dev.blachut.svelte.lang.directives.SvelteDirectivesSupport

class SvelteHtmlAttribute : XmlAttributeImpl(SvelteHtmlElementTypes.SVELTE_HTML_ATTRIBUTE) {
    val directive get() = calcDirective(this)
    val shorthandLetImplicitParameter get() = calcShorthandLetImplicitParameter(this)

    override fun processDeclarations(
        processor: PsiScopeProcessor,
        state: ResolveState,
        lastParent: PsiElement?,
        place: PsiElement,
    ): Boolean {
        if (!name.startsWith("let:")) return true

        val implicit = shorthandLetImplicitParameter
        if (implicit != null) {
            if (!processor.execute(implicit, ResolveState.initial())) {
                return false
            }
        }

        val value = valueElement ?: return true
        val parameter = PsiTreeUtil.findChildOfType(value, SvelteJSParameter::class.java) ?: return true

        var result = true
        parameter.accept(object : JSDestructuringVisitor() {
            override fun visitJSParameter(node: JSParameter) {
                if (result && !processor.execute(node, ResolveState.initial())) {
                    result = false
                }
            }

            override fun visitJSVariable(node: JSVariable) {}
        })
        return result
    }

    override fun getNameElement(): XmlElement {
        if (firstChild is SveltePsiElement) {
            return this
        }

        return super.getNameElement()
    }

    override fun getName(): String {
        if (firstChild !is SveltePsiElement) {
            return super.getName()
        }

        val jsNode = SPREAD_OR_SHORTHAND_FINDER.findChild(firstChildNode) ?: return ""

        return if (jsNode.firstChildNode.elementType === JSElementTypes.SPREAD_EXPRESSION) {
            "<spread>"
        } else {
            jsNode.text
        }
    }

    override fun getReferences(hints: PsiReferenceService.Hints): Array<PsiReference> {
        val directive = directive
        if (directive != null) {
            val referenceFactory = if (valueElement == null) {
                directive.directiveType.shorthandReferenceFactory
            } else {
                directive.directiveType.longhandReferenceFactory
            }
            val ref = referenceFactory?.invoke(this, directive.specifiers[0].rangeInName)

            return listOfNotNull(ref).toTypedArray()
        }

        return super.getReferences(hints)
    }

    override fun getTextOffset(): Int {
        if (directive != null) {
            val shift = name.indexOf(SvelteDirectivesSupport.DIRECTIVE_SEPARATOR) + 1
            return nameElement.textRange.startOffset + shift
        }

        return super.getTextOffset()
    }

    override fun toString(): String {
        return "SvelteHtmlAttribute: $name"
    }

    companion object {
        val SPREAD_OR_SHORTHAND_FINDER: RoleFinder = DefaultRoleFinder(SvelteJSLazyElementTypes.SPREAD_OR_SHORTHAND)

        fun calcDirective(attribute: SvelteHtmlAttribute): SvelteDirectivesSupport.Directive? {
            return CachedValuesManager.getCachedValue(attribute) {
                CachedValueProvider.Result(
                    SvelteDirectivesSupport.parseDirective(attribute.name),
                    PsiModificationTracker.MODIFICATION_COUNT
                )
            }
        }

        fun calcShorthandLetImplicitParameter(attribute: SvelteHtmlAttribute): JSNamedElement? {
            return CachedValuesManager.getCachedValue(attribute) {
                val implicit = if (attribute.name.startsWith("let:") && attribute.valueElement == null) {
                    SvelteDirectiveImplicitParameter(attribute.localName, attribute)
                } else null

                CachedValueProvider.Result(implicit, PsiModificationTracker.MODIFICATION_COUNT)
            }
        }
    }
}
