package ee.deepmooc.controller

import ee.deepmooc.model.AccessLevel
import ee.deepmooc.model.RequiredAccessLevel
import ee.deepmooc.model.User
import ee.deepmooc.repository.UserRepository
import io.jooby.MediaType
import io.jooby.annotations.*
import org.hibernate.Hibernate
import org.pac4j.saml.profile.SAML2Profile
import javax.inject.Inject

@Path("/api/{courseCode}")
class UserController @Inject constructor(
    private val userRepository: UserRepository
) {

    @GET("/me")
    @Produces(MediaType.JSON)
    fun myInfo(@ContextParam user: SAML2Profile): User {
        val username = (user.getAttribute("uid") as ArrayList<*>)[0] as String

        val userEntity: User = userRepository.findByUsername(username) as User

        Hibernate.initialize(userEntity.courseRegistrations)

        return userEntity
    }

    @GET("/users")
    @RequiredAccessLevel(AccessLevel.TEACHER)
    @Produces(MediaType.JSON)
    fun getAllUsers(@ContextParam user: SAML2Profile, @PathParam courseCode: String): List<User> {
        val userEntities = userRepository.findByCourseCode(courseCode)
        userEntities.forEach {
            Hibernate.initialize(it.courseRegistrations)
        }
        return userEntities
    }
}
