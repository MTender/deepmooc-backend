package ee.deepmooc.auth

import ee.deepmooc.model.AccessLevel
import ee.deepmooc.service.AuthService
import io.jooby.pac4j.Pac4jContext
import org.pac4j.core.authorization.authorizer.Authorizer
import org.pac4j.core.context.WebContext
import org.pac4j.core.context.session.SessionStore
import org.pac4j.core.profile.UserProfile

class ApiAuthorizer : Authorizer {

    override fun isAuthorized(context: WebContext?, sessionStore: SessionStore?, profiles: MutableList<UserProfile>?): Boolean {
        if (context == null || profiles == null) return false
        if (context !is Pac4jContext) return false
        if (profiles.size != 1) return false

        val profile = profiles[0]

        val courseCode: String = context.getRequestParameter("courseCode").orElse(null) ?: return true

        val minimumAccessLevel: AccessLevel =
            context.context.route.attributes[MinimumAccessLevel::class.simpleName] as AccessLevel?
                ?: AccessLevel.entries.last()

        val requiredRole: String = AuthService.constructRoleString(courseCode, minimumAccessLevel)

        return profile.roles.contains(requiredRole)
    }
}