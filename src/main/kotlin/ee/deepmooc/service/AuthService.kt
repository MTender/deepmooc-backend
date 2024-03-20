package ee.deepmooc.service

import ee.deepmooc.model.AccessLevel
import ee.deepmooc.model.CourseEntity
import ee.deepmooc.model.CourseRegistrationEntity
import ee.deepmooc.repository.CourseRepository
import ee.deepmooc.repository.UserRepository
import jakarta.inject.Inject

class AuthService @Inject constructor(
    private val userRepository: UserRepository,
    private val courseRepository: CourseRepository
) {

    companion object {

        private const val ROLE_PREFIX = "ROLE_"

        fun constructRoleString(courseCode: String, accessLevel: AccessLevel): String {
            return ROLE_PREFIX + courseCode + "_" + accessLevel
        }

        fun getGrantedRoles(courseCode: String, accessLevel: AccessLevel): Set<String> {
            val grantedRoles: MutableSet<String> = mutableSetOf()

            for (level in AccessLevel.entries) {
                grantedRoles.add(constructRoleString(courseCode, level))
                if (level == accessLevel) break
            }

            return grantedRoles.toSet()
        }
    }

    fun generateUserRoles(username: String): Set<String> {
        val userEntity = userRepository.fetchByUsername(username)

        val courseRegistrations: Map<CourseRegistrationEntity, CourseEntity?> =
            courseRepository.fetchCourseRegistrationsOfUser(userEntity.id)

        val roles = courseRegistrations.flatMap {
            getGrantedRoles(it.value!!.code, it.key.accessLevel)
        }

        return roles.toSet()
    }
}