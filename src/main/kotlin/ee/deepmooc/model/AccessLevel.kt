package ee.deepmooc.model

enum class AccessLevel {
    STUDENT,
    TEACHER
}

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequiredAccessLevel(val value: AccessLevel)