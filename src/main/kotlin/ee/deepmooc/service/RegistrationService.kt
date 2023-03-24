package ee.deepmooc.service

import ee.deepmooc.model.AccessLevel
import ee.deepmooc.model.Course
import ee.deepmooc.model.CourseRegistration
import ee.deepmooc.model.CourseRegistrationEntity
import ee.deepmooc.model.Group
import ee.deepmooc.model.GroupRegistrationEntity
import ee.deepmooc.model.User
import ee.deepmooc.repository.CourseRegistrationRepository
import ee.deepmooc.repository.CourseRepository
import ee.deepmooc.repository.GroupRegistrationRepository
import ee.deepmooc.repository.UserRepository
import javax.inject.Inject

class RegistrationService @Inject constructor(
    private val userRepository: UserRepository,
    private val courseRepository: CourseRepository,
    private val courseRegistrationRepository: CourseRegistrationRepository,
    private val groupRegistrationRepository: GroupRegistrationRepository
) {

    fun getRegistrationsOfUser(username: String): List<CourseRegistration> {
        val userEntity = userRepository.fetchByUsername(username)

        val registrationsToCoursesAndGroups =
            courseRegistrationRepository.fetchWithCoursesAndGroupsByUserId(userEntity.id)

        val courseRegistrations = registrationsToCoursesAndGroups
            .map { entry ->
                CourseRegistration(
                    entity = entry.key,
                    course = Course(entry.value.first),
                    groups = entry.value.second.map { Group(it) }.toSet()
                )
            }

        return courseRegistrations
    }

    fun getRegisteredUsers(courseCode: String): List<CourseRegistration> {
        val registrationsToUsers = userRepository.fetchUsersRegisteredToCourse(courseCode)

        return registrationsToUsers
            .map { CourseRegistration(it.key, User(it.value!!)) }
    }

    fun getRegisteredStudents(courseCode: String): List<CourseRegistration> {
        val registrationsToUsers = userRepository.fetchUsersRegisteredToCourse(courseCode)

        return registrationsToUsers
            .filter { it.key.accessLevel == AccessLevel.STUDENT }
            .map { CourseRegistration(it.key, User(it.value!!)) }
    }

    fun registerUserToCourse(
        userId: Long,
        courseId: Long,
        accessLevel: AccessLevel,
        groupIds: Collection<Long> = setOf()
    ) {
        val courseRegistrationEntity = courseRegistrationRepository.save(
            CourseRegistrationEntity(
                userId = userId,
                courseId = courseId,
                accessLevel = accessLevel
            )
        )

        if (groupIds.isNotEmpty()) {
            val groupRegistrationEntities = groupIds.map {
                GroupRegistrationEntity(
                    groupId = it,
                    courseRegistrationId = courseRegistrationEntity.id
                )
            }

            groupRegistrationRepository.save(groupRegistrationEntities)
        }
    }

    fun removeUserFromCourse(userId: Long, courseId: Long) {
        val courseRegistrationEntity = courseRegistrationRepository.fetchByUserIdAndCourseId(userId, courseId)

        groupRegistrationRepository.deleteByCourseRegistrationId(courseRegistrationEntity.id)

        courseRegistrationRepository.deleteById(courseRegistrationEntity.id)
    }

    fun addUserToGroup(userId: Long, groupId: Long) {
        val courseEntity = courseRepository.fetchByGroupId(groupId)

        val courseRegistrationEntity = courseRegistrationRepository.fetchByUserIdAndCourseId(userId, courseEntity.id)

        val groupRegistrationEntity = GroupRegistrationEntity(
            groupId = groupId,
            courseRegistrationId = courseRegistrationEntity.id
        )

        groupRegistrationRepository.save(groupRegistrationEntity)
    }

    fun removeUserFromGroup(userId: Long, groupId: Long) {
        val courseEntity = courseRepository.fetchByGroupId(groupId)

        val courseRegistrationEntity = courseRegistrationRepository.fetchByUserIdAndCourseId(userId, courseEntity.id)

        groupRegistrationRepository.deleteByGroupIdAndCourseRegistrationId(groupId, courseRegistrationEntity.id)
    }
}