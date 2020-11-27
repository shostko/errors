@file:Suppress("unused")

package by.shostko.error.status

import by.shostko.errors.Error
import by.shostko.errors.deserializeFromMap
import by.shostko.statushandler.Direction
import by.shostko.statushandler.MapThrowable
import by.shostko.statushandler.SimpleStatus

class ErrorStatusFactory(private val mapper: (Throwable) -> Error = Error.Companion::cast) : SimpleStatus.Factory<Error>() {
    override fun createFailed(throwable: Throwable) = SimpleStatus(Direction.NONE, throwable, mapper(throwable))
    override fun createFailed(map: Map<String, Any>) = createFailed(Error.deserializeFromMap(map))
}