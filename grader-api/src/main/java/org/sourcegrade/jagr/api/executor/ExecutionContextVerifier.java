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

package org.sourcegrade.jagr.api.executor;

import com.google.inject.Inject;
import org.jetbrains.annotations.ApiStatus;

@FunctionalInterface
public interface ExecutionContextVerifier {

  static ExecutionContextVerifier ensureNotRecursive() {
    return FactoryProvider.factory.ensureNotRecursive();
  }

  /**
   * @param context The {@link ExecutionContext} to verify
   * @throws Error (or subclass) if the provided {@link ExecutionContext} does not pass verification
   */
  void verify(ExecutionContext context);

  @ApiStatus.Internal
  final class FactoryProvider {
    @Inject
    private static Factory factory;
  }

  @ApiStatus.Internal
  interface Factory {
    ExecutionContextVerifier ensureNotRecursive();
  }
}
