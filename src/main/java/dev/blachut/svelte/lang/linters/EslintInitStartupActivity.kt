package dev.blachut.svelte.lang.linters

import com.intellij.ide.util.runOnceForApp
import com.intellij.openapi.components.serviceAsync
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.util.registry.RegistryManager
import java.util.*

private class EslintInitStartupActivity : ProjectActivity {
  override suspend fun execute(project: Project) {
    val key = "eslint.additional.file.extensions"
    runOnceForApp("svelte.init.key") {
      try {
        val registryManager = serviceAsync<RegistryManager>()
        val stringValue = registryManager.stringValue(key)
        if (stringValue == null || !stringValue.contains("svelte")) {
          val newValue = if (stringValue.isNullOrEmpty()) "svelte" else "$stringValue,svelte"
          registryManager.get(key).setValue(newValue)
        }
      }
      catch (e: MissingResourceException) {
        thisLogger().warn("Cannot find the key: $key")
      }
    }
  }
}
