package ee.deepmooc

import ee.deepmooc.model.AccessLevel
import ee.deepmooc.model.CourseEntity
import ee.deepmooc.model.CourseRegistrationEntity
import ee.deepmooc.model.GroupEntity
import ee.deepmooc.model.GroupRegistrationEntity
import ee.deepmooc.model.UserEntity
import ee.deepmooc.model.courseRegistrations
import ee.deepmooc.model.courses
import ee.deepmooc.model.groupRegistrations
import ee.deepmooc.model.groups
import ee.deepmooc.model.users
import io.jooby.Jooby
import io.jooby.JoobyTest
import io.jooby.require
import okhttp3.OkHttpClient
import org.junit.jupiter.api.BeforeAll
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.jdbc.JdbcDatabase

@JoobyTest(App::class)
open class IntegrationTest {

    protected val testUsers: MutableList<UserEntity> = mutableListOf()
    protected val testCourses: MutableList<CourseEntity> = mutableListOf()
    protected val testGroups: MutableList<GroupEntity> = mutableListOf()
    protected val testCourseRegistrations: MutableList<CourseRegistrationEntity> = mutableListOf()

    private val u = Meta.users
    private val cr = Meta.courseRegistrations
    private val c = Meta.courses
    private val gr = Meta.groupRegistrations
    private val g = Meta.groups

    @BeforeAll
    fun generateTestData(application: Jooby) {
        val db = application.require(JdbcDatabase::class)

        db.withTransaction {
            db.runQuery(
                QueryDsl.insert(u).onDuplicateKeyIgnore(u.username)
                    .multiple(
                        UserEntity(username = "test1"),
                        UserEntity(username = "test2"),
                        UserEntity(username = "test3")
                    )
            )

            testUsers.clear()
            testUsers.addAll(
                db.runQuery(
                    QueryDsl.from(u)
                        .where {
                            u.username like "test_"
                        }
                        .orderBy(u.id)
                )
            )


            db.runQuery(
                QueryDsl.insert(c).onDuplicateKeyIgnore(c.code)
                    .multiple(
                        CourseEntity(name = "Test course I", code = "TEST.01.001"),
                        CourseEntity(name = "Test course II", code = "TEST.01.002")
                    )
            )

            testCourses.clear()
            testCourses.addAll(
                db.runQuery(
                    QueryDsl.from(c)
                        .where {
                            c.code startsWith "TEST."
                        }
                        .orderBy(c.id)
                )
            )

            db.runQuery(
                QueryDsl.insert(g).onDuplicateKeyIgnore(g.name, g.courseId)
                    .multiple(
                        GroupEntity(name = "test_I_1", courseId = testCourses[0].id),
                        GroupEntity(name = "test_I_2", courseId = testCourses[0].id),
                        GroupEntity(name = "test_I_3", courseId = testCourses[0].id)
                    )
            )

            testGroups.clear()
            testGroups.addAll(
                db.runQuery(
                    QueryDsl.from(g)
                        .where {
                            g.name startsWith "test_"
                        }
                        .orderBy(g.id)
                )
            )

            db.runQuery(
                QueryDsl.insert(cr).onDuplicateKeyIgnore(cr.userId, cr.courseId)
                    .multiple(
                        CourseRegistrationEntity(
                            userId = testUsers[0].id,
                            courseId = testCourses[0].id,
                            accessLevel = AccessLevel.TEACHER
                        ),
                        CourseRegistrationEntity(
                            userId = testUsers[0].id,
                            courseId = testCourses[1].id,
                            accessLevel = AccessLevel.STUDENT
                        ),
                        CourseRegistrationEntity(
                            userId = testUsers[1].id,
                            courseId = testCourses[0].id,
                            accessLevel = AccessLevel.STUDENT
                        ),
                        CourseRegistrationEntity(
                            userId = testUsers[1].id,
                            courseId = testCourses[1].id,
                            accessLevel = AccessLevel.STUDENT
                        )
                    )
            )

            testCourseRegistrations.clear()
            testCourseRegistrations.addAll(
                db.runQuery(
                    QueryDsl.from(cr)
                        .where {
                            cr.userId inList testUsers.map { it.id }
                            or {
                                cr.courseId inList testCourses.map { it.id }
                            }
                        }
                        .orderBy(cr.id)
                )
            )

            db.runQuery(
                QueryDsl.insert(gr).onDuplicateKeyIgnore(gr.groupId, gr.courseRegistrationId)
                    .multiple(
                        GroupRegistrationEntity(
                            groupId = testGroups[0].id,
                            courseRegistrationId = testCourseRegistrations[2].id
                        ),
                        GroupRegistrationEntity(
                            groupId = testGroups[2].id,
                            courseRegistrationId = testCourseRegistrations[2].id
                        )
                    )
            )
        }
    }

    companion object {
        @JvmStatic
        protected val client = OkHttpClient()
    }
}