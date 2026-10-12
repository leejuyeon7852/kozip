package com.projects.kozip.address.batch;

import com.projects.kozip.address.entity.RoadAddress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AddressBatchStoreTest {
    private JdbcTemplate jdbc;
    private AddressBatchStore store;

    @BeforeEach
    void setup() {
        var source = new DriverManagerDataSource("jdbc:h2:mem:" + UUID.randomUUID()
                + ";MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
        jdbc = new JdbcTemplate(source);
        jdbc.execute("""
                CREATE TABLE road_address (
                    manage_no VARCHAR(26) PRIMARY KEY, sido VARCHAR(40) NOT NULL,
                    sigungu VARCHAR(40), eupmyeondong VARCHAR(40) NOT NULL, ri VARCHAR(40),
                    jibun_main INT, jibun_sub INT, road_name VARCHAR(80) NOT NULL,
                    building_main INT, building_sub INT, dong VARCHAR(60), zip_code VARCHAR(5),
                    old_road_address VARCHAR(400), is_apartment VARCHAR(1), building_name VARCHAR(400))
                """);
        store = new AddressBatchStore(source);
        store.initialize();
    }

    @Test
    void duplicateReplayPreservesRowsAndAdvancesCheckpoint() {
        store.save(List.of(address("1", "부산")), "file", "hash", 1, false);
        store.save(List.of(address("1", "변경"), address("2", "충북")), "file", "hash", 2, true);
        assertEquals(2, jdbc.queryForObject("SELECT COUNT(*) FROM road_address", Integer.class));
        assertEquals("부산", jdbc.queryForObject("SELECT sido FROM road_address WHERE manage_no='1'", String.class));
        assertEquals(new AddressBatchStore.Progress(2, true), store.progress("file", "hash"));
        assertEquals(new AddressBatchStore.Progress(0, false), store.progress("file", "changed-hash"));
    }

    @Test
    void failedBatchRollsBackBothRowsAndCheckpoint() {
        store.save(List.of(address("1", "부산")), "file", "hash", 1, false);
        assertThrows(Exception.class, () -> store.save(
                List.of(address("2", "충북"), address("3", null)), "file", "hash", 3, true));
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM road_address", Integer.class));
        assertEquals(new AddressBatchStore.Progress(1, false), store.progress("file", "hash"));
    }

    private RoadAddress address(String id, String sido) {
        return RoadAddress.builder().manageNo(id).sido(sido).eupmyeondong("동")
                .roadName("도로").zipCode("12345").isApartment("0").build();
    }

    @Test
    void checkpointFailureAlsoRollsBackInsertedAddresses() {
        jdbc.execute("ALTER TABLE address_import_progress ADD CONSTRAINT valid_line CHECK (line_number < 2)");
        store.save(List.of(address("1", "부산")), "file", "hash", 1, false);
        assertThrows(Exception.class, () -> store.save(
                List.of(address("2", "충북")), "file", "hash", 2, false));
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM road_address", Integer.class));
        assertEquals(new AddressBatchStore.Progress(1, false), store.progress("file", "hash"));
    }
}
