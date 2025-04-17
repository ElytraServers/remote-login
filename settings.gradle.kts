@file:Suppress("SpellCheckingInspection")

rootProject.name = "remote-login"

pluginManagement {
	repositories {
		maven {
			name = "GTNH Maven"
			url = uri("https://nexus.gtnewhorizons.com/repository/public/")
			mavenContent {
				includeGroup("com.gtnewhorizons")
				includeGroup("com.gtnewhorizons.retrofuturagradle")
			}
		}
		gradlePluginPortal()
		mavenCentral()
		mavenLocal()
	}
}

include(":common")
include(":ae2uel")