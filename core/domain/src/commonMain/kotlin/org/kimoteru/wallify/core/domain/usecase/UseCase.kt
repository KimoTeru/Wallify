package org.kimoteru.wallify.core.domain.usecase

abstract class UseCase<in P, out R> {
    operator fun invoke(params: P): R = execute(params)

    protected abstract fun execute(params: P): R
}

operator fun <R> UseCase<Unit, R>.invoke(): R = invoke(Unit)