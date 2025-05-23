plugins {
    java
    id("org.springframework.boot") version "3.4.5"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.graalvm.buildtools.native") version "0.10.6"
    //id("com.vaadin") version "24.4.7"
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

//extra["vaadinVersion"] = "24.6.4"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    runtimeOnly("com.h2database:h2")
    implementation("org.xerial:sqlite-jdbc:3.49.1.0")

    // sqlite dialect
    //implementation("org.hibernate:hibernate-community-dialects:5.6.3.Final")

    //implementation("org.springdoc:springdoc-openapi-ui:1.8.0")
    // either API (just documentation) or API + UI (documentation + Swagger UI)
    //implementation("org.springdoc:springdoc-openapi-starter-webmvc-api:2.5.0")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.8")
    //implementation("jakarta.xml.bind:jakarta.xml.bind-api:4.0.2") // required for webmvc-ui
    //compileOnly("javax.servlet:javax.servlet-api:4.0.1")

    implementation("com.fasterxml.jackson.core:jackson-core:2.19.0")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.19.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.19.0")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.19.0")
    implementation("com.fasterxml.jackson.module:jackson-module-parameter-names:2.19.0")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.19.0")
}

/*dependencyManagement {
    imports {
        mavenBom("com.vaadin:vaadin-bom:${property("vaadinVersion")}")
    }
}*/

tasks.withType<Test> {
    useJUnitPlatform()
}

openApi {
    apiDocsUrl.set("http://localhost:8080/spec")
}

// fix for https://github.com/springdoc/springdoc-openapi-gradle-plugin/issues/102
tasks.forkedSpringBootRun {
    dependsOn("compileAotJava")
    dependsOn("processAotResources")
    doNotTrackState("See https://github.com/springdoc/springdoc-openapi-gradle-plugin/issues/102")
}
