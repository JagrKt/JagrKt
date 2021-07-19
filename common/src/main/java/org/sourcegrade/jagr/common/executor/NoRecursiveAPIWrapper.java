package org.sourcegrade.jagr.common.executor;

import org.sourcegrade.jagr.api.executor.ExecutionContext;
import org.sourcegrade.jagr.api.executor.ExecutionContextVerifier;

public class NoRecursiveAPIWrapper {
  public static void banRecursion(Runnable r) {
    ExecutionContext.runWithVerifiers(r, ExecutionContextVerifier.ensureNotRecursive());
  }
}
