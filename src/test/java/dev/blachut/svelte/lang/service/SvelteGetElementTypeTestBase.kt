// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package dev.blachut.svelte.lang.service

import com.intellij.lang.javascript.evaluation.JSTypeEvaluationLocationProvider.withTypeEvaluationLocation
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.typescript.resolve.TypeScriptCompilerEvaluationFacade
import com.intellij.lang.typescript.tsc.TypeScriptServiceTestMixin
import com.intellij.lang.typescript.tsc.types.TypeScriptCompilerObjectTypeImpl
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.util.ProgressIndicatorUtils
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.registry.Registry
import com.intellij.psi.PsiElement
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import junit.framework.TestCase

object SvelteGetElementTypeTestUtil {

  fun setUpSPTE(fixture: CodeInsightTestFixture, project: Project, disposable: Disposable) {
    TypeScriptServiceTestMixin.setUpTypeScriptService(fixture) {
      it is SveltePluginTypeScriptService
    }
    TypeScriptServiceTestMixin.setUseTypesFromServer(true, project, disposable)
    Registry.get("typescript.service.powered.types.in.other.cases").setValue(true, disposable)
    Registry.get("typescript.service.powered.declaration.types").setValue(true, disposable)
    Registry.get("typescript.service.powered.contextual.types").setValue(true, disposable)
  }

  fun calculateTypeAndVerifyDeclarations(element: PsiElement): JSType {
    val type = runInBackground {
      withTypeEvaluationLocation(element) {
        val t = element.project.service<TypeScriptCompilerEvaluationFacade>().getTypeFromService(element)
        // resolveType() must be called in the same background thread context
        if (t is TypeScriptCompilerObjectTypeImpl) {
          t.asRecordType()
        }
        t
      }
    }
    TestCase.assertNotNull("Type from service should not be null", type)
    return type!!
  }

  fun <T> runInBackground(block: () -> T): T {
    val future = ApplicationManager.getApplication().executeOnPooledThread<T> {
      ProgressManager.getInstance().computeInNonCancelableSection<T, Exception> {
        ReadAction.compute<T, Exception>(block)
      }
    }
    return ProgressIndicatorUtils.awaitWithCheckCanceled(future)
  }
}
