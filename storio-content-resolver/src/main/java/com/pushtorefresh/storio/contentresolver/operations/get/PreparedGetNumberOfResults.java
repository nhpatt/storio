package com.pushtorefresh.storio.contentresolver.operations.get;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import com.pushtorefresh.storio.StorIOException;
import com.pushtorefresh.storio.contentresolver.StorIOContentResolver;
import com.pushtorefresh.storio.contentresolver.queries.Query;
import com.pushtorefresh.storio.operations.internal.MapSomethingToExecuteAsBlocking;
import com.pushtorefresh.storio.operations.internal.OnSubscribeExecuteAsBlocking;

import rx.Observable;
import rx.schedulers.Schedulers;

import static com.pushtorefresh.storio.internal.Checks.checkNotNull;
import static com.pushtorefresh.storio.internal.Environment.throwExceptionIfRxJavaIsNotAvailable;

public class PreparedGetNumberOfResults extends PreparedGet<Integer> {

    @NonNull
    private final GetResolver<Integer> getResolver;

    PreparedGetNumberOfResults(@NonNull StorIOContentResolver storIOContentResolver, @NonNull Query query, @NonNull GetResolver<Integer> getResolver) {
        super(storIOContentResolver, query);
        this.getResolver = getResolver;
    }

    /**
     * Executes Get Operation immediately in current thread.
     * <p>
     * Notice: This is blocking I/O operation that should not be executed on the Main Thread,
     * it can cause ANR (Activity Not Responding dialog), block the UI and drop animations frames.
     * So please, call this method on some background thread. See {@link WorkerThread}.
     *
     * @return non-null {@link Integer} with number of results of the query.
     */
    @WorkerThread
    @NonNull
    @Override
    public Integer executeAsBlocking() {
        final Cursor cursor;

        try {
            cursor = getResolver.performGet(storIOContentResolver, query);
            try {
                return getResolver.mapFromCursor(cursor);
            } finally {
                cursor.close();
            }
        } catch (Exception exception) {
            throw new StorIOException(exception);
        }
    }

    /**
     * Creates "Hot" {@link Observable} which will be subscribed to changes of tables from query
     * and will emit result each time change occurs.
     * <p>
     * First result will be emitted immediately after subscription,
     * other emissions will occur only if changes of tables from query will occur during lifetime of
     * the {@link Observable}.
     * <dl>
     * <dt><b>Scheduler:</b></dt>
     * <dd>Operates on {@link Schedulers#io()}.</dd>
     * </dl>
     * <p>
     * Please don't forget to unsubscribe from this {@link Observable} because
     * it's "Hot" and endless.
     *
     * @return non-null {@link Observable} which will emit non-null
     * number of results of the executed query and will be subscribed to changes of tables from query.
     */
    @NonNull
    @Override
    public Observable<Integer> createObservable() {
        throwExceptionIfRxJavaIsNotAvailable("createObservable()");

        return storIOContentResolver
                .observeChangesOfUri(query.uri()) // each change triggers executeAsBlocking
                .map(MapSomethingToExecuteAsBlocking.newInstance(this))
                .startWith(Observable.create(OnSubscribeExecuteAsBlocking.newInstance(this))) // start stream with first query result
                .onBackpressureLatest()
                .subscribeOn(Schedulers.io());
    }

    /**
     * Builder for {@link PreparedGetNumberOfResults}.
     */
     public static final class Builder {

        @NonNull
        private final StorIOContentResolver storIOContentResolver;

        Builder(@NonNull StorIOContentResolver storIOContentResolver) {
            this.storIOContentResolver = storIOContentResolver;
        }

        /**
         * Required: Specifies query which will be passed to {@link StorIOContentResolver}
         * to get list of objects.
         *
         * @param query non-null query.
         * @return builder.
         * @see Query
         */
        @NonNull
        public CompleteBuilder withQuery(@NonNull Query query) {
            checkNotNull(query, "Please specify query");
            return new CompleteBuilder(storIOContentResolver, query);
        }
    }

    /**
     * Compile-time safe part of builder for {@link PreparedGetNumberOfResults}.
     */
    public static final class CompleteBuilder {

        @NonNull
        static final GetResolver<Integer> STANDARD_GET_RESOLVER = new DefaultGetResolver<Integer>() {
            @NonNull
            @Override
            public Integer mapFromCursor(@NonNull Cursor cursor) {
                return cursor.getCount();
            }
        };

        @NonNull
        private final StorIOContentResolver storIOContentResolver;

        @NonNull
        private final Query query;

        @Nullable
        private GetResolver<Integer> getResolver;

        CompleteBuilder(@NonNull StorIOContentResolver storIOContentResolver, @NonNull Query query) {
            this.storIOContentResolver = storIOContentResolver;
            this.query = query;
        }

        /**
         * Optional: Specifies resolver for Get Operation which can be used
         * to provide custom behavior of Get Operation.
         * <p>
         *
         * @param getResolver nullable resolver for Get Operation.
         * @return builder.
         */
        @NonNull
        public CompleteBuilder withGetResolver(@Nullable GetResolver<Integer> getResolver) {
            this.getResolver = getResolver;
            return this;
        }

        /**
         * Builds new instance of {@link PreparedGetNumberOfResults}.
         *
         * @return new instance of {@link PreparedGetNumberOfResults}.
         */
        @NonNull
        public PreparedGetNumberOfResults prepare() {
            if (getResolver == null) {
                getResolver = STANDARD_GET_RESOLVER;
            }

            return new PreparedGetNumberOfResults(
                    storIOContentResolver,
                    query,
                    getResolver
            );
        }
    }

}