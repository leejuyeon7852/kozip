package com.projects.kozip.address.batch;

import com.projects.kozip.address.entity.RoadAddress;
import com.projects.kozip.config.DataPathProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Comparator;
import java.security.MessageDigest;
import java.util.HexFormat;
import org.springframework.dao.DataAccessException;
import java.sql.SQLException;

@Slf4j
@Component
@ConditionalOnProperty(
        prefix = "kozip.data",
        name = "load-enabled",
        havingValue = "true"
)
@RequiredArgsConstructor
public class AddressDataLoader implements ApplicationRunner {

    private final AddressBatchStore store;
    private final DataPathProperties properties;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (properties.getPath() == null || properties.getPath().isBlank()
                || properties.getBatchSize() < 1) {
            throw new IllegalArgumentException("DATA_PATH와 양수 DATA_BATCH_SIZE를 설정해야 합니다.");
        }

        File dir = new File(properties.getPath());
        File[] files = dir.listFiles((d, name) ->
                name.startsWith("rnaddrkor_") && name.endsWith(".txt"));

        if (files == null || files.length == 0) {
            log.warn("적재할 파일이 없습니다. 경로: {}", properties.getPath());
            return;
        }

        store.initialize();
        Arrays.sort(files, Comparator.comparing(File::getName));
        for (File file : files) {
            log.info("적재 시작: {}", file.getName());
            loadFile(file);
            log.info("적재 완료: {}", file.getName());
        }
    }

    private void loadFile(File file) throws Exception {
        String fingerprint = fingerprint(file);
        AddressBatchStore.Progress progress = store.progress(file.getName(), fingerprint);
        if (progress.completed()) {
            log.info("이미 완료된 파일: {}", file.getName());
            return;
        }
        List<RoadAddress> batch = new ArrayList<>(properties.getBatchSize());
        long lineNumber = 0;
        long lastCheckpoint = progress.lineNumber();
        log.info("{}: {}행 이후부터 재개", file.getName(), lastCheckpoint);

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), Charset.forName("EUC-KR")))) {

            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (lineNumber <= progress.lineNumber()) continue;
                String[] col = line.split("\\|", -1);
                if (col.length < 24) {
                    throw new IllegalArgumentException(file.getName() + ": " + lineNumber + "행의 필드 수가 부족합니다.");
                }
                try {
                    batch.add(parse(col));
                } catch (RuntimeException ex) {
                    throw new IllegalArgumentException(file.getName() + ": " + lineNumber + "행 파싱 실패", ex);
                }
                if (batch.size() == properties.getBatchSize()) {
                    saveWithRetry(batch, file, fingerprint, lineNumber, false);
                    batch.clear();
                    lastCheckpoint = lineNumber;
                    log.info("{}: {}행 저장 완료", file.getName(), lineNumber);
                }
            }

            if (lineNumber < lastCheckpoint) {
                throw new IllegalStateException("파일이 적재 도중 변경되었습니다: " + file.getName());
            }
            saveWithRetry(batch, file, fingerprint, lineNumber, true);
        }
    }

    // SHA-256 해시값을 문자열로 반환
    private String fingerprint(File file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (FileInputStream input = new FileInputStream(file)) {
            byte[] buffer = new byte[65536];
            int size;
            while ((size = input.read(buffer)) != -1) digest.update(buffer, 0, size);
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    // 저장할 때 연결오류가 나도 재시도
    private void saveWithRetry(List<RoadAddress> batch, File file, String fingerprint,
                               long lineNumber, boolean completed) throws InterruptedException {
        for (int attempt = 1; ; attempt++) {
            try {
                store.save(batch, file.getName(), fingerprint, lineNumber, completed);
                return;
            } catch (DataAccessException ex) {
                if (attempt >= 3 || !isConnectionFailure(ex)) throw ex;
                log.warn("{}: {}행 저장 중 연결 실패, 재시도 {}/2", file.getName(), lineNumber, attempt);
                Thread.sleep(1000L * attempt);
            }
        }
    }

    // 재시도할 오류인지 구분
    private boolean isConnectionFailure(Throwable ex) {
        for (Throwable cause = ex; cause != null; cause = cause.getCause()) {
            if (cause instanceof SQLException sql && sql.getSQLState() != null
                    && sql.getSQLState().startsWith("08")) return true;
        }
        return false;
    }

    private RoadAddress parse(String[] col) {
        String buildingName = !col[22].isBlank() ? col[22] : col[23];

        return RoadAddress.builder()
                .manageNo(col[0])
                .sido(col[2])
                .sigungu(col[3].isBlank() ? null : col[3])
                .eupmyeondong(col[4])
                .ri(col[5].isBlank() ? null : col[5])
                .jibunMain(Integer.parseInt(col[7]))
                .jibunSub(Integer.parseInt(col[8]))
                .roadName(col[10])
                .buildingMain(Integer.parseInt(col[12]))
                .buildingSub(Integer.parseInt(col[13]))
                .dong(col[15].isBlank() ? null : col[15])
                .zipCode(col[16])
                .oldRoadAddress(col[17].isBlank() ? null : col[17])
                .isApartment(col[19])
                .buildingName(buildingName.isBlank() ? null : buildingName)
                .build();
    }
}