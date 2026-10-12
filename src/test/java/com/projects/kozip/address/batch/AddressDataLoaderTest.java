package com.projects.kozip.address.batch;

import com.projects.kozip.config.DataPathProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.sql.SQLException;
import org.springframework.dao.DataAccessResourceFailureException;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AddressDataLoaderTest {
    @TempDir Path directory;

    @Test
    void resumesAfterCommittedLineAndMarksCompletion() throws Exception {
        String[] columns = new String[24];
        Arrays.fill(columns, "");
        for (int index : new int[]{7, 8, 12, 13}) columns[index] = "0";
        columns[0] = "1";
        String first = String.join("|", columns);
        columns[0] = "2";
        Files.writeString(directory.resolve("rnaddrkor_test.txt"), first + "\n"
                + String.join("|", columns), Charset.forName("EUC-KR"));
        var store = mock(AddressBatchStore.class);
        when(store.progress(anyString(), anyString())).thenReturn(new AddressBatchStore.Progress(1, false));
        var properties = new DataPathProperties();
        properties.setPath(directory.toString());
        doThrow(new DataAccessResourceFailureException("connection lost", new SQLException("reset", "08S01")))
                .doNothing().when(store).save(anyList(), anyString(), anyString(), anyLong(), anyBoolean());
        new AddressDataLoader(store, properties).run(null);
        verify(store, times(2)).save(argThat(rows -> rows.size() == 1 && rows.getFirst().getManageNo().equals("2")),
                eq("rnaddrkor_test.txt"), anyString(), eq(2L), eq(true));
    }

    @Test
    void completedFileIsNotInsertedAgain() throws Exception {
        Files.writeString(directory.resolve("rnaddrkor_test.txt"), "unused");
        var store = mock(AddressBatchStore.class);
        when(store.progress(anyString(), anyString())).thenReturn(new AddressBatchStore.Progress(1, true));
        var properties = new DataPathProperties();
        properties.setPath(directory.toString());
        new AddressDataLoader(store, properties).run(null);
        verify(store, never()).save(anyList(), anyString(), anyString(), anyLong(), anyBoolean());
    }
}
