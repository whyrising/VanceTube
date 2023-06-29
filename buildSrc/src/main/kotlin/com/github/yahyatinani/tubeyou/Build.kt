package com.github.yahyatinani.tubeyou

import java.io.File
import java.io.FileOutputStream
import java.util.Base64

object Build {
  const val APP_ID = "com.github.yahyatinani.tubeyou"

  const val versionMajor = 0
  const val versionMinor = 0
  const val versionPatch = 1

  fun keyStoreBase64ToStoreFile(keyStoreBase64: String?): File? {
    if (keyStoreBase64 == null) return null

    val tempKeyStoreFile = File.createTempFile("tmp_ks_", ".jks")
    var fos: FileOutputStream? = null
    try {
      fos = FileOutputStream(tempKeyStoreFile)
      fos.write(Base64.getDecoder().decode(keyStoreBase64))
      fos.flush()
    } finally {
      fos?.close()
    }

    return tempKeyStoreFile
  }

  object Versions {
    const val COMPOSE_COMPILER = "1.4.7"
    const val KOTLIN = "1.8"
    const val JVM = "11"
  }
}
