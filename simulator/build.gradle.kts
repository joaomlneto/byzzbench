plugins {
    java
    id("org.springframework.boot") version "3.2.6"
    id("io.spring.dependency-management") version "1.1.5"
    id("org.graalvm.buildtools.native") version "0.9.28"
    id("com.vaadin") version "24.3.12"
    id("com.github.psxpaul.execfork") version "0.2.2"
    id("org.springdoc.openapi-gradle-plugin") version "1.8.0"
}

group = "byzzbench"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

extra["vaadinVersion"] = "24.3.12"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.vaadin:vaadin-spring-boot-starter")
    compileOnly("org.projectlombok:lombok")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation("org.springdoc:springdoc-openapi-ui:1.8.0")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-api:2.5.0")

    implementation("com.fasterxml.jackson.core:jackson-core:2.17.1")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.17.1")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.1")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.17.1")
    implementation("com.fasterxml.jackson.module:jackson-module-parameter-names:2.17.1")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.17.1")
}

dependencyManagement {
    imports {
        mavenBom("com.vaadin:vaadin-bom:${property("vaadinVersion")}")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
