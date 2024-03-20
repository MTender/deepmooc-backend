package ee.deepmooc.modules

import io.jooby.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.serializer
import java.lang.reflect.Type

class KotlinxSerializationModule : Extension, MessageDecoder, MessageEncoder {

    private val mediaType = MediaType.json

    @ExperimentalSerializationApi
    private val format = Json { explicitNulls = false }

    override fun install(application: Jooby) {
        application.decoder(mediaType, this)
        application.encoder(mediaType, this)

        application.errorCode(SerializationException::class.java, StatusCode.BAD_REQUEST)
    }

    @ExperimentalSerializationApi
    override fun decode(ctx: Context, type: Type): Any {
        val body: Body = ctx.body()
        if (body.isInMemory) {
            return Json.decodeFromString(serializer(type), body.bytes().decodeToString())
        }

        return body.stream().use {
            Json.decodeFromStream(serializer(type), it)
        }
    }

    @ExperimentalSerializationApi
    override fun encode(ctx: Context, value: Any): ByteArray {
        ctx.setDefaultResponseType(mediaType)
        val serializer: KSerializer<Any> = serializer(ctx.route.returnType as Type)
        return format.encodeToString(serializer, value).encodeToByteArray()
    }
}