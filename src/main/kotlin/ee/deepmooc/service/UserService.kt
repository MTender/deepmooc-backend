package ee.deepmooc.service

import ee.deepmooc.model.Course
import ee.deepmooc.model.CourseRegistration
import ee.deepmooc.model.User
import ee.deepmooc.repository.UserRepository
import javax.inject.Inject

class UserService @Inject constructor(
    private val userRepository: UserRepository
) {

    fun getUserWithCourses(username: String): User {
        val (userEntity, registrationsToCourses) = userRepository.fetchCoursesByUsername(username)

        if (userEntity == null) throw IllegalArgumentException("No such user")

        val courseRegistrations = registrationsToCourses
            .mapTo(mutableSetOf()) { CourseRegistration(it.key, Course(it.value!!)) }

        return User(userEntity, courseRegistrations)
    }
}