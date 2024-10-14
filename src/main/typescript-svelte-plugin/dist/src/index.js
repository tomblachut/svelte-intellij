"use strict";
var __create = Object.create;
var __defProp = Object.defineProperty;
var __defProps = Object.defineProperties;
var __getOwnPropDesc = Object.getOwnPropertyDescriptor;
var __getOwnPropDescs = Object.getOwnPropertyDescriptors;
var __getOwnPropNames = Object.getOwnPropertyNames;
var __getOwnPropSymbols = Object.getOwnPropertySymbols;
var __getProtoOf = Object.getPrototypeOf;
var __hasOwnProp = Object.prototype.hasOwnProperty;
var __propIsEnum = Object.prototype.propertyIsEnumerable;
var __defNormalProp = (obj, key, value) => key in obj ? __defProp(obj, key, { enumerable: true, configurable: true, writable: true, value }) : obj[key] = value;
var __spreadValues = (a, b) => {
  for (var prop in b || (b = {}))
    if (__hasOwnProp.call(b, prop))
      __defNormalProp(a, prop, b[prop]);
  if (__getOwnPropSymbols)
    for (var prop of __getOwnPropSymbols(b)) {
      if (__propIsEnum.call(b, prop))
        __defNormalProp(a, prop, b[prop]);
    }
  return a;
};
var __spreadProps = (a, b) => __defProps(a, __getOwnPropDescs(b));
var __commonJS = (cb, mod) => function __require() {
  return mod || (0, cb[__getOwnPropNames(cb)[0]])((mod = { exports: {} }).exports, mod), mod.exports;
};
var __copyProps = (to, from, except, desc) => {
  if (from && typeof from === "object" || typeof from === "function") {
    for (let key of __getOwnPropNames(from))
      if (!__hasOwnProp.call(to, key) && key !== except)
        __defProp(to, key, { get: () => from[key], enumerable: !(desc = __getOwnPropDesc(from, key)) || desc.enumerable });
  }
  return to;
};
var __toESM = (mod, isNodeMode, target) => (target = mod != null ? __create(__getProtoOf(mod)) : {}, __copyProps(
  // If the importer is in node compatibility mode or this is not an ESM
  // file that has been converted to a CommonJS file using a Babel-
  // compatible transform (i.e. "__esModule" has not been set), then set
  // "default" to the CommonJS "module.exports" for node compatibility.
  isNodeMode || !mod || !mod.__esModule ? __defProp(target, "default", { value: mod, enumerable: true }) : target,
  mod
));

// node_modules/typescript-svelte-plugin/dist/src/utils.js
var require_utils = __commonJS({
  "node_modules/typescript-svelte-plugin/dist/src/utils.js"(exports2) {
    "use strict";
    Object.defineProperty(exports2, "__esModule", { value: true });
    exports2.isSvelteFilePath = isSvelteFilePath;
    exports2.isVirtualSvelteFilePath = isVirtualSvelteFilePath;
    exports2.toRealSvelteFilePath = toRealSvelteFilePath;
    exports2.toVirtualSvelteFilePath = toVirtualSvelteFilePath;
    exports2.ensureRealSvelteFilePath = ensureRealSvelteFilePath;
    exports2.isNotNullOrUndefined = isNotNullOrUndefined;
    exports2.isInGeneratedCode = isInGeneratedCode;
    exports2.isNoTextSpanInGeneratedCode = isNoTextSpanInGeneratedCode;
    exports2.replaceDeep = replaceDeep;
    exports2.getConfigPathForProject = getConfigPathForProject;
    exports2.isStoreVariableIn$storeDeclaration = isStoreVariableIn$storeDeclaration;
    exports2.get$storeOffsetOf$storeDeclaration = get$storeOffsetOf$storeDeclaration;
    exports2.findNodeAtSpan = findNodeAtSpan;
    exports2.findNodeAtPosition = findNodeAtPosition;
    exports2.isTopLevelExport = isTopLevelExport;
    exports2.isGeneratedSvelteComponentName = isGeneratedSvelteComponentName;
    exports2.offsetOfGeneratedComponentExport = offsetOfGeneratedComponentExport;
    exports2.gatherDescendants = gatherDescendants;
    exports2.findIdentifier = findIdentifier;
    exports2.getProjectDirectory = getProjectDirectory;
    exports2.hasNodeModule = hasNodeModule;
    exports2.isSvelteProject = isSvelteProject;
    exports2.importSvelteCompiler = importSvelteCompiler;
    var path_12 = require("path");
    function isSvelteFilePath(filePath) {
      return filePath.endsWith(".svelte");
    }
    function isVirtualSvelteFilePath(filePath) {
      return filePath.endsWith(".d.svelte.ts");
    }
    function toRealSvelteFilePath(filePath) {
      return filePath.slice(
        0,
        -11
        /* 'd.svelte.ts'.length */
      ) + "svelte";
    }
    function toVirtualSvelteFilePath(svelteFilePath) {
      return isVirtualSvelteFilePath(svelteFilePath) ? svelteFilePath : svelteFilePath.slice(
        0,
        -6
        /* 'svelte'.length */
      ) + "d.svelte.ts";
    }
    function ensureRealSvelteFilePath(filePath) {
      return isVirtualSvelteFilePath(filePath) ? toRealSvelteFilePath(filePath) : filePath;
    }
    function isNotNullOrUndefined(val) {
      return val !== void 0 && val !== null;
    }
    function isInGeneratedCode(text, start, end) {
      const lineStart = text.lastIndexOf("\n", start);
      const lineEnd = text.indexOf("\n", end);
      const lastStart = text.substring(lineStart, start).lastIndexOf("/*\u03A9ignore_start\u03A9*/");
      const lastEnd = text.substring(lineStart, start).lastIndexOf("/*\u03A9ignore_end\u03A9*/");
      return lastStart > lastEnd && text.substring(end, lineEnd).includes("/*\u03A9ignore_end\u03A9*/");
    }
    function isNoTextSpanInGeneratedCode(text, span) {
      return !isInGeneratedCode(text, span.start, span.start + span.length);
    }
    function replaceDeep(obj, searchStr, replacementStr) {
      return _replaceDeep(obj);
      function _replaceDeep(_obj) {
        if (typeof _obj === "string") {
          return _obj.replace(searchStr, replacementStr);
        }
        if (Array.isArray(_obj)) {
          return _obj.map((entry) => _replaceDeep(entry));
        }
        if (typeof _obj === "object") {
          return Object.keys(_obj).reduce((_o, key) => {
            _o[key] = _replaceDeep(_obj[key]);
            return _o;
          }, {});
        }
        return _obj;
      }
    }
    function getConfigPathForProject(project) {
      var _a;
      return (_a = project.canonicalConfigFilePath) != null ? _a : project.getCompilerOptions().configFilePath;
    }
    function isStoreVariableIn$storeDeclaration(text, varStart) {
      return text.lastIndexOf("__sveltets_2_store_get(", varStart) === varStart - "__sveltets_2_store_get(".length;
    }
    function get$storeOffsetOf$storeDeclaration(text, storePosition) {
      return text.lastIndexOf(" =", storePosition) - 1;
    }
    function findNodeAtSpan(node, span, predicate) {
      const { start, length } = span;
      const end = start + length;
      for (const child of node.getChildren()) {
        const childStart = child.getStart();
        if (end <= childStart) {
          return;
        }
        const childEnd = child.getEnd();
        if (start >= childEnd) {
          continue;
        }
        if (start === childStart && end === childEnd) {
          if (!predicate) {
            return child;
          }
          if (predicate(child)) {
            return child;
          }
        }
        const foundInChildren = findNodeAtSpan(child, span, predicate);
        if (foundInChildren) {
          return foundInChildren;
        }
      }
    }
    function findNodeAtPosition(node, pos, predicate) {
      for (const child of node.getChildren()) {
        const childStart = child.getStart();
        if (pos < childStart) {
          return;
        }
        const childEnd = child.getEnd();
        if (pos > childEnd) {
          continue;
        }
        const foundInChildren = findNodeAtPosition(child, pos, predicate);
        if (foundInChildren) {
          return foundInChildren;
        }
        if (!predicate) {
          return child;
        }
        if (predicate(child)) {
          return child;
        }
      }
    }
    function isTopLevelExport(ts, node, source) {
      var _a, _b;
      return ts.isVariableStatement(node) && source.statements.includes(node) || ts.isIdentifier(node) && node.parent && ts.isVariableDeclaration(node.parent) && source.statements.includes((_b = (_a = node.parent) == null ? void 0 : _a.parent) == null ? void 0 : _b.parent) || ts.isIdentifier(node) && node.parent && ts.isFunctionDeclaration(node.parent) && source.statements.includes(node.parent);
    }
    var COMPONENT_SUFFIX = "__SvelteComponent_";
    function isGeneratedSvelteComponentName(className) {
      return className.endsWith(COMPONENT_SUFFIX);
    }
    function offsetOfGeneratedComponentExport(snapshot) {
      return snapshot.getText().lastIndexOf(COMPONENT_SUFFIX);
    }
    function gatherDescendants(node, predicate, dest = []) {
      if (predicate(node)) {
        dest.push(node);
      } else {
        for (const child of node.getChildren()) {
          gatherDescendants(child, predicate, dest);
        }
      }
      return dest;
    }
    function findIdentifier(ts, node) {
      if (ts.isIdentifier(node)) {
        return node;
      }
      if (ts.isFunctionDeclaration(node)) {
        return node.name;
      }
      while (node) {
        if (ts.isIdentifier(node)) {
          return node;
        }
        if (ts.isVariableDeclaration(node) && ts.isIdentifier(node.name)) {
          return node.name;
        }
        node = node.parent;
      }
    }
    function getProjectDirectory(project) {
      const compilerOptions = project.getCompilerOptions();
      if (typeof compilerOptions.configFilePath === "string") {
        return (0, path_12.dirname)(compilerOptions.configFilePath);
      }
      const packageJsonPath = (0, path_12.join)(project.getCurrentDirectory(), "package.json");
      return project.fileExists(packageJsonPath) ? project.getCurrentDirectory() : void 0;
    }
    function hasNodeModule(startPath, module3) {
      try {
        const hasModule = require.resolve(module3, { paths: [startPath] });
        return hasModule;
      } catch (e) {
        return (e == null ? void 0 : e.code) === "ERR_PACKAGE_PATH_NOT_EXPORTED";
      }
    }
    function isSvelteProject(project) {
      const projectDirectory = getProjectDirectory(project);
      if (projectDirectory) {
        return hasNodeModule(projectDirectory, "svelte");
      }
      const packageJsons = project.readDirectory(
        project.getCurrentDirectory(),
        [".json"],
        ["node_modules", "dist", "build"],
        ["**/package.json"],
        // assuming structure like packages/projectName
        3
      ).filter((file) => file.endsWith("package.json") && !hasConfigInConjunction(file, project));
      return packageJsons.some((packageJsonPath) => hasNodeModule((0, path_12.dirname)(packageJsonPath), "svelte"));
    }
    function hasConfigInConjunction(packageJsonPath, project) {
      const dir = (0, path_12.dirname)(packageJsonPath);
      return project.fileExists((0, path_12.join)(dir, "tsconfig.json")) || project.fileExists((0, path_12.join)(dir, "jsconfig.json"));
    }
    function importSvelteCompiler(fromPath) {
      if (!fromPath)
        return void 0;
      try {
        const sveltePath = require.resolve("svelte/compiler", { paths: [fromPath] });
        const compiler = require(sveltePath);
        if (compiler.VERSION.split(".")[0] === "3") {
          return void 0;
        }
        return compiler;
      } catch (e) {
      }
    }
  }
});

// node_modules/typescript-svelte-plugin/dist/src/language-service/call-hierarchy.js
var require_call_hierarchy = __commonJS({
  "node_modules/typescript-svelte-plugin/dist/src/language-service/call-hierarchy.js"(exports2) {
    "use strict";
    Object.defineProperty(exports2, "__esModule", { value: true });
    exports2.decorateCallHierarchy = decorateCallHierarchy;
    var utils_12 = require_utils();
    var ENSURE_COMPONENT_HELPER = "__sveltets_2_ensureComponent";
    function decorateCallHierarchy(ls, snapshotManager, typescript) {
      const provideCallHierarchyIncomingCalls = ls.provideCallHierarchyIncomingCalls;
      const provideCallHierarchyOutgoingCalls = ls.provideCallHierarchyOutgoingCalls;
      ls.provideCallHierarchyIncomingCalls = (fileName, position) => {
        var _a;
        const program = ls.getProgram();
        if (!program) {
          return provideCallHierarchyIncomingCalls(fileName, position);
        }
        const snapshot = snapshotManager.get(fileName);
        const componentExportOffset = isComponentModulePosition(fileName, position) && snapshot ? (0, utils_12.offsetOfGeneratedComponentExport)(snapshot) : -1;
        const redirectedPosition = componentExportOffset >= 0 ? componentExportOffset : position;
        const tsResult = provideCallHierarchyIncomingCalls(fileName, redirectedPosition);
        return tsResult.map((item) => {
          if (!(0, utils_12.isSvelteFilePath)(item.from.file)) {
            return item;
          }
          const snapshot2 = snapshotManager.get(item.from.file);
          const from = convertSvelteCallHierarchyItem(item.from, program);
          if (!from || !snapshot2) {
            return null;
          }
          const fromSpans = item.fromSpans.map((span) => snapshot2.getOriginalTextSpan(span)).filter(utils_12.isNotNullOrUndefined);
          return {
            from,
            fromSpans
          };
        }).concat((_a = getInComingCallsForComponent(ls, program, fileName, redirectedPosition)) != null ? _a : []).filter(utils_12.isNotNullOrUndefined);
      };
      ls.provideCallHierarchyOutgoingCalls = (fileName, position) => {
        var _a, _b, _c;
        const program = ls.getProgram();
        if (!program) {
          return provideCallHierarchyOutgoingCalls(fileName, position);
        }
        const sourceFile = program == null ? void 0 : program.getSourceFile(fileName);
        const renderFunctionOffset = isComponentModulePosition(fileName, position) && sourceFile ? (_b = (_a = sourceFile.statements.find((statement) => {
          var _a2;
          return typescript.isFunctionDeclaration(statement) && ((_a2 = statement.name) == null ? void 0 : _a2.getText()) === "render";
        })) == null ? void 0 : _a.name) == null ? void 0 : _b.getStart() : -1;
        const offset = renderFunctionOffset != null && renderFunctionOffset >= 0 ? renderFunctionOffset : position;
        const snapshot = snapshotManager.get(fileName);
        return provideCallHierarchyOutgoingCalls(fileName, offset).concat(program && sourceFile && isComponentModulePosition(fileName, position) ? (_c = getOutgoingCallsForComponent(program, sourceFile)) != null ? _c : [] : []).map((item) => {
          const to = convertSvelteCallHierarchyItem(item.to, program);
          if (!to || item.to.name.startsWith("__sveltets") || item.to.containerName === "svelteHTML") {
            return null;
          }
          const fromSpans = snapshot ? item.fromSpans.map((span) => snapshot.getOriginalTextSpan(span)).filter(utils_12.isNotNullOrUndefined) : item.fromSpans;
          if (!fromSpans.length) {
            return null;
          }
          return {
            to,
            fromSpans
          };
        }).filter(utils_12.isNotNullOrUndefined);
      };
      function isComponentModulePosition(fileName, position) {
        return (0, utils_12.isSvelteFilePath)(fileName) && position === 0;
      }
      function convertSvelteCallHierarchyItem(item, program) {
        if (!(0, utils_12.isSvelteFilePath)(item.file)) {
          return item;
        }
        const snapshot = snapshotManager.get(item.file);
        if (!snapshot) {
          return null;
        }
        const redirectedCallHierarchyItem = redirectSvelteCallHierarchyItem(snapshot, program, item);
        if (redirectedCallHierarchyItem) {
          return redirectedCallHierarchyItem;
        }
        const selectionSpan = snapshot.getOriginalTextSpan(item.selectionSpan);
        if (!selectionSpan) {
          return null;
        }
        const span = snapshot.getOriginalTextSpan(item.span);
        if (!span) {
          return null;
        }
        return __spreadProps(__spreadValues({}, item), {
          span,
          selectionSpan
        });
      }
      function redirectSvelteCallHierarchyItem(snapshot, program, item) {
        const sourceFile = program.getSourceFile(item.file);
        if (!sourceFile) {
          return null;
        }
        if ((0, utils_12.isGeneratedSvelteComponentName)(item.name)) {
          return toComponentCallHierarchyItem(snapshot, item.file);
        }
        if (item.name === "render") {
          const end = item.selectionSpan.start + item.selectionSpan.length;
          const renderFunction = sourceFile.statements.find((statement) => statement.getStart() <= item.selectionSpan.start && statement.getEnd() >= end);
          if (!renderFunction || !sourceFile.statements.includes(renderFunction)) {
            return null;
          }
          return toComponentCallHierarchyItem(snapshot, item.file);
        }
        return null;
      }
      function toComponentCallHierarchyItem(snapshot, file) {
        const fileSpan = { start: 0, length: snapshot.getOriginalText().length };
        return {
          kind: typescript.ScriptElementKind.moduleElement,
          file,
          name: "",
          selectionSpan: { start: 0, length: 0 },
          span: fileSpan
        };
      }
      function getInComingCallsForComponent(ls2, program, fileName, position) {
        var _a, _b;
        if (!(0, utils_12.isSvelteFilePath)(fileName)) {
          return null;
        }
        return (_b = (_a = ls2.findReferences(fileName, position)) == null ? void 0 : _a.map((ref) => componentRefToIncomingCall(ref, program)).filter(utils_12.isNotNullOrUndefined)) != null ? _b : null;
      }
      function componentRefToIncomingCall(ref, program) {
        const snapshot = (0, utils_12.isSvelteFilePath)(ref.definition.fileName) && snapshotManager.get(ref.definition.fileName);
        const sourceFile = program.getSourceFile(ref.definition.fileName);
        if (!snapshot || !sourceFile) {
          return null;
        }
        const startTags = ref.references.map((ref2) => {
          const generatedTextSpan = snapshot.getGeneratedTextSpan(ref2.textSpan);
          const node = generatedTextSpan && (0, utils_12.findNodeAtSpan)(sourceFile, generatedTextSpan, isComponentStartTag);
          if (node) {
            return ref2;
          }
          return null;
        }).filter(utils_12.isNotNullOrUndefined);
        if (!startTags.length) {
          return null;
        }
        return {
          from: toComponentCallHierarchyItem(snapshot, ref.definition.fileName),
          fromSpans: startTags.map((tag) => tag.textSpan)
        };
      }
      function isComponentStartTag(node) {
        return !!node && node.parent && typescript.isCallExpression(node.parent) && typescript.isIdentifier(node.parent.expression) && node.parent.expression.text === ENSURE_COMPONENT_HELPER && typescript.isIdentifier(node) && node === node.parent.arguments[0];
      }
      function getOutgoingCallsForComponent(program, sourceFile) {
        var _a, _b, _c, _d;
        const groups = /* @__PURE__ */ new Map();
        const startTags = (0, utils_12.gatherDescendants)(sourceFile, isComponentStartTag);
        const typeChecker = program.getTypeChecker();
        for (const startTag of startTags) {
          const type = typeChecker.getTypeAtLocation(startTag);
          const symbol = (_a = type.aliasSymbol) != null ? _a : type.symbol;
          const declaration = (_c = symbol == null ? void 0 : symbol.valueDeclaration) != null ? _c : (_b = symbol == null ? void 0 : symbol.declarations) == null ? void 0 : _b[0];
          if (!declaration || !typescript.isClassDeclaration(declaration)) {
            continue;
          }
          let group = groups.get(declaration);
          if (!group) {
            group = [];
            groups.set(declaration, group);
          }
          group.push({ start: startTag.getStart(), length: startTag.getWidth() });
        }
        return (_d = Array.from(groups).map(([declaration, group]) => {
          var _a2, _b2;
          const file = declaration.getSourceFile().fileName;
          const name = (_b2 = (_a2 = declaration.name) == null ? void 0 : _a2.getText()) != null ? _b2 : file.slice(file.lastIndexOf("."));
          const span = { start: declaration.getStart(), length: declaration.getWidth() };
          const selectionSpan = declaration.name ? { start: declaration.name.getStart(), length: declaration.name.getWidth() } : span;
          return {
            to: {
              file,
              kind: typescript.ScriptElementKind.classElement,
              name,
              selectionSpan,
              span
            },
            fromSpans: group
          };
        })) != null ? _d : null;
      }
    }
  }
});

// node_modules/typescript-svelte-plugin/dist/src/language-service/sveltekit.js
var require_sveltekit = __commonJS({
  "node_modules/typescript-svelte-plugin/dist/src/language-service/sveltekit.js"(exports2) {
    "use strict";
    Object.defineProperty(exports2, "__esModule", { value: true });
    exports2.kitExports = void 0;
    exports2.isKitRouteExportAllowedIn = isKitRouteExportAllowedIn;
    exports2.getVirtualLS = getVirtualLS;
    var utils_12 = require_utils();
    var svelte2tsx_1 = require("svelte2tsx");
    var cache = /* @__PURE__ */ new WeakMap();
    function createApiExport(name) {
      return {
        allowedIn: ["api", "server"],
        displayParts: [
          {
            text: "export",
            kind: "keyword"
          },
          {
            text: " ",
            kind: "space"
          },
          {
            text: "async",
            kind: "keyword"
          },
          {
            text: " ",
            kind: "space"
          },
          {
            text: "function",
            kind: "keyword"
          },
          {
            text: " ",
            kind: "space"
          },
          {
            text: name,
            kind: "localName"
          },
          {
            text: "(",
            kind: "punctuation"
          },
          {
            text: "event",
            kind: "parameterName"
          },
          {
            text: ": ",
            kind: "punctuation"
          },
          {
            text: "RequestEvent",
            kind: "interfaceName"
          },
          {
            text: ")",
            kind: "punctuation"
          },
          {
            text: ": ",
            kind: "punctuation"
          },
          {
            text: "Promise",
            kind: "keyword"
          },
          {
            text: "<",
            kind: "punctuation"
          },
          {
            text: "Response",
            kind: "interfaceName"
          },
          {
            text: ">",
            kind: "punctuation"
          }
        ],
        documentation: [
          {
            text: `Handles ${name} requests. More info: https://kit.svelte.dev/docs/routing#server`,
            kind: "text"
          }
        ]
      };
    }
    exports2.kitExports = {
      prerender: {
        allowedIn: ["layout", "page", "api", "server", "universal"],
        displayParts: [
          {
            text: "const",
            kind: "keyword"
          },
          {
            text: " ",
            kind: "space"
          },
          {
            text: "prerender",
            kind: "localName"
          },
          {
            text: ": ",
            kind: "punctuation"
          },
          {
            text: "boolean",
            kind: "keyword"
          },
          {
            text: " | ",
            kind: "punctuation"
          },
          {
            text: "'auto'",
            kind: "stringLiteral"
          }
        ],
        documentation: [
          {
            text: "Control whether or not this page is prerendered. More info: https://kit.svelte.dev/docs/page-options#prerender",
            kind: "text"
          }
        ]
      },
      ssr: {
        allowedIn: ["layout", "page", "server", "universal"],
        displayParts: [
          {
            text: "const",
            kind: "keyword"
          },
          {
            text: " ",
            kind: "space"
          },
          {
            text: "ssr",
            kind: "localName"
          },
          {
            text: ": ",
            kind: "punctuation"
          },
          {
            text: "boolean",
            kind: "keyword"
          }
        ],
        documentation: [
          {
            text: "Control whether or not this page is server-side rendered. More info: https://kit.svelte.dev/docs/page-options#ssr",
            kind: "text"
          }
        ]
      },
      csr: {
        allowedIn: ["layout", "page", "server", "universal"],
        displayParts: [
          {
            text: "const",
            kind: "keyword"
          },
          {
            text: " ",
            kind: "space"
          },
          {
            text: "csr",
            kind: "localName"
          },
          {
            text: ": ",
            kind: "punctuation"
          },
          {
            text: "boolean",
            kind: "keyword"
          }
        ],
        documentation: [
          {
            text: "Control whether or not this page is hydrated (i.e. if JS is delivered to the client). More info: https://kit.svelte.dev/docs/page-options#csr",
            kind: "text"
          }
        ]
      },
      trailingSlash: {
        allowedIn: ["layout", "page", "api", "server", "universal"],
        displayParts: [
          {
            text: "const",
            kind: "keyword"
          },
          {
            text: " ",
            kind: "space"
          },
          {
            text: "trailingSlash",
            kind: "localName"
          },
          {
            text: ": ",
            kind: "punctuation"
          },
          {
            text: "'never' | 'always' | 'ignore'",
            kind: "stringLiteral"
          }
        ],
        documentation: [
          {
            text: "Control how SvelteKit should handle trailing slashes in the URL. More info: https://kit.svelte.dev/docs/page-options#trailingslash",
            kind: "text"
          }
        ]
      },
      config: {
        allowedIn: ["layout", "page", "api", "server", "universal"],
        displayParts: [
          {
            text: "const",
            kind: "keyword"
          },
          {
            text: " ",
            kind: "space"
          },
          {
            text: "config",
            kind: "localName"
          },
          {
            text: ": ",
            kind: "punctuation"
          },
          {
            text: "Config",
            kind: "interfaceName"
          }
        ],
        documentation: [
          {
            text: `With the concept of adapters, SvelteKit is able to run on a variety of platforms. Each of these might have specific configuration to further tweak the deployment, which you can configure here. More info: https://kit.svelte.dev/docs/page-options#config`,
            kind: "text"
          }
        ]
      },
      actions: {
        allowedIn: ["page", "server"],
        displayParts: [
          {
            text: "const",
            kind: "keyword"
          },
          {
            text: " ",
            kind: "space"
          },
          {
            text: "actions",
            kind: "localName"
          },
          {
            text: ": ",
            kind: "punctuation"
          },
          {
            text: "Actions",
            kind: "interfaceName"
          }
        ],
        documentation: [
          {
            text: `An object of methods which handle form POST requests. More info: https://kit.svelte.dev/docs/form-actions`,
            kind: "text"
          }
        ]
      },
      load: {
        allowedIn: ["layout", "page", "server", "universal"],
        displayParts: [
          {
            text: "export",
            kind: "keyword"
          },
          {
            text: " ",
            kind: "space"
          },
          {
            text: "function",
            kind: "keyword"
          },
          {
            text: " ",
            kind: "space"
          },
          {
            text: "load",
            kind: "localName"
          },
          {
            text: "(",
            kind: "punctuation"
          },
          {
            text: "event",
            kind: "parameterName"
          },
          {
            text: ": ",
            kind: "punctuation"
          },
          {
            text: "LoadEvent",
            kind: "interfaceName"
          },
          {
            text: ")",
            kind: "punctuation"
          },
          {
            text: ": ",
            kind: "punctuation"
          },
          {
            text: "Promise",
            kind: "keyword"
          },
          {
            text: "<",
            kind: "punctuation"
          },
          {
            text: "LoadOutput",
            kind: "interfaceName"
          },
          {
            text: ">",
            kind: "punctuation"
          }
        ],
        documentation: [
          {
            text: "Loads data for the given page or layout. More info: https://kit.svelte.dev/docs/load",
            kind: "text"
          }
        ]
      },
      entries: {
        allowedIn: ["api", "page", "server", "universal"],
        displayParts: [
          {
            text: "export",
            kind: "keyword"
          },
          {
            text: " ",
            kind: "space"
          },
          {
            text: "function",
            kind: "keyword"
          },
          {
            text: " ",
            kind: "space"
          },
          {
            text: "entries",
            kind: "functionName"
          },
          {
            text: "() {}",
            kind: "punctuation"
          }
        ],
        documentation: [
          {
            text: "Generate values for dynamic parameters in prerendered pages.\nMore info: https://kit.svelte.dev/docs/page-options#entries",
            kind: "text"
          }
        ]
      },
      GET: createApiExport("GET"),
      POST: createApiExport("POST"),
      PUT: createApiExport("PUT"),
      PATCH: createApiExport("PATCH"),
      DELETE: createApiExport("DELETE"),
      OPTIONS: createApiExport("OPTIONS"),
      HEAD: createApiExport("HEAD"),
      fallback: createApiExport("fallback"),
      // param matching
      match: {
        allowedIn: [],
        displayParts: [],
        documentation: [
          {
            text: `A parameter matcher. More info: https://kit.svelte.dev/docs/advanced-routing#matching`,
            kind: "text"
          }
        ]
      },
      // hooks
      handle: {
        allowedIn: [],
        displayParts: [],
        documentation: [
          {
            text: `The  handle hook runs every time the SvelteKit server receives a request and determines the response. It receives an 'event' object representing the request and a function called 'resolve', which renders the route and generates a Response. This allows you to modify response headers or bodies, or bypass SvelteKit entirely (for implementing routes programmatically, for example). More info: https://kit.svelte.dev/docs/hooks#server-hooks-handle`,
            kind: "text"
          }
        ]
      },
      handleFetch: {
        allowedIn: [],
        displayParts: [],
        documentation: [
          {
            text: `The handleFetch hook allows you to modify (or replace) a 'fetch' request that happens inside a 'load' function that runs on the server (or during pre-rendering). More info: https://kit.svelte.dev/docs/hooks#server-hooks-handlefetch`,
            kind: "text"
          }
        ]
      },
      handleError: {
        allowedIn: [],
        displayParts: [],
        documentation: [
          {
            text: `The handleError hook runs when an unexpected error is thrown while responding to a request. If an unexpected error is thrown during loading or rendering, this function will be called with the error and the event. Make sure that this function _never_ throws an error. More info: https://kit.svelte.dev/docs/hooks#shared-hooks-handleerror`,
            kind: "text"
          }
        ]
      },
      reroute: {
        allowedIn: [],
        displayParts: [],
        documentation: [
          {
            text: `This function allows you to change how URLs are translated into routes. The returned pathname (which defaults to url.pathname) is used to select the route and its parameters. More info: https://kit.svelte.dev/docs/hooks#universal-hooks-reroute`,
            kind: "text"
          }
        ]
      }
    };
    var FORCE_UPDATE_VERSION = "FORCE_UPDATE_VERSION";
    function isKitRouteExportAllowedIn(basename, kitExport) {
      if (!basename.startsWith("+")) {
        return false;
      }
      const allowedIn = kitExport.allowedIn;
      return (basename.includes("layout") ? allowedIn.includes("layout") : basename.includes("+server") ? allowedIn.includes("api") : allowedIn.includes("page")) && (basename.includes("server") ? allowedIn.includes("server") : allowedIn.includes("universal"));
    }
    var kitFilesSettings = {
      paramsPath: "src/params",
      clientHooksPath: "src/hooks.client",
      serverHooksPath: "src/hooks.server",
      universalHooksPath: "src/hooks"
    };
    function getProxiedLanguageService(info, ts, logger) {
      const cachedProxiedLanguageService = cache.get(info);
      if (cachedProxiedLanguageService !== void 0) {
        return cachedProxiedLanguageService != null ? cachedProxiedLanguageService : void 0;
      }
      const projectDirectory = (0, utils_12.getProjectDirectory)(info.project);
      if (projectDirectory && !(0, utils_12.hasNodeModule)(projectDirectory, "@sveltejs/kit")) {
        cache.set(info, null);
        return;
      }
      const originalLanguageServiceHost = info.languageServiceHost;
      class ProxiedLanguageServiceHost {
        constructor() {
          this.files = {};
          this.resolveModuleNames = originalLanguageServiceHost.resolveModuleNames ? (...args) => {
            return originalLanguageServiceHost.resolveModuleNames(
              ...args
            );
          } : void 0;
          this.resolveModuleNameLiterals = originalLanguageServiceHost.resolveModuleNameLiterals ? (...args) => {
            return originalLanguageServiceHost.resolveModuleNameLiterals(
              ...args
            );
          } : void 0;
          this.readDirectory = originalLanguageServiceHost.readDirectory ? (...args) => {
            return originalLanguageServiceHost.readDirectory(...args);
          } : void 0;
          this.getDirectories = originalLanguageServiceHost.getDirectories ? (...args) => {
            return originalLanguageServiceHost.getDirectories(...args);
          } : void 0;
          this.getCancellationToken = originalLanguageServiceHost.getCancellationToken ? () => originalLanguageServiceHost.getCancellationToken() : void 0;
          this.getNewLine = originalLanguageServiceHost.getNewLine ? () => originalLanguageServiceHost.getNewLine() : void 0;
          this.useCaseSensitiveFileNames = originalLanguageServiceHost.useCaseSensitiveFileNames ? () => originalLanguageServiceHost.useCaseSensitiveFileNames() : void 0;
          this.realpath = originalLanguageServiceHost.realpath ? (...args) => originalLanguageServiceHost.realpath(...args) : void 0;
          this.getProjectReferences = originalLanguageServiceHost.getProjectReferences ? () => originalLanguageServiceHost.getProjectReferences() : void 0;
          this.getParsedCommandLine = originalLanguageServiceHost.getParsedCommandLine ? (fileName) => originalLanguageServiceHost.getParsedCommandLine(fileName) : void 0;
          this.getCachedExportInfoMap = originalLanguageServiceHost.getCachedExportInfoMap ? () => originalLanguageServiceHost.getCachedExportInfoMap() : void 0;
          this.getModuleSpecifierCache = originalLanguageServiceHost.getModuleSpecifierCache ? () => originalLanguageServiceHost.getModuleSpecifierCache() : void 0;
          this.getGlobalTypingsCacheLocation = originalLanguageServiceHost.getGlobalTypingsCacheLocation ? () => originalLanguageServiceHost.getGlobalTypingsCacheLocation() : void 0;
          this.getSymlinkCache = originalLanguageServiceHost.getSymlinkCache ? (...args) => originalLanguageServiceHost.getSymlinkCache(
            ...args
          ) : void 0;
          this.getPackageJsonsVisibleToFile = originalLanguageServiceHost.getPackageJsonsVisibleToFile ? (...args) => originalLanguageServiceHost.getPackageJsonsVisibleToFile(
            ...args
          ) : void 0;
          this.getPackageJsonAutoImportProvider = originalLanguageServiceHost.getPackageJsonAutoImportProvider ? () => originalLanguageServiceHost.getPackageJsonAutoImportProvider() : void 0;
          this.getModuleResolutionCache = originalLanguageServiceHost.getModuleResolutionCache ? () => originalLanguageServiceHost.getModuleResolutionCache() : void 0;
        }
        log() {
        }
        trace() {
        }
        error() {
        }
        getCompilationSettings() {
          return originalLanguageServiceHost.getCompilationSettings();
        }
        getCurrentDirectory() {
          return originalLanguageServiceHost.getCurrentDirectory();
        }
        getDefaultLibFileName(o) {
          return originalLanguageServiceHost.getDefaultLibFileName(o);
        }
        getScriptVersion(fileName) {
          const file = this.files[fileName];
          if (!file)
            return originalLanguageServiceHost.getScriptVersion(fileName);
          return file.version.toString();
        }
        getScriptSnapshot(fileName) {
          const file = this.files[fileName];
          if (!file)
            return originalLanguageServiceHost.getScriptSnapshot(fileName);
          return file.file;
        }
        getScriptFileNames() {
          const names = new Set(Object.keys(this.files));
          const files = originalLanguageServiceHost.getScriptFileNames();
          for (const file of files) {
            names.add(file);
          }
          return [...names];
        }
        getKitScriptSnapshotIfUpToDate(fileName) {
          const scriptVersion = this.getScriptVersion(fileName);
          if (!this.files[fileName] || scriptVersion !== originalLanguageServiceHost.getScriptVersion(fileName) || scriptVersion === FORCE_UPDATE_VERSION) {
            return void 0;
          }
          return this.files[fileName];
        }
        upsertKitFile(fileName) {
          const result = svelte2tsx_1.internalHelpers.upsertKitFile(ts, fileName, kitFilesSettings, () => {
            var _a;
            return (_a = info.languageService.getProgram()) == null ? void 0 : _a.getSourceFile(fileName);
          });
          if (!result) {
            return;
          }
          const { text, addedCode } = result;
          const snap = ts.ScriptSnapshot.fromString(text);
          snap.getChangeRange = (_) => void 0;
          this.files[fileName] = {
            version: this.files[fileName] === void 0 ? FORCE_UPDATE_VERSION : originalLanguageServiceHost.getScriptVersion(fileName),
            file: snap,
            addedCode
          };
          return this.files[fileName];
        }
        readFile(fileName) {
          const file = this.files[fileName];
          return file ? file.file.getText(0, file.file.getLength()) : originalLanguageServiceHost.readFile(fileName);
        }
        fileExists(fileName) {
          return this.files[fileName] !== void 0 || originalLanguageServiceHost.fileExists(fileName);
        }
      }
      const languageServiceHost = new ProxiedLanguageServiceHost();
      const languageService = ts.createLanguageService(languageServiceHost, createProxyRegistry(ts, originalLanguageServiceHost, kitFilesSettings));
      cache.set(info, { languageService, languageServiceHost });
      return {
        languageService,
        languageServiceHost
      };
    }
    function createProxyRegistry(ts, originalLanguageServiceHost, options) {
      const registry = ts.createDocumentRegistry();
      return registry;
    }
    function getVirtualLS(fileName, info, ts, logger) {
      var _a;
      const proxy = getProxiedLanguageService(info, ts, logger);
      if (!proxy) {
        return;
      }
      const result = (_a = proxy.languageServiceHost.getKitScriptSnapshotIfUpToDate(fileName)) != null ? _a : proxy.languageServiceHost.upsertKitFile(fileName);
      if (result) {
        return {
          languageService: proxy.languageService,
          addedCode: result.addedCode,
          toVirtualPos: (pos) => svelte2tsx_1.internalHelpers.toVirtualPos(pos, result.addedCode),
          toOriginalPos: (pos) => svelte2tsx_1.internalHelpers.toOriginalPos(pos, result.addedCode)
        };
      }
    }
  }
});

// node_modules/typescript-svelte-plugin/dist/src/language-service/completions.js
var require_completions = __commonJS({
  "node_modules/typescript-svelte-plugin/dist/src/language-service/completions.js"(exports2) {
    "use strict";
    Object.defineProperty(exports2, "__esModule", { value: true });
    exports2.decorateCompletions = decorateCompletions;
    var path_12 = require("path");
    var utils_12 = require_utils();
    var sveltekit_1 = require_sveltekit();
    var componentPostfix = "__SvelteComponent_";
    function decorateCompletions(ls, info, ts, logger) {
      const getCompletionsAtPosition = ls.getCompletionsAtPosition;
      ls.getCompletionsAtPosition = (fileName, position, options, settings) => {
        var _a, _b, _c, _d, _e;
        let completions;
        const result = (0, sveltekit_1.getVirtualLS)(fileName, info, ts);
        if (result) {
          const { languageService, toVirtualPos, toOriginalPos } = result;
          completions = languageService.getCompletionsAtPosition(fileName, toVirtualPos(position), options, settings);
          if (completions) {
            completions.entries = completions.entries.map((c) => {
              if (c.replacementSpan) {
                return __spreadProps(__spreadValues({}, c), {
                  replacementSpan: __spreadProps(__spreadValues({}, c.replacementSpan), {
                    start: toOriginalPos(c.replacementSpan.start).pos
                  })
                });
              }
              return c;
            });
            if (completions.optionalReplacementSpan) {
              completions.optionalReplacementSpan = __spreadProps(__spreadValues({}, completions.optionalReplacementSpan), {
                start: toOriginalPos(completions.optionalReplacementSpan.start).pos
              });
            }
          }
        }
        completions = completions != null ? completions : getCompletionsAtPosition(fileName, position, options, settings);
        if (!completions) {
          const source = (_a = ls.getProgram()) == null ? void 0 : _a.getSourceFile(fileName);
          const node = source && (0, utils_12.findNodeAtPosition)(source, position);
          if (node && (0, utils_12.isTopLevelExport)(ts, node, source)) {
            return {
              entries: Object.entries(sveltekit_1.kitExports).filter(([, value]) => (0, sveltekit_1.isKitRouteExportAllowedIn)((0, path_12.basename)(fileName), value)).map(([key, value]) => ({
                kind: ts.ScriptElementKind.constElement,
                name: key,
                labelDetails: {
                  description: value.documentation.map((d) => d.text).join("")
                },
                sortText: "0",
                data: {
                  __sveltekit: key,
                  exportName: key
                  // TS needs this
                }
              })),
              isGlobalCompletion: false,
              isMemberCompletion: false,
              isNewIdentifierLocation: false,
              isIncomplete: true
            };
          }
          return completions;
        }
        if ((0, path_12.basename)(fileName).startsWith("+")) {
          const $typeImports = /* @__PURE__ */ new Map();
          for (const c of completions.entries) {
            if (((_b = c.source) == null ? void 0 : _b.includes(".svelte-kit/types")) && c.data) {
              $typeImports.set(c.name, c);
            }
          }
          for (const $typeImport of $typeImports.values()) {
            const routesFolder = "src/routes";
            const relativeFileName = (_c = fileName.split(routesFolder)[1]) == null ? void 0 : _c.slice(1);
            if (relativeFileName) {
              const relativePath = (0, path_12.dirname)(relativeFileName) === "." ? "" : `${(0, path_12.dirname)(relativeFileName)}/`;
              const modifiedSource = $typeImport.source.split(".svelte-kit/types")[0] + // note the missing .d.ts at the end - TS wants it that way for some reason
              `.svelte-kit/types/${routesFolder}/${relativePath}$types`;
              completions.entries.push(__spreadProps(__spreadValues({}, $typeImport), {
                // Ensure it's sorted above the other imports
                sortText: !isNaN(Number($typeImport.sortText)) ? String(Number($typeImport.sortText) - 1) : $typeImport.sortText,
                source: modifiedSource,
                data: __spreadProps(__spreadValues({}, $typeImport.data), {
                  fileName: (_d = $typeImport.data.fileName) == null ? void 0 : _d.replace($typeImport.source, modifiedSource),
                  moduleSpecifier: (_e = $typeImport.data.moduleSpecifier) == null ? void 0 : _e.replace($typeImport.source, modifiedSource),
                  __is_sveltekit$typeImport: true
                })
              }));
            }
          }
        }
        return __spreadProps(__spreadValues({}, completions), {
          entries: completions.entries.map((entry) => {
            var _a2;
            if (!(0, utils_12.isSvelteFilePath)(entry.source || "") || !entry.name.endsWith(componentPostfix)) {
              return entry;
            }
            return __spreadProps(__spreadValues({}, entry), {
              insertText: (_a2 = entry.insertText) == null ? void 0 : _a2.replace(componentPostfix, ""),
              name: entry.name.slice(0, -componentPostfix.length)
            });
          })
        });
      };
      const getCompletionEntryDetails = ls.getCompletionEntryDetails;
      ls.getCompletionEntryDetails = (fileName, position, entryName, formatOptions, source, preferences, data) => {
        var _a, _b;
        if (data == null ? void 0 : data.__sveltekit) {
          const key = data == null ? void 0 : data.__sveltekit;
          return {
            name: key,
            kind: ts.ScriptElementKind.constElement,
            kindModifiers: ts.ScriptElementKindModifier.none,
            displayParts: sveltekit_1.kitExports[key].displayParts,
            documentation: sveltekit_1.kitExports[key].documentation
          };
        }
        const is$typeImport = data == null ? void 0 : data.__is_sveltekit$typeImport;
        let details;
        const result = (0, sveltekit_1.getVirtualLS)(fileName, info, ts);
        if (result) {
          const { languageService, toVirtualPos, toOriginalPos } = result;
          details = languageService.getCompletionEntryDetails(fileName, toVirtualPos(position), entryName, formatOptions, source, preferences, data);
          if (details) {
            details.codeActions = (_a = details.codeActions) == null ? void 0 : _a.map((codeAction) => {
              codeAction.changes = codeAction.changes.map((change) => {
                change.textChanges = change.textChanges.map((textChange) => {
                  return __spreadProps(__spreadValues({}, textChange), {
                    span: __spreadProps(__spreadValues({}, textChange.span), {
                      start: toOriginalPos(textChange.span.start).pos
                    })
                  });
                });
                return change;
              });
              return codeAction;
            });
          }
        }
        details = details != null ? details : getCompletionEntryDetails(fileName, position, entryName, formatOptions, source, preferences, data);
        if (details) {
          if (is$typeImport) {
            details.codeActions = (_b = details.codeActions) == null ? void 0 : _b.map((codeAction) => {
              codeAction.description = adjustPath(codeAction.description);
              codeAction.changes = codeAction.changes.map((change) => {
                change.textChanges = change.textChanges.map((textChange) => {
                  textChange.newText = adjustPath(textChange.newText);
                  return textChange;
                });
                return change;
              });
              return codeAction;
            });
            return details;
          } else if ((0, utils_12.isSvelteFilePath)(source || "")) {
            logger.debug("TS found Svelte Component import completion details");
            return (0, utils_12.replaceDeep)(details, componentPostfix, "");
          } else {
            return details;
          }
        }
        if (!(0, utils_12.isSvelteFilePath)(source || "")) {
          return details;
        }
        const svelteDetails = getCompletionEntryDetails(fileName, position, entryName + componentPostfix, formatOptions, source, preferences, data);
        if (!svelteDetails) {
          return void 0;
        }
        logger.debug("Found Svelte Component import completion details");
        return (0, utils_12.replaceDeep)(svelteDetails, componentPostfix, "");
      };
      const getSignatureHelpItems = ls.getSignatureHelpItems;
      ls.getSignatureHelpItems = (fileName, position, options) => {
        const result = (0, sveltekit_1.getVirtualLS)(fileName, info, ts);
        if (result) {
          const { languageService, toVirtualPos } = result;
          return languageService.getSignatureHelpItems(fileName, toVirtualPos(position), options);
        }
        return getSignatureHelpItems(fileName, position, options);
      };
    }
    function adjustPath(path) {
      return path.replace(
        /(['"])(.+?)['"]/,
        // .js logic for node16 module resolution
        (_match, quote, path2) => `${quote}./$types${path2.endsWith(".js") ? ".js" : ""}${quote}`
      );
    }
  }
});

// node_modules/typescript-svelte-plugin/dist/src/language-service/definition.js
var require_definition = __commonJS({
  "node_modules/typescript-svelte-plugin/dist/src/language-service/definition.js"(exports2) {
    "use strict";
    Object.defineProperty(exports2, "__esModule", { value: true });
    exports2.decorateGetDefinition = decorateGetDefinition;
    var utils_12 = require_utils();
    var sveltekit_1 = require_sveltekit();
    function decorateGetDefinition(ls, info, ts, snapshotManager, logger) {
      const getDefinitionAndBoundSpan = ls.getDefinitionAndBoundSpan;
      ls.getDefinitionAndBoundSpan = (fileName, position) => {
        const definition = getDefinitionAndBoundSpan(fileName, position);
        if (!(definition == null ? void 0 : definition.definitions)) {
          return getKitDefinitions(ts, info, fileName, position);
        }
        return __spreadProps(__spreadValues({}, definition), {
          definitions: definition.definitions.map((def) => {
            var _a;
            if (!(0, utils_12.isSvelteFilePath)(def.fileName)) {
              return def;
            }
            let textSpan = (_a = snapshotManager.get(def.fileName)) == null ? void 0 : _a.getOriginalTextSpan(def.textSpan);
            if (!textSpan) {
              textSpan = { start: 0, length: 1 };
            }
            return __spreadProps(__spreadValues({}, def), {
              textSpan,
              // Spare the work for now
              originalTextSpan: void 0,
              contextSpan: void 0,
              originalContextSpan: void 0
            });
          }).filter(utils_12.isNotNullOrUndefined)
        });
      };
    }
    function getKitDefinitions(ts, info, fileName, position) {
      const result = (0, sveltekit_1.getVirtualLS)(fileName, info, ts);
      if (!result)
        return;
      const { languageService, toOriginalPos, toVirtualPos } = result;
      const virtualPos = toVirtualPos(position);
      const definitions = languageService.getDefinitionAndBoundSpan(fileName, virtualPos);
      if (!definitions)
        return;
      return __spreadProps(__spreadValues({}, definitions), {
        textSpan: __spreadProps(__spreadValues({}, definitions.textSpan), { start: toOriginalPos(definitions.textSpan.start).pos })
      });
    }
  }
});

// node_modules/typescript-svelte-plugin/dist/src/language-service/diagnostics.js
var require_diagnostics = __commonJS({
  "node_modules/typescript-svelte-plugin/dist/src/language-service/diagnostics.js"(exports2) {
    "use strict";
    var __importDefault = exports2 && exports2.__importDefault || function(mod) {
      return mod && mod.__esModule ? mod : { "default": mod };
    };
    Object.defineProperty(exports2, "__esModule", { value: true });
    exports2.decorateDiagnostics = decorateDiagnostics;
    var path_12 = __importDefault(require("path"));
    var svelte2tsx_1 = require("svelte2tsx");
    var utils_12 = require_utils();
    var sveltekit_1 = require_sveltekit();
    function decorateDiagnostics(ls, info, typescript, logger) {
      decorateSyntacticDiagnostics(ls, info, typescript, logger);
      decorateSemanticDiagnostics(ls, info, typescript, logger);
      decorateSuggestionDiagnostics(ls, info, typescript, logger);
    }
    function decorateSyntacticDiagnostics(ls, info, typescript, logger) {
      const getSyntacticDiagnostics = ls.getSyntacticDiagnostics;
      ls.getSyntacticDiagnostics = (fileName) => {
        if ((0, utils_12.isSvelteFilePath)(fileName)) {
          return [];
        }
        const kitDiagnostics = getKitDiagnostics("getSyntacticDiagnostics", fileName, info, typescript, logger);
        return kitDiagnostics != null ? kitDiagnostics : getSyntacticDiagnostics(fileName);
      };
    }
    function decorateSemanticDiagnostics(ls, info, typescript, logger) {
      const getSemanticDiagnostics = ls.getSemanticDiagnostics;
      ls.getSemanticDiagnostics = (fileName) => {
        if ((0, utils_12.isSvelteFilePath)(fileName)) {
          return [];
        }
        const kitDiagnostics = getKitDiagnostics("getSemanticDiagnostics", fileName, info, typescript, logger);
        return kitDiagnostics != null ? kitDiagnostics : getSemanticDiagnostics(fileName);
      };
    }
    function decorateSuggestionDiagnostics(ls, info, typescript, logger) {
      const getSuggestionDiagnostics = ls.getSuggestionDiagnostics;
      ls.getSuggestionDiagnostics = (fileName) => {
        if ((0, utils_12.isSvelteFilePath)(fileName)) {
          return [];
        }
        const kitDiagnostics = getKitDiagnostics("getSuggestionDiagnostics", fileName, info, typescript, logger);
        return kitDiagnostics != null ? kitDiagnostics : getSuggestionDiagnostics(fileName);
      };
    }
    function getKitDiagnostics(methodName, fileName, info, ts, logger) {
      var _a, _b;
      const result = (0, sveltekit_1.getVirtualLS)(fileName, info, ts, logger);
      if (!result)
        return;
      const { languageService, toOriginalPos } = result;
      const diagnostics = [];
      for (let diagnostic of languageService[methodName](fileName)) {
        if (!diagnostic.start || !diagnostic.length) {
          diagnostics.push(diagnostic);
          continue;
        }
        const mapped = toOriginalPos(diagnostic.start);
        if (mapped.inGenerated) {
          if (diagnostic.code === 2307) {
            diagnostic = __spreadProps(__spreadValues({}, diagnostic), {
              // adjust length so it doesn't spill over to the next line
              length: 1,
              messageText: typeof diagnostic.messageText === "string" && diagnostic.messageText.includes("./$types") ? diagnostic.messageText + ` (this likely means that SvelteKit's type generation didn't run yet - try running it by executing 'npm run dev' or 'npm run build')` : diagnostic.messageText
            });
          } else if (diagnostic.code === 2694) {
            diagnostic = __spreadProps(__spreadValues({}, diagnostic), {
              // adjust length so it doesn't spill over to the next line
              length: 1,
              messageText: typeof diagnostic.messageText === "string" && diagnostic.messageText.includes("/$types") ? diagnostic.messageText + ` (this likely means that SvelteKit's generated types are out of date - try rerunning it by executing 'npm run dev' or 'npm run build')` : diagnostic.messageText
            });
          } else if (diagnostic.code === 2355) {
            diagnostic = __spreadProps(__spreadValues({}, diagnostic), {
              // adjust length so it doesn't spill over to the next line
              length: 1
            });
          } else {
            continue;
          }
        }
        diagnostic = __spreadProps(__spreadValues({}, diagnostic), {
          start: mapped.pos
        });
        diagnostics.push(diagnostic);
      }
      if (methodName === "getSemanticDiagnostics") {
        const source = (_a = info.languageService.getProgram()) == null ? void 0 : _a.getSourceFile(fileName);
        const basename = path_12.default.basename(fileName);
        const validExports = Object.keys(sveltekit_1.kitExports).filter((key) => (0, sveltekit_1.isKitRouteExportAllowedIn)(basename, sveltekit_1.kitExports[key]));
        if (source && basename.startsWith("+")) {
          const exports3 = svelte2tsx_1.internalHelpers.findExports(
            ts,
            source,
            /* irrelevant */
            false
          );
          for (const exportName of exports3.keys()) {
            if (!validExports.includes(exportName) && !exportName.startsWith("_")) {
              const node = exports3.get(exportName).node;
              const identifier = (_b = (0, utils_12.findIdentifier)(ts, node)) != null ? _b : node;
              diagnostics.push({
                file: source,
                start: identifier.getStart(),
                length: identifier.getEnd() - identifier.getStart(),
                messageText: `Invalid export '${exportName}' (valid exports are ${validExports.join(", ")}, or anything with a '_' prefix)`,
                // make it a warning in case people are stuck on old versions and new exports are added to SvelteKit
                category: ts.DiagnosticCategory.Warning,
                code: 71001
                // arbitrary
              });
            }
          }
        }
      }
      return diagnostics;
    }
  }
});

// node_modules/typescript-svelte-plugin/dist/src/language-service/find-references.js
var require_find_references = __commonJS({
  "node_modules/typescript-svelte-plugin/dist/src/language-service/find-references.js"(exports2) {
    "use strict";
    Object.defineProperty(exports2, "__esModule", { value: true });
    exports2.decorateFindReferences = decorateFindReferences;
    var utils_12 = require_utils();
    function decorateFindReferences(ls, snapshotManager, logger) {
      decorateGetReferencesAtPosition(ls, snapshotManager, logger);
      _decorateFindReferences(ls, snapshotManager, logger);
    }
    function _decorateFindReferences(ls, snapshotManager, logger) {
      const findReferences = ls.findReferences;
      const getReferences = (fileName, position) => {
        var _a;
        return (_a = findReferences(fileName, position)) == null ? void 0 : _a.reduce((acc, curr) => acc.concat(curr.references), []);
      };
      ls.findReferences = (fileName, position) => {
        const references = findReferences(fileName, position);
        return references == null ? void 0 : references.map((reference) => {
          const snapshot = snapshotManager.get(reference.definition.fileName);
          if (!(0, utils_12.isSvelteFilePath)(reference.definition.fileName) || !snapshot) {
            return __spreadProps(__spreadValues({}, reference), {
              references: mapReferences(reference.references, snapshotManager, logger, getReferences)
            });
          }
          const textSpan = snapshot.getOriginalTextSpan(reference.definition.textSpan);
          if (!textSpan) {
            return null;
          }
          return {
            definition: __spreadProps(__spreadValues({}, reference.definition), {
              textSpan,
              // Spare the work for now
              originalTextSpan: void 0
            }),
            references: mapReferences(reference.references, snapshotManager, logger, getReferences)
          };
        }).filter(utils_12.isNotNullOrUndefined);
      };
    }
    function decorateGetReferencesAtPosition(ls, snapshotManager, logger) {
      const getReferencesAtPosition = ls.getReferencesAtPosition;
      ls.getReferencesAtPosition = (fileName, position) => {
        const references = getReferencesAtPosition(fileName, position);
        return references && mapReferences(references, snapshotManager, logger, getReferencesAtPosition);
      };
    }
    function mapReferences(references, snapshotManager, logger, getReferences) {
      const additionalStoreReferences = [];
      const mappedReferences = [];
      for (const reference of references) {
        const snapshot = snapshotManager.get(reference.fileName);
        if (!(0, utils_12.isSvelteFilePath)(reference.fileName) || !snapshot) {
          mappedReferences.push(reference);
          continue;
        }
        const textSpan = snapshot.getOriginalTextSpan(reference.textSpan);
        if (textSpan) {
          mappedReferences.push(mapReference(reference, textSpan));
        } else {
          if ((0, utils_12.isStoreVariableIn$storeDeclaration)(snapshot.getText(), reference.textSpan.start)) {
            additionalStoreReferences.push(...getReferences(reference.fileName, (0, utils_12.get$storeOffsetOf$storeDeclaration)(snapshot.getText(), reference.textSpan.start)) || []);
          }
        }
      }
      for (const reference of additionalStoreReferences) {
        const snapshot = snapshotManager.get(reference.fileName);
        const textSpan = snapshot.getOriginalTextSpan(reference.textSpan);
        if (!textSpan) {
          continue;
        }
        mappedReferences.push(mapReference(reference, textSpan));
      }
      return mappedReferences;
      function mapReference(reference, textSpan) {
        logger.debug("Find references; map textSpan: changed", reference.textSpan, "to", textSpan);
        return __spreadProps(__spreadValues({}, reference), {
          textSpan,
          // Spare the work for now
          contextSpan: void 0,
          originalTextSpan: void 0,
          originalContextSpan: void 0
        });
      }
    }
  }
});

// node_modules/typescript-svelte-plugin/dist/src/language-service/hover.js
var require_hover = __commonJS({
  "node_modules/typescript-svelte-plugin/dist/src/language-service/hover.js"(exports2) {
    "use strict";
    Object.defineProperty(exports2, "__esModule", { value: true });
    exports2.decorateHover = decorateHover;
    var utils_12 = require_utils();
    var sveltekit_1 = require_sveltekit();
    function decorateHover(ls, info, ts, logger) {
      const getQuickInfoAtPosition = ls.getQuickInfoAtPosition;
      ls.getQuickInfoAtPosition = (fileName, position) => {
        var _a, _b;
        const result = (0, sveltekit_1.getVirtualLS)(fileName, info, ts);
        if (!result)
          return getQuickInfoAtPosition(fileName, position);
        const { languageService, toOriginalPos, toVirtualPos } = result;
        const virtualPos = toVirtualPos(position);
        const quickInfo = languageService.getQuickInfoAtPosition(fileName, virtualPos);
        if (!quickInfo)
          return quickInfo;
        const source = (_a = languageService.getProgram()) == null ? void 0 : _a.getSourceFile(fileName);
        const node = source && (0, utils_12.findNodeAtPosition)(source, virtualPos);
        if (node && (0, utils_12.isTopLevelExport)(ts, node, source) && ts.isIdentifier(node)) {
          const name = node.text;
          if (name in sveltekit_1.kitExports && !((_b = quickInfo.documentation) == null ? void 0 : _b.length)) {
            quickInfo.documentation = sveltekit_1.kitExports[name].documentation;
          }
        }
        return __spreadProps(__spreadValues({}, quickInfo), {
          textSpan: __spreadProps(__spreadValues({}, quickInfo.textSpan), { start: toOriginalPos(quickInfo.textSpan.start).pos })
        });
      };
    }
  }
});

// node_modules/typescript-svelte-plugin/dist/src/language-service/implementation.js
var require_implementation = __commonJS({
  "node_modules/typescript-svelte-plugin/dist/src/language-service/implementation.js"(exports2) {
    "use strict";
    Object.defineProperty(exports2, "__esModule", { value: true });
    exports2.decorateGetImplementation = decorateGetImplementation;
    var utils_12 = require_utils();
    function decorateGetImplementation(ls, snapshotManager, logger) {
      const getImplementationAtPosition = ls.getImplementationAtPosition;
      ls.getImplementationAtPosition = (fileName, position) => {
        const implementation = getImplementationAtPosition(fileName, position);
        return implementation == null ? void 0 : implementation.map((impl) => {
          var _a;
          if (!(0, utils_12.isSvelteFilePath)(impl.fileName)) {
            return impl;
          }
          const textSpan = (_a = snapshotManager.get(impl.fileName)) == null ? void 0 : _a.getOriginalTextSpan(impl.textSpan);
          if (!textSpan) {
            return void 0;
          }
          return __spreadProps(__spreadValues({}, impl), {
            textSpan,
            // Spare the work for now
            contextSpan: void 0,
            originalTextSpan: void 0,
            originalContextSpan: void 0
          });
        }).filter(utils_12.isNotNullOrUndefined);
      };
    }
  }
});

// node_modules/typescript-svelte-plugin/dist/src/language-service/inlay-hints.js
var require_inlay_hints = __commonJS({
  "node_modules/typescript-svelte-plugin/dist/src/language-service/inlay-hints.js"(exports2) {
    "use strict";
    Object.defineProperty(exports2, "__esModule", { value: true });
    exports2.decorateInlayHints = decorateInlayHints;
    var sveltekit_1 = require_sveltekit();
    function decorateInlayHints(ls, info, ts, logger) {
      const provideInlayHints = ls.provideInlayHints;
      ls.provideInlayHints = (fileName, span, preferences) => {
        const result = (0, sveltekit_1.getVirtualLS)(fileName, info, ts);
        if (!result) {
          return provideInlayHints(fileName, span, preferences);
        }
        const { languageService, toVirtualPos, toOriginalPos } = result;
        const start = toVirtualPos(span.start);
        return languageService.provideInlayHints(fileName, {
          start,
          length: toVirtualPos(span.start + span.length) - start
        }, preferences).map((hint) => __spreadProps(__spreadValues({}, hint), {
          position: toOriginalPos(hint.position).pos
        }));
      };
    }
  }
});

// node_modules/typescript-svelte-plugin/dist/src/language-service/rename.js
var require_rename = __commonJS({
  "node_modules/typescript-svelte-plugin/dist/src/language-service/rename.js"(exports2) {
    "use strict";
    Object.defineProperty(exports2, "__esModule", { value: true });
    exports2.decorateRename = decorateRename;
    var utils_12 = require_utils();
    function decorateRename(ls, snapshotManager, logger) {
      const findRenameLocations = ls.findRenameLocations;
      ls.findRenameLocations = (fileName, position, findInStrings, findInComments, providePrefixAndSuffixTextForRename) => {
        const renameLocations = findRenameLocations(
          fileName,
          position,
          findInStrings,
          findInComments,
          // @ts-expect-error overload shenanigans
          providePrefixAndSuffixTextForRename
        );
        if (!renameLocations) {
          return void 0;
        }
        const convertedRenameLocations = [];
        const additionalStoreRenameLocations = [];
        for (const renameLocation of renameLocations) {
          const snapshot = snapshotManager.get(renameLocation.fileName);
          if (!(0, utils_12.isSvelteFilePath)(renameLocation.fileName) || !snapshot) {
            convertedRenameLocations.push(renameLocation);
            continue;
          }
          const textSpan = snapshot.getOriginalTextSpan(renameLocation.textSpan);
          if (!textSpan) {
            if ((0, utils_12.isStoreVariableIn$storeDeclaration)(snapshot.getText(), renameLocation.textSpan.start)) {
              additionalStoreRenameLocations.push(...findRenameLocations(renameLocation.fileName, (0, utils_12.get$storeOffsetOf$storeDeclaration)(snapshot.getText(), renameLocation.textSpan.start), false, false, false));
            }
            continue;
          }
          convertedRenameLocations.push(convert(renameLocation, textSpan));
        }
        for (const renameLocation of additionalStoreRenameLocations) {
          const snapshot = snapshotManager.get(renameLocation.fileName);
          const textSpan = snapshot.getOriginalTextSpan(renameLocation.textSpan);
          if (!textSpan) {
            continue;
          }
          textSpan.start += 1;
          textSpan.length -= 1;
          convertedRenameLocations.push(convert(renameLocation, textSpan));
        }
        return convertedRenameLocations;
      };
      function convert(renameLocation, textSpan) {
        const converted = __spreadProps(__spreadValues({}, renameLocation), {
          textSpan
        });
        if (converted.contextSpan) {
          converted.contextSpan = void 0;
        }
        logger.debug("Converted rename location ", converted);
        return converted;
      }
    }
  }
});

// node_modules/typescript-svelte-plugin/dist/src/language-service/update-imports.js
var require_update_imports = __commonJS({
  "node_modules/typescript-svelte-plugin/dist/src/language-service/update-imports.js"(exports2) {
    "use strict";
    var __importDefault = exports2 && exports2.__importDefault || function(mod) {
      return mod && mod.__esModule ? mod : { "default": mod };
    };
    Object.defineProperty(exports2, "__esModule", { value: true });
    exports2.decorateUpdateImports = decorateUpdateImports;
    var path_12 = __importDefault(require("path"));
    var utils_12 = require_utils();
    function decorateUpdateImports(ls, snapshotManager, logger) {
      const getEditsForFileRename = ls.getEditsForFileRename;
      ls.getEditsForFileRename = (oldFilePath, newFilePath, formatOptions, preferences) => {
        const renameLocations = getEditsForFileRename(oldFilePath, newFilePath, formatOptions, preferences);
        return renameLocations == null ? void 0 : renameLocations.filter((renameLocation) => {
          return !(0, utils_12.isSvelteFilePath)(renameLocation.fileName) && !renameLocation.textChanges.some((change) => change.newText.endsWith(".svelte"));
        }).map((renameLocation) => {
          if (path_12.default.basename(renameLocation.fileName).startsWith("+")) {
            renameLocation.textChanges = renameLocation.textChanges.filter((change) => {
              return !change.newText.includes(".svelte-kit/types/");
            });
          }
          return renameLocation;
        });
      };
    }
  }
});

// node_modules/typescript-svelte-plugin/dist/src/language-service/host.js
var require_host = __commonJS({
  "node_modules/typescript-svelte-plugin/dist/src/language-service/host.js"(exports2) {
    "use strict";
    Object.defineProperty(exports2, "__esModule", { value: true });
    exports2.decorateLanguageServiceHost = decorateLanguageServiceHost;
    function decorateLanguageServiceHost(host) {
      var _a;
      const originalReadDirectory = (_a = host.readDirectory) == null ? void 0 : _a.bind(host);
      host.readDirectory = originalReadDirectory ? (path, extensions, exclude, include, depth) => {
        const extensionsWithSvelte = extensions ? [...extensions, ".svelte"] : void 0;
        return originalReadDirectory(path, extensionsWithSvelte, exclude, include, depth);
      } : void 0;
    }
  }
});

// node_modules/typescript-svelte-plugin/dist/src/language-service/navigate-to-items.js
var require_navigate_to_items = __commonJS({
  "node_modules/typescript-svelte-plugin/dist/src/language-service/navigate-to-items.js"(exports2) {
    "use strict";
    Object.defineProperty(exports2, "__esModule", { value: true });
    exports2.decorateNavigateToItems = decorateNavigateToItems;
    var utils_12 = require_utils();
    function decorateNavigateToItems(ls, snapshotManager) {
      const getNavigateToItems = ls.getNavigateToItems;
      ls.getNavigateToItems = (searchValue, maxResultCount, fileName, excludeDtsFiles) => {
        const navigationToItems = getNavigateToItems(searchValue, maxResultCount, fileName, excludeDtsFiles);
        return navigationToItems.map((item) => {
          var _a;
          if (!(0, utils_12.isSvelteFilePath)(item.fileName)) {
            return item;
          }
          if (item.name.startsWith("__sveltets_") || item.name === "render" && !item.containerName) {
            return;
          }
          let textSpan = (_a = snapshotManager.get(item.fileName)) == null ? void 0 : _a.getOriginalTextSpan(item.textSpan);
          if (!textSpan) {
            if ((0, utils_12.isGeneratedSvelteComponentName)(item.name)) {
              textSpan = { start: 0, length: 1 };
            } else {
              return;
            }
          }
          return __spreadProps(__spreadValues({}, item), {
            textSpan
          });
        }).filter(utils_12.isNotNullOrUndefined);
      };
    }
  }
});

// node_modules/typescript-svelte-plugin/dist/src/language-service/file-references.js
var require_file_references = __commonJS({
  "node_modules/typescript-svelte-plugin/dist/src/language-service/file-references.js"(exports2) {
    "use strict";
    Object.defineProperty(exports2, "__esModule", { value: true });
    exports2.decorateFileReferences = decorateFileReferences;
    var utils_12 = require_utils();
    function decorateFileReferences(ls, snapshotManager) {
      const getFileReferences = ls.getFileReferences;
      ls.getFileReferences = (fileName) => {
        const references = getFileReferences(fileName);
        return references.map((ref) => {
          var _a;
          if (!(0, utils_12.isSvelteFilePath)(ref.fileName)) {
            return ref;
          }
          let textSpan = (_a = snapshotManager.get(ref.fileName)) == null ? void 0 : _a.getOriginalTextSpan(ref.textSpan);
          if (!textSpan) {
            return;
          }
          return __spreadProps(__spreadValues({}, ref), {
            textSpan
          });
        }).filter(utils_12.isNotNullOrUndefined);
      };
    }
  }
});

// node_modules/typescript-svelte-plugin/dist/src/language-service/move-to-file.js
var require_move_to_file = __commonJS({
  "node_modules/typescript-svelte-plugin/dist/src/language-service/move-to-file.js"(exports2) {
    "use strict";
    Object.defineProperty(exports2, "__esModule", { value: true });
    exports2.decorateMoveToRefactoringFileSuggestions = decorateMoveToRefactoringFileSuggestions;
    function decorateMoveToRefactoringFileSuggestions(ls) {
      const getMoveToRefactoringFileSuggestions = ls.getMoveToRefactoringFileSuggestions;
      ls.getMoveToRefactoringFileSuggestions = (fileName, positionOrRange, preferences, triggerReason, kind) => {
        const program = ls.getProgram();
        if (!program) {
          return getMoveToRefactoringFileSuggestions(fileName, positionOrRange, preferences, triggerReason, kind);
        }
        const getSourceFiles = program.getSourceFiles;
        try {
          program.getSourceFiles = () => getSourceFiles().filter((file) => !file.fileName.endsWith(".svelte"));
          return getMoveToRefactoringFileSuggestions(fileName, positionOrRange, preferences, triggerReason, kind);
        } finally {
          program.getSourceFiles = getSourceFiles;
        }
      };
    }
  }
});

// node_modules/typescript-svelte-plugin/dist/src/language-service/code-action.js
var require_code_action = __commonJS({
  "node_modules/typescript-svelte-plugin/dist/src/language-service/code-action.js"(exports2) {
    "use strict";
    Object.defineProperty(exports2, "__esModule", { value: true });
    exports2.decorateQuickFixAndRefactor = decorateQuickFixAndRefactor;
    var utils_12 = require_utils();
    function decorateQuickFixAndRefactor(ls, ts, snapshotManager) {
      const getEditsForRefactor = ls.getEditsForRefactor;
      const getCodeFixesAtPosition = ls.getCodeFixesAtPosition;
      ls.getEditsForRefactor = (...args) => {
        const result = getEditsForRefactor(...args);
        if (!result) {
          return;
        }
        const edits = result.edits.map(mapFileTextChanges).filter(utils_12.isNotNullOrUndefined);
        if (edits.length === 0) {
          return;
        }
        return __spreadProps(__spreadValues({}, result), {
          edits
        });
      };
      ls.getCodeFixesAtPosition = (...args) => {
        const result = getCodeFixesAtPosition(...args);
        return result.map((fix) => {
          return __spreadProps(__spreadValues({}, fix), {
            changes: fix.changes.map(mapFileTextChanges).filter(utils_12.isNotNullOrUndefined)
          });
        }).filter((fix) => fix.changes.length > 0);
      };
      function mapFileTextChanges(change) {
        const snapshot = snapshotManager.get(change.fileName);
        if (!(0, utils_12.isSvelteFilePath)(change.fileName) || !snapshot) {
          return change;
        }
        let baseIndent;
        const getBaseIndent = () => {
          if (baseIndent !== void 0) {
            return baseIndent;
          }
          baseIndent = getIndentOfFirstStatement(ts, ls, change.fileName, snapshot);
          return baseIndent;
        };
        const textChanges = change.textChanges.map((textChange) => mapEdit(textChange, snapshot, getBaseIndent)).filter(utils_12.isNotNullOrUndefined);
        if (textChanges.length === 0 || textChanges.length !== change.textChanges.length) {
          return null;
        }
        return __spreadProps(__spreadValues({}, change), {
          textChanges
        });
      }
    }
    function mapEdit(change, snapshot, getBaseIndent) {
      const isNewImportStatement = change.newText.trimStart().startsWith("import");
      if (isNewImportStatement) {
        return mapNewImport(change, snapshot, getBaseIndent);
      }
      const span = snapshot.getOriginalTextSpan(change.span);
      if (!span) {
        return null;
      }
      return {
        span,
        newText: change.newText
      };
    }
    function mapNewImport(change, snapshot, getBaseIndent) {
      const previousLineEnds = getPreviousLineEnds(snapshot.getText(), change.span.start);
      if (previousLineEnds === -1) {
        return null;
      }
      const mappable = snapshot.getOriginalTextSpan({
        start: previousLineEnds,
        length: 0
      });
      if (!mappable) {
        return null;
      }
      const originalText = snapshot.getOriginalText();
      const span = {
        start: originalText.indexOf("\n", mappable.start) + 1,
        length: change.span.length
      };
      const baseIndent = getBaseIndent();
      let newText = baseIndent ? change.newText.split("\n").map((line) => line ? baseIndent + line : line).join("\n") : change.newText;
      return { span, newText };
    }
    function getPreviousLineEnds(text, start) {
      const index = text.lastIndexOf("\n", start);
      if (index === -1) {
        return index;
      }
      if (text[index - 1] === "\r") {
        return index - 1;
      }
      return index;
    }
    function getIndentOfFirstStatement(ts, ls, fileName, snapshot) {
      var _a, _b;
      const firstExportOrImport = (_b = (_a = ls.getProgram()) == null ? void 0 : _a.getSourceFile(fileName)) == null ? void 0 : _b.statements.find((node) => ts.isExportDeclaration(node) || ts.isImportDeclaration(node));
      const originalPosition = firstExportOrImport ? snapshot.getOriginalOffset(firstExportOrImport.getStart()) : -1;
      if (originalPosition === -1) {
        return "";
      }
      const source = snapshot.getOriginalText();
      const start = source.lastIndexOf("\n", originalPosition) + 1;
      let index = start;
      while (index < originalPosition) {
        const char = source[index];
        if (char.trim()) {
          break;
        }
        index++;
      }
      return source.substring(start, index);
    }
  }
});

// node_modules/typescript-svelte-plugin/dist/src/language-service/index.js
var require_language_service = __commonJS({
  "node_modules/typescript-svelte-plugin/dist/src/language-service/index.js"(exports2) {
    "use strict";
    Object.defineProperty(exports2, "__esModule", { value: true });
    exports2.isPatched = isPatched;
    exports2.decorateLanguageService = decorateLanguageService;
    var utils_12 = require_utils();
    var call_hierarchy_1 = require_call_hierarchy();
    var completions_1 = require_completions();
    var definition_1 = require_definition();
    var diagnostics_1 = require_diagnostics();
    var find_references_1 = require_find_references();
    var hover_1 = require_hover();
    var implementation_1 = require_implementation();
    var inlay_hints_1 = require_inlay_hints();
    var rename_1 = require_rename();
    var update_imports_1 = require_update_imports();
    var host_1 = require_host();
    var navigate_to_items_1 = require_navigate_to_items();
    var file_references_1 = require_file_references();
    var move_to_file_1 = require_move_to_file();
    var code_action_1 = require_code_action();
    var patchedProject = /* @__PURE__ */ new Set();
    function isPatched(project) {
      return patchedProject.has(project.getProjectName());
    }
    function decorateLanguageService(ls, snapshotManager, logger, configManager, info, typescript, onDispose) {
      patchedProject.add(info.project.getProjectName());
      const proxy = new Proxy(ls, createProxyHandler(configManager));
      (0, host_1.decorateLanguageServiceHost)(info.languageServiceHost);
      decorateLanguageServiceInner(proxy, snapshotManager, logger, info, typescript, onDispose);
      return proxy;
    }
    function decorateLanguageServiceInner(ls, snapshotManager, logger, info, typescript, onDispose) {
      patchLineColumnOffset(ls, snapshotManager);
      (0, rename_1.decorateRename)(ls, snapshotManager, logger);
      (0, diagnostics_1.decorateDiagnostics)(ls, info, typescript, logger);
      (0, find_references_1.decorateFindReferences)(ls, snapshotManager, logger);
      (0, completions_1.decorateCompletions)(ls, info, typescript, logger);
      (0, definition_1.decorateGetDefinition)(ls, info, typescript, snapshotManager, logger);
      (0, implementation_1.decorateGetImplementation)(ls, snapshotManager, logger);
      (0, update_imports_1.decorateUpdateImports)(ls, snapshotManager, logger);
      (0, call_hierarchy_1.decorateCallHierarchy)(ls, snapshotManager, typescript);
      (0, hover_1.decorateHover)(ls, info, typescript, logger);
      (0, inlay_hints_1.decorateInlayHints)(ls, info, typescript, logger);
      (0, navigate_to_items_1.decorateNavigateToItems)(ls, snapshotManager);
      (0, file_references_1.decorateFileReferences)(ls, snapshotManager);
      (0, move_to_file_1.decorateMoveToRefactoringFileSuggestions)(ls);
      (0, code_action_1.decorateQuickFixAndRefactor)(ls, typescript, snapshotManager);
      decorateDispose(ls, info.project, onDispose);
      return ls;
    }
    function createProxyHandler(configManager) {
      const decorated = {};
      return {
        get(target, p) {
          var _a;
          if (!configManager.getConfig().enable && p !== "dispose") {
            return target[p];
          }
          return (_a = decorated[p]) != null ? _a : target[p];
        },
        set(_, p, value) {
          decorated[p] = value;
          return true;
        }
      };
    }
    function patchLineColumnOffset(ls, snapshotManager) {
      if (!ls.toLineColumnOffset) {
        return;
      }
      const toLineColumnOffset = ls.toLineColumnOffset;
      ls.toLineColumnOffset = (fileName, position) => {
        if ((0, utils_12.isSvelteFilePath)(fileName)) {
          const snapshot = snapshotManager.get(fileName);
          if (snapshot) {
            return snapshot.positionAt(position);
          }
        }
        return toLineColumnOffset(fileName, position);
      };
    }
    function decorateDispose(ls, project, onDispose) {
      const dispose = ls.dispose;
      ls.dispose = () => {
        patchedProject.delete(project.getProjectName());
        onDispose();
        dispose();
      };
      return ls;
    }
  }
});

// node_modules/typescript-svelte-plugin/dist/src/logger.js
var require_logger = __commonJS({
  "node_modules/typescript-svelte-plugin/dist/src/logger.js"(exports2) {
    "use strict";
    Object.defineProperty(exports2, "__esModule", { value: true });
    exports2.Logger = void 0;
    var Logger = class {
      constructor(tsLogService, suppressNonSvelteLogs = false, logDebug = false) {
        this.tsLogService = tsLogService;
        this.logDebug = logDebug;
        if (suppressNonSvelteLogs) {
          const log = this.tsLogService.info.bind(this.tsLogService);
          this.tsLogService.info = (s) => {
            if (s.startsWith("-Svelte Plugin-")) {
              log(s);
            }
          };
        }
      }
      log(...args) {
        const str = args.map((arg) => {
          if (typeof arg === "object") {
            try {
              return JSON.stringify(arg);
            } catch (e) {
              return "[object that cannot by stringified]";
            }
          }
          return arg;
        }).join(" ");
        this.tsLogService.info("-Svelte Plugin- " + str);
      }
      debug(...args) {
        if (!this.logDebug) {
          return;
        }
        this.log(...args);
      }
    };
    exports2.Logger = Logger;
  }
});

// node_modules/typescript-svelte-plugin/dist/src/svelte-sys.js
var require_svelte_sys = __commonJS({
  "node_modules/typescript-svelte-plugin/dist/src/svelte-sys.js"(exports2) {
    "use strict";
    Object.defineProperty(exports2, "__esModule", { value: true });
    exports2.createSvelteSys = createSvelteSys;
    var utils_12 = require_utils();
    function createSvelteSys(ts, logger) {
      const svelteSys = __spreadProps(__spreadValues({}, ts.sys), {
        fileExists(path) {
          return ts.sys.fileExists((0, utils_12.ensureRealSvelteFilePath)(path));
        },
        readDirectory(path, extensions, exclude, include, depth) {
          const extensionsWithSvelte = (extensions != null ? extensions : []).concat(".svelte");
          return ts.sys.readDirectory(path, extensionsWithSvelte, exclude, include, depth);
        },
        readFile(path, encoding) {
          return ts.sys.readFile(path, encoding);
        }
      });
      if (ts.sys.realpath) {
        const realpath = ts.sys.realpath;
        svelteSys.realpath = function(path) {
          if ((0, utils_12.isVirtualSvelteFilePath)(path)) {
            return realpath((0, utils_12.toRealSvelteFilePath)(path));
          }
          return realpath(path);
        };
      }
      return svelteSys;
    }
  }
});

// node_modules/typescript-svelte-plugin/dist/src/module-loader.js
var require_module_loader = __commonJS({
  "node_modules/typescript-svelte-plugin/dist/src/module-loader.js"(exports2) {
    "use strict";
    Object.defineProperty(exports2, "__esModule", { value: true });
    exports2.patchModuleLoader = patchModuleLoader;
    var svelte_sys_1 = require_svelte_sys();
    var utils_12 = require_utils();
    var ModuleResolutionCache = class {
      constructor(projectService) {
        this.projectService = projectService;
        this.cache = /* @__PURE__ */ new Map();
      }
      /**
       * Tries to get a cached module.
       */
      get(moduleName, containingFile) {
        return this.cache.get(this.getKey(moduleName, containingFile));
      }
      /**
       * Caches resolved module, if it is not undefined.
       */
      set(moduleName, containingFile, resolvedModule) {
        if (!resolvedModule) {
          return;
        }
        this.cache.set(this.getKey(moduleName, containingFile), resolvedModule);
      }
      /**
       * Deletes module from cache. Call this if a file was deleted.
       * @param resolvedModuleName full path of the module
       */
      delete(resolvedModuleName) {
        resolvedModuleName = this.projectService.toCanonicalFileName(resolvedModuleName);
        this.cache.forEach((val, key) => {
          if (this.projectService.toCanonicalFileName(val.resolvedFileName) === resolvedModuleName) {
            this.cache.delete(key);
          }
        });
      }
      clear() {
        this.cache.clear();
      }
      getKey(moduleName, containingFile) {
        return this.projectService.toCanonicalFileName(containingFile) + ":::" + this.projectService.toCanonicalFileName((0, utils_12.ensureRealSvelteFilePath)(moduleName));
      }
    };
    function patchModuleLoader(logger, snapshotManager, typescript, lsHost, project, configManager) {
      var _a, _b;
      const svelteSys = (0, svelte_sys_1.createSvelteSys)(typescript, logger);
      const moduleCache = new ModuleResolutionCache(project.projectService);
      const origResolveModuleNames = (_a = lsHost.resolveModuleNames) == null ? void 0 : _a.bind(lsHost);
      const origResolveModuleNamLiterals = (_b = lsHost.resolveModuleNameLiterals) == null ? void 0 : _b.bind(lsHost);
      if (lsHost.resolveModuleNameLiterals) {
        lsHost.resolveModuleNameLiterals = resolveModuleNameLiterals;
      } else {
        lsHost.resolveModuleNames = resolveModuleNames;
      }
      const origRemoveFile = project.removeFile.bind(project);
      project.removeFile = (info, fileExists, detachFromProject) => {
        logger.log("File is being removed. Delete from cache: ", info.fileName);
        moduleCache.delete(info.fileName);
        return origRemoveFile(info, fileExists, detachFromProject);
      };
      const onConfigChanged = () => {
        moduleCache.clear();
      };
      configManager.onConfigurationChanged(onConfigChanged);
      return {
        dispose() {
          configManager.removeConfigurationChangeListener(onConfigChanged);
          moduleCache.clear();
        }
      };
      function resolveModuleNames(moduleNames, containingFile, reusedNames, redirectedReference, compilerOptions, containingSourceFile) {
        logger.debug("Resolving modules names for " + containingFile);
        const resolved = (origResolveModuleNames == null ? void 0 : origResolveModuleNames(moduleNames, containingFile, reusedNames, redirectedReference, compilerOptions, containingSourceFile)) || Array.from(Array(moduleNames.length));
        if (!configManager.getConfig().enable) {
          return resolved;
        }
        return resolved.map((tsResolvedModule, idx) => {
          const moduleName = moduleNames[idx];
          if (!(0, utils_12.isSvelteFilePath)(moduleName) || // corresponding .d.ts files take precedence over .svelte files
          (tsResolvedModule == null ? void 0 : tsResolvedModule.resolvedFileName.endsWith(".d.ts")) || (tsResolvedModule == null ? void 0 : tsResolvedModule.resolvedFileName.endsWith(".d.svelte.ts"))) {
            return tsResolvedModule;
          }
          const result = resolveSvelteModuleNameFromCache(moduleName, containingFile, compilerOptions).resolvedModule;
          return result != null ? result : tsResolvedModule;
        });
      }
      function resolveSvelteModuleName(name, containingFile, compilerOptions) {
        const svelteResolvedModule = typescript.resolveModuleName(
          name,
          containingFile,
          compilerOptions,
          svelteSys
          // don't set mode or else .svelte imports couldn't be resolved
        ).resolvedModule;
        if (!svelteResolvedModule || !(0, utils_12.isVirtualSvelteFilePath)(svelteResolvedModule.resolvedFileName)) {
          return svelteResolvedModule;
        }
        const resolvedFileName = (0, utils_12.ensureRealSvelteFilePath)(svelteResolvedModule.resolvedFileName);
        logger.log("Resolved", name, "to Svelte file", resolvedFileName);
        const snapshot = snapshotManager.create(resolvedFileName);
        if (!snapshot) {
          return void 0;
        }
        const resolvedSvelteModule = {
          extension: snapshot.isTsFile ? typescript.Extension.Ts : typescript.Extension.Js,
          resolvedFileName,
          isExternalLibraryImport: svelteResolvedModule.isExternalLibraryImport
        };
        return resolvedSvelteModule;
      }
      function resolveModuleNameLiterals(moduleLiterals, containingFile, redirectedReference, options, containingSourceFile, reusedNames) {
        var _a2;
        logger.debug("Resolving modules names for " + containingFile);
        const resolved = (_a2 = origResolveModuleNamLiterals == null ? void 0 : origResolveModuleNamLiterals(moduleLiterals, containingFile, redirectedReference, options, containingSourceFile, reusedNames)) != null ? _a2 : moduleLiterals.map(() => ({
          resolvedModule: void 0
        }));
        if (!configManager.getConfig().enable) {
          return resolved;
        }
        return resolved.map((tsResolvedModule, idx) => {
          const moduleName = moduleLiterals[idx].text;
          const resolvedModule = tsResolvedModule.resolvedModule;
          if (!(0, utils_12.isSvelteFilePath)(moduleName) || // corresponding .d.ts files take precedence over .svelte files
          (resolvedModule == null ? void 0 : resolvedModule.resolvedFileName.endsWith(".d.ts")) || (resolvedModule == null ? void 0 : resolvedModule.resolvedFileName.endsWith(".d.svelte.ts"))) {
            return tsResolvedModule;
          }
          const result = resolveSvelteModuleNameFromCache(moduleName, containingFile, options);
          return result.resolvedModule ? result : tsResolvedModule;
        });
      }
      function resolveSvelteModuleNameFromCache(moduleName, containingFile, options) {
        const cachedModule = moduleCache.get(moduleName, containingFile);
        if (cachedModule) {
          return {
            resolvedModule: cachedModule
          };
        }
        const resolvedModule = resolveSvelteModuleName(moduleName, containingFile, options);
        moduleCache.set(moduleName, containingFile, resolvedModule);
        return {
          resolvedModule
        };
      }
    }
  }
});

// node_modules/@jridgewell/sourcemap-codec/dist/sourcemap-codec.umd.js
var require_sourcemap_codec_umd = __commonJS({
  "node_modules/@jridgewell/sourcemap-codec/dist/sourcemap-codec.umd.js"(exports2, module2) {
    (function(global, factory) {
      typeof exports2 === "object" && typeof module2 !== "undefined" ? factory(exports2) : typeof define === "function" && define.amd ? define(["exports"], factory) : (global = typeof globalThis !== "undefined" ? globalThis : global || self, factory(global.sourcemapCodec = {}));
    })(exports2, function(exports3) {
      "use strict";
      const comma = ",".charCodeAt(0);
      const semicolon = ";".charCodeAt(0);
      const chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
      const intToChar = new Uint8Array(64);
      const charToInt = new Uint8Array(128);
      for (let i = 0; i < chars.length; i++) {
        const c = chars.charCodeAt(i);
        intToChar[i] = c;
        charToInt[c] = i;
      }
      function decodeInteger(reader, relative) {
        let value = 0;
        let shift = 0;
        let integer = 0;
        do {
          const c = reader.next();
          integer = charToInt[c];
          value |= (integer & 31) << shift;
          shift += 5;
        } while (integer & 32);
        const shouldNegate = value & 1;
        value >>>= 1;
        if (shouldNegate) {
          value = -2147483648 | -value;
        }
        return relative + value;
      }
      function encodeInteger(builder, num, relative) {
        let delta = num - relative;
        delta = delta < 0 ? -delta << 1 | 1 : delta << 1;
        do {
          let clamped = delta & 31;
          delta >>>= 5;
          if (delta > 0)
            clamped |= 32;
          builder.write(intToChar[clamped]);
        } while (delta > 0);
        return num;
      }
      function hasMoreVlq(reader, max) {
        if (reader.pos >= max)
          return false;
        return reader.peek() !== comma;
      }
      const bufLength = 1024 * 16;
      const td = typeof TextDecoder !== "undefined" ? /* @__PURE__ */ new TextDecoder() : typeof Buffer !== "undefined" ? {
        decode(buf) {
          const out = Buffer.from(buf.buffer, buf.byteOffset, buf.byteLength);
          return out.toString();
        }
      } : {
        decode(buf) {
          let out = "";
          for (let i = 0; i < buf.length; i++) {
            out += String.fromCharCode(buf[i]);
          }
          return out;
        }
      };
      class StringWriter {
        constructor() {
          this.pos = 0;
          this.out = "";
          this.buffer = new Uint8Array(bufLength);
        }
        write(v) {
          const { buffer } = this;
          buffer[this.pos++] = v;
          if (this.pos === bufLength) {
            this.out += td.decode(buffer);
            this.pos = 0;
          }
        }
        flush() {
          const { buffer, out, pos } = this;
          return pos > 0 ? out + td.decode(buffer.subarray(0, pos)) : out;
        }
      }
      class StringReader {
        constructor(buffer) {
          this.pos = 0;
          this.buffer = buffer;
        }
        next() {
          return this.buffer.charCodeAt(this.pos++);
        }
        peek() {
          return this.buffer.charCodeAt(this.pos);
        }
        indexOf(char) {
          const { buffer, pos } = this;
          const idx = buffer.indexOf(char, pos);
          return idx === -1 ? buffer.length : idx;
        }
      }
      const EMPTY = [];
      function decodeOriginalScopes(input) {
        const { length } = input;
        const reader = new StringReader(input);
        const scopes = [];
        const stack = [];
        let line = 0;
        for (; reader.pos < length; reader.pos++) {
          line = decodeInteger(reader, line);
          const column = decodeInteger(reader, 0);
          if (!hasMoreVlq(reader, length)) {
            const last = stack.pop();
            last[2] = line;
            last[3] = column;
            continue;
          }
          const kind = decodeInteger(reader, 0);
          const fields = decodeInteger(reader, 0);
          const hasName = fields & 1;
          const scope = hasName ? [line, column, 0, 0, kind, decodeInteger(reader, 0)] : [line, column, 0, 0, kind];
          let vars = EMPTY;
          if (hasMoreVlq(reader, length)) {
            vars = [];
            do {
              const varsIndex = decodeInteger(reader, 0);
              vars.push(varsIndex);
            } while (hasMoreVlq(reader, length));
          }
          scope.vars = vars;
          scopes.push(scope);
          stack.push(scope);
        }
        return scopes;
      }
      function encodeOriginalScopes(scopes) {
        const writer = new StringWriter();
        for (let i = 0; i < scopes.length; ) {
          i = _encodeOriginalScopes(scopes, i, writer, [0]);
        }
        return writer.flush();
      }
      function _encodeOriginalScopes(scopes, index, writer, state) {
        const scope = scopes[index];
        const { 0: startLine, 1: startColumn, 2: endLine, 3: endColumn, 4: kind, vars } = scope;
        if (index > 0)
          writer.write(comma);
        state[0] = encodeInteger(writer, startLine, state[0]);
        encodeInteger(writer, startColumn, 0);
        encodeInteger(writer, kind, 0);
        const fields = scope.length === 6 ? 1 : 0;
        encodeInteger(writer, fields, 0);
        if (scope.length === 6)
          encodeInteger(writer, scope[5], 0);
        for (const v of vars) {
          encodeInteger(writer, v, 0);
        }
        for (index++; index < scopes.length; ) {
          const next = scopes[index];
          const { 0: l, 1: c } = next;
          if (l > endLine || l === endLine && c >= endColumn) {
            break;
          }
          index = _encodeOriginalScopes(scopes, index, writer, state);
        }
        writer.write(comma);
        state[0] = encodeInteger(writer, endLine, state[0]);
        encodeInteger(writer, endColumn, 0);
        return index;
      }
      function decodeGeneratedRanges(input) {
        const { length } = input;
        const reader = new StringReader(input);
        const ranges = [];
        const stack = [];
        let genLine = 0;
        let definitionSourcesIndex = 0;
        let definitionScopeIndex = 0;
        let callsiteSourcesIndex = 0;
        let callsiteLine = 0;
        let callsiteColumn = 0;
        let bindingLine = 0;
        let bindingColumn = 0;
        do {
          const semi = reader.indexOf(";");
          let genColumn = 0;
          for (; reader.pos < semi; reader.pos++) {
            genColumn = decodeInteger(reader, genColumn);
            if (!hasMoreVlq(reader, semi)) {
              const last = stack.pop();
              last[2] = genLine;
              last[3] = genColumn;
              continue;
            }
            const fields = decodeInteger(reader, 0);
            const hasDefinition = fields & 1;
            const hasCallsite = fields & 2;
            const hasScope = fields & 4;
            let callsite = null;
            let bindings = EMPTY;
            let range;
            if (hasDefinition) {
              const defSourcesIndex = decodeInteger(reader, definitionSourcesIndex);
              definitionScopeIndex = decodeInteger(reader, definitionSourcesIndex === defSourcesIndex ? definitionScopeIndex : 0);
              definitionSourcesIndex = defSourcesIndex;
              range = [genLine, genColumn, 0, 0, defSourcesIndex, definitionScopeIndex];
            } else {
              range = [genLine, genColumn, 0, 0];
            }
            range.isScope = !!hasScope;
            if (hasCallsite) {
              const prevCsi = callsiteSourcesIndex;
              const prevLine = callsiteLine;
              callsiteSourcesIndex = decodeInteger(reader, callsiteSourcesIndex);
              const sameSource = prevCsi === callsiteSourcesIndex;
              callsiteLine = decodeInteger(reader, sameSource ? callsiteLine : 0);
              callsiteColumn = decodeInteger(reader, sameSource && prevLine === callsiteLine ? callsiteColumn : 0);
              callsite = [callsiteSourcesIndex, callsiteLine, callsiteColumn];
            }
            range.callsite = callsite;
            if (hasMoreVlq(reader, semi)) {
              bindings = [];
              do {
                bindingLine = genLine;
                bindingColumn = genColumn;
                const expressionsCount = decodeInteger(reader, 0);
                let expressionRanges;
                if (expressionsCount < -1) {
                  expressionRanges = [[decodeInteger(reader, 0)]];
                  for (let i = -1; i > expressionsCount; i--) {
                    const prevBl = bindingLine;
                    bindingLine = decodeInteger(reader, bindingLine);
                    bindingColumn = decodeInteger(reader, bindingLine === prevBl ? bindingColumn : 0);
                    const expression = decodeInteger(reader, 0);
                    expressionRanges.push([expression, bindingLine, bindingColumn]);
                  }
                } else {
                  expressionRanges = [[expressionsCount]];
                }
                bindings.push(expressionRanges);
              } while (hasMoreVlq(reader, semi));
            }
            range.bindings = bindings;
            ranges.push(range);
            stack.push(range);
          }
          genLine++;
          reader.pos = semi + 1;
        } while (reader.pos < length);
        return ranges;
      }
      function encodeGeneratedRanges(ranges) {
        if (ranges.length === 0)
          return "";
        const writer = new StringWriter();
        for (let i = 0; i < ranges.length; ) {
          i = _encodeGeneratedRanges(ranges, i, writer, [0, 0, 0, 0, 0, 0, 0]);
        }
        return writer.flush();
      }
      function _encodeGeneratedRanges(ranges, index, writer, state) {
        const range = ranges[index];
        const { 0: startLine, 1: startColumn, 2: endLine, 3: endColumn, isScope, callsite, bindings } = range;
        if (state[0] < startLine) {
          catchupLine(writer, state[0], startLine);
          state[0] = startLine;
          state[1] = 0;
        } else if (index > 0) {
          writer.write(comma);
        }
        state[1] = encodeInteger(writer, range[1], state[1]);
        const fields = (range.length === 6 ? 1 : 0) | (callsite ? 2 : 0) | (isScope ? 4 : 0);
        encodeInteger(writer, fields, 0);
        if (range.length === 6) {
          const { 4: sourcesIndex, 5: scopesIndex } = range;
          if (sourcesIndex !== state[2]) {
            state[3] = 0;
          }
          state[2] = encodeInteger(writer, sourcesIndex, state[2]);
          state[3] = encodeInteger(writer, scopesIndex, state[3]);
        }
        if (callsite) {
          const { 0: sourcesIndex, 1: callLine, 2: callColumn } = range.callsite;
          if (sourcesIndex !== state[4]) {
            state[5] = 0;
            state[6] = 0;
          } else if (callLine !== state[5]) {
            state[6] = 0;
          }
          state[4] = encodeInteger(writer, sourcesIndex, state[4]);
          state[5] = encodeInteger(writer, callLine, state[5]);
          state[6] = encodeInteger(writer, callColumn, state[6]);
        }
        if (bindings) {
          for (const binding of bindings) {
            if (binding.length > 1)
              encodeInteger(writer, -binding.length, 0);
            const expression = binding[0][0];
            encodeInteger(writer, expression, 0);
            let bindingStartLine = startLine;
            let bindingStartColumn = startColumn;
            for (let i = 1; i < binding.length; i++) {
              const expRange = binding[i];
              bindingStartLine = encodeInteger(writer, expRange[1], bindingStartLine);
              bindingStartColumn = encodeInteger(writer, expRange[2], bindingStartColumn);
              encodeInteger(writer, expRange[0], 0);
            }
          }
        }
        for (index++; index < ranges.length; ) {
          const next = ranges[index];
          const { 0: l, 1: c } = next;
          if (l > endLine || l === endLine && c >= endColumn) {
            break;
          }
          index = _encodeGeneratedRanges(ranges, index, writer, state);
        }
        if (state[0] < endLine) {
          catchupLine(writer, state[0], endLine);
          state[0] = endLine;
          state[1] = 0;
        } else {
          writer.write(comma);
        }
        state[1] = encodeInteger(writer, endColumn, state[1]);
        return index;
      }
      function catchupLine(writer, lastLine, line) {
        do {
          writer.write(semicolon);
        } while (++lastLine < line);
      }
      function decode(mappings) {
        const { length } = mappings;
        const reader = new StringReader(mappings);
        const decoded = [];
        let genColumn = 0;
        let sourcesIndex = 0;
        let sourceLine = 0;
        let sourceColumn = 0;
        let namesIndex = 0;
        do {
          const semi = reader.indexOf(";");
          const line = [];
          let sorted = true;
          let lastCol = 0;
          genColumn = 0;
          while (reader.pos < semi) {
            let seg;
            genColumn = decodeInteger(reader, genColumn);
            if (genColumn < lastCol)
              sorted = false;
            lastCol = genColumn;
            if (hasMoreVlq(reader, semi)) {
              sourcesIndex = decodeInteger(reader, sourcesIndex);
              sourceLine = decodeInteger(reader, sourceLine);
              sourceColumn = decodeInteger(reader, sourceColumn);
              if (hasMoreVlq(reader, semi)) {
                namesIndex = decodeInteger(reader, namesIndex);
                seg = [genColumn, sourcesIndex, sourceLine, sourceColumn, namesIndex];
              } else {
                seg = [genColumn, sourcesIndex, sourceLine, sourceColumn];
              }
            } else {
              seg = [genColumn];
            }
            line.push(seg);
            reader.pos++;
          }
          if (!sorted)
            sort(line);
          decoded.push(line);
          reader.pos = semi + 1;
        } while (reader.pos <= length);
        return decoded;
      }
      function sort(line) {
        line.sort(sortComparator);
      }
      function sortComparator(a, b) {
        return a[0] - b[0];
      }
      function encode(decoded) {
        const writer = new StringWriter();
        let sourcesIndex = 0;
        let sourceLine = 0;
        let sourceColumn = 0;
        let namesIndex = 0;
        for (let i = 0; i < decoded.length; i++) {
          const line = decoded[i];
          if (i > 0)
            writer.write(semicolon);
          if (line.length === 0)
            continue;
          let genColumn = 0;
          for (let j = 0; j < line.length; j++) {
            const segment = line[j];
            if (j > 0)
              writer.write(comma);
            genColumn = encodeInteger(writer, segment[0], genColumn);
            if (segment.length === 1)
              continue;
            sourcesIndex = encodeInteger(writer, segment[1], sourcesIndex);
            sourceLine = encodeInteger(writer, segment[2], sourceLine);
            sourceColumn = encodeInteger(writer, segment[3], sourceColumn);
            if (segment.length === 4)
              continue;
            namesIndex = encodeInteger(writer, segment[4], namesIndex);
          }
        }
        return writer.flush();
      }
      exports3.decode = decode;
      exports3.decodeGeneratedRanges = decodeGeneratedRanges;
      exports3.decodeOriginalScopes = decodeOriginalScopes;
      exports3.encode = encode;
      exports3.encodeGeneratedRanges = encodeGeneratedRanges;
      exports3.encodeOriginalScopes = encodeOriginalScopes;
      Object.defineProperty(exports3, "__esModule", { value: true });
    });
  }
});

// node_modules/typescript-svelte-plugin/dist/src/source-mapper.js
var require_source_mapper = __commonJS({
  "node_modules/typescript-svelte-plugin/dist/src/source-mapper.js"(exports2) {
    "use strict";
    Object.defineProperty(exports2, "__esModule", { value: true });
    exports2.SourceMapper = void 0;
    var sourcemap_codec_1 = require_sourcemap_codec_umd();
    function binaryInsert(array, value, key) {
      if (0 === key) {
        key = "0";
      }
      const index = 1 + binarySearch(array, key ? value[key] : value, key);
      let i = array.length;
      while (index !== i--) {
        array[1 + i] = array[i];
      }
      array[index] = value;
    }
    function binarySearch(array, target, key) {
      if (!array || 0 === array.length) {
        return -1;
      }
      if (0 === key) {
        key = "0";
      }
      let low = 0;
      let high = array.length - 1;
      while (low <= high) {
        const i = low + (high - low >> 1);
        const item = void 0 === key ? array[i] : array[i][key];
        if (item === target) {
          return i;
        }
        if (item < target) {
          low = i + 1;
        } else {
          high = i - 1;
        }
      }
      if ((low = ~low) < 0) {
        low = ~low - 1;
      }
      return low;
    }
    var SourceMapper = class {
      constructor(mappings) {
        if (typeof mappings === "string") {
          this.mappings = (0, sourcemap_codec_1.decode)(mappings);
        } else {
          this.mappings = mappings;
        }
      }
      getOriginalPosition(position) {
        const lineMap = this.mappings[position.line];
        if (!lineMap) {
          return { line: -1, character: -1 };
        }
        const closestMatch = binarySearch(lineMap, position.character, 0);
        const match = lineMap[closestMatch];
        if (!match) {
          return { line: -1, character: -1 };
        }
        const { 2: line, 3: character } = match;
        return { line, character };
      }
      getGeneratedPosition(position) {
        if (!this.reverseMappings) {
          this.computeReversed();
        }
        const lineMap = this.reverseMappings[position.line];
        if (!lineMap) {
          return { line: -1, character: -1 };
        }
        const closestMatch = binarySearch(lineMap, position.character, 0);
        const match = lineMap[closestMatch];
        if (!match) {
          return { line: -1, character: -1 };
        }
        const { 1: line, 2: character } = match;
        return { line, character };
      }
      computeReversed() {
        this.reverseMappings = {};
        for (let generated_line = 0; generated_line !== this.mappings.length; generated_line++) {
          for (const { 0: generated_index, 2: original_line, 3: original_character_index } of this.mappings[generated_line]) {
            const reordered_char = [
              original_character_index,
              generated_line,
              generated_index
            ];
            if (original_line in this.reverseMappings) {
              binaryInsert(this.reverseMappings[original_line], reordered_char, 0);
            } else {
              this.reverseMappings[original_line] = [reordered_char];
            }
          }
        }
      }
    };
    exports2.SourceMapper = SourceMapper;
  }
});

// node_modules/typescript-svelte-plugin/dist/src/svelte-snapshots.js
var require_svelte_snapshots = __commonJS({
  "node_modules/typescript-svelte-plugin/dist/src/svelte-snapshots.js"(exports2) {
    "use strict";
    Object.defineProperty(exports2, "__esModule", { value: true });
    exports2.SvelteSnapshotManager = exports2.SvelteSnapshot = void 0;
    var svelte2tsx_1 = require("svelte2tsx");
    var source_mapper_1 = require_source_mapper();
    var utils_12 = require_utils();
    var SvelteSnapshot = class {
      constructor(typescript, fileName, svelteCode, mapper, logger, isTsFile) {
        this.typescript = typescript;
        this.fileName = fileName;
        this.svelteCode = svelteCode;
        this.mapper = mapper;
        this.logger = logger;
        this.isTsFile = isTsFile;
        this.convertInternalCodePositions = false;
      }
      update(svelteCode, mapper) {
        this.svelteCode = svelteCode;
        this.mapper = mapper;
        this.lineOffsets = void 0;
        this.log("Updated Snapshot");
      }
      getOriginalTextSpan(textSpan) {
        if (!(0, utils_12.isNoTextSpanInGeneratedCode)(this.getText(), textSpan)) {
          return null;
        }
        const start = this.getOriginalOffset(textSpan.start);
        if (start === -1) {
          return null;
        }
        return {
          start,
          length: textSpan.length
        };
      }
      getOriginalOffset(generatedOffset) {
        if (!this.scriptInfo) {
          return generatedOffset;
        }
        this.toggleMappingMode(true);
        const lineOffset = this.scriptInfo.positionToLineOffset(generatedOffset);
        this.debug("try convert offset", generatedOffset, "/", lineOffset);
        const original = this.mapper.getOriginalPosition({
          line: lineOffset.line - 1,
          character: lineOffset.offset - 1
        });
        this.toggleMappingMode(false);
        if (original.line === -1) {
          return -1;
        }
        const originalOffset = this.scriptInfo.lineOffsetToPosition(original.line + 1, original.character + 1);
        this.debug("converted offset to", original, "/", originalOffset);
        return originalOffset;
      }
      getGeneratedTextSpan(textSpan) {
        const start = this.getGeneratedOffset(textSpan.start);
        if (start === -1) {
          return null;
        }
        return {
          start,
          length: textSpan.length
        };
      }
      getGeneratedOffset(originalOffset) {
        if (!this.scriptInfo) {
          return originalOffset;
        }
        const lineOffset = this.scriptInfo.positionToLineOffset(originalOffset);
        const original = this.mapper.getGeneratedPosition({
          line: lineOffset.line - 1,
          character: lineOffset.offset - 1
        });
        if (original.line === -1) {
          return -1;
        }
        this.toggleMappingMode(true);
        const generatedOffset = this.scriptInfo.lineOffsetToPosition(original.line + 1, original.character + 1);
        this.toggleMappingMode(false);
        this.debug("converted offset to", original, "/", generatedOffset);
        return generatedOffset;
      }
      setAndPatchScriptInfo(scriptInfo) {
        scriptInfo.scriptKind = this.typescript.ScriptKind.TS;
        const positionToLineOffset = scriptInfo.positionToLineOffset.bind(scriptInfo);
        scriptInfo.positionToLineOffset = (position) => {
          if (this.convertInternalCodePositions) {
            const lineOffset2 = positionToLineOffset(position);
            this.debug("positionToLineOffset for generated code", position, lineOffset2);
            return lineOffset2;
          }
          const lineOffset = this.positionAt(position);
          this.debug("positionToLineOffset for original code", position, lineOffset);
          return { line: lineOffset.line + 1, offset: lineOffset.character + 1 };
        };
        const lineOffsetToPosition = scriptInfo.lineOffsetToPosition.bind(scriptInfo);
        scriptInfo.lineOffsetToPosition = (line, offset) => {
          if (this.convertInternalCodePositions) {
            const position2 = lineOffsetToPosition(line, offset);
            this.debug("lineOffsetToPosition for generated code", { line, offset }, position2);
            return position2;
          }
          const position = this.offsetAt({ line: line - 1, character: offset - 1 });
          this.debug("lineOffsetToPosition for original code", { line, offset }, position);
          return position;
        };
        this.scriptInfo = scriptInfo;
        this.log("patched scriptInfo");
      }
      /**
       * Get the line and character based on the offset
       * @param offset The index of the position
       */
      positionAt(offset) {
        offset = this.clamp(offset, 0, this.svelteCode.length);
        const lineOffsets = this.getLineOffsets();
        let low = 0;
        let high = lineOffsets.length;
        if (high === 0) {
          return { line: 0, character: offset };
        }
        while (low < high) {
          const mid = Math.floor((low + high) / 2);
          if (lineOffsets[mid] > offset) {
            high = mid;
          } else {
            low = mid + 1;
          }
        }
        const line = low - 1;
        return { line, character: offset - lineOffsets[line] };
      }
      /**
       * Get the index of the line and character position
       * @param position Line and character position
       */
      offsetAt(position) {
        const lineOffsets = this.getLineOffsets();
        if (position.line >= lineOffsets.length) {
          return this.svelteCode.length;
        } else if (position.line < 0) {
          return 0;
        }
        const lineOffset = lineOffsets[position.line];
        const nextLineOffset = position.line + 1 < lineOffsets.length ? lineOffsets[position.line + 1] : this.svelteCode.length;
        return this.clamp(nextLineOffset, lineOffset, lineOffset + position.character);
      }
      getLineOffsets() {
        if (this.lineOffsets) {
          return this.lineOffsets;
        }
        const lineOffsets = [];
        const text = this.svelteCode;
        let isLineStart = true;
        for (let i = 0; i < text.length; i++) {
          if (isLineStart) {
            lineOffsets.push(i);
            isLineStart = false;
          }
          const ch = text.charAt(i);
          isLineStart = ch === "\r" || ch === "\n";
          if (ch === "\r" && i + 1 < text.length && text.charAt(i + 1) === "\n") {
            i++;
          }
        }
        if (isLineStart && text.length > 0) {
          lineOffsets.push(text.length);
        }
        this.lineOffsets = lineOffsets;
        return lineOffsets;
      }
      clamp(num, min, max) {
        return Math.max(min, Math.min(max, num));
      }
      log(...args) {
        this.logger.log("SvelteSnapshot:", this.fileName, "-", ...args);
      }
      debug(...args) {
        this.logger.debug("SvelteSnapshot:", this.fileName, "-", ...args);
      }
      toggleMappingMode(convertInternalCodePositions) {
        this.convertInternalCodePositions = convertInternalCodePositions;
      }
      getText() {
        var _a;
        const snapshot = (_a = this.scriptInfo) == null ? void 0 : _a.getSnapshot();
        if (!snapshot) {
          return "";
        }
        return snapshot.getText(0, snapshot.getLength());
      }
      getOriginalText() {
        return this.svelteCode;
      }
    };
    exports2.SvelteSnapshot = SvelteSnapshot;
    var SvelteSnapshotManager = class {
      constructor(typescript, projectService, svelteOptions, logger, configManager, svelteCompiler) {
        this.typescript = typescript;
        this.projectService = projectService;
        this.svelteOptions = svelteOptions;
        this.logger = logger;
        this.configManager = configManager;
        this.svelteCompiler = svelteCompiler;
        this.snapshots = /* @__PURE__ */ new Map();
        this.patchProjectServiceReadFile();
      }
      get(fileName) {
        return this.snapshots.get(this.projectService.toCanonicalFileName(fileName));
      }
      create(fileName) {
        const canonicalFilePath = this.projectService.toCanonicalFileName(fileName);
        if (this.snapshots.has(canonicalFilePath)) {
          return this.snapshots.get(canonicalFilePath);
        }
        const scriptInfo = this.projectService.getOrCreateScriptInfoForNormalizedPath(this.typescript.server.toNormalizedPath(fileName), false);
        if (!scriptInfo) {
          this.logger.log("Was not able get snapshot for", fileName);
          return;
        }
        try {
          scriptInfo.getSnapshot();
        } catch (e) {
          this.logger.log("Loading Snapshot failed", fileName);
        }
        const snapshot = this.snapshots.get(this.projectService.toCanonicalFileName(fileName));
        if (!snapshot) {
          this.logger.log("Svelte snapshot was not found after trying to load script snapshot for", fileName);
          return;
        }
        snapshot.setAndPatchScriptInfo(scriptInfo);
        this.snapshots.set(canonicalFilePath, snapshot);
        return snapshot;
      }
      patchProjectServiceReadFile() {
        if (!this.projectService.host[onReadSvelteFile]) {
          this.logger.log("patching projectService host readFile");
          this.projectService.host[onReadSvelteFile] = [];
          const readFile = this.projectService.host.readFile;
          this.projectService.host.readFile = (path, encoding) => {
            var _a, _b;
            if (!this.configManager.getConfig().enable) {
              return readFile(path, encoding);
            }
            const normalizedPath = path.replace(/\\/g, "/");
            if (normalizedPath.endsWith("node_modules/svelte/types/runtime/ambient.d.ts")) {
              return "";
            } else if (normalizedPath.endsWith("svelte2tsx/svelte-jsx.d.ts")) {
              const originalText = readFile(path) || "";
              const toReplace = '/// <reference lib="dom" />';
              return originalText.replace(toReplace, " ".repeat(toReplace.length));
            } else if (normalizedPath.endsWith("svelte2tsx/svelte-shims.d.ts")) {
              let originalText = readFile(path) || "";
              if (!originalText.includes("// -- start svelte-ls-remove --")) {
                return originalText;
              }
              const startIdx = originalText.indexOf("// -- start svelte-ls-remove --");
              const endIdx = originalText.indexOf("// -- end svelte-ls-remove --");
              originalText = originalText.substring(0, startIdx) + " ".repeat(endIdx - startIdx) + originalText.substring(endIdx);
              return originalText;
            } else if ((0, utils_12.isSvelteFilePath)(path)) {
              this.logger.debug("Read Svelte file:", path);
              const svelteCode = readFile(path) || "";
              const isTsFile = true;
              let code;
              let mapper;
              try {
                const result = (0, svelte2tsx_1.svelte2tsx)(svelteCode, {
                  filename: path.split("/").pop(),
                  isTsFile,
                  mode: "ts",
                  typingsNamespace: this.svelteOptions.namespace,
                  // Don't search for compiler from current path - could be a different one from which we have loaded the svelte2tsx globals
                  parse: (_a = this.svelteCompiler) == null ? void 0 : _a.parse,
                  version: (_b = this.svelteCompiler) == null ? void 0 : _b.VERSION
                });
                code = result.code;
                mapper = new source_mapper_1.SourceMapper(result.map.mappings);
                this.logger.log("Successfully read Svelte file contents of", path);
              } catch (e) {
                this.logger.log("Error loading Svelte file:", path, " Using fallback.");
                this.logger.debug("Error:", e);
                code = "export default class extends Svelte2TsxComponent<any,any,any> {}";
                mapper = new source_mapper_1.SourceMapper("");
              }
              this.projectService.host[onReadSvelteFile].forEach((listener) => listener(path, svelteCode, isTsFile, mapper));
              return code;
            } else {
              return readFile(path, encoding);
            }
          };
        }
        this.projectService.host[onReadSvelteFile].push((path, svelteCode, isTsFile, mapper) => {
          const canonicalFilePath = this.projectService.toCanonicalFileName(path);
          const existingSnapshot = this.snapshots.get(canonicalFilePath);
          if (existingSnapshot) {
            existingSnapshot.update(svelteCode, mapper);
          } else {
            this.snapshots.set(canonicalFilePath, new SvelteSnapshot(this.typescript, path, svelteCode, mapper, this.logger, isTsFile));
          }
        });
      }
    };
    exports2.SvelteSnapshotManager = SvelteSnapshotManager;
    var onReadSvelteFile = Symbol("sveltePluginPatchSymbol");
  }
});

// node_modules/typescript-svelte-plugin/dist/src/config-manager.js
var require_config_manager = __commonJS({
  "node_modules/typescript-svelte-plugin/dist/src/config-manager.js"(exports2) {
    "use strict";
    Object.defineProperty(exports2, "__esModule", { value: true });
    exports2.ConfigManager = void 0;
    var events_1 = require("events");
    var configurationEventName = "configuration-changed";
    var ConfigManager = class {
      constructor() {
        this.emitter = new events_1.EventEmitter();
        this.config = {
          enable: true,
          assumeIsSvelteProject: false
        };
      }
      onConfigurationChanged(listener) {
        this.emitter.on(configurationEventName, listener);
      }
      removeConfigurationChangeListener(listener) {
        this.emitter.off(configurationEventName, listener);
      }
      isConfigChanged(config) {
        return config.enable !== this.config.enable;
      }
      updateConfigFromPluginConfig(config) {
        this.config = __spreadValues(__spreadValues({}, this.config), config);
        this.emitter.emit(configurationEventName, config);
      }
      getConfig() {
        return this.config;
      }
    };
    exports2.ConfigManager = ConfigManager;
  }
});

// node_modules/typescript-svelte-plugin/dist/src/project-svelte-files.js
var require_project_svelte_files = __commonJS({
  "node_modules/typescript-svelte-plugin/dist/src/project-svelte-files.js"(exports2) {
    "use strict";
    Object.defineProperty(exports2, "__esModule", { value: true });
    exports2.ProjectSvelteFilesManager = void 0;
    var utils_12 = require_utils();
    var ProjectSvelteFilesManager = class _ProjectSvelteFilesManager {
      static getInstance(projectName) {
        return this.instances.get(projectName);
      }
      constructor(typescript, project, serverHost, snapshotManager, logger, parsedCommandLine, configManager) {
        this.typescript = typescript;
        this.project = project;
        this.serverHost = serverHost;
        this.snapshotManager = snapshotManager;
        this.logger = logger;
        this.parsedCommandLine = parsedCommandLine;
        this.configManager = configManager;
        this.projectFileToOriginalCasing = /* @__PURE__ */ new Map();
        this.directoryWatchers = /* @__PURE__ */ new Set();
        this.onConfigChanged = (config) => {
          this.disposeWatchers();
          this.clearProjectFile();
          if (config.enable) {
            this.setupWatchers();
            this.updateProjectSvelteFiles();
          }
        };
        if (configManager.getConfig().enable) {
          this.setupWatchers();
          this.updateProjectSvelteFiles();
        }
        configManager.onConfigurationChanged(this.onConfigChanged);
        _ProjectSvelteFilesManager.instances.set(project.getProjectName(), this);
      }
      updateProjectConfig(serviceHost) {
        var _a;
        const parsedCommandLine = (_a = serviceHost.getParsedCommandLine) == null ? void 0 : _a.call(serviceHost, (0, utils_12.getConfigPathForProject)(this.project));
        if (!parsedCommandLine) {
          return;
        }
        this.disposeWatchers();
        this.clearProjectFile();
        this.parsedCommandLine = parsedCommandLine;
        this.setupWatchers();
        this.updateProjectSvelteFiles();
      }
      getFiles() {
        return Array.from(this.projectFileToOriginalCasing.values());
      }
      /**
       * Create directory watcher for include and exclude
       * The watcher in tsserver doesn't support svelte file
       * It won't add new created svelte file to root
       */
      setupWatchers() {
        for (const directory in this.parsedCommandLine.wildcardDirectories) {
          if (!Object.prototype.hasOwnProperty.call(this.parsedCommandLine.wildcardDirectories, directory)) {
            continue;
          }
          const watchDirectoryFlags = this.parsedCommandLine.wildcardDirectories[directory];
          const watcher = this.serverHost.watchDirectory(directory, this.watcherCallback.bind(this), watchDirectoryFlags === this.typescript.WatchDirectoryFlags.Recursive, this.parsedCommandLine.watchOptions);
          this.directoryWatchers.add(watcher);
        }
      }
      watcherCallback(fileName) {
        if (!(0, utils_12.isSvelteFilePath)(fileName)) {
          return;
        }
        this.updateProjectSvelteFiles();
      }
      updateProjectSvelteFiles() {
        const fileNamesAfter = this.readProjectSvelteFilesFromFs().map((file) => ({
          originalCasing: file,
          canonicalFileName: this.project.projectService.toCanonicalFileName(file)
        }));
        const removedFiles = new Set(this.projectFileToOriginalCasing.keys());
        const newFiles = [];
        for (const file of fileNamesAfter) {
          const existingFile = this.projectFileToOriginalCasing.get(file.canonicalFileName);
          if (!existingFile) {
            newFiles.push(file);
            continue;
          }
          removedFiles.delete(file.canonicalFileName);
          if (existingFile !== file.originalCasing) {
            this.projectFileToOriginalCasing.set(file.canonicalFileName, file.originalCasing);
          }
        }
        for (const newFile of newFiles) {
          this.addFileToProject(newFile.originalCasing);
          this.projectFileToOriginalCasing.set(newFile.canonicalFileName, newFile.originalCasing);
        }
        for (const removedFile of removedFiles) {
          this.removeFileFromProject(removedFile, false);
          this.projectFileToOriginalCasing.delete(removedFile);
        }
      }
      addFileToProject(newFile) {
        this.snapshotManager.create(newFile);
        const snapshot = this.project.projectService.getScriptInfo(newFile);
        if (!snapshot) {
          return;
        }
        if (this.project.isRoot(snapshot)) {
          this.logger.debug(`File ${newFile} is already in root`);
          return;
        }
        this.project.addRoot(snapshot);
      }
      readProjectSvelteFilesFromFs() {
        const fileSpec = this.parsedCommandLine.raw;
        const { include, exclude } = fileSpec;
        if ((include == null ? void 0 : include.length) === 0) {
          return [];
        }
        return this.typescript.sys.readDirectory(this.project.getCurrentDirectory() || process.cwd(), [".svelte"], exclude, include).map(this.typescript.server.toNormalizedPath);
      }
      removeFileFromProject(file, exists = true) {
        const info = this.project.getScriptInfo(file);
        if (info) {
          this.project.removeFile(info, exists, true);
        }
      }
      disposeWatchers() {
        this.directoryWatchers.forEach((watcher) => watcher.close());
        this.directoryWatchers.clear();
      }
      clearProjectFile() {
        this.projectFileToOriginalCasing.forEach((file) => this.removeFileFromProject(file));
        this.projectFileToOriginalCasing.clear();
      }
      dispose() {
        this.disposeWatchers();
        this.projectFileToOriginalCasing.clear();
        this.configManager.removeConfigurationChangeListener(this.onConfigChanged);
        _ProjectSvelteFilesManager.instances.delete(this.project.getProjectName());
      }
    };
    exports2.ProjectSvelteFilesManager = ProjectSvelteFilesManager;
    ProjectSvelteFilesManager.instances = /* @__PURE__ */ new Map();
  }
});

// node_modules/typescript-svelte-plugin/dist/src/index.js
var path_1 = require("path");
var language_service_1 = require_language_service();
var logger_1 = require_logger();
var module_loader_1 = require_module_loader();
var svelte_snapshots_1 = require_svelte_snapshots();
var config_manager_1 = require_config_manager();
var project_svelte_files_1 = require_project_svelte_files();
var utils_1 = require_utils();
function init(modules) {
  const configManager = new config_manager_1.ConfigManager();
  let resolvedSvelteTsxFiles;
  const isSvelteProjectCache = /* @__PURE__ */ new Map();
  function create(info) {
    var _a, _b, _c, _d, _e;
    const logger = new logger_1.Logger(info.project.projectService.logger);
    if (!((_a = info.config) == null ? void 0 : _a.assumeIsSvelteProject) && !isSvelteProjectWithCache(info.project)) {
      logger.log("Detected that this is not a Svelte project, abort patching TypeScript");
      return info.languageService;
    }
    if ((0, language_service_1.isPatched)(info.project)) {
      logger.log("Already patched. Checking tsconfig updates.");
      (_b = project_svelte_files_1.ProjectSvelteFilesManager.getInstance(info.project.getProjectName())) == null ? void 0 : _b.updateProjectConfig(info.languageServiceHost);
      return info.languageService;
    }
    configManager.updateConfigFromPluginConfig(info.config);
    if (configManager.getConfig().enable) {
      logger.log("Starting Svelte plugin");
    } else {
      logger.log("Svelte plugin disabled");
      logger.log(info.config);
    }
    const parsedCommandLine = (_d = (_c = info.languageServiceHost).getParsedCommandLine) == null ? void 0 : _d.call(_c, (0, utils_1.getConfigPathForProject)(info.project));
    const getScriptSnapshot = info.languageServiceHost.getScriptSnapshot.bind(info.languageServiceHost);
    info.languageServiceHost.getScriptSnapshot = (fileName) => {
      const normalizedPath = fileName.replace(/\\/g, "/");
      if (normalizedPath.endsWith("node_modules/svelte/types/runtime/ambient.d.ts")) {
        return modules.typescript.ScriptSnapshot.fromString("");
      } else if (normalizedPath.endsWith("node_modules/svelte/types/index.d.ts")) {
        const snapshot = getScriptSnapshot(fileName);
        if (snapshot) {
          const originalText = snapshot.getText(0, snapshot.getLength());
          const startIdx = originalText.indexOf(`declare module '*.svelte' {`);
          const endIdx = originalText.indexOf(`
}`, startIdx + 1) + 2;
          return modules.typescript.ScriptSnapshot.fromString(originalText.substring(0, startIdx) + " ".repeat(endIdx - startIdx) + originalText.substring(endIdx));
        }
      } else if (normalizedPath.endsWith("svelte2tsx/svelte-jsx.d.ts")) {
        const snapshot = getScriptSnapshot(fileName);
        if (snapshot) {
          const originalText = snapshot.getText(0, snapshot.getLength());
          const toReplace = '/// <reference lib="dom" />';
          return modules.typescript.ScriptSnapshot.fromString(originalText.replace(toReplace, " ".repeat(toReplace.length)));
        }
        return snapshot;
      } else if (normalizedPath.endsWith("svelte2tsx/svelte-shims.d.ts")) {
        const snapshot = getScriptSnapshot(fileName);
        if (snapshot) {
          let originalText = snapshot.getText(0, snapshot.getLength());
          if (!originalText.includes("// -- start svelte-ls-remove --")) {
            return snapshot;
          }
          const startIdx = originalText.indexOf("// -- start svelte-ls-remove --");
          const endIdx = originalText.indexOf("// -- end svelte-ls-remove --");
          originalText = originalText.substring(0, startIdx) + " ".repeat(endIdx - startIdx) + originalText.substring(endIdx);
          return modules.typescript.ScriptSnapshot.fromString(originalText);
        }
        return snapshot;
      }
      return getScriptSnapshot(fileName);
    };
    const svelteOptions = ((_e = parsedCommandLine == null ? void 0 : parsedCommandLine.raw) == null ? void 0 : _e.svelteOptions) || { namespace: "svelteHTML" };
    logger.log("svelteOptions:", svelteOptions);
    logger.debug(parsedCommandLine == null ? void 0 : parsedCommandLine.wildcardDirectories);
    const snapshotManager = new svelte_snapshots_1.SvelteSnapshotManager(modules.typescript, info.project.projectService, svelteOptions, logger, configManager, (0, utils_1.importSvelteCompiler)((0, utils_1.getProjectDirectory)(info.project)));
    const projectSvelteFilesManager = parsedCommandLine ? new project_svelte_files_1.ProjectSvelteFilesManager(modules.typescript, info.project, info.serverHost, snapshotManager, logger, parsedCommandLine, configManager) : void 0;
    const moduleLoaderDisposable = (0, module_loader_1.patchModuleLoader)(logger, snapshotManager, modules.typescript, info.languageServiceHost, info.project, configManager);
    const updateProjectWhenConfigChanges = () => {
      var _a2, _b2;
      (_b2 = (_a2 = info.project).markAsDirty) == null ? void 0 : _b2.call(_a2);
      if (projectSvelteFilesManager) {
        info.project.updateGraph();
      }
    };
    configManager.onConfigurationChanged(updateProjectWhenConfigChanges);
    return (0, language_service_1.decorateLanguageService)(info.languageService, snapshotManager, logger, configManager, info, modules.typescript, () => {
      projectSvelteFilesManager == null ? void 0 : projectSvelteFilesManager.dispose();
      configManager.removeConfigurationChangeListener(updateProjectWhenConfigChanges);
      moduleLoaderDisposable.dispose();
    });
  }
  function getExternalFiles(project) {
    var _a, _b;
    if (!isSvelteProjectWithCache(project) || !configManager.getConfig().enable) {
      return [];
    }
    const configFilePath = (0, utils_1.getProjectDirectory)(project);
    const svelteTsxFiles = resolveSvelteTsxFiles(configFilePath);
    if (!configFilePath) {
      svelteTsxFiles.forEach((file) => {
        openSvelteTsxFileForInferredProject(project, file);
      });
    }
    return svelteTsxFiles.concat((_b = (_a = project_svelte_files_1.ProjectSvelteFilesManager.getInstance(project.getProjectName())) == null ? void 0 : _a.getFiles()) != null ? _b : []);
  }
  function resolveSvelteTsxFiles(configFilePath) {
    if (resolvedSvelteTsxFiles) {
      return resolvedSvelteTsxFiles;
    }
    const svelteTsPath = (0, path_1.dirname)(require.resolve("svelte2tsx"));
    const sveltePath = require.resolve("svelte/compiler", configFilePath ? { paths: [configFilePath] } : void 0);
    const VERSION = require(sveltePath).VERSION;
    const isSvelte3 = VERSION.split(".")[0] === "3";
    const svelteHtmlDeclaration = isSvelte3 ? void 0 : (0, path_1.join)((0, path_1.dirname)(sveltePath), "svelte-html.d.ts");
    const svelteHtmlFallbackIfNotExist = svelteHtmlDeclaration && modules.typescript.sys.fileExists(svelteHtmlDeclaration) ? svelteHtmlDeclaration : "./svelte-jsx-v4.d.ts";
    const svelteTsxFiles = (isSvelte3 ? ["./svelte-shims.d.ts", "./svelte-jsx.d.ts", "./svelte-native-jsx.d.ts"] : [
      "./svelte-shims-v4.d.ts",
      svelteHtmlFallbackIfNotExist,
      "./svelte-native-jsx.d.ts"
    ]).map((f) => modules.typescript.sys.resolvePath((0, path_1.resolve)(svelteTsPath, f)));
    resolvedSvelteTsxFiles = svelteTsxFiles;
    return svelteTsxFiles;
  }
  function isSvelteProjectWithCache(project) {
    const cached = isSvelteProjectCache.get(project.getProjectName());
    if (cached !== void 0) {
      return cached;
    }
    const result = !!(0, utils_1.isSvelteProject)(project);
    isSvelteProjectCache.set(project.getProjectName(), result);
    return result;
  }
  function onConfigurationChanged(config) {
    if (configManager.isConfigChanged(config)) {
      configManager.updateConfigFromPluginConfig(config);
    }
  }
  function openSvelteTsxFileForInferredProject(project, file) {
    const normalizedPath = modules.typescript.server.toNormalizedPath(file);
    if (project.containsFile(normalizedPath)) {
      return;
    }
    const scriptInfo = project.projectService.getOrCreateScriptInfoForNormalizedPath(
      normalizedPath,
      /*openedByClient*/
      true,
      project.readFile(file)
    );
    if (!scriptInfo) {
      return;
    }
    if (!project.projectService.openFiles.has(scriptInfo.path)) {
      project.projectService.openFiles.set(scriptInfo.path, void 0);
    }
    if (project.projectRootPath) {
      project.addRoot(scriptInfo);
    }
  }
  return { create, getExternalFiles, onConfigurationChanged };
}
module.exports = init;
//# sourceMappingURL=index.js.map
