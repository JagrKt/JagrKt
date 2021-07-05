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

import java.util.List;

/**
 * Instances of this interface are immutable
 */
@ApiStatus.NonExtendable
public interface GradeResult {

  static GradeResult ofCorrect(int points) {
    return FactoryProvider.factory.ofCorrect(points);
  }

  static GradeResult ofIncorrect(int points) {
    return FactoryProvider.factory.ofIncorrect(points);
  }

  static GradeResult ofNone() {
    return FactoryProvider.factory.ofNone();
  }

  static GradeResult of(int correctPoints, int incorrectPoints) {
    return FactoryProvider.factory.of(correctPoints, incorrectPoints);
  }

  static GradeResult of(int correctPoints, int incorrectPoints, String comment) {
    return FactoryProvider.factory.of(correctPoints, incorrectPoints, comment);
  }

  static GradeResult of(GradeResult grade, GradeResult... otherGrades) {
    return FactoryProvider.factory.of(grade, otherGrades);
  }

  static GradeResult of(GradeResult grade, Iterable<? extends GradeResult> otherGrades) {
    return FactoryProvider.factory.of(grade, otherGrades);
  }

  static GradeResult ofMax(Criterion criterion) {
    return FactoryProvider.factory.ofMax(criterion);
  }

  static GradeResult ofMin(Criterion criterion) {
    return FactoryProvider.factory.ofMin(criterion);
  }

  static GradeResult withComments(GradeResult grade, Iterable<String> comments) {
    return FactoryProvider.factory.withComments(grade, comments);
  }

  /**
   * @return The number of points that are definitely correct, as determined by the automatic grader.
   */
  int getCorrectPoints();

  /**
   * @return The number of points that are definitely incorrect, as determined by the automatic grader.
   */
  int getIncorrectPoints();

  List<String> getComments();

  @ApiStatus.Internal
  final class FactoryProvider {
    @Inject
    private static Factory factory;
  }

  /**
   * For documentation see static methods above
   */
  @ApiStatus.Internal
  interface Factory {
    GradeResult ofCorrect(int points);

    GradeResult ofIncorrect(int points);

    GradeResult ofNone();

    GradeResult of(int correctPoints, int incorrectPoints);

    GradeResult of(int correctPoints, int incorrectPoints, String comment);

    GradeResult of(GradeResult grade, GradeResult... otherGrades);

    GradeResult of(GradeResult grade, Iterable<? extends GradeResult> otherGrades);

    GradeResult ofMax(Criterion criterion);

    GradeResult ofMin(Criterion criterion);

    GradeResult withComments(GradeResult grade, Iterable<String> comments);
  }
}
