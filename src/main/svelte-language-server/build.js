// based on https://github.com/vuejs/language-tools/blob/5607f45835ab85e0b5a0747614a4ed9989a28cec/extensions/vscode/scripts/build.js

const path = require('path');
const fs = require('fs');
const esbuild = require('esbuild');

// .js is here, so it's easier to Find in Files
const packageRelativePath = "bin/server.js"
    .replace(".js", "");

const ownPackageJson = require('./package.json');
const languageServerPackage = ownPackageJson.name;

const theirPackageJson = require(`./node_modules/${languageServerPackage}/package.json`);

if (ownPackageJson.version !== theirPackageJson.version) {
    throw new Error(`Make sure that the version in package.json matches the version of official server distribution (${theirPackageJson.version})`);
}

// typescript-svelte-plugin loads svelte2tsx from bin/node_modules, so the dependencies of svelte2tsx must be there too
const svelte2tsx = `svelte2tsx`;
const runtimeDependencies = [
  svelte2tsx,
  `dedent-js`,
  `scule`,
];

const svelte2tsxDependencies = Object.keys(require(`./node_modules/${svelte2tsx}/package.json`).dependencies ?? {});
const missingDependencies = svelte2tsxDependencies.filter(dependency => !runtimeDependencies.includes(dependency));
if (missingDependencies.length > 0) {
    throw new Error(`Add the svelte2tsx dependencies to runtimeDependencies: ${missingDependencies.join(', ')}`);
}

esbuild.build({
    entryPoints: {
        [packageRelativePath]: `./node_modules/${languageServerPackage}/${packageRelativePath}`,
    },
    outdir: '.',
    bundle: true,
    external: ['prettier', 'prettier-plugin-svelte', 'typescript'],
    format: 'cjs',
    platform: 'node',
    target: 'es2015',
    define: {'process.env.NODE_ENV': '"production"'},
    sourcemap: "linked",
    minify: process.argv.includes('--minify'),
    metafile: process.argv.includes('--metafile'),
    plugins: [
        {
            // The server reads the Prettier config before completions, quick fixes and organize imports.
            // importPrettier throws when the project has no prettier, because prettier is not shipped next to server.js.
            // Return a stub without a config instead, so the server uses its default format settings.
            // The IDE disables LSP formatting, so the server never formats with the stub.
            name: 'optional-prettier',
            setup(build) {
                build.onLoad({filter: /svelte-language-server[\\/]dist[\\/]src[\\/]importPackage\.js$/}, args => {
                    const contents = fs.readFileSync(args.path, 'utf8');
                    const eager =
                        'function importPrettier(fromPath) {\n' +
                        '    const pkg = getPackageInfo(\'prettier\', fromPath);\n';
                    if (!contents.includes(eager)) {
                        throw new Error('importPackage.js layout changed; update optional-prettier in build.js');
                    }
                    const tolerant =
                        'function importPrettier(fromPath) {\n' +
                        '    let pkg;\n' +
                        '    try {\n' +
                        '        pkg = getPackageInfo(\'prettier\', fromPath);\n' +
                        '    }\n' +
                        '    catch (e) {\n' +
                        '        return { resolveConfig: () => Promise.resolve(null) };\n' +
                        '    }\n';
                    return {contents: contents.replace(eager, tolerant), loader: 'js'};
                });
            },
        },
        {
            name: 'umd2esm',
            setup(build) {
                build.onResolve({filter: /^(vscode-.*-languageservice|jsonc-parser)/}, args => {
                    const pathUmdMay = require.resolve(args.path, {paths: [args.resolveDir]})
                    // Call twice the replace is to solve the problem of the path in Windows
                    const pathEsm = pathUmdMay.replace('/umd/', '/esm/').replace('\\umd\\', '\\esm\\')
                    return {path: pathEsm}
                })
            },
        },
    ],
});

fs.rmSync('./bin/node_modules/', {recursive: true, force: true});

for (const dependency of runtimeDependencies) {
  fs.cpSync(`./node_modules/${dependency}`, `./bin/node_modules/${dependency}`, {recursive: true});
}
