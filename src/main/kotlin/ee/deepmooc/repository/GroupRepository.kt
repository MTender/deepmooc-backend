package ee.deepmooc.repository

import ee.deepmooc.model.*
import ee.deepmooc.repository.util.RepositoryUtils.Companion.CR_JOIN_GR
import ee.deepmooc.repository.util.RepositoryUtils.Companion.G_JOIN_GR
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.query.singleOrNull
import org.komapper.jdbc.JdbcDatabase
import javax.inject.Inject

class GroupRepository @Inject constructor(
    private val db: JdbcDatabase
) {

    private val cr = Meta.courseRegistrations
    private val gr = Meta.groupRegistrations
    private val g = Meta.groups

    fun findByIdAndCourseId(groupId: Long, courseId: Long): GroupEntity? {
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

    fun findByUserIdAndCourseId(userId: Long, courseId: Long): Set<GroupEntity> {
        val store = db.runQuery(
            QueryDsl.from(g)
                .leftJoin(gr, G_JOIN_GR)
                .leftJoin(cr, CR_JOIN_GR)
                .where {
                    g.courseId eq courseId
                    and {
                        cr.userId eq userId
                    }
                }
                .include(g)
        )

        return store[g]
    }
}