package ee.deepmooc.model

import org.komapper.annotation.KomapperAutoIncrement
import org.komapper.annotation.KomapperEntity
import org.komapper.annotation.KomapperId
import org.komapper.annotation.KomapperTable

@KomapperEntity(aliases = ["users"])
@KomapperTable(name = "users")
data class UserEntity(
    @KomapperId
    @KomapperAutoIncrement
    val id: Long = 0,

    val username: String
)