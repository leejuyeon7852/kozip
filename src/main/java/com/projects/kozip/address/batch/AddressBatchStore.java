package com.projects.kozip.address.batch;

import com.projects.kozip.address.entity.RoadAddress;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.util.List;

@Component
@ConditionalOnProperty(prefix = "kozip.data", name = "load-enabled", havingValue = "true")
public class AddressBatchStore {
    private final JdbcTemplate jdbc;
    private final TransactionTemplate transaction;

    public AddressBatchStore(DataSource dataSource) {
        jdbc = new JdbcTemplate(dataSource);
        transaction = new TransactionTemplate(new JdbcTransactionManager(dataSource));
    }

    // 진행 기록용 테이블
    public void initialize() {
        jdbc.execute("""
                CREATE TABLE IF NOT EXISTS address_import_progress (
                    file_name VARCHAR(255) NOT NULL,
                    fingerprint CHAR(64) NOT NULL,
                    line_number BIGINT NOT NULL,
                    completed BOOLEAN NOT NULL,
                    PRIMARY KEY (file_name, fingerprint)
                ) ENGINE=InnoDB
                """);
    }

    public Progress progress(String fileName, String fingerprint) {
        List<Progress> result = jdbc.query("""
                SELECT line_number, completed FROM address_import_progress
                WHERE file_name = ? AND fingerprint = ?
                """, (rs, row) -> new Progress(rs.getLong(1), rs.getBoolean(2)), fileName, fingerprint);
        return result.isEmpty() ? new Progress(0, false) : result.getFirst();
    }

    // Address rows and their checkpoint must commit together, including on retry after a lost connection.
    public void save(List<RoadAddress> addresses, String fileName, String fingerprint,
                     long lineNumber, boolean completed) {
        transaction.executeWithoutResult(status -> {
            if (!addresses.isEmpty()) {
                jdbc.batchUpdate("""
                        INSERT INTO road_address (manage_no, sido, sigungu, eupmyeondong, ri,
                            jibun_main, jibun_sub, road_name, building_main, building_sub, dong,
                            zip_code, old_road_address, is_apartment, building_name)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        ON DUPLICATE KEY UPDATE manage_no = road_address.manage_no
                        """, addresses, addresses.size(), (ps, a) -> {
                    ps.setString(1, a.getManageNo());
                    ps.setString(2, a.getSido());
                    ps.setString(3, a.getSigungu());
                    ps.setString(4, a.getEupmyeondong());
                    ps.setString(5, a.getRi());
                    ps.setInt(6, a.getJibunMain());
                    ps.setInt(7, a.getJibunSub());
                    ps.setString(8, a.getRoadName());
                    ps.setInt(9, a.getBuildingMain());
                    ps.setInt(10, a.getBuildingSub());
                    ps.setString(11, a.getDong());
                    ps.setString(12, a.getZipCode());
                    ps.setString(13, a.getOldRoadAddress());
                    ps.setString(14, a.getIsApartment());
                    ps.setString(15, a.getBuildingName());
                });
            }
            jdbc.update("""
                    INSERT INTO address_import_progress (file_name, fingerprint, line_number, completed)
                    VALUES (?, ?, ?, ?)
                    ON DUPLICATE KEY UPDATE line_number = ?, completed = ?
                    """, fileName, fingerprint, lineNumber, completed, lineNumber, completed);
        });
    }

    public record Progress(long lineNumber, boolean completed) {}
}
