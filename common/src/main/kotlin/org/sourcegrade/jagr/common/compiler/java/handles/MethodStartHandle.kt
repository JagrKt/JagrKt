package org.sourcegrade.jagr.common.compiler.java.handles

import org.sourcegrade.jagr.common.Config
import org.sourcegrade.jagr.common.compiler.java.handles.SourceHandleStatements.notRecStmt
import org.sourcegrade.jagr.common.compiler.java.handles.SourceHandleStatements.timeoutStmt

class MethodStartHandle(override val position: Int, override val config: Config) : SourceHandle {
  override fun process(sb: StringBuilder) {
    if (config.instrumentations.timeoutSourcecode.enabled && config.instrumentations.notRecursiveSourcecode.enabled)
      sb.insert(
        position,
        "{ $timeoutStmt $notRecStmt "
      )
    else if (config.instrumentations.timeoutSourcecode.enabled) {
      sb.insert(
        position,
        "{ $timeoutStmt "
      )
    } else if (config.instrumentations.notRecursiveSourcecode.enabled) {
      sb.insert(
        position,
        "{ $notRecStmt "
      )
    }
  }
}
