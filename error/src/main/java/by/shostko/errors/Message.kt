@file:Suppress("unused")

package by.shostko.errors

import android.content.Context
import androidx.annotation.StringRes
import org.json.JSONArray
import org.json.JSONObject

@Suppress("RedundantNullableReturnType")
interface MessageProvider {
    fun log(): String
    fun message(context: Context): CharSequence?

    object Empty : MessageProvider {
        override fun log(): String = Error.config.nullLog
        override fun message(context: Context): CharSequence? = null
    }

    data class Direct(internal val message: CharSequence?) : MessageProvider {
        override fun log(): String = message.messageToLog()
        override fun message(context: Context): CharSequence? = message
    }

    class FromRes(@StringRes internal val messageResId: Int) : MessageProvider {
        override fun log(): String = messageResId.messageToLog()
        override fun message(context: Context): CharSequence? = context.getString(messageResId)
    }

    class FromFormattedRes(@StringRes internal val messageResId: Int, internal val args: Array<out Any?>) : MessageProvider {
        override fun log(): String = messageResId.messageToLog(args)
        override fun message(context: Context): CharSequence? = context.getString(messageResId, *args)
    }

    companion object {
        private const val SERIALIZATION_KEY: String = "message"

        private fun MessageProvider.serialize(): JSONObject = JSONObject().run {
            when (this@serialize) {
                Empty -> this
                is Direct -> put("message", message)
                is FromRes -> put("messageResId", messageResId.toResourceName())
                is FromFormattedRes -> put("messageResId", messageResId.toResourceName())
                    .put("args", JSONArray().apply { args.forEach { put(it) } })
                is MessageWithLogProvider -> put("log", log).put("wrapped", wrapped.serialize())
                is MessageWithoutLogProvider -> put("noLog", true).put("wrapped", wrapped.serialize())
                else -> throw UnsupportedOperationException("Can't serialize such MessageProvider: $this")
            }
        }

        private fun deserialize(json: JSONObject): MessageProvider = if (json.has("message")) {
            Direct(json.getString("message"))
        } else if (json.has("messageResId") && json.has("args")) {
            FromFormattedRes(
                messageResId = json.getString("messageResId").toResourceId(),
                args = json.getJSONArray("args").run { Array(length()) { get(it) } }
            )
        } else if (json.has("messageResId")) {
            FromRes(json.getString("messageResId").toResourceId())
        } else if (json.has("noLog") && json.getBoolean("noLog")) {
            MessageWithoutLogProvider(
                wrapped = deserialize(json.getJSONObject("wrapped"))
            )
        } else if (json.has("log")) {
            MessageWithLogProvider(
                log = json.getString("log"),
                wrapped = deserialize(json.getJSONObject("wrapped"))
            )
        } else {
            Empty
        }

        internal fun JSONObject.put(message: MessageProvider): JSONObject = put(SERIALIZATION_KEY, message.serialize())
        internal fun JSONObject.getMessageProvider(): MessageProvider = deserialize(getJSONObject(SERIALIZATION_KEY))

        internal val MessageProvider.canBeSerialized: Boolean
            get() = when (this) {
                Empty, is Direct, is FromRes, is FromFormattedRes -> true
                else -> false
            }
    }
}

private class MessageWithLogProvider(val log: String, val wrapped: MessageProvider) : MessageProvider {
    override fun log(): String = log
    override fun message(context: Context): CharSequence? = wrapped.message(context)
}

private class MessageWithoutLogProvider(val wrapped: MessageProvider) : MessageProvider {
    override fun log(): String = Error.config.nullLog
    override fun message(context: Context): CharSequence? = wrapped.message(context)
}

internal fun MessageProvider.withLog(log: String): MessageProvider = MessageWithLogProvider(log, this)
internal fun MessageProvider.noLog(): MessageProvider = MessageWithoutLogProvider(this)