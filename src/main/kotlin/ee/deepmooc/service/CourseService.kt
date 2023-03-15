package ee.deepmooc.service

import ee.deepmooc.model.Course
import ee.deepmooc.model.CourseRegistration
import ee.deepmooc.model.User
import ee.deepmooc.repository.CourseRepository
import javax.inject.Inject

class CourseService @Inject constructor(
    private val courseRepository: CourseRepository
) {

    fun getCourseWithUsers(courseCode: String): Course {
        val (courseEntity, registrationsToUsers) = courseRepository.fetchUsersByCourseCode(courseCode)

        if (courseEntity == null) throw IllegalArgumentException("No such course")

        val courseRegistrations = registrationsToUsers
            .mapTo(mutableSetOf()) { CourseRegistration(it.key, User(it.value!!)) }

        return Course(courseEntity, courseRegistrations)
    }

    fun getCourseWithStudents(courseCode: String): Course {
        val (courseEntity, registrationsToStudents) = courseRepository.fetchStudentsByCourseCode(courseCode)

        if (courseEntity == null) throw IllegalArgumentException("No such course")

        val courseRegistrations = registrationsToStudents
            .mapTo(mutableSetOf()) { CourseRegistration(it.key, User(it.value!!)) }

        return Course(courseEntity, courseRegistrations)
    }
}