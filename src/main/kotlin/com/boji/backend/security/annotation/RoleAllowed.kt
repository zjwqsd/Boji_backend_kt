package com.boji.backend.security.annotation


@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RoleAllowed(vararg val roles: String)
