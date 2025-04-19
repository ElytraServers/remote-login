plugins {
	java
	kotlin("jvm") version "2.1.20"
}

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(8)
	}
}

repositories {
	mavenCentral()
	maven {
		name = "GTCEu"
		url = uri("https://maven.gtceu.com/")
	}
}

dependencies {
	implementation(platform("io.ktor:ktor-bom:3.1.2"))
	implementation("io.ktor:ktor-server-core")
	implementation("io.ktor:ktor-server-cio")
	implementation("io.ktor:ktor-server-content-negotiation")
	implementation("io.ktor:ktor-serialization-gson")
	implementation("io.ktor:ktor-server-auth")
	implementation("io.ktor:ktor-server-status-pages")
	implementation("io.ktor:ktor-server-cors")

	implementation("org.jetbrains:annotations:24.0.0")
	implementation("io.ktor:ktor-serialization-jackson:3.1.2")
	implementation("io.ktor:ktor-server-auth-jvm:3.1.2")
	implementation("io.ktor:ktor-server-core-jvm:3.1.2")
	implementation("io.ktor:ktor-server-host-common-jvm:3.1.2")
	implementation("io.ktor:ktor-server-status-pages-jvm:3.1.2")
	implementation("io.ktor:ktor-server-cors-jvm:3.1.2")
	compileOnly("org.apache.logging.log4j:log4j-api:2.24.3")

	// provided in Minecraft
	compileOnly("com.google.guava:guava:33.4.8-jre")

	compileOnly("org.projectlombok:lombok:1.18.38")
	annotationProcessor("org.projectlombok:lombok:1.18.38")
}