package org.openedx.core.domain.model

data class CoursePreview(
    val id: String,
    val name: String,
    val org: String,
    val imageUrl: String,
    val shortDescription: String,
)
