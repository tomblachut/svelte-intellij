package dev.blachut.svelte.lang

import com.intellij.codeInsight.daemon.impl.analysis.XmlHighlightVisitor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.lang.javascript.ecmascript6.TypeScriptUtil
import com.intellij.lang.javascript.highlighting.JSHighlightDescriptor
import com.intellij.lang.javascript.highlighting.JSHighlighter
import com.intellij.lang.javascript.highlighting.JSSemanticHighlightingUtil
import com.intellij.lang.javascript.psi.resolve.JSResolveResult
import com.intellij.lang.javascript.validation.JSAnnotatingVisitor
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiPolyVariantReference
import com.intellij.psi.PsiReference
import com.intellij.psi.ResolveResult
import com.intellij.psi.impl.source.resolve.reference.impl.PsiMultiReference
import com.intellij.psi.xml.XmlAttribute
import dev.blachut.svelte.lang.directives.ScopeReference
import dev.blachut.svelte.lang.directives.ShorthandLetReference
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap

class SvelteDirectiveAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element !is XmlAttribute) return

        val highlighter = JSAnnotatingVisitor.getHighlighter(element)

        for (ref in element.references) {
            val attrKey = when (ref) {
                is PsiMultiReference -> {
                    val innerRef = ref.references.find { it is ScopeReference } ?: continue
                    highlight(innerRef, highlighter)
                }
                is ScopeReference -> {
                    highlight(ref, highlighter)
                }
                is ShorthandLetReference -> {
                    highlight(ref, highlighter)
                }
                else -> continue
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

    private fun highlight(reference: PsiReference, highlighter: JSHighlighter): TextAttributesKey? {
        val resolve = reference.resolve() ?: return null
        return JSSemanticHighlightingUtil.buildHighlightForResolveResult(
            resolve,
            reference.element,
        )?.getAttributesKey(highlighter)
    }

    // based on JSSemanticHighlightingUtil.highlight(JSPsiReferenceElement node, JSHighlighter highlighter, AnnotationHolder holder)
    private fun highlight(reference: PsiPolyVariantReference, highlighter: JSHighlighter): TextAttributesKey? {
        val results: Array<ResolveResult> = reference.multiResolve(false)
        if (results.isNotEmpty()) {
            if (JSResolveResult.isTooManyCandidatesResult(results)) {
//        } else if (JSSemanticHighlightingUtil.isGlobalUndefined(node)) {
            } else {
                var countByTypes = Object2IntOpenHashMap<JSHighlightDescriptor?>()
                var tsdOccurred = false
                for (result in results) {
                    val resolve = result.element
                    if (resolve != null) {
                        val file = resolve.containingFile
                        val isFromTsd = file != null && TypeScriptUtil.isDefinitionFile(file)
                        if (isFromTsd && !tsdOccurred) {
                            tsdOccurred = true
                            countByTypes = Object2IntOpenHashMap()
                        }
                        if (!tsdOccurred || isFromTsd) {
                            val info = JSSemanticHighlightingUtil.buildHighlightForResolveResult(
                                resolve,
                                reference.element,
                            )
                            if (info != null) {
                                countByTypes.put(info, countByTypes.getInt(info) + 1)
                            }
                        }
                    }
                }

                val maxCountRef = Ref.create(0)
                val infoRef: Ref<JSHighlightDescriptor?> = Ref.create(null)
                countByTypes.object2IntEntrySet().forEach { entry ->
                    val key = entry.key
                    val count = entry.intValue
                    if (count > maxCountRef.get()) {
                        maxCountRef.set(count)
                        infoRef.set(key)
                    } else if (count == maxCountRef.get() && infoRef.get()!!.debugName > key!!.debugName) {
                        infoRef.set(key)
                    }
                }

                return infoRef.get()?.getAttributesKey(highlighter)
            }
        }

        return null
    }
}
