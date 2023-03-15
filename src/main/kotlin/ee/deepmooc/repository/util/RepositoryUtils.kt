package ee.deepmooc.repository.util

import ee.deepmooc.model.courseRegistrations
import ee.deepmooc.model.courses
import ee.deepmooc.model.users
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.query.on

class RepositoryUtils {

    companion object {

        private val u = Meta.users
        private val cr = Meta.courseRegistrations
        private val c = Meta.courses

        val U_JOIN_CR = on { u.id eq cr.userId }
        val C_JOIN_CR = on { c.id eq cr.courseId }
    }
}
