package dev.blachut.svelte.lang.linters

import com.intellij.ide.util.RunOnceUtil
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.openapi.util.registry.Registry
import java.util.*

class EslintInitStartupActivity : StartupActivity {

  override fun runActivity(project: Project) {
    val key = "eslint.additional.file.extensions"

    RunOnceUtil.runOnceForApp("svelte.init.key") {
      try {
        val stringValue = Registry.stringValue(key)
        if (!stringValue.contains("svelte")) {
          val newValue = if (stringValue.isEmpty()) "svelte" else "$stringValue,svelte"
          val value = Registry.get(key)
          value.setValue(newValue)
        }
      }
      catch (e: MissingResourceException) {
        Logger.getInstance(EslintInitStartupActivity::class.java).warn("Cannot find the key: $key")
      }
    }
  }
}
