package ee.deepmooc.dto

import ee.deepmooc.model.GroupEntity
import kotlinx.serialization.Serializable

@Serializable
data class Group(
    val id: Long,
    val name: String
) {

    constructor(entity: GroupEntity) : this(entity.id, entity.name)
}