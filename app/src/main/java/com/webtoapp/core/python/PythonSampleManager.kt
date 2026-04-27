package com.webtoapp.core.python

import android.content.Context
import com.webtoapp.core.i18n.AppStringsProvider
import com.webtoapp.core.sample.SampleProjectExtractor
import com.webtoapp.core.sample.TypedSampleProject

/**
 * project.
 */
object PythonSampleManager {
    
    fun getSampleProjects(context: Context): List<TypedSampleProject> {
        val baseIds = listOf("python-flask", "python-fastapi", "python-django")
        return listOf(
            TypedSampleProject(
                id = "${baseIds[0]}${SampleProjectExtractor.getLanguageSuffix(context, baseIds[0])}",
                name = AppStringsProvider.current().samplePythonFlaskName,
                description = AppStringsProvider.current().samplePythonFlaskDesc,
                frameworkName = "Flask",
                icon = "science",
                tags = listOf("Flask 3", AppStringsProvider.current().sampleTagWsgi, "Jinja2"),
                brandColor = 0xFF333333
            ),
            TypedSampleProject(
                id = "${baseIds[1]}${SampleProjectExtractor.getLanguageSuffix(context, baseIds[1])}",
                name = AppStringsProvider.current().samplePythonFastapiName,
                description = AppStringsProvider.current().samplePythonFastapiDesc,
                frameworkName = "FastAPI",
                icon = "bolt",
                tags = listOf("FastAPI", AppStringsProvider.current().sampleTagAsgi, AppStringsProvider.current().sampleTagOpenapi),
                brandColor = 0xFF009688
            ),
            TypedSampleProject(
                id = "${baseIds[2]}${SampleProjectExtractor.getLanguageSuffix(context, baseIds[2])}",
                name = AppStringsProvider.current().samplePythonDjangoName,
                description = AppStringsProvider.current().samplePythonDjangoDesc,
                frameworkName = "Django",
                icon = "eco",
                tags = listOf("Django 5", AppStringsProvider.current().sampleTagOrm, AppStringsProvider.current().sampleTagAdmin),
                brandColor = 0xFF092E20
            )
        )
    }
    
    suspend fun extractSampleProject(
        context: Context,
        projectId: String
    ): Result<String> = SampleProjectExtractor.extractSampleProject(context, projectId)
}


