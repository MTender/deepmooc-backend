package ee.deepmooc.auth

import ee.deepmooc.model.AccessLevel

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class MinimumAccessLevel(val value: AccessLevel)
