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

    fun save(groupRegistrationEntities: List<GroupRegistrationEntity>) {
        db.runQuery(
            QueryDsl.insert(gr).onDuplicateKeyIgnore()
                .multiple(groupRegistrationEntities)
        )
    }

    fun deleteByGroupIdAndCourseRegistrationIds(groupId: Long, courseRegistrationIds: List<Long>) {
        db.runQuery(
            QueryDsl.delete(gr)
                .where {
                    gr.groupId eq groupId
                    and {
                        gr.courseRegistrationId inList courseRegistrationIds
                    }
                }
        )
    }

    fun deleteByCourseRegistrationIds(courseRegistrationIds: List<Long>) {
        db.runQuery(
            QueryDsl.delete(gr)
                .where {
                    gr.courseRegistrationId inList courseRegistrationIds
                }
        )
    }
}