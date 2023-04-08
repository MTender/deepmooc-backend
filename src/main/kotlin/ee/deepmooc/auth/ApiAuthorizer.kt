package ee.deepmooc.auth

import ee.deepmooc.model.AccessLevel
import ee.deepmooc.model.MinimumAccessLevel
import ee.deepmooc.service.AuthService
import io.jooby.pac4j.Pac4jContext
import org.pac4j.core.authorization.authorizer.Authorizer
import org.pac4j.core.context.WebContext
import org.pac4j.core.profile.CommonProfile

class ApiAuthorizer : Authorizer<CommonProfile> {

    override fun isAuthorized(context: WebContext?, profiles: MutableList<CommonProfile>?): Boolean {
        if (context == null || profiles == null) return false
        if (context !is Pac4jContext) return false
        if (profiles.size != 1) return false

        val profile = profiles[0]

        val courseCode: String = context.getRequestParameter("courseCode").orElse(null) ?: return true

        val minimumAccessLevel: AccessLevel =
            context.context.route.attributes[MinimumAccessLevel::class.simpleName] as AccessLevel?
                ?: AccessLevel.values().last()

        val requiredPermission: String = AuthService.constructPermissionString(courseCode, minimumAccessLevel)

        return profile.permissions.contains(requiredPermission)
    }
}