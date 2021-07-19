package org.sourcegrade.jagr.common.compiler.java.handles

import org.sourcegrade.jagr.common.Config

class MethodEndHandle(override val position: Int, override val config: Config) : SourceHandle {
  override fun process(sb: StringBuilder) {
    if (config.instrumentations.timeoutSourcecode.enabled || config.instrumentations.notRecursiveSourcecode.enabled) {
      sb.insert(position, "}")
    }
  }
}
