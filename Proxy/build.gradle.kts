plugins {
	id("org.jetbrains.kotlin.jvm")
}

repositories {
	mavenCentral()

	maven("https://repo.papermc.io/repository/maven-public/")
	maven("https://repo.aikar.co/content/groups/aikar/")
}

dependencies {
	compileOnly("com.velocitypowered:velocity-api:3.1.2-SNAPSHOT")

	implementation(project(":Common"))

	implementation("co.aikar:acf-velocity:0.5.1-SNAPSHOT")

	implementation("net.dv8tion:JDA:5.0.0-alpha.18") {
		exclude("opus-java")
	}
}