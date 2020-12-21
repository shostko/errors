@file:Suppress("unused")

package by.shostko.error.rx

import androidx.annotation.StringRes
import by.shostko.errors.Error
import by.shostko.errors.ErrorCode
import by.shostko.errors.Identifier
import by.shostko.errors.MessageProvider
import io.reactivex.*

fun <T : Throwable> Observable<T>.asError(): Observable<Error> = map { Error.cast(it) }
fun <T : Throwable> Flowable<T>.asError(): Flowable<Error> = map { Error.cast(it) }
fun <T : Throwable> Single<T>.asError(): Single<Error> = map { Error.cast(it) }
fun <T : Throwable> Maybe<T>.asError(): Maybe<Error> = map { Error.cast(it) }

fun <T : Throwable> Observable<T>.asMaterializedError(): Observable<Error> = map { Error.materialize(it) }
fun <T : Throwable> Flowable<T>.asMaterializedError(): Flowable<Error> = map { Error.materialize(it) }
fun <T : Throwable> Single<T>.asMaterializedError(): Single<Error> = map { Error.materialize(it) }
fun <T : Throwable> Maybe<T>.asMaterializedError(): Maybe<Error> = map { Error.materialize(it) }

fun <T> Observable<T>.withMaterializedError(): Observable<T> = onErrorResumeNext { th: Throwable -> Observable.error(Error.materialize(th)) }
fun <T> Observable<T>.wrap(code: ErrorCode): Observable<T> = onErrorResumeNext { th: Throwable -> Observable.error(Error.wrap(th, code)) }
fun <T> Observable<T>.wrap(builder: ErrorCode.Builder.() -> Unit): Observable<T> = onErrorResumeNext { th: Throwable -> Observable.error(Error.wrap(th, builder)) }
fun <T> Observable<T>.wrap(id: Identifier): Observable<T> = onErrorResumeNext { th: Throwable -> Observable.error(Error.wrap(th, id)) }
fun <T> Observable<T>.wrap(id: Identifier, message: MessageProvider): Observable<T> = onErrorResumeNext { th: Throwable -> Observable.error(Error.wrap(th, id, message)) }
fun <T> Observable<T>.wrap(id: Identifier, message: String?): Observable<T> = onErrorResumeNext { th: Throwable -> Observable.error(Error.wrap(th, id, message)) }
fun <T> Observable<T>.wrap(id: Identifier, @StringRes resId: Int): Observable<T> = onErrorResumeNext { th: Throwable -> Observable.error(Error.wrap(th, id, resId)) }
fun <T> Observable<T>.wrap(id: Identifier, @StringRes resId: Int, vararg args: Any): Observable<T> = onErrorResumeNext { th: Throwable -> Observable.error(Error.wrap(th, id, resId, args)) }

fun <T> Flowable<T>.withMaterializedError(): Flowable<T> = onErrorResumeNext { th: Throwable -> Flowable.error(Error.materialize(th)) }
fun <T> Flowable<T>.wrap(code: ErrorCode): Flowable<T> = onErrorResumeNext { th: Throwable -> Flowable.error(Error.wrap(th, code)) }
fun <T> Flowable<T>.wrap(builder: ErrorCode.Builder.() -> Unit): Flowable<T> = onErrorResumeNext { th: Throwable -> Flowable.error(Error.wrap(th, builder)) }
fun <T> Flowable<T>.wrap(id: Identifier): Flowable<T> = onErrorResumeNext { th: Throwable -> Flowable.error(Error.wrap(th, id)) }
fun <T> Flowable<T>.wrap(id: Identifier, message: MessageProvider): Flowable<T> = onErrorResumeNext { th: Throwable -> Flowable.error(Error.wrap(th, id, message)) }
fun <T> Flowable<T>.wrap(id: Identifier, message: String?): Flowable<T> = onErrorResumeNext { th: Throwable -> Flowable.error(Error.wrap(th, id, message)) }
fun <T> Flowable<T>.wrap(id: Identifier, @StringRes resId: Int): Flowable<T> = onErrorResumeNext { th: Throwable -> Flowable.error(Error.wrap(th, id, resId)) }
fun <T> Flowable<T>.wrap(id: Identifier, @StringRes resId: Int, vararg args: Any): Flowable<T> = onErrorResumeNext { th: Throwable -> Flowable.error(Error.wrap(th, id, resId, args)) }

fun <T> Single<T>.withMaterializedError(): Single<T> = onErrorResumeNext { th: Throwable -> Single.error(Error.materialize(th)) }
fun <T> Single<T>.wrap(code: ErrorCode): Single<T> = onErrorResumeNext { th: Throwable -> Single.error(Error.wrap(th, code)) }
fun <T> Single<T>.wrap(builder: ErrorCode.Builder.() -> Unit): Single<T> = onErrorResumeNext { th: Throwable -> Single.error(Error.wrap(th, builder)) }
fun <T> Single<T>.wrap(id: Identifier): Single<T> = onErrorResumeNext { th: Throwable -> Single.error(Error.wrap(th, id)) }
fun <T> Single<T>.wrap(id: Identifier, message: MessageProvider): Single<T> = onErrorResumeNext { th: Throwable -> Single.error(Error.wrap(th, id, message)) }
fun <T> Single<T>.wrap(id: Identifier, message: String?): Single<T> = onErrorResumeNext { th: Throwable -> Single.error(Error.wrap(th, id, message)) }
fun <T> Single<T>.wrap(id: Identifier, @StringRes resId: Int): Single<T> = onErrorResumeNext { th: Throwable -> Single.error(Error.wrap(th, id, resId)) }
fun <T> Single<T>.wrap(id: Identifier, @StringRes resId: Int, vararg args: Any): Single<T> = onErrorResumeNext { th: Throwable -> Single.error(Error.wrap(th, id, resId, args)) }

fun <T> Maybe<T>.withMaterializedError(): Maybe<T> = onErrorResumeNext { th: Throwable -> Maybe.error(Error.materialize(th)) }
fun <T> Maybe<T>.wrap(code: ErrorCode): Maybe<T> = onErrorResumeNext { th: Throwable -> Maybe.error(Error.wrap(th, code)) }
fun <T> Maybe<T>.wrap(builder: ErrorCode.Builder.() -> Unit): Maybe<T> = onErrorResumeNext { th: Throwable -> Maybe.error(Error.wrap(th, builder)) }
fun <T> Maybe<T>.wrap(id: Identifier): Maybe<T> = onErrorResumeNext { th: Throwable -> Maybe.error(Error.wrap(th, id)) }
fun <T> Maybe<T>.wrap(id: Identifier, message: MessageProvider): Maybe<T> = onErrorResumeNext { th: Throwable -> Maybe.error(Error.wrap(th, id, message)) }
fun <T> Maybe<T>.wrap(id: Identifier, message: String?): Maybe<T> = onErrorResumeNext { th: Throwable -> Maybe.error(Error.wrap(th, id, message)) }
fun <T> Maybe<T>.wrap(id: Identifier, @StringRes resId: Int): Maybe<T> = onErrorResumeNext { th: Throwable -> Maybe.error(Error.wrap(th, id, resId)) }
fun <T> Maybe<T>.wrap(id: Identifier, @StringRes resId: Int, vararg args: Any): Maybe<T> = onErrorResumeNext { th: Throwable -> Maybe.error(Error.wrap(th, id, resId, args)) }

fun Completable.withMaterializedError(): Completable = onErrorResumeNext { Completable.error(Error.materialize(it)) }
fun Completable.wrap(code: ErrorCode): Completable = onErrorResumeNext { Completable.error(Error.wrap(it, code)) }
fun Completable.wrap(builder: ErrorCode.Builder.() -> Unit): Completable = onErrorResumeNext { Completable.error(Error.wrap(it, builder)) }
fun Completable.wrap(id: Identifier): Completable = onErrorResumeNext { Completable.error(Error.wrap(it, id)) }
fun Completable.wrap(id: Identifier, message: MessageProvider): Completable = onErrorResumeNext { Completable.error(Error.wrap(it, id, message)) }
fun Completable.wrap(id: Identifier, message: String?): Completable = onErrorResumeNext { Completable.error(Error.wrap(it, id, message)) }
fun Completable.wrap(id: Identifier, @StringRes resId: Int): Completable = onErrorResumeNext { Completable.error(Error.wrap(it, id, resId)) }
fun Completable.wrap(id: Identifier, @StringRes resId: Int, vararg args: Any): Completable = onErrorResumeNext { Completable.error(Error.wrap(it, id, resId, args)) }