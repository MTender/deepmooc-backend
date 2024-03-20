package ee.deepmooc.modules

import ee.deepmooc.auth.ApiAuthorizer
import ee.deepmooc.auth.RoleManager
import io.jooby.Extension
import io.jooby.Jooby
import io.jooby.pac4j.Pac4jModule
import org.pac4j.core.authorization.generator.AuthorizationGenerator
import org.pac4j.core.credentials.Credentials
import org.pac4j.core.credentials.TokenCredentials
import org.pac4j.core.credentials.authenticator.Authenticator
import org.pac4j.core.profile.CommonProfile
import org.pac4j.http.client.direct.HeaderClient

class TestAuthModule : Extension {

    override fun install(application: Jooby) {
        val rolesGenerator = RoleManager.getRolesGenerator(application)

        application.install(Pac4jModule()
            .client("/api/*", ApiAuthorizer::class.java) {
                generateTestHeaderClient(rolesGenerator)
            }
        )
    }

    private fun generateTestHeaderClient(rolesGenerator: AuthorizationGenerator): HeaderClient {
        val headerClient = HeaderClient("Authentication", Authenticator { credentials: Credentials, _, _ ->
            val token = (credentials as TokenCredentials).token

            val profile = CommonProfile()
            profile.addAttribute("uid", listOf(token))

            credentials.userProfile = profile
        })

        headerClient.addAuthorizationGenerator(rolesGenerator)

        return headerClient
    }
}