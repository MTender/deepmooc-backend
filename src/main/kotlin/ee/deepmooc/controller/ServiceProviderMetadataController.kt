package ee.deepmooc.controller

import io.jooby.MediaType
import io.jooby.annotations.GET
import io.jooby.annotations.Path
import io.jooby.annotations.Produces
import io.swagger.v3.oas.annotations.tags.Tag
import java.io.File
import javax.inject.Inject
import javax.inject.Named

@Path("/saml-sp-metadata")
@Tag(name = "SAML")
class ServiceProviderMetadataController @Inject constructor(
    @Named("saml.spMetadataPath")
    private val spMetadataResourcePath: String
) {

    @GET
    @Produces(MediaType.XML)
    fun spMetadata(): File {
        return File(spMetadataResourcePath)
    }
}