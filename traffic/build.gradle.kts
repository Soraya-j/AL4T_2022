plugins {
    application
}

group = "be.ecam"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}
application {
     mainClassName = "trafficsim.Simulation"
}
tasks.test {
    useJUnitPlatform()
}