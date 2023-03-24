package ee.deepmooc.service

import ee.deepmooc.model.AccessLevel
import ee.deepmooc.model.CourseEntity
import ee.deepmooc.model.CourseRegistrationEntity
import ee.deepmooc.repository.CourseRepository
import ee.deepmooc.repository.UserRepository
import javax.inject.Inject

class AuthService @Inject constructor(
    private val userRepository: UserRepository,
    private val courseRepository: CourseRepository
) {

    companion object {

        private const val PERMISSION_PREFIX = "perm_"

        fun constructPermissionString(courseCode: String, accessLevel: AccessLevel): String {
            return PERMISSION_PREFIX + courseCode + "_" + accessLevel
        }

        fun getGrantedPermissions(courseCode: String, accessLevel: AccessLevel): Set<String> {
            val grantedPermissions: MutableSet<String> = mutableSetOf()

            for (level in AccessLevel.values()) {
                grantedPermissions.add(constructPermissionString(courseCode, level))
                if (level == accessLevel) break
            }

            return grantedPermissions.toSet()
        }
    }

    fun generateUserPermissions(username: String): Set<String> {
        val userEntity = userRepository.fetchByUsername(username)

        val courseRegistrations: Map<CourseRegistrationEntity, CourseEntity?> =
            courseRepository.fetchCourseRegistrationsOfUser(userEntity.id)

        val permissions = courseRegistrations.flatMap {
            getGrantedPermissions(it.value!!.code, it.key.accessLevel)
        }

        return permissions.toSet()
    }
}