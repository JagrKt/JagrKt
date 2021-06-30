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

import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Comment

@ConfigSerializable
class Config {

  @Comment("The locations of the following directories may be configured here")
  val dir: Dir = Dir()

  val extras: Extras = Extras()

  val grading: Grading = Grading()

  val transformers = Transformers()
}

@ConfigSerializable
class Dir {

  @Comment("Runtime dependencies for submissions")
  var libs: String = "libs"

  @Comment("Rubrics export directory")
  var rubrics: String = "rubrics"

  @Comment("Solutions directory for compilation of test jars")
  var solutions: String = "solutions"

  @Comment("Submissions ingest directory")
  var submissions: String = "submissions"

  @Comment("Submission export directory")
  var submissionsExport: String = "submissions-export"

  @Comment("Test jar ingest directory")
  var tests: String = "tests"
}

@ConfigSerializable
class Extras {

  abstract class Extra {
    val enabled: Boolean = true
  }

  @ConfigSerializable
  class MoodleUnpack : Extra() {
    val studentIdRegex: String = "[a-z]{2}[0-9]{2}[a-z]{4}"
  }

  val moodleUnpack: MoodleUnpack = MoodleUnpack()
}

@ConfigSerializable
class Transformers {

  abstract class Transformer {
    val enabled = true
  }

  @ConfigSerializable
  class TimeoutTransformer : Transformer() {
    @Comment(
      """
The grading thread's maximum permitted elapsed userTime in milliseconds since the last timeout before an
AssertionFailedError is thrown. If a thread's userTime satisfies
(userTime - lastTimeout) > individualTimeout,
the current userTime is stored for comparison later, and an AssertionFailedError is thrown to be caught by JUnit.
"""
    )
    val individualTimeout = 10_000L

    @Comment(
      """
The grading thread's maximum permitted elapsed userTime in milliseconds (from thread start) before an
AssertionFailedError is thrown. If a thread's userTime satisfies
((userTime - lastTimeout) > individualTimeout) && (userTime > totalTimeout),
an AssertionFailedError is thrown to be caught by JUnit. Note that lastTimeout is not reset in this case, and all further
invocations of checkTimeout() will result in an AssertionFailedError
"""
    )
    val totalTimeout = 150_000L
  }

  val timeout = TimeoutTransformer()
}

@ConfigSerializable
class Grading {

  @Comment("The maximum amount of concurrent threads to use for grading")
  val concurrentThreads: Int = 4
}
