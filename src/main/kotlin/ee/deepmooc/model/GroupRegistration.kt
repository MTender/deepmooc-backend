package ee.deepmooc.model

import org.komapper.annotation.KomapperAutoIncrement
import org.komapper.annotation.KomapperEntity
import org.komapper.annotation.KomapperId
import org.komapper.annotation.KomapperTable

@KomapperEntity(aliases = ["groupRegistrations"])
@KomapperTable(name = "group_registrations")
data class GroupRegistrationEntity(

    @KomapperId
    @KomapperAutoIncrement
    val id: Long = 0,

    val groupId: Long,

    val courseRegistrationId: Long
)