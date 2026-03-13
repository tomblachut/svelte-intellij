// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import type ts from "typescript/lib/tsserverlibrary"
import type {Position, SvelteSnapshotManager} from "./svelte-snapshots"

type TypeScript = typeof ts
type Logger = ts.server.Logger

export type SimpleRange = [startOffset: number, endOffset: number]

interface Range {
  start: Position
  end: Position
}

interface ReverseMapping {
  fileName: string
  sourceRange: Range
}

type ReverseMapper = (
  sourceFile: ts.SourceFile,
  generatedRange: Range,
) => ReverseMapping | undefined

export function isSvelteFile(fileName: string): boolean {
  return fileName.endsWith(".svelte")
}

/**
 * Map original .svelte positions to generated TypeScript positions.
 * Uses svelte2tsx's source map via snapshot.getGeneratedOffset().
 */
export function toGeneratedRange(
  snapshot: { getGeneratedOffset(originalOffset: number): number },
  startOffset: number,
  endOffset: number,
): SimpleRange | undefined {
  const generatedStart = snapshot.getGeneratedOffset(startOffset)
  const generatedEnd = snapshot.getGeneratedOffset(endOffset)
  if (generatedStart < 0 || generatedEnd < 0) {
    return undefined
  }
  return [generatedStart, generatedEnd]
}

/**
 * Create a ReverseMapper that translates generated TypeScript positions back to
 * original .svelte file positions using svelte2tsx's source map.
 *
 * For unmapped positions (synthesized component type), returns the <script> tag
 * position as a safe anchor to prevent "Invalid range" errors.
 */
export function createReverseMapper(
  ts: TypeScript,
  snapshotManager: SvelteSnapshotManager,
  logger: Logger,
): ReverseMapper {
  return (sourceFile: ts.SourceFile, generatedRange: Range): ReverseMapping | undefined => {
    const fileName = sourceFile.fileName
    if (!isSvelteFile(fileName)) {
      return undefined
    }

    const snapshot = snapshotManager.get(fileName)
    if (!snapshot?.mapper) {
      return undefined
    }

    try {
      const originalText = snapshot.svelteCode || snapshot.getOriginalText?.() || ""
      const originalStart = snapshot.mapper.getOriginalPosition(generatedRange.start)
      const originalEnd = snapshot.mapper.getOriginalPosition(generatedRange.end)

      if (originalStart.line >= 0 && originalEnd.line >= 0) {
        const lineCount = countLines(originalText)
        return {
          fileName,
          sourceRange: {
            start: clamp(originalStart, lineCount),
            end: clamp(originalEnd, lineCount),
          },
        }
      }

      // Unmapped position — return <script> tag as safe anchor
      return { fileName, sourceRange: findScriptTag(originalText) }
    } catch (e) {
      logger.info(`[ws-typescript-svelte-plugin] reverseMapper error: ${e}`)
      return undefined
    }
  }
}

function findScriptTag(text: string): Range {
  const lines = text.split("\n")
  for (let i = 0; i < lines.length; i++) {
    const match = lines[i].match(/<script\b[^>]*>/)
    if (match) {
      const col = match.index ?? 0
      return {
        start: { line: i, character: col },
        end: { line: i, character: col + match[0].length },
      }
    }
  }
  return { start: { line: 0, character: 0 }, end: { line: 0, character: 0 } }
}

function clamp(pos: Position, lineCount: number): Position {
  return {
    line: Math.min(pos.line, Math.max(0, lineCount - 1)),
    character: pos.character,
  }
}

function countLines(text: string): number {
  if (!text) return 0
  let count = 1
  for (let i = 0; i < text.length; i++) {
    if (text[i] === "\n") count++
  }
  return count
}
