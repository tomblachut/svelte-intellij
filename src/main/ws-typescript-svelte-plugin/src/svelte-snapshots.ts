// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import type ts from "typescript/lib/tsserverlibrary"

declare const DEBUG_SNAPSHOTS: boolean

/**
 * Position in line/character format
 */
export interface Position {
  line: number
  character: number
}

/**
 * Interface for the SourceMapper from typescript-svelte-plugin.
 * This mapper converts between generated and original positions using source maps.
 */
export interface SourceMapper {
  getOriginalPosition(position: Position): Position
  getGeneratedPosition(position: Position): Position
}

/**
 * Interface matching the SvelteSnapshot class from typescript-svelte-plugin.
 * We access the internal mapper and svelteCode for position conversions.
 */
export interface SvelteSnapshot {
  /** The original .svelte file content */
  svelteCode: string
  /** The mapper from typescript-svelte-plugin */
  mapper: SourceMapper

  /** Get the generated TypeScript code */
  getText(): string
  /** Get the original Svelte code */
  getOriginalText(): string

  /** Convert offset to position in original svelte code */
  positionAt(offset: number): Position
  /** Convert position to offset in original svelte code */
  offsetAt(position: Position): number

  /** Convert original offset to generated offset */
  getGeneratedOffset(originalOffset: number): number
  /** Convert generated offset to original offset */
  getOriginalOffset(generatedOffset: number): number
}

/**
 * Interface for accessing SvelteSnapshot instances.
 */
export interface SvelteSnapshotManager {
  get(fileName: string): SvelteSnapshot | undefined
}

/**
 * Get the SvelteSnapshotManager by accessing typescript-svelte-plugin's internal storage.
 *
 * Contract: typescript-svelte-plugin (package "typescript-svelte-plugin", tested with v0.3.x)
 * stores SvelteSnapshot instances in a Map on projectService, keyed by a Symbol whose
 * description contains "sveltePluginPatchSymbol". If the upstream plugin changes this
 * internal storage mechanism, snapshot lookups will silently return undefined and
 * position mapping will gracefully degrade (IDE features fall back to no type info).
 *
 * IMPORTANT: The lookup is done lazily on each get() call because:
 * 1. typescript-svelte-plugin may not have initialized yet when our plugin starts
 * 2. Snapshots are created on-demand when .svelte files are first accessed
 */
export function getSvelteSnapshotManager(
  projectService: ts.server.ProjectService,
  logger?: ts.server.Logger
): SvelteSnapshotManager {
  // Cache the symbol once found to avoid repeated lookups
  let cachedSymbol: symbol | null | undefined = undefined

  // Log initial state for debugging
  const initialSymbols = Object.getOwnPropertySymbols(projectService)
  logger?.info(`[ws-typescript-svelte-plugin] getSvelteSnapshotManager: projectService has ${initialSymbols.length} symbols at init: ${initialSymbols.map(s => s.toString()).join(", ")}`)

  function findSvelteSymbol(): symbol | null {
    if (cachedSymbol !== undefined) {
      return cachedSymbol
    }

    const allSymbols = Object.getOwnPropertySymbols(projectService)
    const found = allSymbols.find(sym =>
      sym.toString().includes("sveltePluginPatchSymbol")
    )

    if (found) {
      cachedSymbol = found
      logger?.info("[ws-typescript-svelte-plugin] Found sveltePluginPatchSymbol on projectService")
    } else {
      // Don't cache null - maybe the symbol will be added later
      logger?.info(`[ws-typescript-svelte-plugin] sveltePluginPatchSymbol not found yet. Available symbols: ${allSymbols.map(s => s.toString()).join(", ")}`)
    }

    return found || null
  }

  function getInternalSnapshots(): Map<string, SvelteSnapshot> | null {
    const symbol = findSvelteSymbol()
    if (!symbol) {
      return null
    }

    const snapshots = (projectService as any)[symbol] as Map<string, SvelteSnapshot> | undefined
    if (!snapshots) {
      logger?.info("[ws-typescript-svelte-plugin] sveltePluginPatchSymbol found but no snapshots map")
      return null
    }

    return snapshots
  }

  return {
    get(fileName: string): SvelteSnapshot | undefined {
      const internalSnapshots = getInternalSnapshots()
      if (!internalSnapshots) {
        return undefined
      }

      // Try multiple path variations since path normalization can differ
      const canonicalFileName = projectService.toCanonicalFileName(fileName)
      let snapshot = internalSnapshots.get(canonicalFileName)

      // Also try the raw fileName in case canonicalization differs
      if (!snapshot) {
        snapshot = internalSnapshots.get(fileName)
      }

      if (snapshot) {
        logger?.info(`[ws-typescript-svelte-plugin] Found snapshot for ${fileName} (size: ${internalSnapshots.size})`)
        if (DEBUG_SNAPSHOTS) dumpSnapshots(fileName, snapshot, internalSnapshots)
      } else {
        const keys = Array.from(internalSnapshots.keys())
        logger?.info(
          `[ws-typescript-svelte-plugin] No snapshot for ${fileName} (canonical: ${canonicalFileName}), ` +
          `available (${keys.length}): ${keys.slice(0, 5).join(", ")}${keys.length > 5 ? "..." : ""}`
        )
      }

      return snapshot
    }
  }
}

function dumpSnapshots(
  requestedFile: string,
  snapshot: SvelteSnapshot,
  allSnapshots: Map<string, SvelteSnapshot>,
) {
  try {
    const fs = require("fs")
    const entry: Record<string, unknown> = {
      timestamp: new Date().toISOString(),
      requestedFile,
      totalSnapshots: allSnapshots.size,
      allFiles: Array.from(allSnapshots.keys()),
      snapshot: {
        originalCode: snapshot.svelteCode || snapshot.getOriginalText?.() || "<unavailable>",
        originalLineCount: (snapshot.svelteCode || "").split("\n").length,
        generatedCode: snapshot.getText?.() || "<unavailable>",
        generatedLineCount: (snapshot.getText?.() || "").split("\n").length,
        hasMapper: !!snapshot.mapper,
      },
    }

    fs.appendFileSync("/tmp/svelte-snapshots-debug.json", JSON.stringify(entry, null, 2) + "\n---\n")
  } catch (_e) {
    // Ignore write errors
  }
}
