package org.sourcegrade.jagr.common.compiler.java

import org.sourcegrade.jagr.common.Config
import org.sourcegrade.jagr.common.compiler.java.handles.MethodEndHandle
import org.sourcegrade.jagr.common.compiler.java.handles.MethodStartHandle
import org.sourcegrade.jagr.common.compiler.java.handles.SourceHandle
import spoon.processing.AbstractProcessor
import spoon.reflect.declaration.CtMethod

class MethodProcessor(private val handles: MutableCollection<SourceHandle>, val config: Config) : AbstractProcessor<CtMethod<*>>() {

  override fun process(element: CtMethod<*>) {
    element.body?.position?.let {
      handles += MethodEndHandle(it.sourceEnd +1, config)
      handles += MethodStartHandle(it.sourceStart, config)
    }
  }
}
