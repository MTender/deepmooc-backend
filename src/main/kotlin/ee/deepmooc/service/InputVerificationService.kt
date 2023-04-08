package ee.deepmooc.service

import ee.deepmooc.repository.CourseRegistrationRepository
import ee.deepmooc.repository.GroupRepository
import javax.inject.Inject

class InputVerificationService @Inject constructor(
    private val groupRepository: GroupRepository,
    private val courseRegistrationRepository: CourseRegistrationRepository
) {

    fun verifyGroupMatchesCourse(groupId: Long, courseId: Long) {
        if (groupRepository.findByIdAndCourseCode(groupId, courseId) == null) {
            throw IllegalArgumentException("Group id does not match course code")
        }
    }

    fun verifyUsersRegisteredToCourse(userIds: List<Long>, courseId: Long) {
        val courseRegistrationEntities = courseRegistrationRepository.findByUserIdsAndCourseId(userIds, courseId)

        if (courseRegistrationEntities.size != userIds.size) {
            throw IllegalArgumentException("At least one of the users is not registered to the course")
        }
    }
}