<p align="center">
  <img width="320" src="./svelte-intellij-header.png" alt="logo of svelte-intellij repository">
</p>

# svelte-intellij

Support for <a href="https://svelte.dev/">Svelte</a> in your IDE of choice. Works in JetBrains products that support JavaScript.

<p>Work in progress, pretty usable already.</p>

## Installation
* Use official [Plugin Repository](https://plugins.jetbrains.com/plugin/12375-svelte)
* Head over to [releases tab](https://github.com/tomblachut/svelte-intellij/releases), download `.jar` archive and [Install plugin from disk](https://www.jetbrains.com/help/webstorm/managing-plugins.html#install_plugin_from_disk)

<h2>Features</h2>

<ul>
    <li>Syntax highlighting</li>
    <li>Code formatting</li>
    <li>Typing assistance</li>
    <li>Partial completion suggestions</li>
    <li>Navigation from components to their definition</li>
</ul>

<h2>Known issues</h2>

<ul>
    <li>Component imports are marked as unused</li>
    <li>$ subscriptions aren't recognised properly</li>
    <li>Props completion and validation isn't provided</li>
    <li>Directives are treated as plain attributes</li>
    <li>...</li>
</ul>

<h2>Support</h2>

This project is run by one person, because I want Svelte to succeed. Please, leave a star and spread the word.

<h2>Contributing</h2>

Contributions are very much welcome! 

IntelliJ Platform runs on JVM, so this can seem like a huge limitation, but don't worry. Kotlin looks a lot like TypeScript. Bigger issue is scarce documentation for their API. I'll write a few words describing it some day.

If you don't know how to write IntelliJ plugins, please report what could be improved. IntelliJ has a huge feature set, I personally don't use everything it has to offer.
