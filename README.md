<p align="center">
  <img width="320" src="./media/header.png" alt="Svelte IDE plugin. WebStorm, IntelliJ IDEA, more.">
</p>

[![Build](https://github.com/tomblachut/svelte-intellij/workflows/Build/badge.svg)](https://github.com/tomblachut/svelte-intellij/actions?query=workflow%3ABuild)
[![Version](https://img.shields.io/jetbrains/plugin/v/12375-svelte.svg)](https://plugins.jetbrains.com/plugin/12375-svelte)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/12375-svelte.svg)](https://plugins.jetbrains.com/plugin/12375-svelte)
[![Twitter Follow](https://img.shields.io/twitter/follow/tomblachut?style=flat)](https://twitter.com/tomblachut)

<!-- Plugin description -->
Support for <a href="https://svelte.dev/">Svelte</a> in your IDE of choice.

## Features

* Syntax highlighting
* Code formatting
* Typing assistance
* Emmet-style abbreviations of Svelte blocks
* Completions of components, props and some directives
* Auto import of components
* Navigation from components to their definition
* Debugger integration
<!-- Plugin description end -->

## Installation

- Using IDE built-in plugin system:
  
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "Svelte"</kbd> >
  <kbd>Install Plugin</kbd>
  
- Manually:

  Download the [latest release](https://github.com/tomblachut/svelte-intellij/releases/latest) and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

## Compatible IDEs

Works in JavaScript-enabled JetBrains IDEs. Information about version compatibility and older releases is available in [version history](https://plugins.jetbrains.com/plugin/12375-svelte/versions).

## Contributing

Contributions are very much welcome!

IntelliJ Platform runs on JVM, yet Kotlin feels a lot like TypeScript. [IntelliJ Platform SDK documentation](https://plugins.jetbrains.com/docs/intellij/) is a good starting point.

### Building and running the plugin

You'll need IntelliJ IDEA. The Community Edition is free and sufficient, you can also use Ultimate edition if you have one.

Clone the repository and run `./gradlew runIde` (There are also intermediate tasks).

IntelliJ Ultimate will download in a bit, and you'll be prompted with license dialog - choose evaluation mode.

Ready to share `.zip` archive can be found in `build/distributions` after running `./gradlew buildPlugin`.

### Running tests

Tests can be run with `./gradlew test` command or even better or by using run action in IntelliJ IDEA.

### Writing Tests

Some tests in IntelliJ Platform are similar to Jest snapshots.

You need to create a `.svelte` file in `scr/test/resources` directory and add function composed of the word `test` and file name to appropriate class.
When running test for the first time, file with expected results will be created, please commit it. When updating test, simply delete snapshot file and rerun test.
