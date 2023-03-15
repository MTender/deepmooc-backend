package ee.deepmooc.modules

import io.jooby.Route
import io.jooby.Route.Decorator
import io.jooby.ServiceKey
import org.komapper.jdbc.JdbcDatabase

class KomapperTransactionalRequest(
    private val key: ServiceKey<JdbcDatabase>,
    private var enabledByDefault: Boolean = true
) : Decorator {

    constructor() : this(ServiceKey.key(JdbcDatabase::class.java))

    fun enabledByDefault(enabledByDefault: Boolean): KomapperTransactionalRequest {
        this.enabledByDefault = enabledByDefault
        return this
    }

    override fun apply(next: Route.Handler): Route.Handler {
        return Route.Handler { ctx ->
            if (ctx.route.isTransactional(enabledByDefault)) {
                val db: JdbcDatabase = ctx.require(key)

                db.withTransaction {
                    next.apply(ctx)
                }
            } else {
                next.apply(ctx)
            }
        }
    }
}