package com.test.ecs.util

import java.io.File
import java.lang.reflect.Method
import java.net.{URL, URLClassLoader}

import org.slf4j.LoggerFactory


object HadoopUtils {
  private val LOG = LoggerFactory.getLogger(getClass())

  def loadHadoopConf(): Unit = {
    val hadoopConfDir = new File(EnvProperty.get(EnvProperty.HADOOP_CONF_DIR))
    if (hadoopConfDir.exists()) {
      addFile(hadoopConfDir)
      LOG.info("Loaded hadoop configuration from " + hadoopConfDir.getPath)
    }else{
      LOG.info("Could not load hadoop configuration, using local mode instead")
    }
  }

  private def addFile(filePath: String) {
    val f: File = new File(filePath);
    addFile(f)
  }

  private def addFile(f: File) {
    addURL(f.toURI().toURL());
  }

  private def addURL(u: URL) {
    val sysLoader: URLClassLoader = ClassLoader.getSystemClassLoader().asInstanceOf[URLClassLoader]
    val sysClass = classOf[URLClassLoader];
    val method: Method = sysClass.getDeclaredMethod("addURL", classOf[URL]);
    method.setAccessible(true)
    method.invoke(sysLoader, u)
  }
}
