package ee.deepmooc.model

import ee.deepmooc.auth.PermissionManager
import ee.deepmooc.auth.RoleManager
import kotlinx.serialization.Serializable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "course_registrations")
class CourseRegistrationEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    val user: UserEntity,

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    val course: CourseEntity,

    @Enumerated(EnumType.STRING)
    @Column(name = "access_level")
    val accessLevel: AccessLevel
) {

    fun getRoleString(): String {
        return RoleManager.getRoleString(course.code, accessLevel)
    }

    fun getGrantedPermissions(): Set<String> {
        return PermissionManager.getGrantedPermissions(course.code, accessLevel)
    }
}

@Serializable
data class CourseRegistration(val id: Long, val course: Course, val accessLevel: AccessLevel) {
    constructor(courseRegistrationEntity: CourseRegistrationEntity) : this(
        courseRegistrationEntity.id,
        Course(courseRegistrationEntity.course),
        courseRegistrationEntity.accessLevel
    )
}