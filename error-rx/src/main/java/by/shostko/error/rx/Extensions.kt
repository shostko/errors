@file:Suppress("unused")

package by.shostko.error.rx

import androidx.annotation.StringRes
import by.shostko.errors.Error
import by.shostko.errors.ErrorCode
import io.reactivex.*

fun <T : Throwable> Observable<T>.asError(): Observable<Error> = map { Error.cast(it) }
fun <T : Throwable> Flowable<T>.asError(): Flowable<Error> = map { Error.cast(it) }
fun <T : Throwable> Single<T>.asError(): Single<Error> = map { Error.cast(it) }
fun <T : Throwable> Maybe<T>.asError(): Maybe<Error> = map { Error.cast(it) }

fun <T : Throwable> Observable<T>.asMaterializedError(): Observable<Error> = map { Error.materialize(it) }
fun <T : Throwable> Flowable<T>.asMaterializedError(): Flowable<Error> = map { Error.materialize(it) }
fun <T : Throwable> Single<T>.asMaterializedError(): Single<Error> = map { Error.materialize(it) }
fun <T : Throwable> Maybe<T>.asMaterializedError(): Maybe<Error> = map { Error.materialize(it) }

fun <T> Observable<T>.wrap(code: ErrorCode): Observable<T> = onErrorResumeNext { th: Throwable -> Observable.error(Error.wrap(th, code)) }
fun <T> Observable<T>.wrap(builder: ErrorCode.Builder.() -> Unit): Observable<T> = onErrorResumeNext { th: Throwable -> Observable.error(Error.wrap(th, builder)) }
fun <T> Observable<T>.wrap(id: String): Observable<T> = onErrorResumeNext { th: Throwable -> Observable.error(Error.wrap(th, id)) }
fun <T> Observable<T>.wrap(id: String, message: String?): Observable<T> = onErrorResumeNext { th: Throwable -> Observable.error(Error.wrap(th, id, message)) }
fun <T> Observable<T>.wrap(id: String, @StringRes resId: Int): Observable<T> = onErrorResumeNext { th: Throwable -> Observable.error(Error.wrap(th, id, resId)) }
fun <T> Observable<T>.wrap(id: String, @StringRes resId: Int, vararg args: Any): Observable<T> = onErrorResumeNext { th: Throwable -> Observable.error(Error.wrap(th, id, resId, args)) }

fun <T> Flowable<T>.wrap(code: ErrorCode): Flowable<T> = onErrorResumeNext { th: Throwable -> Flowable.error(Error.wrap(th, code)) }
fun <T> Flowable<T>.wrap(builder: ErrorCode.Builder.() -> Unit): Flowable<T> = onErrorResumeNext { th: Throwable -> Flowable.error(Error.wrap(th, builder)) }
fun <T> Flowable<T>.wrap(id: String): Flowable<T> = onErrorResumeNext { th: Throwable -> Flowable.error(Error.wrap(th, id)) }
fun <T> Flowable<T>.wrap(id: String, message: String?): Flowable<T> = onErrorResumeNext { th: Throwable -> Flowable.error(Error.wrap(th, id, message)) }
fun <T> Flowable<T>.wrap(id: String, @StringRes resId: Int): Flowable<T> = onErrorResumeNext { th: Throwable -> Flowable.error(Error.wrap(th, id, resId)) }
fun <T> Flowable<T>.wrap(id: String, @StringRes resId: Int, vararg args: Any): Flowable<T> = onErrorResumeNext { th: Throwable -> Flowable.error(Error.wrap(th, id, resId, args)) }

fun <T> Single<T>.wrap(code: ErrorCode): Single<T> = onErrorResumeNext { th: Throwable -> Single.error(Error.wrap(th, code)) }
fun <T> Single<T>.wrap(builder: ErrorCode.Builder.() -> Unit): Single<T> = onErrorResumeNext { th: Throwable -> Single.error(Error.wrap(th, builder)) }
fun <T> Single<T>.wrap(id: String): Single<T> = onErrorResumeNext { th: Throwable -> Single.error(Error.wrap(th, id)) }
fun <T> Single<T>.wrap(id: String, message: String?): Single<T> = onErrorResumeNext { th: Throwable -> Single.error(Error.wrap(th, id, message)) }
fun <T> Single<T>.wrap(id: String, @StringRes resId: Int): Single<T> = onErrorResumeNext { th: Throwable -> Single.error(Error.wrap(th, id, resId)) }
fun <T> Single<T>.wrap(id: String, @StringRes resId: Int, vararg args: Any): Single<T> = onErrorResumeNext { th: Throwable -> Single.error(Error.wrap(th, id, resId, args)) }

fun <T> Maybe<T>.wrap(code: ErrorCode): Maybe<T> = onErrorResumeNext { th: Throwable -> Maybe.error(Error.wrap(th, code)) }
fun <T> Maybe<T>.wrap(builder: ErrorCode.Builder.() -> Unit): Maybe<T> = onErrorResumeNext { th: Throwable -> Maybe.error(Error.wrap(th, builder)) }
fun <T> Maybe<T>.wrap(id: String): Maybe<T> = onErrorResumeNext { th: Throwable -> Maybe.error(Error.wrap(th, id)) }
fun <T> Maybe<T>.wrap(id: String, message: String?): Maybe<T> = onErrorResumeNext { th: Throwable -> Maybe.error(Error.wrap(th, id, message)) }
fun <T> Maybe<T>.wrap(id: String, @StringRes resId: Int): Maybe<T> = onErrorResumeNext { th: Throwable -> Maybe.error(Error.wrap(th, id, resId)) }
fun <T> Maybe<T>.wrap(id: String, @StringRes resId: Int, vararg args: Any): Maybe<T> = onErrorResumeNext { th: Throwable -> Maybe.error(Error.wrap(th, id, resId, args)) }

fun Completable.wrap(code: ErrorCode): Completable = onErrorResumeNext { Completable.error(Error.wrap(it, code)) }
fun Completable.wrap(builder: ErrorCode.Builder.() -> Unit): Completable = onErrorResumeNext { Completable.error(Error.wrap(it, builder)) }
fun Completable.wrap(id: String): Completable = onErrorResumeNext { Completable.error(Error.wrap(it, id)) }
fun Completable.wrap(id: String, message: String?): Completable = onErrorResumeNext { Completable.error(Error.wrap(it, id, message)) }
fun Completable.wrap(id: String, @StringRes resId: Int): Completable = onErrorResumeNext { Completable.error(Error.wrap(it, id, resId)) }
fun Completable.wrap(id: String, @StringRes resId: Int, vararg args: Any): Completable = onErrorResumeNext { Completable.error(Error.wrap(it, id, resId, args)) }