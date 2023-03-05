package ee.deepmooc.auth

import ee.deepmooc.model.AccessLevel

class PermissionManager {
    companion object {

        private const val PERMISSION_PREFIX = "perm_"

        fun constructPermissionString(courseCode: String, role: AccessLevel): String {
            return PERMISSION_PREFIX + courseCode + "_" + role
        }

        fun getGrantedPermissions(courseCode: String, userRole: AccessLevel): List<String> {
            val grantedPermissions: MutableList<String> = mutableListOf()

            for (role in AccessLevel.values()) {
                grantedPermissions.add(constructPermissionString(courseCode, role))
                if (role == userRole) break
            }

            return grantedPermissions.toList()
        }
    }
}