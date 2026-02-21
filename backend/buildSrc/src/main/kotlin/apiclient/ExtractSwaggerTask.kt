package apiclient

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.net.URI

abstract class ExtractSwaggerTask : DefaultTask() {

    @get:OutputFile
    val swaggerFile: File = project.file("swagger.json")

    private val serverUrl = "http://localhost:8080"
    private val apiDocsPath = "/v3/api-docs"

    init {
        group = "api-client"
        description = "Start backend server, extract swagger.json, then stop"
    }

    @TaskAction
    fun extract() {
        val bootRun = ProcessBuilder("${project.projectDir}/gradlew", "bootRun", "-q")
            .directory(project.projectDir)
            .redirectErrorStream(true)
            .start()

        try {
            waitForServer()
            val json = URI("$serverUrl$apiDocsPath").toURL().readText()
            swaggerFile.writeText(json)
            logger.lifecycle("swagger.json extracted (${swaggerFile.length()} bytes)")
        } finally {
            bootRun.destroyForcibly()
            try {
                ProcessBuilder("bash", "-c", "lsof -ti:8080 | xargs kill -9 2>/dev/null || true")
                    .start().waitFor()
            } catch (_: Exception) {}
            Thread.sleep(2000)
        }
    }

    private fun waitForServer() {
        repeat(30) {
            try {
                URI("$serverUrl$apiDocsPath").toURL().readText()
                return
            } catch (_: Exception) {
                Thread.sleep(2000)
            }
        }
        throw RuntimeException("Server did not start within 60 seconds")
    }
}
