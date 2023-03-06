package ee.deepmooc.repository

import ee.deepmooc.model.UserEntity
import javax.inject.Inject
import javax.persistence.EntityManager
import javax.persistence.NonUniqueResultException

class UserRepository @Inject constructor(
    private val em: EntityManager
) {

    fun findByUsername(username: String): UserEntity? {
        val userEntities = em.createQuery("from UserEntity where username = :username", UserEntity::class.java)
            .setParameter("username", username)
            .resultList

        return when (userEntities.size) {
            0 -> null
            1 -> userEntities[0]
            else -> throw NonUniqueResultException()
        }
    }

    fun findByCourseCode(courseCode: String): List<UserEntity> {
        return em.createQuery("select u from UserEntity u inner join u.courseRegistrations cr where cr.course.code = :courseCode", UserEntity::class.java)
            .setParameter("courseCode", courseCode)
            .resultList
    }
}