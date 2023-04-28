package ee.deepmooc

import ee.deepmooc.model.*
import io.jooby.Jooby
import io.jooby.JoobyTest
import io.jooby.require
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import org.junit.jupiter.api.BeforeAll
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.jdbc.JdbcDatabase

@ExperimentalSerializationApi
@JoobyTest(App::class)
open class IntegrationTest {

    companion object {

        @JvmStatic
        protected val format = Json { explicitNulls = false }

        @JvmStatic
        protected val client = OkHttpClient()

        @JvmStatic
        protected val testUsers: MutableList<UserEntity> = mutableListOf()

        @JvmStatic
        protected val testCourses: MutableList<CourseEntity> = mutableListOf()

        @JvmStatic
        protected val testGroups: MutableList<GroupEntity> = mutableListOf()

        @JvmStatic
        protected val testCourseRegistrations: MutableList<CourseRegistrationEntity> = mutableListOf()

        @JvmStatic
        protected val testGroupRegistrations: MutableList<GroupRegistrationEntity> = mutableListOf()

        @JvmStatic
        protected val u = Meta.users

        @JvmStatic
        protected val cr = Meta.courseRegistrations

        @JvmStatic
        protected val c = Meta.courses

        @JvmStatic
        protected val gr = Meta.groupRegistrations

        @JvmStatic
        protected val g = Meta.groups

        @Volatile
        private var initialized = false

        @BeforeAll
        @JvmStatic
        fun generateTestData(application: Jooby) {
            if (initialized) return

            val db = application.require(JdbcDatabase::class)

            // users

            db.runQuery(
                QueryDsl.insert(u).onDuplicateKeyIgnore(u.username)
                    .multiple(
                        UserEntity(username = "test1"),
                        UserEntity(username = "test2"),
                        UserEntity(username = "test3")
                    )
            )

            testUsers.addAll(
                db.runQuery(
                    QueryDsl.from(u)
                        .where {
                            u.username like "test_"
                        }
                        .orderBy(u.id)
                )
            )

            // courses

            db.runQuery(
                QueryDsl.insert(c).onDuplicateKeyIgnore(c.code)
                    .multiple(
                        CourseEntity(name = "Test course I", code = "TEST.01.001"),
                        CourseEntity(name = "Test course II", code = "TEST.01.002")
                    )
            )

            testCourses.addAll(
                db.runQuery(
                    QueryDsl.from(c)
                        .where {
                            c.code startsWith "TEST."
                        }
                        .orderBy(c.id)
                )
            )

            // groups

            db.runQuery(
                QueryDsl.insert(g).onDuplicateKeyIgnore(g.name, g.courseId)
                    .multiple(
                        GroupEntity(name = "test_I_1", courseId = testCourses[0].id),
                        GroupEntity(name = "test_I_2", courseId = testCourses[0].id),
                        GroupEntity(name = "test_I_3", courseId = testCourses[0].id)
                    )
            )

            testGroups.addAll(
                db.runQuery(
                    QueryDsl.from(g)
                        .where {
                            g.name startsWith "test_"
                        }
                        .orderBy(g.id)
                )
            )

            // delete registrations

            db.runQuery(
                QueryDsl.delete(gr)
                    .where {
                        gr.groupId inList testGroups.map { it.id }
                    }
            )

            db.runQuery {
                QueryDsl.delete(cr)
                    .where {
                        cr.userId inList testUsers.map { it.id }
                    }
            }

            // create registrations

            testCourseRegistrations.addAll(
                db.runQuery(
                    QueryDsl.insert(cr)
                        .multiple(
                            CourseRegistrationEntity(
                                userId = testUsers[0].id,
                                courseId = testCourses[0].id,
                                accessLevel = AccessLevel.TEACHER
                            ),
                            CourseRegistrationEntity(
                                userId = testUsers[0].id,
                                courseId = testCourses[1].id,
                                accessLevel = AccessLevel.TEACHER
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
            )

            testGroupRegistrations.addAll(
                db.runQuery(
                    QueryDsl.insert(gr)
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
            )

            initialized = true
        }
    }
}