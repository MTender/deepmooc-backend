package ee.deepmooc.auth

import ee.deepmooc.service.AuthService
import io.jooby.Jooby
import io.jooby.kt.require
import org.pac4j.core.authorization.generator.AuthorizationGenerator
import org.pac4j.core.profile.UserProfile
import java.util.*

class RoleManager {

    companion object {

        fun getRolesGenerator(app: Jooby): AuthorizationGenerator {
            return AuthorizationGenerator { _, _, profile ->

                val authService = app.require(AuthService::class)

                profile.addRoles(authService.generateUserRoles(profile.getUid()))

                Optional.of(profile)
            }
        }
    }
}

fun UserProfile.getUid(): String {
    return (this.getAttribute("uid")!! as List<*>)[0] as String
}