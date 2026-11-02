package org.kimoteru.wallify.core.data.mapper

fun interface Mapper<in From, out To> {
    fun map(from: From): To
}

interface BiMapper<A, B> : Mapper<A, B> {
    fun reverse(from: B): A
}

fun <From, To> Mapper<From, To>.mapList(from: Iterable<From>): List<To> = from.map(::map)

fun <From, To> Mapper<From, To>.mapOrNull(from: From?): To? = from?.let(::map)