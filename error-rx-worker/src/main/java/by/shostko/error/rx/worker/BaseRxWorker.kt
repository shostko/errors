@file:Suppress("unused")

package by.shostko.error.rx.worker

import android.content.Context
import androidx.work.Data
import androidx.work.RxWorker
import androidx.work.WorkerParameters
import io.reactivex.Completable
import io.reactivex.Single

abstract class BaseRxWorker(context: Context, workerParameters: WorkerParameters) : RxWorker(context, workerParameters) {

    protected open fun throwableToData(throwable: Throwable): Data = throwable.toWorkData()

    protected open fun throwableToResult(throwable: Throwable): Result = Result.failure(throwableToData(throwable))
}

abstract class BaseRxSingleWorker(context: Context, workerParameters: WorkerParameters) : BaseRxWorker(context, workerParameters) {

    final override fun createWork(): Single<Result> = createSingleWork()
        .onErrorReturn(this::throwableToResult)

    protected abstract fun createSingleWork(): Single<Result>
}

abstract class BaseRxCompletableWorker(context: Context, workerParameters: WorkerParameters) : BaseRxWorker(context, workerParameters) {

    final override fun createWork(): Single<Result> = createCompletableWork()
        .toSingleDefault(Result.success())
        .onErrorReturn(this::throwableToResult)

    protected abstract fun createCompletableWork(): Completable
}