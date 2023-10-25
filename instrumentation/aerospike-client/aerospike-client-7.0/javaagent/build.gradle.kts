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

//  testInstrumentation(project(":instrumentation:jedis:jedis-1.4:javaagent"))
//  testInstrumentation(project(":instrumentation:jedis:jedis-4.0:javaagent"))

//  latestDepTestLibrary("redis.clients:jedis:3.+") // see jedis-4.0 module
}

tasks {
  test {
    jvmArgs("-Djava.net.preferIPv4Stack=true")
    usesService(gradle.sharedServices.registrations["testcontainersBuildService"].service)
  }
}
