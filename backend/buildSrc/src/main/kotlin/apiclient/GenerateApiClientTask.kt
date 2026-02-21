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
    val templatesDir: File = project.file("openapi-templates")

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

        File(outputDir, "package.json").writeText(
            """{
  "name": "@ject-2-test/backend-api-client",
  "version": "$version",
  "description": "Backend API client generated from OpenAPI spec",
  "main": "dist/index.js",
  "types": "dist/index.d.ts",
  "files": ["dist"],
  "scripts": {
    "build": "tsc",
    "prepublishOnly": "npm run build"
  },
  "dependencies": {
    "axios": "^1.7.0"
  },
  "devDependencies": {
    "typescript": "^5.5.0"
  },
  "license": "MIT"
}
"""
        )

        File(outputDir, "tsconfig.json").writeText(
            """{
  "compilerOptions": {
    "target": "ES2020",
    "module": "commonjs",
    "lib": ["ES2020", "DOM"],
    "declaration": true,
    "strict": true,
    "noImplicitAny": true,
    "strictNullChecks": true,
    "moduleResolution": "node",
    "esModuleInterop": true,
    "skipLibCheck": true,
    "forceConsistentCasingInFileNames": true,
    "outDir": "./dist",
    "rootDir": "."
  },
  "include": ["*.ts", "api/**/*.ts", "models/**/*.ts"],
  "exclude": ["node_modules", "dist"]
}
"""
        )

        logger.lifecycle("API client generated at ${outputDir.absolutePath} (version: $version)")
    }
}
