// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package dev.blachut.svelte.lang.index

import com.intellij.lang.ecmascript6.index.ES6FileIncludeProvider
import com.intellij.lang.ecmascript6.index.JSFrameworkFileIncludeProvider
import com.intellij.lang.ecmascript6.psi.ES6ImportDeclaration
import com.intellij.lang.ecmascript6.psi.impl.ES6ImportPsiUtil
import com.intellij.psi.impl.include.FileIncludeInfo
import com.intellij.psi.xml.XmlTag
import com.intellij.util.indexing.FileContent
import dev.blachut.svelte.lang.SvelteHtmlFileType
import dev.blachut.svelte.lang.psi.SvelteHtmlFile
import dev.blachut.svelte.lang.psi.getJsEmbeddedContent


class SvelteFileIncludeProvider : JSFrameworkFileIncludeProvider(SvelteHtmlFileType) {
  override fun getIncludeInfos(content: FileContent): Array<FileIncludeInfo> {
    if (!ES6FileIncludeProvider.checkTextHasFromKeyword(content)) return emptyArray()

    val psiFile = content.psiFile as SvelteHtmlFile
    val importDeclarations = processScriptTag(psiFile.moduleScript) +
                             processScriptTag(psiFile.instanceScript)

    return createFileIncludeInfos(importDeclarations)
  }

  private fun processScriptTag(tag: XmlTag?): List<ES6ImportDeclaration> {
    val embeddedContent = tag?.let {
      getJsEmbeddedContent(it)
    } ?: return emptyList()
    return ES6ImportPsiUtil.getImportDeclarations(embeddedContent)
  }
}
