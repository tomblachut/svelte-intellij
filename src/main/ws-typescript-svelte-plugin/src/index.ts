// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import type ts from "typescript/lib/tsserverlibrary"
import { decorateIdeLanguageServiceExtensions } from "./decorateLanguageService"
import { getSvelteSnapshotManager } from "./svelte-snapshots"

const init: ts.server.PluginModuleFactory = (modules) => {
  const ts = modules.typescript

  return {
    create(info: ts.server.PluginCreateInfo): ts.LanguageService {
      const { languageService, project } = info
      const logger = project.projectService.logger

      logger.info("[ws-typescript-svelte-plugin] Initializing plugin")

      // Get the SvelteSnapshotManager from typescript-svelte-plugin's internal storage
      const snapshotManager = getSvelteSnapshotManager(project.projectService, logger)

      decorateIdeLanguageServiceExtensions(ts, languageService, snapshotManager, logger)

      return languageService
    },

    getExternalFiles(_project: ts.server.Project): string[] {
      return []
    }
  }
}

export = init
