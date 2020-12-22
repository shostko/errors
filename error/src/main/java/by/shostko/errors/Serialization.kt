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

internal fun deserializeErrorCode(str: String): ErrorCode {
    val json = str.toJsonOrNull()
    return if (json?.getStringOrNull(CodeSerializationKey) == InternalErrorCode.SERIALIZATION_KEY) {
        try {
            InternalErrorCode.deserialize(json)
        } catch (th: Throwable) {
            throw RuntimeException("Error while using internal deserializer for: $str", th)
        }
    } else try {
        Error.config.deserialize(str) ?: throw UnsupportedOperationException("Please provide correct custom deserializer for: $str")
    } catch (th: Throwable) {
        throw RuntimeException("Error while using custom deserializer for: $str", th)
    }
}

internal fun JSONObject.requireCodeSerializationKey(key: String) {
    if (getStringOrNull(CodeSerializationKey) != key) {
        throw UnsupportedOperationException("Can't deserialize $key from $this")
    }
}

internal fun JSONObject.getStringOrNull(name: String): String? = try {
    getString(name)
} catch (e: JSONException) {
    null
}

internal fun String.toJsonOrNull(): JSONObject? = try {
    JSONObject(this)
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
                    .put("class", tmp::class.java.name)
                    .put("className", tmp::class.java.simpleName)
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
                classNameFull = obj.getString("class"),
                classNameSimple = obj.getString("className"),
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
    internal val classNameFull: String,
    internal val classNameSimple: String,
    message: String?,
    cause: Throwable?
) : Throwable(message, cause) {
    override fun toString(): String = "$classNameSimple(${message})"
}