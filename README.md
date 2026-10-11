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

자동 적재를 켤 때는 접근 가능한 `DATA_PATH`를 지정해야 합니다.
자동 적재를 끈 경우에도 검색에 사용할 데이터와 인덱스는 DB에 준비되어 있어야 합니다.

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