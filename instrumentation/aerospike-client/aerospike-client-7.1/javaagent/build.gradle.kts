plugins {
  id("otel.javaagent-instrumentation")
}

muzzle {
  pass {
    group.set("com.aerospike")
    module.set("aerospike-client")
    versions.set("[7.1.0,)")
    assertInverse.set(true)
  }
}

dependencies {
  library("com.aerospike:aerospike-client:7.1.0")

  compileOnly("com.google.auto.value:auto-value-annotations")
  annotationProcessor("com.google.auto.value:auto-value")
  testInstrumentation(project(":instrumentation:aerospike-client:aerospike-client-7.1:javaagent"))
}

tasks {
  test {
    jvmArgs("-Djava.net.preferIPv4Stack=true")
    usesService(gradle.sharedServices.registrations["testcontainersBuildService"].service)
  }
}
