<p align="center">
  <img width="320" src="./svelte-intellij-header.png" alt="logo of svelte-intellij repository">
</p>

# svelte-intellij

Support for <a href="https://svelte.dev/">Svelte</a> in your IDE of choice. Works in JetBrains products that support JavaScript.

<p>Work in progress, pretty usable already.</p>

## Installation
* Use official [Plugin Repository](https://plugins.jetbrains.com/plugin/12375-svelte)
* Head over to [releases tab](https://github.com/tomblachut/svelte-intellij/releases), download `.zip` archive and [Install plugin from disk](https://www.jetbrains.com/help/webstorm/managing-plugins.html#install_plugin_from_disk)

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
    <li>$ subscriptions aren't recognised properly</li>
    <li>JavaScript isn't highlighted inside attributes</li>
    <li>Directives are treated as plain attributes</li>
    <li>...</li>
</ul>

<h2>Support</h2>

This project is run by one person, because I want Svelte to succeed. Please, leave a star and spread the word.

<h2>Contributing</h2>

Contributions are very much welcome! 

IntelliJ Platform runs on JVM, so this can seem like a huge limitation, but don't worry. Kotlin feels a lot like TypeScript. Bigger issue is scarce documentation for their API.

Otherwise, please report what could be improved. IntelliJ has a huge feature set, I personally don't use everything it has to offer.

<h2>Building and running the plugin</h2>

You'll need IntelliJ IDEA. Community edition is free and works, you can also use Ultimate edition if you have one. 

Clone the repository and run `./gradlew :runIde` (There are also other intermediate tasks). 

IntelliJ Ultimate will download in a bit and you'll be prompted with license dialog - choose evaluation mode. After 30 days delete `build/idea-sandbox` and start again.

Ready to share `.zip` archive can be found in `build/distributions` after running `./gradlew :buildPlugin`.

<h2>Running tests</h2>

Tests can be run with `./gradlew :test` command or even better or by using run action in IntelliJ IDEA. 

<h2>Writing Tests</h2>

Some of the tests in IntelliJ Platform (notably lexer and parser ones) are similar to Jest snapshots.

You need to create a `.svelte` file in `scr/test/resources` directory and add function composed of the word `test` and file name to appropriate class.
Then run the test. File with expected results will be created and you need to commit it. When updating test simply delete expected file and rerun test.

Lexer tests use `.tokens.txt` extensions, while Parser tests use `.txt`.
