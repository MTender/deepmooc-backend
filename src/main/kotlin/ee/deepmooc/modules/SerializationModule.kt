package ee.deepmooc.modules

import io.jooby.Context
import io.jooby.Extension
import io.jooby.Jooby
import io.jooby.MediaType
import io.jooby.MessageDecoder
import io.jooby.MessageEncoder
import io.jooby.StatusCode
import io.jooby.body
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.serializer
import java.lang.reflect.Type

class SerializationModule : Extension, MessageDecoder, MessageEncoder {

    private val mediaType = MediaType.json

    override fun install(application: Jooby) {
        application.decoder(mediaType, this)
        application.encoder(mediaType, this)

        application.errorCode(SerializationException::class.java, StatusCode.BAD_REQUEST)
    }

    @ExperimentalSerializationApi
    override fun decode(ctx: Context, type: Type): Any {
        if (ctx.body.isInMemory) {
            return Json.decodeFromString(serializer(type), ctx.body.bytes().decodeToString())
        }

        return ctx.body.stream().use {
            Json.decodeFromStream(serializer(type), it)
        }
    }

    override fun encode(ctx: Context, value: Any): ByteArray {
        ctx.setDefaultResponseType(mediaType)
        val serializer: KSerializer<Any> = serializer(ctx.route.returnType as Type)
        return Json.encodeToString(serializer, value).encodeToByteArray()
    }
}