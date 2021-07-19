package org.sourcegrade.jagr.common.compiler.java.handles

import org.sourcegrade.jagr.common.Config
import org.sourcegrade.jagr.common.compiler.java.handles.SourceHandleStatements.timeoutStmt

class LoopBodyStartHandle(override val position: Int, override val config: Config) : SourceHandle {
  override fun process(sb: StringBuilder) {
    if (config.instrumentations.timeoutSourcecode.enabled) {
      sb.insert(position, "{ $timeoutStmt ")
    }
  }
}
