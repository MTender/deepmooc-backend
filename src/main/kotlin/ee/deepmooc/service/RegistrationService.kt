package ee.deepmooc.service

import ee.deepmooc.model.AccessLevel
import ee.deepmooc.model.Course
import ee.deepmooc.model.CourseRegistration
import ee.deepmooc.model.CourseRegistrationEntity
import ee.deepmooc.model.Group
import ee.deepmooc.model.GroupRegistrationEntity
import ee.deepmooc.model.User
import ee.deepmooc.repository.CourseRegistrationRepository
import ee.deepmooc.repository.GroupRegistrationRepository
import ee.deepmooc.repository.GroupRepository
import ee.deepmooc.repository.UserRepository
import javax.inject.Inject

class RegistrationService @Inject constructor(
    private val userRepository: UserRepository,
    private val courseRegistrationRepository: CourseRegistrationRepository,
    private val groupRegistrationRepository: GroupRegistrationRepository,
    private val groupRepository: GroupRepository
) {

    fun getCourseRegistrationsOfUser(username: String): List<CourseRegistration> {
        val userEntity = userRepository.fetchByUsername(username)

        val registrationsToCourses = courseRegistrationRepository.fetchWithCourses(userEntity.id)

        val courseRegistrations = registrationsToCourses
            .map { entry ->
                CourseRegistration(
                    entity = entry.key,
                    course = Course(entry.value)
                )
            }

        return courseRegistrations
    }

    fun getGroupsOfUser(username: String, courseId: Long): List<Group> {
        val userEntity = userRepository.fetchByUsername(username)

        return getGroupsOfUser(userEntity.id, courseId)
    }

    fun getGroupsOfUser(userId: Long, courseId: Long): List<Group> {
        val groupEntities = groupRepository.findByUserIdAndCourseId(userId, courseId)

        return groupEntities.map { Group(it) }
    }

    fun getRegistrations(courseId: Long): List<CourseRegistration> {
        val registrationsToUsers = userRepository.fetchUsersRegisteredToCourse(courseId)

        return registrationsToUsers
            .map { CourseRegistration(it.key, User(it.value!!)) }
    }

    fun addUsersToCourse(userIds: Collection<Long>, courseId: Long, accessLevel: AccessLevel) {
        val courseRegistrationEntities = userIds.map {
            CourseRegistrationEntity(
                userId = it,
                courseId = courseId,
                accessLevel = accessLevel
            )
        }

        courseRegistrationRepository.upsert(courseRegistrationEntities)
    }

    fun removeUsersFromCourse(userIds: List<Long>, courseId: Long) {
        val courseRegistrationEntityIds =
            courseRegistrationRepository.findByUserIdsAndCourseId(userIds, courseId).map { it.id }

        groupRegistrationRepository.deleteByCourseRegistrationIds(courseRegistrationEntityIds)

        courseRegistrationRepository.deleteByIds(courseRegistrationEntityIds)
    }

    fun addUsersToGroup(userIds: List<Long>, groupId: Long) {
        val courseRegistrationEntities = courseRegistrationRepository.findByUserIdsAndGroupIdThroughCourse(userIds, groupId)

        val groupRegistrationEntities = courseRegistrationEntities.map {
            GroupRegistrationEntity(
                groupId = groupId,
                courseRegistrationId = it.id
            )
        }

        groupRegistrationRepository.save(groupRegistrationEntities)
    }

    fun removeUsersFromGroup(userIds: List<Long>, groupId: Long) {
        val courseRegistrationEntities = courseRegistrationRepository.findByUserIdsAndGroupIdThroughCourse(userIds, groupId)

        groupRegistrationRepository.deleteByGroupIdAndCourseRegistrationIds(
            groupId,
            courseRegistrationEntities.map { it.id })
    }
}