// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package dev.blachut.svelte.lang

import com.intellij.openapi.application.writeAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.codeInsightFixture
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.junit5.fixture.TestFixture
import com.intellij.testFramework.junit5.fixture.moduleFixture
import com.intellij.testFramework.junit5.fixture.projectFixture
import com.intellij.testFramework.junit5.fixture.tempPathFixture
import com.intellij.testFramework.junit5.fixture.testFixture
import org.jetbrains.annotations.TestOnly
import java.nio.file.Path

private val SVELTE_MODULE_ROOT = Key.create<Path>("svelte.test.module.root")

/**
 * Creates the opened project of a Svelte test class with one module.
 * The module directory is the content root and the source root of the module. It is not the project directory.
 * Declare it in a companion object, so that the tests of the class share the project and the module.
 * Use it with [svelteCodeInsightFixture].
 *
 * A change of the module, for example a new source root, stays for the next test of the class.
 *
 * Declare it as an instance property when a test of the class creates a `package.json`, for example with [configureSvelteDependencies].
 * Then each test gets its own project.
 * The pointers of `PackageJsonPolySymbolsRegistryManager` to a `package.json` live as long as the project,
 * so a shared project fails the leak check after each test.
 * The `node_modules` libraries of the project also stay for the next test.
 */
@TestOnly
fun svelteProjectFixture(): TestFixture<Project> = testFixture("svelteProjectFixture") {
  val moduleRootFixture = tempPathFixture()
  val projectFixture = projectFixture(openAfterCreation = true)
  projectFixture.moduleFixture(moduleRootFixture, addPathToSourceRoot = true).init()
  val project = projectFixture.init()
  project.putUserData(SVELTE_MODULE_ROOT, moduleRootFixture.init())
  initialized(project) {}
}

/**
 * Creates a [CodeInsightTestFixture] on the module of [projectFixture]. [projectFixture] must come from [svelteProjectFixture].
 * The module directory is the temp directory of the fixture, and [testDataPath] is its test data path.
 * After the test, the fixture deletes the files of the test through the VFS.
 * Declare the result as an instance property, so that each test gets its own fixture.
 */
@TestOnly
@JvmOverloads
fun svelteCodeInsightFixture(
  projectFixture: TestFixture<Project>,
  testDataPath: String = getSvelteTestDataPath(),
): TestFixture<CodeInsightTestFixture> = testFixture("svelteCodeInsightFixture") {
  val fixture = codeInsightFixture(projectFixture, svelteModuleRootFixture(projectFixture)).init()
  fixture.testDataPath = testDataPath
  initialized(fixture) {}
}

private fun svelteModuleRootFixture(projectFixture: TestFixture<Project>): TestFixture<Path> = testFixture("svelteModuleRootFixture") {
  val project = projectFixture.init()
  val root = checkNotNull(project.getUserData(SVELTE_MODULE_ROOT)) { "Create the project with svelteProjectFixture()" }
  initialized(root) {
    val directory = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(root) ?: return@initialized
    directory.refresh(false, true)
    writeAction {
      for (child in directory.children) {
        child.delete(this)
      }
    }
  }
}
