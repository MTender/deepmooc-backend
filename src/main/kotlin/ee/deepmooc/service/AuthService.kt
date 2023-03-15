package ee.deepmooc.service

import ee.deepmooc.model.AccessLevel
import ee.deepmooc.model.User
import javax.inject.Inject

class AuthService @Inject constructor(
    private val userService: UserService
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
        val user: User = userService.getUserWithCourses(username)

        val permissions = user.courseRegistrations!!.flatMap {
            getGrantedPermissions(it.course!!.code, it.accessLevel)
        }

        return permissions.toSet()
    }
}