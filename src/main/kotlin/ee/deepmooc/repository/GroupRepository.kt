package ee.deepmooc.repository

import ee.deepmooc.model.GroupEntity
import ee.deepmooc.model.groups
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.query.singleOrNull
import org.komapper.jdbc.JdbcDatabase
import javax.inject.Inject

class GroupRepository @Inject constructor(
    private val db: JdbcDatabase
) {

    private val g = Meta.groups

    fun findByIdAndCourseCode(groupId: Long, courseId: Long): GroupEntity? {
        return db.runQuery(
            QueryDsl.from(g)
                .where {
                    g.id eq groupId
                    and {
                        g.courseId eq courseId
                    }
                }
                .singleOrNull()
        )
    }
}