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

package org.jagrkt.common

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

fun <T, R> Array<T>.parallelForEach(scope: CoroutineScope = GlobalScope, block: suspend (T) -> R) {
  return asSequence().parallelForEach(size, scope, block)
}

fun <T, R> List<T>.parallelForEach(scope: CoroutineScope = GlobalScope, block: suspend (T) -> R) {
  return asSequence().parallelForEach(size, scope, block)
}

fun <T, R> Sequence<T>.parallelForEach(size: Int, scope: CoroutineScope = GlobalScope, block: suspend (T) -> R) {
  return parallel(size, scope, { forEach { it.await() } }, block)
}

fun <T, R> Array<T>.parallelMap(scope: CoroutineScope = GlobalScope, block: suspend (T) -> R): List<R> {
  return asSequence().parallelMap(size, scope, block)
}

fun <T, R> List<T>.parallelMap(scope: CoroutineScope = GlobalScope, block: suspend (T) -> R): List<R> {
  return asSequence().parallelMap(size, scope, block)
}

fun <T, R> Sequence<T>.parallelMap(size: Int, scope: CoroutineScope = GlobalScope, block: suspend (T) -> R): List<R> {
  return parallel(size, scope, { map { it.await() } }, block)
}

fun <T, R> Array<T>.parallelMapNotNull(scope: CoroutineScope = GlobalScope, block: suspend (T) -> R?): List<R> {
  return asSequence().parallelMapNotNull(size, scope, block)
}

fun <T, R> List<T>.parallelMapNotNull(scope: CoroutineScope = GlobalScope, block: suspend (T) -> R?): List<R> {
  return asSequence().parallelMapNotNull(size, scope, block)
}

fun <T, R> Sequence<T>.parallelMapNotNull(size: Int, scope: CoroutineScope = GlobalScope, block: suspend (T) -> R?): List<R> {
  return parallel(size, scope, { mapNotNull { it.await() } }, block)
}

private fun <T, R, C> Sequence<T>.parallel(
  size: Int, scope: CoroutineScope = GlobalScope,
  mapper: suspend (Array<Deferred<R>>).() -> C,
  block: suspend (T) -> R,
): C {
  val iter = iterator()
  val deferreds = Array(size) {
    val next = iter.next()
    scope.async { block(next) }
  }
  return runBlocking { deferreds.mapper() }
}
