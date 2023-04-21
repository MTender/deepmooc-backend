package ee.deepmooc.model

import org.komapper.annotation.KomapperAutoIncrement
import org.komapper.annotation.KomapperEntity
import org.komapper.annotation.KomapperId
import org.komapper.annotation.KomapperTable

@KomapperEntity(aliases = ["groups"])
@KomapperTable(name = "groups")
data class GroupEntity(
    @KomapperId
    @KomapperAutoIncrement
    val id: Long = 0,

    val name: String,

    val courseId: Long
)