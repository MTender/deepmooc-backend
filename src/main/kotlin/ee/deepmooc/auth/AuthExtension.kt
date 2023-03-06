package ee.deepmooc.auth

import com.typesafe.config.Config
import ee.deepmooc.model.UserEntity
import ee.deepmooc.repository.UserRepository
import io.jooby.Extension
import io.jooby.Jooby
import io.jooby.pac4j.Pac4jModule
import io.jooby.require
import org.pac4j.core.authorization.generator.AuthorizationGenerator
import org.pac4j.saml.client.SAML2Client
import org.pac4j.saml.config.SAML2Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.UrlResource
import java.util.*

class AuthExtension : Extension {

    override fun install(app: Jooby) {
        val rolesAndPermissionsGenerator = getRolesAndPermissionsGenerator(app)

        app.install(Pac4jModule()
            .client("/api/*", ApiAuthorizer::class.java) {
                generateSAML2Client(it, rolesAndPermissionsGenerator)
            }

        )
    }

    private fun getRolesAndPermissionsGenerator(app: Jooby): AuthorizationGenerator {
        return AuthorizationGenerator { _, profile ->

            val username = (profile.getAttribute("uid")!! as ArrayList<*>)[0] as String

            val userRepository = app.require(UserRepository::class)
            val userEntity: UserEntity? = userRepository.findByUsername(username)

            if (userEntity != null) {
                profile.addRoles(
                    userEntity.courseRegistrations.map {
                        it.getRoleString()
                    }
                )

                profile.addPermissions(
                    userEntity.courseRegistrations.map {
                        it.getGrantedPermissions()
                    }.flatten()
                )
            }

            Optional.of(profile)
        }
    }

    private fun generateSAML2Client(conf: Config, rolesAndPermissionsGenerator: AuthorizationGenerator): SAML2Client {
        val samlConfig = SAML2Configuration(
            ClassPathResource(conf.getString("saml.keystore.path")),
            conf.getString("saml.keystore.pwd"),
            conf.getString("saml.keystore.privateKeyPwd"),
            UrlResource(conf.getString("saml.idpMetadataUrl"))
        )

        samlConfig.serviceProviderEntityId = conf.getString("saml.spEntityId")
        samlConfig.setServiceProviderMetadataPath(conf.getString("saml.spMetadataPath"))
        samlConfig.isAuthnRequestSigned = true

        val samlClient = SAML2Client(samlConfig)
        samlClient.callbackUrl = conf.getString("saml.callbackUrl")

        samlClient.addAuthorizationGenerator(rolesAndPermissionsGenerator)

        return samlClient
    }
}