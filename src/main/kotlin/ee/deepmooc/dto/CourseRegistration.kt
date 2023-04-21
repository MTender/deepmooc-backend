package ee.deepmooc.dto

import ee.deepmooc.model.AccessLevel
import ee.deepmooc.model.CourseEntity
import kotlinx.serialization.Serializable

@Serializable
data class CourseRegistration(
    val id: Long,
    val name: String,
    val code: String,
    val accessLevel: AccessLevel
) {

    constructor(courseEntity: CourseEntity, accessLevel: AccessLevel) : this(
        id = courseEntity.id,
        name = courseEntity.name,
        code = courseEntity.code,
        accessLevel = accessLevel
    )
}