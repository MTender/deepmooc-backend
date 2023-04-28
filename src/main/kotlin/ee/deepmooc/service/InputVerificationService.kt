package ee.deepmooc.service

import ee.deepmooc.repository.CourseRegistrationRepository
import ee.deepmooc.repository.CourseRepository
import ee.deepmooc.repository.GroupRepository
import ee.deepmooc.repository.UserRepository
import javax.inject.Inject

class InputVerificationService @Inject constructor(
    private val userRepository: UserRepository,
    private val groupRepository: GroupRepository,
    private val courseRegistrationRepository: CourseRegistrationRepository,
    private val courseRepository: CourseRepository
) {

    fun verifyGroupMatchesCourse(groupId: Long, courseId: Long) {
        if (groupRepository.findByIdAndCourseId(groupId, courseId) == null) {
            throw IllegalArgumentException("Group id does not match course code")
        }
    }

    fun verifyUsersRegisteredToCourse(userIds: List<Long>, courseId: Long) {
        val courseRegistrationEntities = courseRegistrationRepository.findByUserIdsAndCourseId(userIds, courseId)

        if (courseRegistrationEntities.size != userIds.size) {
            throw IllegalArgumentException("At least one of the users is not registered to the course")
        }
    }

    fun verifyCourseCode(courseCode: String): Long {
        val courseEntity = courseRepository.findByCode(courseCode) ?: throw IllegalArgumentException("No such course")

        return courseEntity.id
    }

    fun verifyUsersExist(userIds: List<Long>) {
        val userEntities = userRepository.findUsers(userIds)

        if (userIds.size != userEntities.size) {
            throw IllegalArgumentException("At least one of the users does not exist")
        }
    }
}