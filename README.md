<p align="center">
  <img width="320" src="./media/header.png" alt="Svelte IDE plugin. WebStorm, IntelliJ IDEA, more.">
</p>

<p>Support for <a href="https://svelte.dev/">Svelte</a> in your IDE of choice.</p>

<p>Work in progress, pretty usable already.</p>

<h2>Features</h2>

<ul>
    <li>Syntax highlighting</li>
    <li>Code formatting</li>
    <li>Typing assistance</li>
    <li>Completions of components and props</li>
    <li>Auto import of components</li>
    <li>Navigation from components to their definition</li>
</ul>

<h2>Known issues</h2>

<ul>
    <li>$ labels aren't recognised properly</li>
    <li>Directives are treated as plain attributes</li>
    <li>No TypeScript</li>
    <li>...</li>
</ul>

## Installation
* Use official [Plugin Repository](https://plugins.jetbrains.com/plugin/12375-svelte)
* Head over to [releases tab](https://github.com/tomblachut/svelte-intellij/releases), download `.zip` archive and [Install plugin from disk](https://www.jetbrains.com/help/webstorm/managing-plugins.html#install_plugin_from_disk)

## Compatible IDEs

Works in JavaScript-enabled JetBrains IDEs starting from the version 2019.2.
Information about older releases is available in [version history](https://plugins.jetbrains.com/plugin/12375-svelte/versions).

## Maintenance & support

This is a side project, there are weeks when I just don't want to reply because I'm mentally exhausted after my full time job. Please respect that.

If you want to motivate me a bit, please leave a star, follow me on Twitter ([@tomblachut](https://twitter.com/tomblachut)) and spread the word. You can also [send me money](https://www.paypal.me/tomblachut). Thanks!

## Contributing

Contributions are very much welcome! 

That said IDE development is a complex task and project is still in its early stages so its often hard to tell exactly what should be contributed without doing time-consuming analysis.

IntelliJ Platform runs on JVM, yet Kotlin feels a lot like TypeScript. [IntelliJ Platform SDK documentation](https://jetbrains.org/intellij/sdk/docs/intro/welcome.html) is a good starting point.

### Building and running the plugin

You'll need IntelliJ IDEA. The Community Edition is free and sufficient, you can also use Ultimate edition if you have one. 

Clone the repository and run `./gradlew runIde` (There are also intermediate tasks). 

IntelliJ Ultimate will download in a bit, and you'll be prompted with license dialog - choose evaluation mode. After 30 days delete `build/idea-sandbox` and start again.

Ready to share `.zip` archive can be found in `build/distributions` after running `./gradlew buildPlugin`.

### Running tests

Tests can be run with `./gradlew test` command or even better or by using run action in IntelliJ IDEA. 

### Writing Tests

Some tests in IntelliJ Platform are similar to Jest snapshots.

You need to create a `.svelte` file in `scr/test/resources` directory and add function composed of the word `test` and file name to appropriate class.
When running test for the first time, file with expected results will be created, please commit it. When updating test, simply delete snapshot file and rerun test.
