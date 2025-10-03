plugins {
    id("io.micronaut.application") version "4.5.4"
    id("com.gradleup.shadow") version "8.3.7"
}

version = "0.1"
group = "com.piandphi"

repositories {
    mavenCentral()
}

dependencies {
    // Annotation processing
    annotationProcessor("io.micronaut.serde:micronaut-serde-processor")
    
    // Micronaut core
    implementation("io.micronaut:micronaut-runtime")
    implementation("io.micronaut:micronaut-discovery-core")
    
    // HTTP client/server
    implementation("io.micronaut:micronaut-http-client")
    implementation("io.micronaut:micronaut-http-server-netty")
    
    // Serialization
    implementation("io.micronaut.serde:micronaut-serde-jackson")
    
    // Jackson for JSON parsing
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
    implementation("com.fasterxml.jackson.core:jackson-core:2.15.2")
    
    // Jakarta annotations
    implementation("jakarta.annotation:jakarta.annotation-api")
    
    // Logging
    runtimeOnly("ch.qos.logback:logback-classic")
    runtimeOnly("org.yaml:snakeyaml")
    
    
    implementation("org.jsoup:jsoup:1.17.2")
    
    compileOnly("javax.annotation:javax.annotation-api")
    
    // Caffeine cache
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")
    
    // Resilience4j core modules
    implementation("io.github.resilience4j:resilience4j-core:2.3.0")
    implementation("io.github.resilience4j:resilience4j-circuitbreaker:2.3.0")
    implementation("io.github.resilience4j:resilience4j-retry:2.3.0")
    implementation("io.github.resilience4j:resilience4j-ratelimiter:2.3.0")
//    implementation("io.github.resilience4j:resilience4j-decorators:2.2.0") // ✅ Required for Decorators
    
    implementation("com.microsoft.playwright:playwright:1.44.0")
}



application {
    mainClass = "com.piandphi.Application"
}
java {
    sourceCompatibility = JavaVersion.toVersion("21")
    targetCompatibility = JavaVersion.toVersion("21")
}

micronaut {
    testRuntime("junit5")
    processing {
        incremental(true)
        annotations("com.piandphi.*")
    }
}


tasks.named<io.micronaut.gradle.docker.NativeImageDockerfile>("dockerfileNative") {
    jdkVersion = "21"
}
