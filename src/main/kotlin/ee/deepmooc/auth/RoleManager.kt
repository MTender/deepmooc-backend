package ee.deepmooc.auth

import ee.deepmooc.model.AccessLevel

class RoleManager {
    companion object {

        private const val ROLE_PREFIX = "ROLE_"

        fun getRoleString(courseCode: String, accessLevel: AccessLevel): String {
            return ROLE_PREFIX + courseCode + "_" + accessLevel
        }
    }
}