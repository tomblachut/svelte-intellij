const fs = require('fs');
const path = require('path');

const runtimeRequire = require;

module.exports = loadTypeScript();

function loadTypeScript() {
  const tsdk = consumeTypeScriptSdkArgument();
  if (tsdk) {
    const typeScriptPath = findTypeScriptSdkEntryPoint(tsdk);
    if (typeScriptPath) {
      return runtimeRequire(typeScriptPath);
    }
  }

  return runtimeRequire('typescript');
}

function consumeTypeScriptSdkArgument() {
  for (let i = 2; i < process.argv.length; i++) {
    const arg = process.argv[i];
    if (arg === '--tsdk' && i + 1 < process.argv.length) {
      const tsdk = process.argv[i + 1];
      process.argv.splice(i, 2);
      return tsdk;
    }
    if (arg.startsWith('--tsdk=')) {
      process.argv.splice(i, 1);
      return arg.slice('--tsdk='.length);
    }
  }

  return undefined;
}

function findTypeScriptSdkEntryPoint(tsdk) {
  const candidates = [
    path.join(tsdk, 'typescript.js'),
    path.join(tsdk, 'tsserverlibrary.js'),
    path.join(tsdk, 'lib', 'typescript.js'),
    path.join(tsdk, 'lib', 'tsserverlibrary.js'),
  ];

  return candidates.find(candidate => fs.existsSync(candidate));
}
