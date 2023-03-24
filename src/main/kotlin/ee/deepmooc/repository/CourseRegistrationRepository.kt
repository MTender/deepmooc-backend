package ee.deepmooc.repository

import ee.deepmooc.model.CourseEntity
import ee.deepmooc.model.CourseRegistrationEntity
import ee.deepmooc.model.GroupEntity
import ee.deepmooc.model.courseRegistrations
import ee.deepmooc.model.courses
import ee.deepmooc.model.groupRegistrations
import ee.deepmooc.model.groups
import ee.deepmooc.repository.util.RepositoryUtils.Companion.CR_JOIN_GR
import ee.deepmooc.repository.util.RepositoryUtils.Companion.C_JOIN_CR
import ee.deepmooc.repository.util.RepositoryUtils.Companion.G_JOIN_GR
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.query.EntityStore
import org.komapper.core.dsl.query.single
import org.komapper.jdbc.JdbcDatabase
import javax.inject.Inject

class CourseRegistrationRepository @Inject constructor(
    private val db: JdbcDatabase
) {

    private val cr = Meta.courseRegistrations
    private val c = Meta.courses
    private val gr = Meta.groupRegistrations
    private val g = Meta.groups

    fun fetchWithCoursesAndGroupsByUserId(userId: Long): Map<CourseRegistrationEntity, Pair<CourseEntity, Set<GroupEntity>>> {
        val store: EntityStore = db.runQuery(
            QueryDsl.from(cr)
                .leftJoin(c, C_JOIN_CR)
                .leftJoin(gr, CR_JOIN_GR)
                .leftJoin(g, G_JOIN_GR)
                .where { cr.userId eq userId }
                .include(cr, g, c)
        )

        val registrationsToGroups = store.oneToMany(cr, g)
        val registrationIdsToCourses = store.oneToOneById(cr, c)

        val combinedMap: MutableMap<CourseRegistrationEntity, Pair<CourseEntity, Set<GroupEntity>>> = mutableMapOf()
        registrationsToGroups.forEach { entry ->
            combinedMap[entry.key] = Pair(registrationIdsToCourses[entry.key.id]!!, entry.value)
        }

        return combinedMap
    }

    fun fetchByUserIdAndCourseId(userId: Long, courseId: Long): CourseRegistrationEntity {
        return db.runQuery(
            QueryDsl.from(cr)
                .where {
                    cr.userId eq userId
                    cr.courseId eq courseId
                }
                .single()
        )
    }

    fun save(courseRegistrationEntity: CourseRegistrationEntity): CourseRegistrationEntity {
        return db.runQuery(
            QueryDsl.insert(cr)
                .single(courseRegistrationEntity)
        )
    }

    fun deleteById(id: Long) {
        db.runQuery(
            QueryDsl.delete(cr)
                .where { cr.id eq id }
        )
    }
}