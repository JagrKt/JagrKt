/*
 *   JagrKt - JagrKt.org
 *   Copyright (C) 2021 Alexander Staeding
 *   Copyright (C) 2021 Contributors
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.jagrkt.common.compiler.java

import javax.tools.FileObject
import javax.tools.ForwardingJavaFileManager
import javax.tools.JavaFileManager
import javax.tools.JavaFileObject

class ExtendedStandardJavaFileManager(
  javaFileManager: JavaFileManager,
  private val classPath: Map<String, CompiledClass>,
  private val classStorage: MutableMap<String, CompiledClass>,
) : ForwardingJavaFileManager<JavaFileManager>(javaFileManager) {

  override fun getJavaFileForOutput(
    location: JavaFileManager.Location,
    className: String,
    kind: JavaFileObject.Kind,
    sibling: FileObject,
  ): JavaFileObject {
    try {
      val compiledClass = CompiledClass.Runtime(className)
      classStorage[compiledClass.name] = compiledClass
      return compiledClass
    } catch (e: Exception) {
      throw IllegalStateException("Error creating class file for $className", e)
    }
  }

  override fun list(
    location: JavaFileManager.Location?,
    packageName: String?,
    kinds: MutableSet<JavaFileObject.Kind>,
    recurse: Boolean,
  ): Iterable<JavaFileObject> {
    val fromDelegate = super.list(location, packageName, kinds, recurse)
    if (packageName != null
      && kinds.contains(JavaFileObject.Kind.CLASS)
      && location != null
      && location.name == "CLASS_PATH"
    ) {
      return fromDelegate + getCompiledClassesFromClassPath(packageName)
    }
    return fromDelegate
  }

  private fun getCompiledClassesFromClassPath(packageName: String): List<JavaFileObject> {
    return classPath.asSequence()
      .filter { it.key.startsWith(packageName) }
      .map { it.value }
      .toList()
  }

  override fun inferBinaryName(location: JavaFileManager.Location, file: JavaFileObject): String {
    if (file is CompiledClass.Existing) {
      return file.className
    }
    return super.inferBinaryName(location, file)
  }
}
