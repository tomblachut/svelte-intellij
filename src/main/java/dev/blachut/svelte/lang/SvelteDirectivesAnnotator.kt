package dev.blachut.svelte.lang

import com.intellij.codeInsight.daemon.impl.analysis.XmlHighlightVisitor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.lang.javascript.ecmascript6.TypeScriptUtil
import com.intellij.lang.javascript.highlighting.JSHighlighter
import com.intellij.lang.javascript.highlighting.JSSemanticHighlightingUtil
import com.intellij.lang.javascript.highlighting.JSSemanticHighlightingUtil.TextAttributeKeyInfo
import com.intellij.lang.javascript.psi.resolve.JSResolveResult
import com.intellij.lang.javascript.validation.JSAnnotatingVisitor
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiPolyVariantReference
import com.intellij.psi.PsiReference
import com.intellij.psi.ResolveResult
import com.intellij.psi.xml.XmlAttribute
import dev.blachut.svelte.lang.directives.ScopeReference
import dev.blachut.svelte.lang.directives.ShorthandLetReference
import gnu.trove.TObjectIntHashMap

class SvelteDirectivesAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element is XmlAttribute) {
            val highlighter = JSAnnotatingVisitor.getHighlighter(element)

            for (ref in element.references) {
                if (!(ref is ScopeReference || ref is ShorthandLetReference)) {
                    continue;
                }

                val attrKey = if (ref is PsiPolyVariantReference) {
                    highlight(ref, highlighter)
                } else {
                    highlight(ref, highlighter)
                }

                val elementRange = ref.element.textRange
                val refRange = ref.rangeInElement.shiftRight(elementRange.startOffset)

                if (!refRange.intersects(elementRange)) continue

                if (attrKey != null) {
                    holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(refRange).textAttributes(attrKey)
                        .create()
                } else if (!ref.isSoft && ref.resolve() == null) {
                    val message = XmlHighlightVisitor.getErrorDescription(ref)
                    holder.newAnnotation(HighlightSeverity.ERROR, message).range(refRange)
                        .highlightType(ProblemHighlightType.LIKE_UNKNOWN_SYMBOL).create()
                }
            }
        }
    }

    private fun highlight(reference: PsiReference, highlighter: JSHighlighter): TextAttributesKey? {
        val resolve = reference.resolve() ?: return null
        return JSSemanticHighlightingUtil.buildHighlightForResolveResult(
            resolve,
            reference.element,
            highlighter,
        )?.type
    }

    // based on JSSemanticHighlightingUtil.highlight(JSPsiReferenceElement node, JSHighlighter highlighter, AnnotationHolder holder)
    private fun highlight(reference: PsiPolyVariantReference, highlighter: JSHighlighter): TextAttributesKey? {
        val results: Array<ResolveResult> = reference.multiResolve(false)
        if (results.isNotEmpty()) {
            if (JSResolveResult.isTooManyCandidatesResult(results)) {
//        } else if (JSSemanticHighlightingUtil.isGlobalUndefined(node)) {
            } else {
                var countByTypes: TObjectIntHashMap<TextAttributeKeyInfo?> = TObjectIntHashMap()
                var tsdOccurred = false
                for (result in results) {
                    val resolve = result.element
                    if (resolve != null) {
                        val file = resolve.containingFile
                        val isFromTsd = file != null && TypeScriptUtil.isDefinitionFile(file)
                        if (isFromTsd && !tsdOccurred) {
                            tsdOccurred = true
                            countByTypes = TObjectIntHashMap()
                        }
                        if (!tsdOccurred || isFromTsd) {
                            val info = JSSemanticHighlightingUtil.buildHighlightForResolveResult(
                                resolve,
                                reference.element,
                                highlighter,
                            )
                            if (info != null) {
                                countByTypes.put(info, countByTypes[info] + 1)
                            }
                        }
                    }
                }

                val maxCountRef = Ref.create(0)
                val infoRef: Ref<TextAttributeKeyInfo?> = Ref.create(null)
                countByTypes.forEachEntry { key: TextAttributeKeyInfo?, count: Int ->
                    if (count > maxCountRef.get()) {
                        maxCountRef.set(count)
                        infoRef.set(key)
                    } else if (count == maxCountRef.get() && infoRef.get()!!.text.compareTo(key!!.text) > 0) {
                        infoRef.set(key)
                    }
                    true
                }

                return infoRef.get()?.type
            }
        }

        return null
    }
}
