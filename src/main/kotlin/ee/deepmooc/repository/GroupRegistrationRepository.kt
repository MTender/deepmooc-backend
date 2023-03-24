package ee.deepmooc.repository

import ee.deepmooc.model.GroupRegistrationEntity
import ee.deepmooc.model.groupRegistrations
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.jdbc.JdbcDatabase
import javax.inject.Inject

class GroupRegistrationRepository @Inject constructor(
    private val db: JdbcDatabase
) {

    private val gr = Meta.groupRegistrations

    fun save(groupRegistrationEntity: GroupRegistrationEntity): GroupRegistrationEntity {
        return save(listOf(groupRegistrationEntity))[0]
    }

    fun save(groupRegistrationEntities: List<GroupRegistrationEntity>): List<GroupRegistrationEntity> {
        return db.runQuery(
            QueryDsl.insert(gr)
                .multiple(groupRegistrationEntities)
        )
    }

    fun deleteByGroupIdAndCourseRegistrationId(groupId: Long, courseRegistrationId: Long) {
        db.runQuery(
            QueryDsl.delete(gr)
                .where {
                    gr.groupId eq groupId
                    gr.courseRegistrationId eq courseRegistrationId
                }
        )
    }

    fun deleteByCourseRegistrationId(courseRegistrationId: Long) {
        db.runQuery(
            QueryDsl.delete(gr)
                .where {
                    gr.courseRegistrationId eq courseRegistrationId
                }
        )
    }
}