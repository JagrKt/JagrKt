package jagrinternal.instrumentation;

import org.opentest4j.AssertionFailedError;

import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings("unused")
public class LoopHandler {
  public static final ThreadLocal<AtomicBoolean> NO_LOOPS_MODE = ThreadLocal.withInitial(AtomicBoolean::new);

  public static void willEnterLoop() {
    if (NO_LOOPS_MODE.get().get()) throw new AssertionFailedError("Iterative invocation detected");
  }
}
