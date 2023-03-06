package ee.deepmooc.model

import kotlinx.serialization.Serializable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "courses")
class CourseEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @Column(nullable = false)
    val name: String,

    @Column(nullable = false)
    val code: String
)

@Serializable
data class Course(val id: Long, val name: String, val code: String) {
    constructor(courseEntity: CourseEntity) : this(courseEntity.id, courseEntity.name, courseEntity.code)
}