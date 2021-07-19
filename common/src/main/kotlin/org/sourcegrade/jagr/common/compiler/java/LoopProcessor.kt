package org.sourcegrade.jagr.common.compiler.java

import org.sourcegrade.jagr.common.Config
import org.sourcegrade.jagr.common.compiler.java.handles.*
import spoon.processing.AbstractProcessor
import spoon.reflect.code.CtLoop

class LoopProcessor(private val handles: MutableCollection<SourceHandle>, val config: Config) : AbstractProcessor<CtLoop>() {

  override fun process(element: CtLoop) {
    element.position.let {
      handles += LoopStartHandle(it.sourceStart, config)
      handles += LoopEndHandle(it.sourceEnd + 1, config)
    }
    if (element.body == null) {
      handles += LoopBodyEndHandle(element.position.sourceEnd, config)
      handles += LoopBodyStartHandle(element.position.sourceEnd, config)
    } else {
      element.body.position.let {
        handles += LoopBodyStartHandle(it.sourceStart, config)
        handles += LoopBodyEndHandle(it.sourceEnd + 1, config)
      }
    }
  }
}
