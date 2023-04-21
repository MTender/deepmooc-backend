package ee.deepmooc.repository

import ee.deepmooc.model.*
import ee.deepmooc.repository.util.RepositoryUtils.Companion.CR_JOIN_GR
import ee.deepmooc.repository.util.RepositoryUtils.Companion.C_JOIN_CR
import ee.deepmooc.repository.util.RepositoryUtils.Companion.U_JOIN_CR
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.query.single
import org.komapper.jdbc.JdbcDatabase
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val db: JdbcDatabase
) {

    private val u = Meta.users
    private val cr = Meta.courseRegistrations
    private val c = Meta.courses
    private val gr = Meta.groupRegistrations

    fun fetchByUsername(username: String): UserEntity {
        return db.runQuery(
            QueryDsl.from(u)
                .where { u.username eq username }
                .single()
        )
    }

    fun fetchUsersRegisteredToCourse(courseId: Long): Map<CourseRegistrationEntity, UserEntity> {
        val store = db.runQuery(
            QueryDsl.from(c)
                .leftJoin(cr, C_JOIN_CR)
                .leftJoin(u, U_JOIN_CR)
                .where { c.id eq courseId }
                .includeAll()
        )

        if (store[c].isEmpty()) throw NoSuchElementException("No such course")

        @Suppress("UNCHECKED_CAST")
        return store.oneToOne(cr, u) as Map<CourseRegistrationEntity, UserEntity>
    }

    fun findUsersInGroup(groupId: Long): Set<UserEntity> {
        val store = db.runQuery(
            QueryDsl.from(gr)
                .leftJoin(cr, CR_JOIN_GR)
                .leftJoin(u, U_JOIN_CR)
                .where { gr.groupId eq groupId }
                .include(u)
        )

        return store[u]
    }

    fun save(userEntity: UserEntity): UserEntity {
        return db.runQuery(
            QueryDsl.insert(u)
                .single(userEntity)
        )
    }
}