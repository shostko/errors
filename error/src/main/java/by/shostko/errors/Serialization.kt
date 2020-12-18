@file:Suppress("unused")

package by.shostko.errors

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

internal const val CodeSerializationKey: String = "by.shostko.ErrorCode.key"
internal const val ErrorMapKey: String = "by.shostko.Error.key"

internal fun serializeErrorCode(code: ErrorCode): String =
    if (code is ErrorCode.Serializable && (code !is InternalErrorCode || code.canBeSerialized)) {
        code.serialize()
    } else try {
        Error.config.serialize(code)
    } catch (th: Throwable) {
        throw RuntimeException("Error while using custom serializer for: $code", th)
    }

internal fun deserializeErrorCode(str: String): ErrorCode = try {
    val json = JSONObject(str)
    when (json.getStringOrNull(CodeSerializationKey)) {
        InternalErrorCode.SERIALIZATION_KEY -> InternalErrorCode.deserialize(json)
        SimpleErrorCode.SERIALIZATION_KEY -> SimpleErrorCode.deserialize(json)
        ResErrorCode.SERIALIZATION_KEY -> ResErrorCode.deserialize(json)
        FormattedResErrorCode.SERIALIZATION_KEY -> FormattedResErrorCode.deserialize(json)
        StaticMessageErrorCode.SERIALIZATION_KEY -> StaticMessageErrorCode.deserialize(json)
        BaseResErrorCode.SERIALIZATION_KEY -> BaseResErrorCode.deserialize(json)
        BaseFormattedResErrorCode.SERIALIZATION_KEY -> BaseFormattedResErrorCode.deserialize(json)
        BaseFallbackDelegate.SERIALIZATION_KEY -> BaseFallbackDelegate.deserialize(json)
        else -> try {
            Error.config.deserialize(str) ?: throw UnsupportedOperationException("Please provide correct custom deserializer for: $str")
        } catch (th: Throwable) {
            throw RuntimeException("Error while using custom deserializer for: $str", th)
        }
    }
} catch (th: Throwable) {
    throw RuntimeException("Error while using internal deserializer for: $str", th)
}

internal fun JSONObject.getStringOrNull(name: String): String? = try {
    getString(name)
} catch (e: JSONException) {
    null
}

fun Error.serialize(): String = JSONArray().apply {
    var tmp: Throwable? = this@serialize
    while (tmp != null) {
        when (tmp) {
            NoError -> put("NoError")
            UnknownError -> put("UnknownError")
            is Error -> put(ErrorCode.serialize(tmp.code))
            else -> put(
                JSONObject()
                    .put("class", tmp::class.java.simpleName)
                    .put("message", tmp.message)
            )
        }
        tmp = tmp.cause
    }
}.toString()

fun Error.Companion.deserialize(str: String): Error = try {
    val array = JSONArray(str)
    var tmp: Throwable? = null
    val length = array.length()
    for (i in 1..length) {
        tmp = when (val obj = array[length - i]) {
            is JSONObject -> ReplicaThrowable(
                className = obj.getString("class"),
                message = obj.getStringOrNull("message"),
                cause = tmp
            )
            !is String -> UnknownError
            "NoError" -> NoError
            "UnknownError" -> UnknownError
            else -> {
                val code = ErrorCode.deserialize(obj)
                if (tmp == null) custom(code) else wrap(tmp, code)
            }
        }
    }
    tmp?.let { cast(it) } ?: NoError
} catch (th: Throwable) {
    throw UnsupportedOperationException("Can't deserialize into Error: $str", th)
}

fun Error.serialiseToPair(): Pair<String, String> = ErrorMapKey to serialize()
fun Error.serialiseToMap(): Map<String, String> = mapOf(ErrorMapKey to serialize())

fun Error.Companion.deserializeFromPair(pair: Pair<String, Any>): Error = if (pair.first == ErrorMapKey) deserialize(pair.second.toString()) else NoError
fun Error.Companion.deserializeFromMap(map: Map<String, Any>): Error = map[ErrorMapKey]?.let { deserialize(it.toString()) } ?: NoError

internal class ReplicaThrowable(
    private val className: String,
    message: String?,
    cause: Throwable?
) : Throwable(message, cause) {
    override fun toString(): String {
        val id = Error.config.domainToId(className)
        return "$className($id; ${message})"
    }
}