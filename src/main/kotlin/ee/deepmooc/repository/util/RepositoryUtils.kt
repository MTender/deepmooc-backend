package ee.deepmooc.repository.util

import ee.deepmooc.model.courseRegistrations
import ee.deepmooc.model.courses
import ee.deepmooc.model.groupRegistrations
import ee.deepmooc.model.groups
import ee.deepmooc.model.users
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.query.on

class RepositoryUtils {

    companion object {

        private val u = Meta.users
        private val cr = Meta.courseRegistrations
        private val c = Meta.courses
        private val gr = Meta.groupRegistrations
        private val g = Meta.groups

        val U_JOIN_CR = on { u.id eq cr.userId }
        val C_JOIN_CR = on { c.id eq cr.courseId }
        val CR_JOIN_GR = on { cr.id eq gr.courseRegistrationId }
        val G_JOIN_GR = on { g.id eq gr.groupId }
    }
}
