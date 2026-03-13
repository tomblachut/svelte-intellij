// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
const esbuild = require('esbuild');
const fs = require('fs');
const path = require('path');

const debug = process.argv.includes('--debug');
const outDir = '../svelte-service/node_modules/ws-typescript-svelte-plugin';

// Ensure output directory exists
fs.mkdirSync(outDir, { recursive: true });

esbuild.buildSync({
  entryPoints: ['src/index.ts'],
  bundle: true,
  platform: 'node',
  target: 'es2015',
  outfile: path.join(outDir, 'index.js'),
  format: 'cjs',
  external: ['typescript'],
  minify: !debug,
  define: { 'DEBUG_SNAPSHOTS': debug ? 'true' : 'false' },
});

// Copy package.json
fs.copyFileSync('package.json', path.join(outDir, 'package.json'));

console.log(`Build complete!${debug ? ' (debug mode)' : ''}`);
