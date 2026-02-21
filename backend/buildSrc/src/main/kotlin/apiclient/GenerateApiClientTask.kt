package apiclient

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class GenerateApiClientTask : DefaultTask() {

    @get:InputFile
    val swaggerFile: File = project.file("swagger.json")

    @get:InputDirectory
    val templatesDir: File = project.file("api-client-templates")

    @get:OutputDirectory
    val outputDir: File = project.file("generated-api-client")

    init {
        group = "api-client"
        description = "Generate TypeScript Axios API client from swagger.json"
    }

    @TaskAction
    fun generate() {
        if (outputDir.exists()) outputDir.deleteRecursively()

        val result = ProcessBuilder(
            "npx", "@openapitools/openapi-generator-cli", "generate",
            "-i", swaggerFile.absolutePath,
            "-g", "typescript-axios",
            "-o", outputDir.absolutePath,
            "-t", templatesDir.absolutePath,
            "--additional-properties=supportsES6=true,withSeparateModelsAndApi=true,apiPackage=api,modelPackage=models"
        )
            .directory(project.projectDir)
            .redirectErrorStream(true)
            .start()

        result.inputStream.bufferedReader().forEachLine { logger.lifecycle(it) }
        val exitCode = result.waitFor()
        if (exitCode != 0) throw RuntimeException("openapi-generator failed with exit code $exitCode")

        val version = project.version.toString().removeSuffix("-SNAPSHOT")

        // Copy template files with version substitution
        File(templatesDir, "package.json").readText()
            .replace("\"version\": \"0.0.0\"", "\"version\": \"$version\"")
            .let { File(outputDir, "package.json").writeText(it) }

        File(templatesDir, "tsconfig.json").copyTo(File(outputDir, "tsconfig.json"), overwrite = true)

        logger.lifecycle("API client generated at ${outputDir.absolutePath} (version: $version)")
    }
}
