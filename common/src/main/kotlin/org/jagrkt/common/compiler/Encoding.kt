package org.jagrkt.common.compiler

import org.mozilla.universalchardet.UniversalDetector
import java.io.InputStream
import java.nio.charset.Charset

fun InputStream.readEncoded(): String {
  val buffer: ByteArray = readAllBytes()
  val detector = UniversalDetector()
  detector.handleData(buffer)
  detector.dataEnd()
  return if (detector.detectedCharset != null) {
    String(buffer, Charset.forName(detector.detectedCharset))
  } else {
    String(buffer)
  }
}
