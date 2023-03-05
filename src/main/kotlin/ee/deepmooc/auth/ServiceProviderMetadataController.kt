package ee.deepmooc.auth

import io.jooby.MediaType
import io.jooby.annotations.GET
import io.jooby.annotations.Path
import io.jooby.annotations.Produces
import java.io.File
import javax.inject.Inject
import javax.inject.Named

@Path("/saml-sp-metadata")
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