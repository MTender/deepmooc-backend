package ee.deepmooc.repository

import ee.deepmooc.model.User
import javax.inject.Inject
import javax.persistence.EntityManager
import javax.persistence.NonUniqueResultException

class UserRepository @Inject constructor(
    private val em: EntityManager
) {

    fun findByUsername(username: String): User? {
        val users = em.createQuery("from User where username = :username", User::class.java)
            .setParameter("username", username)
            .resultList

        return when (users.size) {
            0 -> null
            1 -> users[0]
            else -> throw NonUniqueResultException()
        }
    }

    fun findByCourseCode(courseCode: String): List<User> {
        return em.createQuery("select u from User u inner join u.courseRegistrations cr where cr.course.code = :courseCode", User::class.java)
            .setParameter("courseCode", courseCode)
            .resultList
    }
}