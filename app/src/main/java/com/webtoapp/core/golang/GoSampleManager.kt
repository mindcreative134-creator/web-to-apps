package com.webtoapp.core.golang

import android.content.Context
import com.webtoapp.core.i18n.AppStringsProvider
import com.webtoapp.core.sample.SampleProjectExtractor
import com.webtoapp.core.sample.TypedSampleProject

/**
 * project.
 */
object GoSampleManager {
    
    fun getSampleProjects(context: Context): List<TypedSampleProject> {
        val baseIds = listOf("go-gin", "go-fiber", "go-echo")
        return listOf(
            TypedSampleProject(
                id = "${baseIds[0]}${SampleProjectExtractor.getLanguageSuffix(context, baseIds[0])}",
                name = AppStringsProvider.current().sampleGoGinName,
                description = AppStringsProvider.current().sampleGoGinDesc,
                frameworkName = "Gin",
                icon = "cocktail",
                tags = listOf("Gin 1.9", AppStringsProvider.current().sampleTagRest, AppStringsProvider.current().sampleTagMiddleware),
                brandColor = 0xFF0090FF
            ),
            TypedSampleProject(
                id = "${baseIds[1]}${SampleProjectExtractor.getLanguageSuffix(context, baseIds[1])}",
                name = AppStringsProvider.current().sampleGoFiberName,
                description = AppStringsProvider.current().sampleGoFiberDesc,
                frameworkName = "Fiber",
                icon = "rocket",
                tags = listOf("Fiber 2", AppStringsProvider.current().sampleTagHighPerf),
                brandColor = 0xFF7B2FBF
            ),
            TypedSampleProject(
                id = "${baseIds[2]}${SampleProjectExtractor.getLanguageSuffix(context, baseIds[2])}",
                name = AppStringsProvider.current().sampleGoEchoName,
                description = AppStringsProvider.current().sampleGoEchoDesc,
                frameworkName = "Echo",
                icon = "volume",
                tags = listOf("Echo 4", AppStringsProvider.current().sampleTagMinimalApi),
                brandColor = 0xFF00BCD4
            )
        )
    }
    
    suspend fun extractSampleProject(
        context: Context,
        projectId: String
    ): Result<String> = SampleProjectExtractor.extractSampleProject(context, projectId)
}


