@file:Suppress("unused")

package by.shostko.error.rx.worker

import androidx.work.Data
import androidx.work.workDataOf
import by.shostko.errors.Error
import by.shostko.errors.asError
import by.shostko.errors.deserializeFromMap
import by.shostko.errors.serialiseToPair

fun Throwable.toWorkData(): Data = workDataOf(asError().serialiseToPair())

fun Error.toWorkData(): Data = workDataOf(serialiseToPair())

fun Data.toError(): Error = Error.deserializeFromMap(keyValueMap)