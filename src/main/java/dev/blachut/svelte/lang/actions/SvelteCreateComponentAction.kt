// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package dev.blachut.svelte.lang.actions

import com.intellij.ide.actions.CreateFileFromTemplateAction
import com.intellij.ide.actions.CreateFileFromTemplateDialog
import com.intellij.ide.fileTemplates.impl.CustomFileTemplate
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import dev.blachut.svelte.lang.icons.SvelteIcons

class SvelteCreateComponentAction : CreateFileFromTemplateAction(NAME, DESCRIPTION, SvelteIcons.COLOR), DumbAware {
    companion object {
        private const val TEMPLATE_NAME: String = "Svelte Component"
        private const val NAME = "Svelte Component"
        private const val DESCRIPTION = "Creates Svelte component file"
    }

    override fun buildDialog(project: Project, directory: PsiDirectory, builder: CreateFileFromTemplateDialog.Builder) {
        builder
            .setTitle("New $NAME")
            .addKind(NAME, SvelteIcons.COLOR, TEMPLATE_NAME)
    }

    override fun getActionName(directory: PsiDirectory?, newName: String, templateName: String?): String = "Create $NAME $newName"

    override fun createFile(name: String, templateName: String?, dir: PsiDirectory?): PsiFile? {
        val template = CustomFileTemplate(name, "svelte")
        return createFileFromTemplate(name, template, dir)
    }
}
