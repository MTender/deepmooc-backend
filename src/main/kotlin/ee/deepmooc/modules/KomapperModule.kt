package ee.deepmooc.modules

import io.jooby.Extension
import io.jooby.Jooby
import io.jooby.ServiceKey
import io.jooby.ServiceRegistry
import org.komapper.dialect.postgresql.jdbc.PostgreSqlJdbcDialect
import org.komapper.jdbc.JdbcDatabase
import javax.sql.DataSource

class KomapperModule(
    private val name: String
) : Extension {

    constructor() : this("db")

    override fun install(application: Jooby) {
        val registry: ServiceRegistry = application.services

        val dataSource = findDataSource(registry)

        val db = JdbcDatabase(dataSource, PostgreSqlJdbcDialect())

        registry.putIfAbsent(ServiceKey.key(JdbcDatabase::class.java), db)
        registry.put(ServiceKey.key(JdbcDatabase::class.java, name), db)
    }

    private fun findDataSource(registry: ServiceRegistry): DataSource {
        return registry.get(ServiceKey.key(DataSource::class.java, name))
    }
}