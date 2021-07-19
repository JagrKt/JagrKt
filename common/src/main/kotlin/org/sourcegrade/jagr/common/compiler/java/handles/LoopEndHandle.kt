package org.sourcegrade.jagr.common.compiler.java.handles

import org.sourcegrade.jagr.common.Config


class LoopEndHandle(override val position: Int, override val config: Config) : SourceHandle {
  override fun process(sb: StringBuilder) {
    if (config.instrumentations.notIterativeSourcecode.enabled) {
      sb.insert(position, "}")
    }
  }
}
