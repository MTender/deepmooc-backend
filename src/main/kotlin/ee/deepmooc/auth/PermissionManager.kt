package ee.deepmooc.auth

import ee.deepmooc.model.AccessLevel

class PermissionManager {
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
}