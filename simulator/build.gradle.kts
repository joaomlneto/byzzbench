plugins {
    java
    id("org.springframework.boot") version "3.2.6"
    id("io.spring.dependency-management") version "1.1.6"
    id("org.graalvm.buildtools.native") version "0.10.2"
    id("com.vaadin") version "24.4.7"
    id("com.github.psxpaul.execfork") version "0.2.2"
    id("org.springdoc.openapi-gradle-plugin") version "1.9.0"
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

extra["vaadinVersion"] = "24.4.7"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.vaadin:vaadin-spring-boot-starter")
    compileOnly("org.projectlombok:lombok")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    //implementation("org.springdoc:springdoc-openapi-ui:1.8.0")
    // either API (just documentation) or API + UI (documentation + Swagger UI)
    //implementation("org.springdoc:springdoc-openapi-starter-webmvc-api:2.5.0")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")
    //implementation("jakarta.xml.bind:jakarta.xml.bind-api:4.0.2") // required for webmvc-ui
    //compileOnly("javax.servlet:javax.servlet-api:4.0.1")

    implementation("com.fasterxml.jackson.core:jackson-core:2.17.2")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.17.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.17.2")
    implementation("com.fasterxml.jackson.module:jackson-module-parameter-names:2.17.2")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.17.2")
}

dependencyManagement {
    imports {
        mavenBom("com.vaadin:vaadin-bom:${property("vaadinVersion")}")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
