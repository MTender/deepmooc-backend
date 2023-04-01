package ee.deepmooc.auth

import ee.deepmooc.service.AuthService
import io.jooby.Jooby
import io.jooby.require
import org.pac4j.core.authorization.generator.AuthorizationGenerator
import org.pac4j.core.profile.UserProfile
import java.util.*

class PermissionManager {

    companion object {

        fun getPermissionsGenerator(app: Jooby): AuthorizationGenerator {
            return AuthorizationGenerator { _, profile ->

                val authService = app.require(AuthService::class)

                profile.addPermissions(authService.generateUserPermissions(profile.getUid()))

                Optional.of(profile)
            }
        }
    }
}

fun UserProfile.getUid(): String {
    return (this.getAttribute("uid")!! as List<*>)[0] as String
}