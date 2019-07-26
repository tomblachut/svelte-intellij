package dev.blachut.svelte.lang.codeInsight

import com.intellij.lang.javascript.modules.JSModuleNameInfo
import com.intellij.openapi.vfs.VirtualFile

data class ComponentLookupObject(val file: VirtualFile, val props: List<String?>?, val moduleInfo: JSModuleNameInfo)
