package ee.deepmooc.repository

import ee.deepmooc.model.*
import ee.deepmooc.repository.util.RepositoryUtils.Companion.C_JOIN_CR
import ee.deepmooc.repository.util.RepositoryUtils.Companion.C_JOIN_G
import jakarta.inject.Inject
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.query.EntityStore
import org.komapper.jdbc.JdbcDatabase

class CourseRegistrationRepository @Inject constructor(
    private val db: JdbcDatabase
) {

    private val cr = Meta.courseRegistrations
    private val c = Meta.courses
    private val g = Meta.groups

    fun fetchWithCourses(userId: Long): Map<CourseRegistrationEntity, CourseEntity> {
        val store: EntityStore = db.runQuery(
            QueryDsl.from(cr)
                .leftJoin(c, C_JOIN_CR)
                .where { cr.userId eq userId }
                .includeAll()
        )

        @Suppress("UNCHECKED_CAST")
        return store.manyToOne(cr, c) as Map<CourseRegistrationEntity, CourseEntity>
    }

    fun findByUserIdsAndCourseId(userIds: List<Long>, courseId: Long): List<CourseRegistrationEntity> {
        return db.runQuery(
            QueryDsl.from(cr)
                .where {
                    cr.courseId eq courseId
                    and {
                        cr.userId inList userIds
                    }
                }
        )
    }

    fun findByUserIdsAndGroupIdThroughCourse(userIds: List<Long>, groupId: Long): List<CourseRegistrationEntity> {
        return db.runQuery(
            QueryDsl.from(cr)
                .leftJoin(c, C_JOIN_CR)
                .leftJoin(g, C_JOIN_G)
                .where {
                    g.id eq groupId
                    and {
                        cr.userId inList userIds
                    }
                }
        )
    }

    fun upsert(courseRegistrationEntities: List<CourseRegistrationEntity>) {
        db.runQuery(
            QueryDsl.insert(cr).onDuplicateKeyUpdate(cr.userId, cr.courseId)
                .multiple(courseRegistrationEntities)
        )
    }

    fun deleteByIds(ids: List<Long>) {
        db.runQuery(
            QueryDsl.delete(cr)
                .where { cr.id inList ids }
        )
    }
}