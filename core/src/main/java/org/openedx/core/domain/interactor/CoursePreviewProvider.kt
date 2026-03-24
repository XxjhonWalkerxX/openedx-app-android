package org.openedx.core.domain.interactor

import org.openedx.core.domain.model.CoursePreview

interface CoursePreviewProvider {
    suspend fun getPreviews(): List<CoursePreview>
}
