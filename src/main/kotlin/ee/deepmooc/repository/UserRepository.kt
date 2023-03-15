package ee.deepmooc.repository

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
import org.komapper.core.dsl.query.EntityStore
import org.komapper.jdbc.JdbcDatabase
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val db: JdbcDatabase
) {

    private val u = Meta.users
    private val cr = Meta.courseRegistrations
    private val c = Meta.courses

    fun fetchCoursesByUsername(
        username: String
    ): Pair<UserEntity?, Map<CourseRegistrationEntity, CourseEntity?>> {
        val store: EntityStore = db.runQuery(
            QueryDsl.from(u)
                .innerJoin(cr, U_JOIN_CR)
                .innerJoin(c, C_JOIN_CR)
                .where { u.username eq username }
                .includeAll()
        )

        return Pair(store[u].singleOrNull(), store.manyToOne(cr, c))
    }
}