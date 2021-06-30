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

package org.jagrkt.api.rubric;

import com.google.inject.Inject;
import org.jetbrains.annotations.ApiStatus;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestIdentifier;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.Callable;

@FunctionalInterface
public interface JUnitTestRef {

  static JUnitTestRef ofClass(Class<?> clazz) {
    return FactoryProvider.factory.ofClass(clazz);
  }

  static JUnitTestRef ofMethod(Method method) {
    return FactoryProvider.factory.ofMethod(method);
  }

  static JUnitTestRef ofClass(Callable<Class<?>> clazzSupplier) {
    return FactoryProvider.factory.ofClass(clazzSupplier);
  }

  static JUnitTestRef ofMethod(Callable<Method> methodSupplier) {
    return FactoryProvider.factory.ofMethod(methodSupplier);
  }

  static JUnitTestRef and(JUnitTestRef... testRefs) {
    return FactoryProvider.factory.and(testRefs);
  }

  static JUnitTestRef or(JUnitTestRef... testRefs) {
    return FactoryProvider.factory.or(testRefs);
  }

  static JUnitTestRef not(JUnitTestRef testRef) {
    return FactoryProvider.factory.not(testRef);
  }

  TestExecutionResult get(Map<TestIdentifier, TestExecutionResult> testResults);

  @ApiStatus.Internal
  final class FactoryProvider {
    @Inject
    private static Factory factory;
  }

  @ApiStatus.Internal
  interface Factory {
    JUnitTestRef ofClass(Class<?> clazz);

    JUnitTestRef ofMethod(Method method);

    JUnitTestRef ofClass(Callable<Class<?>> clazzSupplier);

    JUnitTestRef ofMethod(Callable<Method> methodSupplier);

    JUnitTestRef and(JUnitTestRef... testRefs);

    JUnitTestRef or(JUnitTestRef... testRefs);

    JUnitTestRef not(JUnitTestRef testRef);
  }
}
