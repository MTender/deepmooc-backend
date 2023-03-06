package ee.deepmooc.model

import kotlinx.serialization.Serializable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.OneToMany
import javax.persistence.Table

@Entity
@Table(name = "users")
class UserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @Column(nullable = false)
    val username: String,

    @OneToMany(mappedBy = "user")
    val courseRegistrations: Set<CourseRegistrationEntity>
)

@Serializable
data class User(val id: Long, val username: String, val courseRegistrations: Set<CourseRegistration>) {
    constructor(userEntity: UserEntity) : this(
        userEntity.id,
        userEntity.username,
        userEntity.courseRegistrations.map { CourseRegistration(it) }.toSet()
    )
}