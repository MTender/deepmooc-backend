package ee.deepmooc.model

import com.fasterxml.jackson.annotation.JsonIgnore
import ee.deepmooc.auth.PermissionManager
import ee.deepmooc.auth.RoleManager
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
class CourseRegistration(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne
    val course: Course,

    @Enumerated(EnumType.STRING)
    @Column(name = "access_level")
    val accessLevel: AccessLevel
) {

    fun getRoleString(): String {
        return RoleManager.getRoleString(course.code, accessLevel)
    }

    fun getGrantedPermissions(): List<String> {
        return PermissionManager.getGrantedPermissions(course.code, accessLevel)
    }
}