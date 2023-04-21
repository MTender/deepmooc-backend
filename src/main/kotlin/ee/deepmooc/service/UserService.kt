package ee.deepmooc.service

import ee.deepmooc.dto.User
import ee.deepmooc.model.UserEntity
import ee.deepmooc.repository.UserRepository
import javax.inject.Inject

class UserService @Inject constructor(
    private val userRepository: UserRepository
) {

    fun getUser(username: String): User {
        val userEntity = userRepository.fetchByUsername(username)

        return User(userEntity)
    }

    fun createUser(user: User) {
        val userEntity = UserEntity(
            username = user.username
        )
        userRepository.save(userEntity)
    }
}