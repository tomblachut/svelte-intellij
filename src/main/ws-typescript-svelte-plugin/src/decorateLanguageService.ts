// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import type ts from "typescript/lib/tsserverlibrary"
import type { SvelteSnapshotManager } from "./svelte-snapshots"
import { createReverseMapper, toGeneratedRange, isSvelteFile } from "./ranges"

type TypeScript = typeof ts
type Logger = ts.server.Logger

/**
 * Decorates IDE-specific language service extensions to handle position mapping
 * between original .svelte files and generated TypeScript.
 */
export function decorateIdeLanguageServiceExtensions(
  ts: TypeScript,
  languageService: ts.LanguageService,
  snapshotManager: SvelteSnapshotManager,
  logger: Logger
): void {
  // Store original methods
  const {
    webStormGetElementType,
    webStormGetSymbolType,
    webStormGetTypeProperties,
    webStormGetTypeProperty,
    webStormGetCompletionSymbols,
    webStormGetResolvedSignature,
  } = languageService

  // Check if IDE methods are available
  if (
    webStormGetElementType === undefined ||
    webStormGetSymbolType === undefined ||
    webStormGetTypeProperties === undefined ||
    webStormGetTypeProperty === undefined ||
    webStormGetCompletionSymbols === undefined
  ) {
    logger.info("[ws-typescript-svelte-plugin] IDE methods not available, skipping decoration")
    return
  }

  logger.info("[ws-typescript-svelte-plugin] Decorating IDE language service extensions")

  /**
   * Helper to wrap methods that only need reverseMapper (no input transformation)
   */
  function withReverseMapper<
    O extends ts.WebStormGetOptions,
    R extends Record<never, never> | undefined
  >(source: (options: O) => R): (options: O) => R {
    return (options) => {
      return source({
        ...options,
        reverseMapper: createReverseMapper(options.ts, snapshotManager, logger),
      })
    }
  }

  /**
   * webStormGetElementType - Get type at a specific position
   * Requires both input transformation AND reverseMapper for .svelte files
   */
  languageService.webStormGetElementType = (options) => {
    const { ts: typescript, fileName, startOffset, endOffset } = options

    if (!isSvelteFile(fileName)) {
      // For non-Svelte files, just add reverseMapper for types that reference .svelte files
      return webStormGetElementType({
        ...options,
        reverseMapper: createReverseMapper(typescript, snapshotManager, logger),
      })
    }

    // Get the SvelteSnapshot for position mapping
    const snapshot = snapshotManager.get(fileName)
    if (!snapshot) {
      return undefined
    }

    // Transform positions: original → generated
    const generatedRange = toGeneratedRange(snapshot, startOffset, endOffset)
    if (!generatedRange) {
      return undefined
    }

    const [generatedStart, generatedEnd] = generatedRange

    return webStormGetElementType({
      ...options,
      startOffset: generatedStart,
      endOffset: generatedEnd,
      reverseMapper: createReverseMapper(typescript, snapshotManager, logger),
    })
  }

  // Methods that only need reverseMapper (no input position transformation)
  languageService.webStormGetCompletionSymbols = withReverseMapper(webStormGetCompletionSymbols)
  languageService.webStormGetSymbolType = withReverseMapper(webStormGetSymbolType)
  languageService.webStormGetTypeProperties = withReverseMapper(webStormGetTypeProperties)
  languageService.webStormGetTypeProperty = withReverseMapper(webStormGetTypeProperty)

  if (webStormGetResolvedSignature) {
    languageService.webStormGetResolvedSignature = withReverseMapper(webStormGetResolvedSignature)
  }

  logger.info("[ws-typescript-svelte-plugin] IDE language service extensions decorated successfully")
}
