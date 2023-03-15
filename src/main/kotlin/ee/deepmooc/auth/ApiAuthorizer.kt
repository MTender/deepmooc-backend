package ee.deepmooc.auth

import ee.deepmooc.model.AccessLevel
import ee.deepmooc.model.RequiredAccessLevel
import ee.deepmooc.service.AuthService
import io.jooby.pac4j.Pac4jContext
import org.pac4j.core.authorization.authorizer.Authorizer
import org.pac4j.core.context.WebContext
import org.pac4j.saml.profile.SAML2Profile

class ApiAuthorizer : Authorizer<SAML2Profile> {

    override fun isAuthorized(context: WebContext?, profiles: MutableList<SAML2Profile>?): Boolean {
        if (context == null || profiles == null) return false
        if (context !is Pac4jContext) return false
        if (profiles.size != 1) return false

        val profile = profiles[0]

        val courseCode: String = context.getRequestParameter("courseCode").orElse(null) ?: return true

        val requiredAccessLevel: AccessLevel =
            context.context.route.attributes[RequiredAccessLevel::class.simpleName] as AccessLevel?
                ?: AccessLevel.values().last()

        val requiredPermission: String = AuthService.constructPermissionString(courseCode, requiredAccessLevel)

        return profile.permissions.contains(requiredPermission)
    }
}