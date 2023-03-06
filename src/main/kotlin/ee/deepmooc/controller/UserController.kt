package ee.deepmooc.controller

import ee.deepmooc.model.AccessLevel
import ee.deepmooc.model.RequiredAccessLevel
import ee.deepmooc.model.User
import ee.deepmooc.model.UserEntity
import ee.deepmooc.repository.UserRepository
import io.jooby.MediaType
import io.jooby.annotations.*
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

        val userEntity: UserEntity = userRepository.findByUsername(username) as UserEntity

        return User(userEntity)
    }

    @GET("/users")
    @RequiredAccessLevel(AccessLevel.TEACHER)
    @Produces(MediaType.JSON)
    fun getAllUsers(@ContextParam user: SAML2Profile, @PathParam courseCode: String): List<User> {
        val userEntities = userRepository.findByCourseCode(courseCode)

        return userEntities.map { User(it) }
    }
}
