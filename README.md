# 🗺️ KoZip

> 대한민국 우편번호 및 주소 검색 API

주소기반산업지원서비스의 공공 주소 데이터를 활용하여 우편번호, 도로명, 건물명 등으로 주소를 검색하는 프로젝트입니다.

## ✨ 주요 기능

- 5자리 우편번호 검색
- 도로명·건물명 등 주소 키워드 검색
- 도로명과 건물번호를 조합한 검색
- 도로명주소·지번주소·우편번호 반환
- 검색어가 너무 짧거나 결과가 많을 때 안내 메시지 반환
- 공공 주소 데이터 파일 적재
- 환경변수를 통한 자동 데이터 적재 ON/OFF

## 🛠️ 기술 스택

| 구분 | 기술 |
| --- | --- |
| 언어 | Java 21 |
| 프레임워크 | Spring Boot |
| 데이터 접근 | Spring Data JPA |
| 데이터베이스 | MySQL |
| 검색 | MySQL FULLTEXT · ngram |
| 빌드 | Gradle |
| API 문서 | springdoc-openapi · Swagger UI |
| 컨테이너 | Docker |
| 부하 테스트 | Gatling · 별도 Maven 모듈 |

## 📁 프로젝트 구조

```text
kozip/
├── .github/
├── gradle/
├── loadtest/
├── src/
│   └── main/
│       ├── java/com/projects/kozip/
│       │   ├── address/
│       │   │   ├── batch/
│       │   │   ├── controller/
│       │   │   ├── dto/
│       │   │   ├── entity/
│       │   │   ├── repository/
│       │   │   └── service/
│       │   └── config/
│       └── resources/
│           └── application.yml
├── .dockerignore
├── .gitignore
├── Dockerfile
└── build.gradle
```

## ⚙️ 환경변수

| 변수 | 설명 | 기본값 |
| --- | --- | --- |
| `DB_URL` | MySQL JDBC 연결 주소 | 없음 · 필수 |
| `DB_USERNAME` | DB 사용자명 | 없음 · 필수 |
| `DB_PASSWORD` | DB 비밀번호 | 없음 · 필수 |
| `DATA_PATH` | 주소 데이터 파일이 있는 디렉터리 | 빈 문자열 |
| `DATA_LOAD_ENABLED` | 서버 시작 시 자동 데이터 적재 여부 | `false` |
| `DATA_BATCH_SIZE` | 한 트랜잭션으로 저장할 주소 수 | `1000` |

자동 적재를 켤 때는 접근 가능한 `DATA_PATH`를 지정해야 합니다.
자동 적재를 끈 경우에도 검색에 사용할 데이터와 인덱스는 DB에 준비되어 있어야 합니다.

### 주소 데이터 적재 및 재개

- 기존 `road_address`와 FULLTEXT 인덱스를 유지한 채 실행합니다. 테이블을 비울 필요가 없습니다.
- JDBC 배치와 `rewriteBatchedStatements`로 주소별 조회 없이 묶어서 저장합니다.
- 기본키 `manage_no`가 이미 있으면 기존 주소를 유지합니다. 이 적재기는 신규 데이터 보충용이며 기존 주소 변경·삭제 동기화는 하지 않습니다.
- `address_import_progress` 테이블을 자동 생성합니다. 파일명과 SHA-256, 마지막 저장 행, 완료 여부를 기록합니다. DB 계정에 테이블 생성 권한이 필요합니다.
- 주소 저장과 진행 위치 저장은 같은 트랜잭션입니다. 연결 오류(SQLState `08`)는 최대 두 번 재시도하고 실패하면 종료합니다.
- 기존 방식으로 일부 적재한 DB는 처음 한 번 파일 앞부분을 읽고 중복을 건너뜁니다. 이후 재실행은 마지막 커밋 행 이후부터 저장합니다. 재개 시에도 해시 계산과 앞부분 읽기는 필요합니다.
- 같은 파일명이라도 내용이 변경되면 새 파일로 취급합니다. 적재 중에는 파일을 변경하지 마세요. 적재 프로세스는 한 개만 실행하세요.
- 잘못된 행은 파일명과 행 번호를 표시하고 중단합니다. 로그의 행 수는 처리 위치이며 새로 삽입한 주소 수가 아닙니다.
- 중지 후 동일한 `DATA_PATH`와 `DATA_LOAD_ENABLED=true`로 다시 실행하면 재개합니다. 적재가 끝나면 운영 앱은 `DATA_LOAD_ENABLED=false`로 실행합니다.
- 주소 테이블을 직접 비운 경우 진행 테이블도 함께 비워야 재적재됩니다. 진행 테이블만 비워도 기존 주소는 중복 저장되지 않습니다.

## 💻 로컬 실행

Java 21과 MySQL을 준비하고, IntelliJ 실행 구성에 환경변수를 설정합니다.

```dotenv
DB_URL=jdbc:mysql://localhost:3306/kozip?serverTimezone=Asia/Seoul&characterEncoding=UTF-8
DB_USERNAME=your_username
DB_PASSWORD=your_password
DATA_LOAD_ENABLED=false
```

`KozipApplication`을 실행하거나 프로젝트 루트에서 다음 명령을 실행합니다.
터미널에서 실행할 경우 환경변수를 해당 터미널에도 설정해야 합니다.

```bash
./gradlew bootRun
```

- Swagger UI: http://localhost:8080/swagger-ui.html
- 검색 예시: http://localhost:8080/api/address?query=06236

## 🐳 Docker 실행

아래 명령은 Git Bash 기준이며, `build.gradle`과 `Dockerfile`이 있는 폴더에서 실행합니다.

### 🔐 환경변수 파일

Dockerfile 옆에 `.env.docker` 파일을 생성합니다.

```dotenv
DB_URL=jdbc:mysql://host.docker.internal:3306/kozip?serverTimezone=Asia/Seoul&characterEncoding=UTF-8
DB_USERNAME=your_username
DB_PASSWORD=your_password
DATA_LOAD_ENABLED=false
```

`.env.docker`는 `.gitignore`와 `.dockerignore`에 등록하여
Git 및 Docker 빌드 대상에서 제외합니다.

Docker Desktop 환경에서 `host.docker.internal`은 호스트 PC에 연결할 때 사용합니다.
MySQL이 PC의 3306 포트로 접근 가능해야 합니다.

### 📦 JAR 및 이미지 빌드

```bash
./gradlew bootJar
docker build -t kozip:local .
```

Dockerfile은 빌드된 `build/libs/kozip-0.0.1-SNAPSHOT.jar`를 사용합니다.
소스 코드를 수정했다면 JAR와 이미지를 다시 빌드해야 합니다.

### ▶️ 컨테이너 실행

```bash
docker run --name kozip-local \
  --env-file .env.docker \
  -p 8081:8080 \
  kozip:local
```

호스트의 8081 포트를 컨테이너의 8080 포트에 연결합니다.

- Swagger UI: http://localhost:8081/swagger-ui.html
- 검색 예시: http://localhost:8081/api/address?query=06236

### ⏹️ 중지 및 재시작

```bash
docker stop kozip-local
docker start -a kozip-local
```

이미 생성된 컨테이너는 `docker start`로 재시작합니다.

### 📋 로그 확인

```bash
docker logs -f kozip-local
```

## ☁️ AWS 배포 및 CI/CD

- Amazon ECR에 Docker 이미지를 저장하고, Amazon ECS Fargate에서 앱을 실행합니다.
- 주소 데이터는 Amazon RDS for MySQL에 저장하며, 앱 로그는 CloudWatch에서 확인합니다.
- `main` 대상 PR에서는 테스트와 JAR 빌드를 수행합니다.
- `main`에 push하면 빌드한 JAR로 Docker 이미지를 생성하고, 커밋 SHA를 태그로 ECR에 업로드합니다.
- GitHub Actions는 OIDC로 AWS 역할을 사용하고, 새 이미지로 태스크 정의를 갱신하여 `kozip-service`에 배포합니다.
- 운영 앱은 `DATA_LOAD_ENABLED=false`, `SPRING_JPA_HIBERNATE_DDL_AUTO=none`으로 실행합니다.
- 배포 작업은 `wait-for-service-stability: true`로 ECS 서비스 안정화를 기다립니다. 이 확인은 API 요청 검증을 대신하지 않으므로 검색 응답은 별도로 확인합니다.

### 배포 시간 확인

2026년 10월 13일 배포 실행 기록을 10월 14일에 확인했습니다. 다음 값은 해당 실행의 관측값이며 매번 보장되는 시간은 아닙니다.

| 단계 | 소요 시간 |
| --- | --- |
| build 작업 | 24초 |
| Docker 이미지 빌드 및 ECR 업로드 단계 | 35초 |
| ECS 배포 및 안정화 대기 단계 | 5분 37초 |

- Spring Boot 시작 로그: 19:03:26 KST, 앱 시작 약 40초.
- ECS 이벤트: 19:02 새 태스크 시작 → 19:03 기존 태스크 종료 → 19:06 배포 완료. 이벤트 시각은 분 단위로 표시되었습니다.
- 관측한 세 구간 중 ECS 배포 단계가 약 85%를 차지했습니다. 앱 시작 이후에도 배포 완료까지 약 2~3분이 걸렸으며, 이 구간의 정확한 원인과 Actions의 상태 조회 지연 여부는 확인하지 않았습니다.
- 현재 배포는 정상 완료되므로 안정화 대기를 유지하고, 추가 시간 최적화는 보류합니다.

### 프로젝트 범위와 보류한 개선점

주소 검색 API, 공공 주소 데이터 적재, AWS 컨테이너 배포와 CI/CD 구축을 현재 프로젝트 범위로 마무리합니다. 다음 항목은 필요해질 때 검토합니다.

- 배포 후 health 및 검색 응답 검증 자동화.
- ALB 등을 이용한 고정 접속 주소 구성. 현재는 태스크 교체 시 퍼블릭 IP를 다시 확인합니다.
- ECS 배포 및 안정화 대기 시간의 추가 분석과 최적화.
