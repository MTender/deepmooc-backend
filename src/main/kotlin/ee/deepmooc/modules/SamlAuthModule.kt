package ee.deepmooc.modules

import com.typesafe.config.Config
import ee.deepmooc.auth.ApiAuthorizer
import ee.deepmooc.auth.RoleManager
import io.jooby.Extension
import io.jooby.Jooby
import io.jooby.pac4j.Pac4jModule
import org.pac4j.core.authorization.generator.AuthorizationGenerator
import org.pac4j.saml.client.SAML2Client
import org.pac4j.saml.config.SAML2Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.UrlResource

class SamlAuthModule : Extension {

    override fun install(application: Jooby) {
        val rolesGenerator = RoleManager.getRolesGenerator(application)

        application.install(Pac4jModule()
            .client("/api/*", ApiAuthorizer::class.java) {
                generateSAML2Client(it, rolesGenerator)
            }
        )
    }

    private fun generateSAML2Client(conf: Config, rolesGenerator: AuthorizationGenerator): SAML2Client {
        val samlConfig = SAML2Configuration(
            ClassPathResource(conf.getString("saml.keystore.path")),
            conf.getString("saml.keystore.pwd"),
            conf.getString("saml.keystore.privateKeyPwd"),
            UrlResource(conf.getString("saml.idpMetadataUrl"))
        )

        samlConfig.serviceProviderEntityId = conf.getString("saml.spEntityId")
//        samlConfig.serviceProviderMetadataResource = FileSystemResource("src/main/resources/static/sp-metadata.xml")
        samlConfig.isAuthnRequestSigned = true

        val samlClient = SAML2Client(samlConfig)
        samlClient.callbackUrl = conf.getString("saml.callbackUrl")

        samlClient.addAuthorizationGenerator(rolesGenerator)

        return samlClient
    }
}