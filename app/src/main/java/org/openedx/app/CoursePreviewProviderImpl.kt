package org.openedx.app

import org.openedx.core.config.Config
import org.openedx.core.domain.interactor.CoursePreviewProvider
import org.openedx.core.domain.model.CoursePreview
import org.openedx.discovery.domain.interactor.DiscoveryInteractor
import org.openedx.foundation.extension.toImageLink

class CoursePreviewProviderImpl(
    private val discoveryInteractor: DiscoveryInteractor,
    private val config: Config,
) : CoursePreviewProvider {

    override suspend fun getPreviews(): List<CoursePreview> {
        val apiHostUrl = config.getApiHostURL()
        return try {
            val courseList = discoveryInteractor.getCoursesList(
                username = null,
                organization = null,
                pageNumber = 1,
            )
            courseList.results.map { course ->
                CoursePreview(
                    id = course.id,
                    name = course.name,
                    org = course.org,
                    imageUrl = course.media.bannerImage?.uriAbsolute
                        ?.takeIf { it.isNotEmpty() }?.toImageLink(apiHostUrl)
                        ?: course.media.image?.large
                        ?.takeIf { it.isNotEmpty() }?.toImageLink(apiHostUrl)
                        ?: course.media.courseImage?.uri
                        ?.takeIf { it.isNotEmpty() }?.toImageLink(apiHostUrl)
                        ?: "",
                    shortDescription = course.shortDescription,
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
