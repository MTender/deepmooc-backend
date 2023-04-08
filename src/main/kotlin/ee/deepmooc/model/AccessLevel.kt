package ee.deepmooc.model

enum class AccessLevel {
    STUDENT,
    TEACHER
}

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class MinimumAccessLevel(val value: AccessLevel)