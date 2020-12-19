@file:Suppress("unused")

package by.shostko.errors

import org.json.JSONObject

interface Identifier {
    fun short(): String
    fun full(): String

    interface Abs : Identifier {
        val domain: String
        val index: Int
        val description: String

        override fun short(): String = domain.domainToId() + index
        override fun full(): String = "$domain.$description"
    }

    data class Impl(
        override val domain: String,
        override val index: Int,
        override val description: String
    ) : Abs {
        constructor(domain: Class<*>, index: Int, description: String) : this(domain.toDomain(), index, description)
        constructor(domain: Any, index: Int, description: String) : this(domain.toDomain(), index, description)
    }

    data class Simple(
        val short: String,
        val full: String
    ) : Identifier {

        constructor(full: String) : this(
            short = full.filter { it.isUpperCase() },
            full = full
        )

        override fun short(): String = short
        override fun full(): String = full
    }

    companion object {
        private const val SERIALIZATION_KEY: String = "identifier"

        private fun Identifier.serialize(): JSONObject = JSONObject()
            .put("short", short())
            .put("full", full())

        private fun deserialize(json: JSONObject) = Simple(
            short = json.getString("short"),
            full = json.getString("full")
        )

        internal fun JSONObject.put(id: Identifier): JSONObject = put(SERIALIZATION_KEY, id.serialize())
        internal fun JSONObject.getIdentifier(): Identifier = deserialize(getJSONObject(SERIALIZATION_KEY))
    }
}

fun Identifier.build(): ErrorCode = InternalErrorCode(this, MessageProvider.Empty)
fun Identifier.with(provider: MessageProvider): ErrorCode = InternalErrorCode(this, provider)
fun Identifier.with(message: String?): ErrorCode = InternalErrorCode(this, MessageProvider.Direct(message))
fun Identifier.with(messageResId: Int): ErrorCode = InternalErrorCode(this, MessageProvider.FromRes(messageResId))
fun Identifier.with(messageResId: Int, vararg args: Any): ErrorCode = InternalErrorCode(this, MessageProvider.FromFormattedRes(messageResId, args))