/*
 *   Jagr - SourceGrade.org
 *   Copyright (C) 2021 Alexander Staeding
 *   Copyright (C) 2021 Contributors
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package jagrinternal.instrumentation;

import com.google.inject.Inject;
import org.sourcegrade.jagr.common.executor.ExecutionContextFactoryImpl;
import org.sourcegrade.jagr.common.executor.StackTraceVerifier;

public final class ExecutionContextHandler {

  @Inject
  private static ExecutionContextFactoryImpl CONTEXTS;

  public static void checkExecutionContext() {
    final StackTraceElement[] callStack = Thread.currentThread().getStackTrace();
    for (StackTraceVerifier verifier : CONTEXTS.getOrCreateStack()) {
      verifier.verify(callStack);
    }
  }
}
