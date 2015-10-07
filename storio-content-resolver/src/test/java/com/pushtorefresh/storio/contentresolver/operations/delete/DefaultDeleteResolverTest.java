package com.pushtorefresh.storio.contentresolver.operations.delete;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.pushtorefresh.storio.contentresolver.StorIOContentResolver;
import com.pushtorefresh.storio.contentresolver.queries.DeleteQuery;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DefaultDeleteResolverTest {

    @Test
    public void performDelete() {
        final StorIOContentResolver storIOContentResolver = mock(StorIOContentResolver.class);
        final StorIOContentResolver.Internal internal = mock(StorIOContentResolver.Internal.class);

        when(storIOContentResolver.internal())
                .thenReturn(internal);

        final int expectedNumberOfRowsDeleted = 1;

        when(internal.delete(any(DeleteQuery.class)))
                .thenReturn(expectedNumberOfRowsDeleted);

        final Uri expectedUri = mock(Uri.class);

        final DeleteQuery expectedDeleteQuery = DeleteQuery.builder()
                .uri(expectedUri)
                .where("test where clause")
                .whereArgs("test")
                .build();

        final TestItem testItem = TestItem.newInstance();

        final DefaultDeleteResolver<TestItem> defaultDeleteResolver = new DefaultDeleteResolver<TestItem>() {
            @NonNull
            @Override
            protected DeleteQuery mapToDeleteQuery(@NonNull StorIOContentResolver storIOContentResolver, @NonNull TestItem object) {
                assertThat(object).isSameAs(testItem);
                return expectedDeleteQuery;
            }
        };

        // Performing Delete Operation
        final DeleteResult deleteResult = defaultDeleteResolver.performDelete(storIOContentResolver, testItem);

        // checks that required delete was performed
        verify(internal, times(1)).delete(expectedDeleteQuery);

        // only one delete should be performed
        verify(internal, times(1)).delete(any(DeleteQuery.class));

        // delete result checks
        assertThat(deleteResult.numberOfRowsDeleted()).isEqualTo(expectedNumberOfRowsDeleted);
        assertThat(deleteResult.affectedUris()).hasSize(1);
        assertThat(deleteResult.affectedUris()).contains(expectedUri);
    }
}
