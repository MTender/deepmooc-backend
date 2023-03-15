package ee.deepmooc.repository

import ee.deepmooc.model.AccessLevel
import ee.deepmooc.model.CourseEntity
import ee.deepmooc.model.CourseRegistrationEntity
import ee.deepmooc.model.UserEntity
import ee.deepmooc.model.courseRegistrations
import ee.deepmooc.model.courses
import ee.deepmooc.model.users
import ee.deepmooc.repository.util.RepositoryUtils.Companion.C_JOIN_CR
import ee.deepmooc.repository.util.RepositoryUtils.Companion.U_JOIN_CR
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.jdbc.JdbcDatabase
import javax.inject.Inject

class CourseRepository @Inject constructor(
    private val db: JdbcDatabase
) {

    private val u = Meta.users
    private val cr = Meta.courseRegistrations
    private val c = Meta.courses

    fun fetchUsersByCourseCode(courseCode: String): Pair<CourseEntity?, Map<CourseRegistrationEntity, UserEntity?>> {
        val store = db.runQuery(
            QueryDsl.from(c)
                .innerJoin(cr, C_JOIN_CR)
                .innerJoin(u, U_JOIN_CR)
                .where { c.code eq courseCode }
                .includeAll()
        )
        return Pair(store[c].singleOrNull(), store.manyToOne(cr, u))
    }

    fun fetchStudentsByCourseCode(courseCode: String): Pair<CourseEntity?, Map<CourseRegistrationEntity, UserEntity?>> {
        val store = db.runQuery(
            QueryDsl.from(c)
                .innerJoin(cr, C_JOIN_CR)
                .innerJoin(u, U_JOIN_CR)
                .where {
                    c.code eq courseCode
                    cr.accessLevel eq AccessLevel.STUDENT
                }
                .includeAll()
        )
        return Pair(store[c].singleOrNull(), store.manyToOne(cr, u))
    }
}