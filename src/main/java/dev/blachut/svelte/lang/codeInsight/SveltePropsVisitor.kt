package dev.blachut.svelte.lang.codeInsight

import com.intellij.lang.javascript.psi.JSRecursiveWalkingElementVisitor
import com.intellij.lang.javascript.psi.JSVariable

internal class SveltePropsVisitor : JSRecursiveWalkingElementVisitor() {
  val props: List<String> get() = _props
  private val _props = mutableListOf<String>()

  override fun visitJSVariable(variable: JSVariable) {
    if (variable.isExported) {
      _props.add(variable.name!!)
    }
  }
}
