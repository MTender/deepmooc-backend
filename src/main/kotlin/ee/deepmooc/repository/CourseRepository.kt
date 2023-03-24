package ee.deepmooc.repository

import ee.deepmooc.model.CourseEntity
import ee.deepmooc.model.CourseRegistrationEntity
import ee.deepmooc.model.courseRegistrations
import ee.deepmooc.model.courses
import ee.deepmooc.model.groups
import ee.deepmooc.repository.util.RepositoryUtils.Companion.C_JOIN_CR
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.query.EntityStore
import org.komapper.core.dsl.query.single
import org.komapper.jdbc.JdbcDatabase
import javax.inject.Inject

class CourseRepository @Inject constructor(
    private val db: JdbcDatabase
) {

    private val cr = Meta.courseRegistrations
    private val c = Meta.courses
    private val g = Meta.groups

    fun fetchCourseRegistrationsOfUser(userId: Long): Map<CourseRegistrationEntity, CourseEntity?> {
        val store: EntityStore = db.runQuery(
            QueryDsl.from(cr)
                .innerJoin(c, C_JOIN_CR)
                .where { cr.userId eq userId }
                .includeAll()
        )

        return store.oneToOne(cr, c)
    }

    fun fetchCourseByCode(courseCode: String): CourseEntity {
        return db.runQuery(
            QueryDsl.from(c)
                .where { c.code eq courseCode }
                .single()
        )
    }

    fun fetchByGroupId(groupId: Long): CourseEntity {
        return db.runQuery(
            QueryDsl.from(c)
                .leftJoin(g) { g.courseId eq c.id }
                .where { g.id eq groupId }
                .single()
        )
    }
}