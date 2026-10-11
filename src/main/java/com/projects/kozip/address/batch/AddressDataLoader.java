package com.projects.kozip.address.batch;

import com.projects.kozip.address.entity.RoadAddress;
import com.projects.kozip.address.repository.RoadAddressRepository;
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

@Slf4j
@Component
@ConditionalOnProperty(
        prefix = "kozip.data",
        name = "load-enabled",
        havingValue = "true"
)
@RequiredArgsConstructor
public class AddressDataLoader implements ApplicationRunner {

    private final RoadAddressRepository repository;
    private final DataPathProperties properties;

    private static final int BATCH_SIZE = 5000;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (repository.count() > 0) {
            log.info("주소 데이터가 이미 존재합니다. 적재를 건너뜁니다.");
            return;
        }

        File dir = new File(properties.getPath());
        File[] files = dir.listFiles((d, name) ->
                name.startsWith("rnaddrkor_") && name.endsWith(".txt"));

        if (files == null || files.length == 0) {
            log.warn("적재할 파일이 없습니다. 경로: {}", properties.getPath());
            return;
        }

        for (File file : files) {
            log.info("적재 시작: {}", file.getName());
            loadFile(file);
            log.info("적재 완료: {}", file.getName());
        }
    }

    private void loadFile(File file) throws Exception {
        List<RoadAddress> batch = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), Charset.forName("EUC-KR")))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String[] col = line.split("\\|", -1);
                if (col.length < 24) continue;

                batch.add(parse(col));

                if (batch.size() == BATCH_SIZE) {
                    repository.saveAll(batch);
                    batch.clear();
                }
            }

            if (!batch.isEmpty()) {
                repository.saveAll(batch);
            }
        }
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