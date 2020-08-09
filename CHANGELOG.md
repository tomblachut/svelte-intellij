# Changelog

## [Unreleased]
- More subtle file icons
- Stop marking $ labels as unnecessary
- Support await catch shorthand tag
- Completions for src and global attributes of style tag
- Complete parser rewrite
- Svelte is no longer a template language, so it can be used in one
- Improved brace highlighting and auto insertion
- Improved completion of svelte tags
- More robust Emmet support
- Complete rewrite of reference resolving
- Fixed find usages for attribute expressions
- Complete rewrite of formatter
- Improved automatic indentation etc
- One-line blocks stay one-line
- Added missing fold regions
- Individual fold levels for Svelte block clauses 
- Improvements around shorthand attribute inspections and intentions
- More graceful handling of typos
- Ton of other small fixes
- Dropped compatibility with 2019.x platform

## [0.12.2]

- Fix compatibility with 2020.2 versions
- Recognize $$restProps

## [0.12.1]

- Extend supported IDE version range to 2020.x
- Support basic interpolations inside style attributes

## [0.12.0]

- Support for different CSS dialects via lang attribute
- Initial support for $-prefixed store references
- Enable completion of JS declarations from script tags
- Initial support for module context scripts (inside Svelte files only)
- More robust logic of resolving component declarations
- Unresolved components are highlighted the same as other identifiers
- Remove buggy prop insertion while completing tag name
- Stop inserting mustaches after typing = for attributes
- Enable quote matching
- Bug fixes & stability improvements

## [0.11.1]

- Fix regression about not working import suggestions

## [0.11.0]

- Stop annotating directives as unknown attributes
- Enable CSS references and completions
- Recognize $$props variable
- Highlight unresolved references inside script tag the same as inside template expressions
- Limit IDE finding references to variables defined in e.g. config files
- Fix IDE error occurring for empty shorthand attribute
- Minor fixes & stability improvements

## [0.10.0]

- Support attribute value expressions
- Support shorthand attribute expressions
- Support spread attributes

## [0.9.1]

- Fix buggy auto-inserted each block closing tag
- Correctly set minimum version compatibility to 2019.2

## [0.9.0]

- Connect template expressions to definitions inside script tag
- Support complex JS expressions in Svelte tags
- Recognize more component imports
- Properly parse Svelte components with a lowercase name matching HTML single tags
- Add Svelte Component to New file menu
- Improve stability

## [0.8.0]

- Add auto import for components
- Highlight not imported components
- Mark unused component imports properly
- Fix Svelte blocks breaking on identifiers containing the words if, as or then
- Support @html & @debug expressions
- Display better error messages for incomplete code
- Extend supported IDE version range to 2019.2

## [0.7.0]

- Support code formatting
- Emmet-style expansions for Svelte tags. Try typing if\[TAB\]
- Auto indent when writing newline between Svelte tags
- Automatically insert closing Svelte tags
- Support folding regions (+/- icons in the gutter) for Svelte tags
- Support Comment with Line/Block Comment actions
- Highlight Svelte tag mustaches in the same color as keywords
- Improve parser behavior for incomplete code

## [0.6.0]

- Add syntax highlighting for JS inside blocks & expressions (bar attributes)
- Add syntax highlighting for Svelte keywords
- Improve parser recovery after errors
- Additional minor improvements

## [0.5.0]

- First public release
