@file:Suppress("SpellCheckingInspection")

plugins {
	java
	`java-library`
	`maven-publish`

	idea

	id("com.gtnewhorizons.retrofuturagradle") version "1.4.1"
}

version = "1.0.0-snapshot"
group = "cn.elytra.mod.rl"

val modId = "remote_login"

base {
	archivesName = "remote_login"
}

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(8)
		vendor = JvmVendorSpec.ADOPTIUM
	}
	withJavadocJar()
	withSourcesJar()
}

val embed = configurations.create("embed")
configurations.implementation.get().extendsFrom(embed)

minecraft {
	mcVersion = "1.12.2"

	mcpMappingChannel = "stable"
	mcpMappingVersion = "39"

	username = "Developer"

	extraRunJvmArguments += "-ea:${group}"
	extraRunJvmArguments += "-Dmixin.hotSwap=true"
	extraRunJvmArguments += "-Dmixin.checks.interfaces=true"
	extraRunJvmArguments += "-Dmixin.debug.export=true"
}

repositories {
	mavenCentral()
	maven {
		name = "Cleanroom MC"
		url = uri("https://maven.cleanroommc.com")
	}
	maven {
		name = "GTCEu"
		url = uri("https://maven.gtceu.com")
	}
	maven {
		name = "BlameJared Maven"
		url = uri("https://maven.blamejared.com")
	}
	maven {
		name = "CurseMaven"
		url = uri("https://cursemaven.com")
	}
}

dependencies {
	embed(project(":common", configuration = "default"))

	implementation("appeng:ae2-uel:v0.56.7:dev") { isTransitive = false }
	implementation("mezz:jei:4.27.3") { isTransitive = false }

	implementation(rfg.deobf(files("libs/ItemRenderDark-0.3.1.jar")))

}

tasks.processResources {

}

tasks.jar {
	manifest {
	}
}

tasks.test {
	useJUnitPlatform()
}

operator fun <T> ListProperty<T>.plusAssign(value: T) {
	this.set(this.get() + value)
}