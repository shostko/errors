package by.shostko.errors

import androidx.annotation.StringRes

interface EnumErrorCode : ErrorCode, Identifier.Abs {

    val ordinal: Int
    val name: String

    override val domain: String
        get() = this.toDomain()

    override val index: Int
        get() = ordinal

    override val description: String
        get() = name

    override fun id(): Identifier = this
    override fun isFallback(): Boolean = false
}

fun <E : Enum<E>> E.asErrorCodeIdentifier(): Identifier = Identifier.Impl(this, ordinal, name)

@Suppress("unused")
enum class TemplateErrorCode(messageProvider: MessageProvider) : EnumErrorCode, MessageProvider by messageProvider {
    // Direct("message"),
    // FromRes(R.string.error_text),
    // FromFormattedRes(R.string.error_text_with_args, 42),
    NoMessage;

    constructor() : this(MessageProvider.Empty)
    constructor(message: String?) : this(MessageProvider.Direct(message))
    constructor(@StringRes messageResId: Int) : this(MessageProvider.FromRes(messageResId))
    constructor(@StringRes messageResId: Int, vararg args: Any) : this(MessageProvider.FromFormattedRes(messageResId, args))
}