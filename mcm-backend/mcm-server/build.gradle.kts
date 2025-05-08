plugins {
    alias(libs.plugins.mcm.java.conventions)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.lombok)
}

dependencies {
    implementation(project(":mcm-core"))
    implementation(libs.mapstruct)
    annotationProcessor(libs.mapstruct.processor)
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.actuator)
    implementation("org.springframework.boot:spring-boot-starter-security")
    testImplementation(libs.spring.boot.starter.test)
    implementation(libs.gson)
    testImplementation(libs.apache.commons.lang3)
    testImplementation("org.springframework.security:spring-security-test")
    implementation(libs.datafaker)
    implementation(libs.spring.boot.starter.data.neo4j)
    testImplementation(libs.spring.boot.starter.test)

    testImplementation(libs.neo4j.harness) {
        exclude(group = "org.neo4j", module = "neo4j-slf4j-provider")
    }
}

tasks.test {
    useJUnitPlatform()
}