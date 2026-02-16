package com.seguro.quotes.common

sealed class Either <out L, out R> {
    data class Left<out L>(val value: L) : Either<L, Nothing>()
    data class Right<out R>(val value: R) : Either<Nothing, R>()

    val isRight get() = this is Right<R>
    val isLeft get() = this is Left<L>

    fun getOrNull(): R? = when (this) {
        is Right -> value
        is Left -> null
    }

    // Transform Right, keep Left
    fun <T> map(transform: (R) -> T): Either<L, T> = when (this) {
        is Right -> Right(transform(value))
        is Left -> this
    }

    // FlatMap (evita Either<Either<...>>)
    fun <T> flatMap(transform: (R) -> Either<@UnsafeVariance L, T>): Either<L, T> = when (this) {
        is Right -> transform(value)
        is Left -> this
    }
}