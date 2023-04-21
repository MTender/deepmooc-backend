package ee.deepmooc.dto

import ee.deepmooc.model.UserEntity
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Long,
    val username: String
) {

    constructor(userEntity: UserEntity) : this(
        id = userEntity.id,
        username = userEntity.username
    )

    constructor(registeredUser: RegisteredUser) : this(
        id = registeredUser.id,
        username = registeredUser.username,
    )
}