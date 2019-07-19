package dev.blachut.svelte.lang.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.XmlElementVisitor
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.xml.XmlTag
import com.intellij.psi.xml.XmlToken

class SvelteUnresolvedComponentInspection : LocalInspectionTool() {

    // a way to avoid duplicate inspections on the same tag
    val inspectedTags: MutableList<XmlTag> = mutableListOf()

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        inspectedTags.clear()
        return object : XmlElementVisitor() {

            override fun visitXmlToken(token: XmlToken?) {
                if (token != null && token.parent is XmlTag) {
                    val tag = token.parent as XmlTag
                    if (inspectedTags.contains(tag)) {
                        return
                    }
                    if (StringUtil.isCapitalized(tag.name)) {
                        if (tag.descriptor?.declaration == null) {
                            val fileName = "${tag.name}.svelte"
                            // check if we have a corresponding svelte file
                            val files = FilenameIndex.getFilesByName(tag.project, fileName, GlobalSearchScope.allScope(tag.project))
                            if (files.isNotEmpty()) {
                                val references = tag.references
                                references.map { holder.registerProblem(it) }
                                inspectedTags.add(tag)
                            }
                        }
                    }
                }
            }
        }
    }
}