# CodeChef 

![프로젝트 대표 이미지](./CodeChef.png)

현업 개발자가 예비 개발자의 코드를 리뷰하고 성장하는 커뮤니티 입니다.
---

## KEY Summary

### 🍁 **기술적 의사 결정: Elasticsearch를 사용해 랭킹 시스템을 구현한 이유 **

1. **배경**
   - 코드 리뷰 커뮤니티에선 코드에 대한 문제를 해결하거나, 정보를 찾기 위해 키워드로 검색을 많이 시도합니다.
     그러므로 사용자들이 자주 검색하는 키워드를 랭킹으로 보여주면 비슷한 문제를 가진 사용자들이 관련 질문과 답변을 쉽게 찾을 수 있습니다.
     또 주요 주제가 실시간으로 반영되면서, 커뮤니티의 흐름을 파악하는것이 중요했습니다.

2. **선택지**
   1. MySQL을 사용하여 검색어와 검색 횟수를 DB에 저장하고, SQL 쿼리를 사용해 랭킹 계산하는 방법
   2. Redis의 ZSet을 사용하여 실시간 검색어 랭킹 계산하는 방법
   3. Elasticsearch의 Terms Aggregation을 사용하여 랭킹 계산하는 방법


3. **요구사항**
   1. 특정 기간 동안 검색된 검색어 중 **가장 많이 검색된 10개 키워드**와 해당 **검색 횟수**를 집계하여 반환해야 한다.
   2. 검색어는 로그로 저장되며, 대규모 데이터에도 처리가 적합하여야 한다.
   3. 새로운 검색 요청이 들어올 경우, 해당 데이터는 실시간으로 저장되고 다음 랭킹 결과에 반영되어야 한다.

4. **도입 이유 및 해결 방안**
   - MySQL은 데이터의 일관성 유지와 데이터 저장에 강점이 있고 복잡한 쿼리의 처리가 가능하지만, 실시간 업데이트에 취약하고 대규모 데이터 처리에서 비효율적입니다.
      
     Redis의 ZSet은 실시간 업데이트 측면에서는 적합하지만, 메모리 기반으로 동작하므로 저장 가능한 데이터의 크기가 서버 메모리 용량에 의존합니다.
     즉, 검색 로그가 점점 증가하면, 메모리 한계로 인해 데이터 손실이나 성능 저하가 발생할 수 있습니다. 또한 별도의 시각화가 어렵다는 단점이 있습니다.
   
     Elasticsearch는 Redis에 비해 대규모 데이터 처리에 적합할 뿐만 아니라, Terms Aggregation 기능으로 다양한 필터링 기능을 쉽게 추가할 수 있습니다. 또한, Kibana를 통해 손쉽게 데이터 시각화도 가능합니다.
     그러므로 우리 프로젝트에선 Elasticsearch를 사용하여 실시간 인기 검색어 랭킹을 구현하기로 결정하였습니다.
---

### 🍁 **트러블 슈팅: **

1. **문제**  
   - 실시간 게시물 랭킹 이나 유저 랭킹을 조회시에 어떠한 자료구조를 선택하는 것이 적합할까에 대해서 고민이 발생하였습니다.

   **해결 방안**  
   - 레디스의 Sorted Set 자료 구조를 사용하게 되었습니다.
     여러가지 자료구조 중(List,String,Set … 등등 ) Sorted Set을 사용하게 된 이유는, 
     점수나 조회수 기준으로 3등까지 랭킹을 조회해야하는 기능을 구현해야 했어야 했습니다.
     이러한 요구사항을 바탕으로 Sorted Set의 특징으로는 각 요소는 고유한 값(value)과 점수(score)를 가집니다 이러한 형태로 데이터를 삽입 하고, 
     데이터 삽입 시점에 점수를 기준으로 **자동으로 오름차순 정렬**이 되고, 정렬 연산 없이 정렬된 데이터를 효율적으로 조회 가능하고, 
     일반적인 시간 복잡도로는 **O(log(N))** 이므로 프로젝트에 랭킹 구현에 적합하다고 판단 했습니다.

2. **문제**  
   - 실시간 게시물 조회나 랭킹 조회시에 캐싱을 사용하는데 어떤식으로 레디스에 테이터를 관리하는것이 적합할까에 대해서 고민이 발생했습니다.

   **해결 방안**  
   - **Write-Through 및 Read-Through 전략 을 사용하게 되었습니다.**
     **쓰기전략에는 Write-Through 전략을 선택한 이유는 다음과 같습니다. 저희 프로젝트에는 캐싱과 db에 일관성 있는 데이터, 
     레디스의 최신 상태를 유지를 하는게 가장 중요하다고 판단 했습니다. 그래서 데이터 쓰기 시점에 DB와 레디스에 동시에 기록되고, 
     쓰**기 시점에 즉시 업데이트 되므로 캐시 데이터가 최신 데이터로 유지가 가능한**Write-Through 전략을 선택하게 되었습니다.**

     읽기전략에는 **Read-Through 전략**을 선택한 이유는 다음과 같습니다. 당시 프로젝트에 **조회 요청이 빈번하게 발생하며,
     빠른 응답 속도가 사용자 경험에 매우 중요**하다고 판단했기 때문입니다. Read-Through 전략은 데이터 조회시 **캐시에서 데이터를 우선적으로 조회**하고, 
     만약 캐시에 데이터가 없는 경우에만 **DB에서 데이터를 가져와 캐시에 저장**합니다. 이를 통해, **대부분의 읽기 요청을 캐시에서 처리**함으로써 DB 부하를 줄이고,
     **빠른 응답 속도**를 제공할 수 있습니다. 또한, 캐시 미스(Cache Miss) 발생 시 데이터를 자동으로 갱신하는 장점이 있어서 선택하게 되었습니다.

---

## 인프라 아키텍처 & 적용 기술

### 아키텍처 다이어그램
![Infra Architecture](./service_architecture.png)

위 아키텍처는 **** 구조를 나타냅니다.  


<details>
<summary><b>📦 적용 기술 상세보기</b></summary>

### 💾 ****
- **Redis**  
  - 

### 📬 **메시징 시스템**
- **Kafka**  
  - 

### 🌐 **인프라 및 배포**
- **Docker**  
  - 
- **Prometheus & Grafana**  
  - 

</details>



## 주요 기능

### 🍁 **검색 : Elasticsearch를 사용한 검색 기능 구현**
- Elasticsearch를 사용하여 게시글 검색 기능 구현

### 🍁 ****
- 

### 🍁 ****
- 

---

## 기술적 고도화
<details>
<summary><b>🍁 인덱스 도입으로 검색 성능 30% 개선</b></summary>
[문제 인식]

게시글 검색을 위해 풀 테이블 스캔을 하는건 너무 비효율적이고 성능 소비가 심하다.

대량의 데이터가 생길수록 응답 속도는 더 큰 차이를 보인다. 그래서 풀 테이블 스캔이 아니라, 인덱스를 생성하여 데이터 조회 속도를 높이고, 리소스 사용을 최적화 한다.

인덱스

책의 색인처럼 특정 데이터를 빠르게 찾을 수 있도록 하는 특별한 데이터 구조이다.

- **B-Tree 인덱스**: 범위 검색, 정렬된 데이터 조회에 적합하여 대부분의 데이터베이스에서 기본적으로 사용된다.
- **Hash 인덱스**: 특정 값과 일치하는 데이터를 찾는 데 빠르며, 범위 검색에는 적합하지 않다.

[선택 이유]

- **검색 속도를 높인다**: 인덱스가 있으면 특정 데이터 위치를 바로 찾아가니까 전체 테이블을 검색할 필요 없이 빠르게 결과를 얻는다.
- **응답 시간을 줄인다**: 쿼리 실행 시 필요한 데이터만 추려내기 때문에, 응답 시간이 짧아져 사용자에게 빠르게 결과를 보여준다.
- **데이터베이스 성능을 개선한다**: 인덱스를 사용하면 데이터베이스가 더 적은 자원으로 원하는 데이터를 찾아내 성능이 전반적으로 향상된다.
- **처리 부담을 낮춘다**: 전체 테이블 스캔을 하면 데이터가 많아질수록 부담이 커지지만, 인덱스는 필요한 데이터만 조회하기 때문에 부담이 훨씬 덜하다.
- **쿼리 최적화를 돕는다**: 인덱스가 적절하게 설정되어 있으면 복잡한 쿼리에서도 최적의 검색 경로를 찾을 수 있어 쿼리가 효율적으로 실행된다

[해결 방안]	

Board 엔티티에 @Index 어노테이션을 설정하여, 기본 인덱스를 설정하였다.

하지만 애플리케이션 실행 중, 오류가 발생하였는데, MYSQL의 InnoDB는 인덱스의 최대 크기를 3072byte로 제한을 한다.

![](assets/images/index/entity.png)

Board 엔티티의 title과 contents를 중심으로 검색하려고 하면, 4 * 2200 즉 8800바이트가 발생한다.

그래서 우리는 기본 인덱스를 사용하지 않고, 이를 해결해줄 수 있는 부분 인덱스를 사용하기로 한다.

![](assets/images/index/createPartialIndex.png)

애플리케이션을 실행할 때 Local에서 CREATE INDEX boards_title_contents_partial_index ON boards (title(50), contents(200))”; 쿼리를 실행한다.

이는 title의 길이 50, contents의 길이 200까지만 인덱스를 설정한다는 뜻이다.

그래서 인덱스를 부분으로 걸어 인덱스의 크기를 줄이고 저장 공간을 절약한다.

![](assets/images/index/checkIndex.png)

인덱싱이 정상적으로 등록됐다.

![](assets/images/index/jmeter_index_output.png)

10만개의 더미 데이터를 넣고 조회를 100번 하여 성능 비교를 했는데 놀랍게도 차이가 없다.

![](assets/images/index/old_query.png)

확인 해보니 LIKE문에서 인덱스을 걸려면 CONCAT문과 함께 사용해야 한다.

![](assets/images/index/new_query.png)

코드를 이렇게 수정하면 인덱스가 적용 된다.

![](assets/images/index/range.png)

type = range로 boardSearch 쿼리 인덱싱 적용 완료 확인

[해결 완료]
- 결과

| 처리 방식 | 평균 응답시간 | 처리량 | 표준 편차 | 최소 응답시간 | 최대 응답시간 |
| --- | --- | --- | --- | --- | --- |
| 인덱스 | 38 | 25.9/sec | 2.83 | 37 | 65 |
| DB | 54 | 18.4/sec | 1.17 | 53 | 62 |

인덱싱을 적용한 쿼리가 이전 쿼리에 비해 약 30%의 빠른 평균 응답 속도와 처리량을 보인다.
</details>

<details>
<summary><b>🍁 배포 ci/cd</b></summary>
   
### [내가 구현한 기능]

### CICD

---

### [주요 로직]

**GitHub Actions 시나리오**

개발자가 `main` 브랜치에 푸시하면, GitHub Actions 워크플로우가 자동 실행된다. 해당 워크플로우는 다음 단계를 포함한다.

1. **리포지토리 체크아웃**
    - `actions/checkout@v4`를 통해 애플리케이션 코드 리포지토리를 체크아웃한다.
2. **EC2 SSH 설정**
    - EC2 연결에 필요한 `EC2_HOST`, `EC2_USER`, `SSH_PRIVATE_KEY` 환경 변수를 사용해 SSH 설정을 구성한다.
    - `SSH_PRIVATE_KEY`를 파일로 저장해 SSH 접근 권한을 설정한다.
3. **자바 환경 구성**
    - `actions/setup-java@v4`를 사용해 자바 17 환경을 설치하고, Gradle 빌드를 위한 설정을 마친다.
4. **Gradle 빌드**
    - `gradlew` 파일에 실행 권한을 부여하고, `./gradlew build` 명령어로 프로젝트를 Gradle 빌드해 애플리케이션 JAR 파일을 생성한다.
5. **Docker 이미지 빌드 및 DockerHub 푸시**
    - `docker/login-action@v1`를 통해 DockerHub에 로그인한 후, Docker 이미지를 `DOCKER_USERNAME`/`my-app:latest` 이름으로 빌드하여 DockerHub에 푸시한다.
6. **EC2 서버에 SSH 접속 후 배포**
    - `appleboy/ssh-action@v1.1.0`을 사용해 EC2 서버에 SSH 접속한 후, 다음과 같은 배포 과정을 진행한다:
        - DockerHub에 로그인 후, 기존 애플리케이션 컨테이너를 중지하고, 사용하지 않는 컨테이너 및 이미지를 정리해 공간을 확보한다.
        - DockerHub에서 최신 애플리케이션 이미지를 `pull`한 후, 환경 변수 설정 파일 `.env`를 적용해 포트 8080에서 애플리케이션 컨테이너를 실행한다.

---

이를 통해 EC2 서버에서 최신 애플리케이션 상태가 유지될 수 있도록 하며, 자동화된 배포 과정을 완성한다.

---

### [배경]

기존 수동 배포 과정은 속도 저하와 반복 작업으로 인한 비효율성을 초래했다. 자동화된 CI/CD 프로세스를 도입해 배포를 최적화할 필요가 있었다.

---

### [요구사항]

1. CI/CD 파이프라인 자동화
2. EC2 서버와의 안전한 통신 및 배포 설정
3. Docker Hub와의 간편한 연계 및 이미지 관리

---

### [선택지]

1. Jenkins
2. GitHub Actions

---

### [의사결정/사유]

**배경**

당시 EC2 프리 티어 인스턴스를 사용 중이며, 메모리는 1GB이다. Docker와 Jenkins를 동시에 구동하려 했으나 성능이 크게 저하되는 문제가 발생했다. Jenkins는 일반적으로 2GB 이상의 메모리를 요구하고 Docker로 구동할 때에도 메모리 여유가 필요해, 현재 인스턴스로는 Docker와 Jenkins를 함께 운영하기 어려웠다. 결과적으로 최소 4GB 이상의 메모리가 필요할 것으로 보였다.

**문제 해결**

EC2 인스턴스를 업그레이드하거나, Jenkins를 로컬에서 구동하는 방안을 검토했으나 Jenkins 대신 **GitHub Actions**를 사용하기로 결정했다. 이는 Jenkins의 설정 부담을 줄이고 EC2 환경에서 CI/CD를 효과적으로 구현할 수 있는 대안으로 판단했다.

**성과**

GitHub Actions를 통해 CI/CD 파이프라인을 자동화하며 수동 배포 과정을 효율적으로 개선했다. 배포 속도가 크게 단축되었고 인적 오류가 줄어들어 전체적인 개발 생산성이 높아졌다.

---

### [회고]

### 1. **Jenkins 대신 GitHub Actions 사용의 이점**

GitHub Actions는 Jenkins와 비교해 다음과 같은 장점을 지닌다:

- **간편한 통합**: GitHub Actions는 GitHub 리포지토리와 바로 연결되어 추가 설정 없이 CI/CD 파이프라인을 쉽게 구성할 수 있다. Jenkins는 GitHub 통합을 위해 웹훅이나 플러그인 설정이 필요하다.
- **비용 절감**: GitHub Actions는 퍼블릭 리포지토리에서 무료로 제공되며, 기본 요금제에도 일정량의 워크플로우 사용이 포함되어 비용 절감에 효과적이다. Jenkins는 서버 운영과 클라우드 사용에 따른 추가 비용이 발생할 수 있다.
- **관리 편의성과 유연한 스케일링**: GitHub Actions는 서버리스로 제공되며 추가 서버 관리가 필요 없고, Jenkins와 달리 확장 시 별도 서버 관리가 필요 없다.
- **YAML 기반 구성**: 설정 파일이 `.yml` 형식으로 기록되어 버전 관리와 코드 리뷰가 용이해 협업에 유리하다.
- **병렬 실행 지원**: GitHub Actions는 다양한 작업을 병렬로 쉽게 실행할 수 있으며, 자체 호스팅 러너로 특정 작업을 분산 처리할 수 있다. Jenkins도 병렬 빌드를 지원하지만, 설정이 더 복잡하다.

### 2. **GitHub Actions 사용 시 Docker Hub의 장점**

Docker 이미지를 저장할 레지스트리로 Docker Hub를 선택하는 경우, AWS ECR과 비교해 다음과 같은 장점이 있다:

- **사용성과 접근성**: Docker Hub는 Docker 커뮤니티의 기본 레지스트리로 널리 사용되며, 퍼블릭 이미지의 배포와 접근이 용이하다.
- **간편한 설정**: Docker Hub는 GitHub Actions와 쉽게 연동되며 간단한 인증으로 설정할 수 있다. AWS ECR을 사용하려면 IAM 역할 및 정책 설정 같은 추가 작업이 필요하다.
- **공개 이미지 관리 편리성**: 오픈 소스 프로젝트나 외부에 Docker 이미지를 배포할 경우 Docker Hub가 더 유리하다.
- **환경 독립성**: Docker Hub는 다양한 클라우드 환경과 온프레미스에서도 쉽게 사용할 수 있어, ECR보다 환경 독립성이 높다.

---

위와 같은 이유로 EC2 프리 티어 환경에서는 Jenkins 대신 GitHub Actions와 Docker Hub를 사용해 CI/CD 파이프라인을 구성하는 것이 효율적인 대안이라 판단했다.
<img width="817" alt="스크린샷 2024-11-14 오전 10 13 52" src="https://github.com/user-attachments/assets/8d51c968-96ed-44b5-9487-f4b43b992070">
</details>

<details>
<summary><b>🍁 실시간 게시물 랭킹 조회 (캐싱)</b></summary>
실시간 게시물 랭킹 조회 DB에서의 조회와 Redis를 캐싱(Cacheable 사용)한 것에서의 조회
   
![image (1)](https://github.com/user-attachments/assets/bdc65203-a763-4e83-a861-7410714e6985)


막대 그래프

![output (2)](https://github.com/user-attachments/assets/8e91a780-cd4c-4184-880f-e4135ee30560)

위 그래프는 `DB 랭킹 조회`와 `redis 랭킹 조회`의 성능을 비교한 것이다. 각 지표에 대한 설명은 다음과 같다:

1. **Average Response Time**: 평균 응답 시간에서 Redis가 DB보다 훨씬 빠르다 (DB: 26ms, Redis: 13ms). Redis가 캐싱을 통해 더 빠른 응답을 제공하고 있음을 나타낸다.
2. **Throughput**: 처리량(초당 요청 수) 역시 Redis가 DB보다 높아 초당 더 많은 요청을 처리할 수 있다.
3. **Min and Max Response Time**: 최소 및 최대 응답 시간에서 DB의 변동 폭이 더 크다. Redis는 비교적 일관된 응답 시간을 제공하지만 DB는 요청의 최대 응답 시간이 높아질 수 있다.
4. **Standard Deviation of Response Time**: 응답 시간의 표준 편차 역시 DB가 높아 응답 시간의 일관성이 낮다.
5. **Received KB/sec and Sent KB/sec**: 초당 송수신된 데이터 양에서 Redis가 더 높은 값을 보이지만, 큰 차이는 없다.

전체적으로 Redis가 DB보다 더 빠르고 안정적인 성능을 보이며, 캐시를 사용하는 것이 성능 향상에 도움이 됨을 확인할 수 있다.

---

## 인기 게시물 실시간 랭킹 조회 기능

### 1. 구현 기능

사용자들이 조회수 기준으로 인기 게시물을 실시간으로 확인할 수 있도록 **인기 게시물 랭킹 조회 기능**을 구현했습니다. 주요 목표는 빠른 응답 속도와 높은 조회 성능을 통해 사용자 경험을 개선하고, 시스템 부하를 줄이는 것이었습니다.

### 2. 주요 로직

이 기능은 Redis 캐싱을 활용해 사용자에게 실시간 인기 게시물을 제공합니다. 구체적인 로직은 다음과 같습니다:

- **Redis 캐시와 @Cacheable 어노테이션**을 사용하여 인기 게시물 데이터를 캐시에 저장하고, 조회 요청이 발생할 때마다 캐시에서 데이터를 불러오도록 했습니다. 이를 통해 DB 접근 없이도 신속한 응답이 가능합니다.
- **인기 게시물 데이터 및 상세 정보는 Redis 캐시에 저장**되어 있어, 조회 요청이 있을 때 캐시에서 즉시 반환됩니다.

### 3. 구현 배경 및 요구사항

이 기능은 다음과 같은 문제 해결 요구사항을 기반으로 설계되었습니다.

- **기존 문제점**: 초기에 DB에서 직접 인기 게시물을 조회하는 방식이었으나, 다수의 요청이 들어올 경우 DB 부하가 가중되어 응답 속도가 느려지는 문제가 있었습니다.
- **해결 필요성**: 실시간으로 사용자에게 인기 게시물 정보를 빠르게 제공해야 했기에, **DB 조회를 최소화하면서 신속하게 데이터 접근이 가능한 Redis 캐싱**이 필요하다고 판단했습니다.

### 4. 의사 결정 과정

기술 선택 과정에서 고려한 두 가지 방안을 비교하였습니다:

- **DB에서 직접 조회**: DB에 있는 데이터를 그대로 조회하는 방법은 구현이 간단하지만, 속도가 느리고 요청이 많아질 경우 DB에 과부하가 걸립니다.
- **Redis 캐싱 사용**: Redis는 메모리 기반의 캐시로, 빠른 응답을 제공하고 DB 접근을 줄여 부하를 낮출 수 있습니다. 하지만 캐시 설정을 위한 추가 개발이 필요합니다.

이를 바탕으로, **신속한 응답과 안정성을 고려해 Redis 캐시**를 사용하여 사용자에게 인기 게시물 정보를 제공하는 것이 가장 효율적이라고 판단했습니다.

### 5. 문제 해결 과정

**문제 정의**: Redis 캐싱을 설정했음에도 성능 개선이 이루어지지 않는 현상이 있었습니다. 캐시어블(@Cacheable) 어노테이션을 사용했지만, 여전히 레포지토리에서 데이터를 조회하고 있었기 때문에 캐시의 이점이 줄어들었습니다.

**해결 방안**:

- 캐시가 없는 경우에만 DB를 조회하고, 조회한 데이터를 Redis에 저장하는 방식으로 코드 로직을 수정했습니다. 이를 통해 불필요한 DB 접근을 방지하고 캐싱 효과를 극대화했습니다.

**결과**: 문제 해결 후, 응답 속도가 DB만 사용할 때보다 26ms에서 13ms로 단축되었고, 초당 처리 가능한 요청 수도 증가했습니다. 응답 시간의 변동폭도 줄어들어, 안정적인 성능을 확보할 수 있었습니다.

### 6. 성과 및 회고

- **응답 속도 개선**: Redis 캐시 도입으로 실시간 인기 게시물 랭킹을 빠르게 조회할 수 있게 되어, 사용자 응답 시간이 크게 향상되었습니다.
- **안정적인 성능**: Redis 캐시는 DB에 비해 일관된 성능을 유지하며, 변동폭이 적어 사용자에게 더 안정적인 경험을 제공했습니다.
- **DB 부하 감소**: DB 접근 횟수를 줄임으로써 전체 시스템 부하가 감소하였고, 더 많은 요청을 안정적으로 처리할 수 있게 되었습니다.

**기술 선택 회고**: 

- Redis Sentinel을 선택한 이유
    - 빠른 조회 성능뿐만 아니라 **서비스의 고가용성과 장애 대응 능력**을 확보하기 위함이었습니다. Redis Sentinel을 통해 **장애 발생 시 자동으로 캐시 서버를 감지하고 복구**하여, 캐시 시스템이 다운되더라도 즉각적인 복구가 가능하도록 설정했습니다. 덕분에 사용자들은 실시간으로 인기 게시물 데이터를 안정적으로 조회할 수 있었습니다.
</details>

<details>
<summary><b>🍁 게시글 조회 동시성 1 (비관적 락)</b></summary>
   
### 내가 구현한 기능

**동시성 문제 해결을 위한 조회수 업데이트 최적화**

### 주요 로직

게시물 조회 시, 조회수 업데이트로 인해 발생하는 동시성 문제를 해결하는 로직을 구현하였습니다. 캐싱을 위해 Redis를 사용하지만, DB에 조회수를 직접 업데이트하는 코드가 동시성 문제를 유발하고 있어, 이 문제를 해결하기 위해 **비관적 락(Pessimistic Lock)** 전략을 도입하였습니다.

### 배경

많은 양의 요청이 올 때 게시물 조회수 업데이트 로직에서 조회수 업데이트가 제대로 반영되지 않는 문제를 확인하였습니다.

### 요구사항

1. 조회수 업데이트 시 DB 성능 저하를 최소화한다.
2. 게시물 조회수는 정확하게 반영되도록 한다.
3. 대량의 동시 요청 시에도 안정성을 유지한다.

### 선택지

1. **비관적 락(Pessimistic Lock)**: 레포지토리 계층에서 데이터를 읽고 쓸 때마다 락을 걸어 동시 접근을 방지한다.
2. **낙관적 락(Optimistic Lock)**: 트랜잭션 종료 시점에서만 충돌 검사를 하고, 충돌 발생 시 재시도로 문제를 해결한다.

### 의사결정/사유

**비관적 락**을 선택한 이유는 다음과 같습니다:

1. **동시성 문제 해결**: Redis에 실시간 조회수를 저장하고, DB에 전체 조회수를 기록하는 구조에서, 조회수 증가가 실패 없이 일관되게 반영되어야 했습니다. Redis를 먼저 업데이트하고 DB에 저장하는 방식이기 때문에, 조회수가 부정확하게 증가할 위험을 최소화해야 했습니다. 비관적 락은 데이터를 수정하는 동안 다른 트랜잭션의 접근을 막아주므로 이러한 문제를 해결할 수 있었습니다.
2. **안정성 향상**: 비관적 락은 트랜잭션이 실행되는 동안 다른 트랜잭션의 접근을 차단하여 중복 증가를 방지합니다. 따라서, 낙관적 락이 제공할 수 없는 재시도 없이도 정확한 조회수를 유지할 수 있었습니다.
3. **정확성 보장**: 비관적 락을 적용하면 Redis와 DB 간의 조회수 불일치를 방지할 수 있습니다. Redis는 실시간 조회수 캐싱에 이용하며 한 시간 간격으로 캐시를 삭제하여 최신 정보를 유지하지만, DB에는 전체 조회수를 지속적으로 기록하도록 하였습니다. 이 접근 방식은 대량의 요청이 들어와도 정확한 조회수 관리가 가능하게 합니다.

### 성과

비관적 락을 도입한 결과, 실시간 인기 게시물 조회수 업데이트 성능이 개선되었고, 동시 요청이 많은 상황에서도 조회수가 일관되게 반영되는 성과를 거두었습니다. 이로써 사용자가 실시간으로 조회수를 정확하게 확인할 수 있게 되었습니다.

### 회고

조회수 업데이트 로직에 비관적 락을 적용함으로써, Redis와 DB 간의 조회수 불일치를 방지하며 성능과 정확성을 모두 개선할 수 있었습니다. 낙관적 락도 검토했으나, 동시성 환경에서 재시도와 캐시 불일치 문제가 발생할 수 있어 비관적 락을 선택한 것이 올바른 결정이었다고 판단합니다. 낙관적 락을 사용하게 되면 재시도를 하게 되어 Redis의 값의 정확성을 보장할 수 없었기에 비관적 락을 선택한 것이 좋은 방법이었다고 생각합니다.

### 피드백 및 문제 정의

- 트랜잭션 내에서 롤백이 발생하면 DB만 롤백되고 Redis의 상태는 유지되는 문제가 발견되었습니다. 이는 데이터 일관성 측면에서 큰 문제로, 향후 해결 방안을 모색해야 할 부분입니다.
- AOP를 활용하여 트랜잭션 이벤트 리스너를 사용하고, 커밋되는 시점에만 Redis를 업데이트하는 방안을 고려할 수 있습니다.
- 또한, DB 업데이트를 먼저 진행하고 Redis를 업데이트하는 순서로 코드를 변경할 경우, DB에서 문제가 발생하면 Redis는 업데이트되지 않으므로 안정성을 높일 수 있을지도 모릅니다.
- 이러한 접근이 실질적으로 효과적인지를 확인하기 위해 테스트가 필요합니다.
- **코드 변경 전**:
    
    ```java
    java
    코드 복사
    @Transactional
    public void 게시글조회() {
        // 1. Redis 조회수 증가
        board.view++;
    
        if (true) {
            throw new RuntimeException(); // 예외 발생 시 Redis 조회수 반영됨
        }
    
        // 2. DB 조회 및 조회수 증가
    }
    
    ```
    
- **코드 변경 후**:
    
    ```java
    
    @Transactional
    public void 게시글조회() {
        // 1. DB 조회 및 조회수 증가
        // 2. Redis 조회수 증가 (트랜잭션 성공 시에만 반영)
    
        if (true) {
            throw new RuntimeException(); // 예외 발생 시 Redis 조회수 미반영
        }
    }
    
    ```
    

> 트랜잭션 내에서 예외가 발생하면 Redis 조회수는 반영되지 않고, DB 롤백 시 데이터 일관성이 유지됩니다. 이를 위해 추후 **트랜잭션 이벤트 리스너**를 사용해 커밋 시점에 Redis에 조회수를 반영하도록 설정하는 방향으로 수정을 하여 데이터 정확성을 더욱 강화할 계획입니다.
>
</details>

<details>
<summary><b>🍁 게시글 조회 동시성 2 (낙관적 락)</b></summary>
낙관적 락 실행
   
<img width="836" alt="스크린샷 2024-11-05 오후 9 14 38" src="https://github.com/user-attachments/assets/5f7a7e3d-2b8f-42ed-aa7c-0ef5db458cd5">

비관적 락 실행

<img width="836" alt="스크린샷 2024-11-05 오후 9 17 12" src="https://github.com/user-attachments/assets/aa919d21-0f4b-4efc-9e9f-b0c7472cbec6">

first test가 비관적 락, second test가 낙관적 락

![first가 비관, second가 낙관](https://github.com/user-attachments/assets/6e9d0e5f-028f-42f4-9679-2f322368880b)


### [내가 구현한 기능]

**동시성 문제 해결을 위한 조회수 업데이트 최적화**

---

### [주요 로직]

실시간 인기 게시물 조회 시 발생하는 조회수 업데이트 동시성 문제를 해결하기 위해 **락(lock) 전략**을 도입했습니다. 초기에는 비관적 락을 사용하여 동시성 문제를 제어했으며, 이후 트랜잭션 커밋 시점에만 Redis에 조회수를 반영하는 방식으로 코드가 수정되면서 **낙관적 락**으로 전환할 수 있었습니다. Redis 캐싱을 활용해 실시간 조회수를 관리하며, 낙관적 락을 통해 성능을 최적화했습니다.

---

### [배경]

실시간 인기 게시물 기능은 높은 동시성을 요구하는 작업으로, 기존 방식에서는 동시 요청이 많을 경우 조회수가 정확하게 반영되지 않는 문제가 발생했습니다. Redis 캐시를 사용해 실시간 조회수를 반영했으나, DB와 Redis 간의 불일치로 조회수가 부정확하게 증가하거나 반영되지 않는 상황이 발생하여 문제를 해결할 필요가 있었습니다.

---

### [요구사항]

1. 실시간 조회수 업데이트 시 DB 성능을 유지하고 부하를 최소화해야 합니다.
2. 조회수 업데이트 시 인기 게시물의 조회수가 정확히 반영되어야 합니다.
3. 동시 요청이 많은 상황에서도 조회수의 일관성을 유지해야 합니다.

---

### [선택지]

1. **비관적 락(Pessimistic Lock)**: 조회수 증가 트랜잭션 동안 다른 접근을 막아 동시성 문제를 방지하는 방식입니다.
2. **낙관적 락(Optimistic Lock)**: 트랜잭션 종료 시점에서만 충돌을 검토하며, 충돌이 발생할 경우 재시도하여 문제를 해결합니다.

---

### [의사결정/사유]

- **초기 결정: 비관적 락 사용**
    - **데이터 일관성 문제 해결**: 초기에는 비관적 락을 사용했습니다. 낙관적 락을 사용하면 충돌 발생 시 재시도로 해결할 수 있지만, 재시도가 발생하는 동안 Redis에 반영된 값이 DB와 일치하지 않을 수 있었습니다. 조회수 증가가 반복되면서 Redis와 DB 간의 조회수가 일치하지 않는 상황을 방지하고자 비관적 락을 통해 데이터 접근을 제어했습니다.
    - **트랜잭션 내 롤백 문제**: 비관적 락을 통해 데이터 일관성을 개선하려 했으나, 트랜잭션 내에서 롤백이 발생하면 DB만 롤백되고 Redis는 그대로 유지되는 문제가 발생했습니다. 이는 조회수의 데이터 정확성에 영향을 미치는 큰 문제였으며, 이를 해결하기 위해 커밋된 시점에서만 Redis에 조회수를 반영하는 트랜잭션 이벤트 리스너를 도입했습니다.
- **낙관적 락으로 전환**
    - **트랜잭션 이벤트 리스너 도입 효과**: 트랜잭션 이벤트 리스너를 사용해 트랜잭션이 커밋된 이후에만 Redis에 조회수를 반영하도록 설정하면서, 이제는 낙관적 락을 사용할 수 있는 환경이 마련되었습니다.
    - **조회수 업데이트의 특성**: 조회수 증가는 단순히 숫자를 증가시키는 작업이므로 데이터 간 의존성이 낮습니다. 낙관적 락으로 전환한 후에도 충돌 발생 시 재시도로 간단히 복구할 수 있어 성능 저하가 적고 효율적입니다.
    - **낙관적 락의 성능 이점**: 비관적 락은 트랜잭션 수행 동안 락을 유지하므로 성능에 부담이 될 수 있습니다. 반면, 낙관적 락은 트랜잭션 종료 시점에서만 충돌 검토를 하여 자원 소모가 적고, 실시간 성능을 유지할 수 있습니다.
    - **동시성 요구사항 충족**: 낙관적 락은 충돌 시 즉시 재시도로 문제를 해결할 수 있어 높은 동시 요청 상황에서도 실시간 성능을 보장할 수 있었습니다.

---

### [성과]

낙관적 락을 적용한 결과, 조회수 정확도와 일관성이 크게 향상되었습니다. 트랜잭션 이벤트 리스너로 트랜잭션 커밋 후에만 Redis에 조회수를 반영하게 되어 데이터 불일치 문제가 해결되었으며, 실시간 인기 게시물의 조회수가 대량 요청 시에도 정확하게 반영되었습니다.

---

### [회고]

**기술의 장단점**: 초기에는 비관적 락을 통해 데이터 일관성을 확보하려 했으나, Redis와 DB 간 조회수 불일치 문제가 해결되지 않아 트랜잭션 이벤트 리스너를 도입했습니다. 이후 낙관적 락으로 전환하면서 성능과 일관성을 모두 확보할 수 있었습니다. 그러나 여전히 Redis 캐싱과 DB 업데이트 간의 타이밍 차이로 약간의 지연이 발생할 수 있어, 실시간 성능 모니터링이 필요합니다.

**다시 시도한다면?**: 비동기 처리를 추가하여 이벤트 리스너가 별도의 스레드에서 Redis 조회수를 업데이트하도록 개선할 수 있습니다. 이를 통해 조회수 동기화 속도를 더욱 높여 실시간 인기 게시물의 조회수 정확성과 사용자 경험을 강화할 수 있을 것입니다.

### 비동기 처리 적용 예시

```java

import org.springframework.scheduling.annotation.Async;

@Async
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public void handleViewCountIncrement(BoardDetailEvent event) {
    String redisViewKey = "board:viewcount:" + event.getBoardId();

    Long viewCount = redisTemplate.opsForValue().get(redisViewKey) != null
            ? redisTemplate.opsForValue().increment(redisViewKey)
            : redisTemplate.opsForValue().increment(redisViewKey, 0);
    log.info("Incremented Redis view count for boardId {}: {}", event.getBoardId(), viewCount);

    // 랭킹 업데이트
    updateRanking(event.getBoardId(), viewCount);
}

```

---

### [문제 해결 과정]

### 성능 개선 요약

비관적 락에서 낙관적 락으로 전환하며 트랜잭션 커밋 후에만 Redis에 반영하는 트랜잭션 이벤트 리스너를 도입해, 동시성 문제를 해결하고 조회수 업데이트의 성능을 최적화했습니다.

---

### 문제 정의

- 실시간 인기 게시물 기능에서 동시성이 높은 상황에서 조회수가 정확하게 반영되지 않는 문제가 발생했습니다.
- Redis 캐싱만으로는 데이터 불일치 문제를 해결하기 어려웠으며, 초기의 비관적 락 방식에서도 Redis와 DB 간 일관성을 확보하기에 한계가 있었습니다.

### 가설

낙관적 락을 사용하면서 트랜잭션 커밋 후에만 Redis에 반영하도록 하면, 데이터 일관성을 유지하면서도 동시성 문제를 해결할 수 있을 것입니다.

### 해결 방안

1. **비관적 락 도입 후 낙관적 락으로 전환**: 초기에는 비관적 락으로 데이터 접근을 엄격히 제어했으나, 트랜잭션 이벤트 리스너를 통해 Redis 반영 시점을 조정하여 낙관적 락을 사용할 수 있는 구조로 전환했습니다.
2. **트랜잭션 이벤트 리스너 도입**: 트랜잭션 커밋 후에만 Redis에 조회수를 반영하게 하여 Redis와 DB 간 데이터 일관성을 유지할 수 있도록 설정했습니다.

### 해결 완료

- **결과**: 낙관적 락 적용 후 조회수 업데이트가 일관성 있게 반영되었으며, 동시성 문제로 인한 데이터 불일치도 크게 줄었습니다.
- **전후 데이터 비교**: 비관적 락 적용 시에는 조회수가 제대로 반영되지 않는 상황이 있었으나, 트랜잭션 이벤트 리스너와 낙관적 락을 조합한 이후 조회수가 정확히 반영되었으며 Redis와 DB 간 일관성도 개선되었습니다.
</details>

<details>
<summary><b>🍁 데이터 수호자들 :  동시성 이슈와의 싸움에서 살아남기</b></summary>

   ### 동시성 제어 방식의 발전 과정 및 성능 분석

이번 성능 테스트는 **DB 바로 저장** 방식에서 시작하여 **낙관적 락, 비관적 락, Lua 스크립트, 분산 락**으로 발전하는 과정을 통해, 각 방식이 가진 성능적 특징과 개선 효과를 평가했습니다. 각 방식의 성능을 측정한 결과는 다음과 같습니다.

1. **DB 바로 저장** > 2. **낙관적 락** > 3. **비관적 락** > 4. **Lua 스크립트** > 5. **분산 락** 순으로 개선하였으며, 각 방식의 주요 특징과 개선된 이유를 구체적으로 설명하겠습니다.

---

### 1. DB 바로 저장

- 
    
    ```
    @Test
    @DisplayName("락없이 테스트")
    public void testWithoutLock() throws InterruptedException {
        // 동작 시간 측정 시작
        long startTime = System.currentTimeMillis();
    
        // given
        Long id = 1L;
    
        // when
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(1000);
        for (int i = 0; i < 1000; i++) {
            executorService.execute(() -> {
                try {
                    eventService.eventPointsDirectDB(id);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executorService.shutdown();
    
        // then
        User updatedUser = userRepository.findById(1L).orElseThrow();
        System.out.println("저장된 포인트: " + updatedUser.getPoint());
    
        // 동작 시간 측정 종료
        long endTime = System.currentTimeMillis();
        System.out.println("Execution time (DB 바로 저장): " + (endTime - startTime) + " ms");
    }
    
    결과
    
    저장된 포인트: 101000
    Execution time (DB 바로 저장): 2834 ms
    ```
    

DB 바로 저장 방식은 동시성 제어가 없이 여러 요청이 동시에 데이터베이스에 접근하는 방식입니다. 간단하게 구현할 수 있지만, 동시성 문제가 발생할 가능성이 큽니다.

- **스레드 활성화 시간**: 활성 스레드가 초반에 급격히 증가하다가 서서히 감소하는 경향을 보입니다. 여러 스레드가 동시에 DB에 접근하면서 충돌이 빈번하게 발생하기 때문에 **초기 부하가 상당히 큽니다.**
- **응답 시간**: 응답 시간이 매우 불안정하게 증가하며, 스레드 충돌로 인해 **응답 속도가 점점 느려지는 경향**을 보입니다.
- **트랜잭션 수**: 트랜잭션 수는 초반에 급격히 증가하지만 이후에는 부하가 심해지며 속도가 줄어듭니다.
- **포인트 지급 상태**: 초기에 포인트가 비정상적으로 지급되는 경우가 발생하며, 데이터 일관성에 문제가 있습니다.

- 테스트 코드 및 결과
    
    ```
    @Test
    @DisplayName("락없이 테스트")
    public void testWithoutLock() throws InterruptedException {
        // 동작 시간 측정 시작
        long startTime = System.currentTimeMillis();
    
        // given
        Long id = 1L;
    
        // when
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(1000);
        for (int i = 0; i < 1000; i++) {
            executorService.execute(() -> {
                try {
                    eventService.eventPointsDirectDB(id);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executorService.shutdown();
    
        // then
        User updatedUser = userRepository.findById(1L).orElseThrow();
        System.out.println("저장된 포인트: " + updatedUser.getPoint());
    
        // 동작 시간 측정 종료
        long endTime = System.currentTimeMillis();
        System.out.println("Execution time (DB 바로 저장): " + (endTime - startTime) + " ms");
    }
    
    결과
    
    저장된 포인트: 101000
    Execution time (DB 바로 저장): 2834 ms
    ```
    

DB 바로 저장 방식은 **성능 저하와 데이터 불일치 문제**로 인해 개선이 필요했습니다.

---

### 2. 낙관적 락

낙관적 락은 데이터 경합이 발생할 가능성을 낮게 보고, 충돌 시에만 재시도하도록 하는 방식입니다. 동시성 제어를 위한 첫 개선 단계였습니다.

- **스레드 활성화 시간**: 스레드 활성화가 여전히 빠르며, 재시도로 인해 초기에는 안정적이지만 경합이 잦아지면 **스레드 수가 급격히 증가**하는 경향이 나타났습니다.
- **응답 시간**: 낙관적 락은 초기에는 응답 시간이 안정적이었으나, **충돌이 빈번해질수록 응답 시간이 불안정해지고 지연**이 발생했습니다.
- **트랜잭션 수**: 비정상적인 증가 없이 일정 수준을 유지했지만, 충돌이 증가할수록 감소하는 경향이 나타났습니다.
- **포인트 지급 상태**: DB 바로 저장보다 일관성은 높아졌으나, 여전히 일부 요청이 누락되거나 재시도 시 데이터 충돌이 발생할 수 있었습니다.

- 테스트 코드 및 결과
    
    ```
    @Test
    @DisplayName("낙관락 테스트")
    public void testOptimistic() throws InterruptedException {
    
        // 동작 시간 측정 시작
        long startTime = System.currentTimeMillis();
    
        // given
        Long id = 2L;
        AtomicInteger totalFailures = new AtomicInteger(0);
    
        // when
        ExecutorService executorService = Executors.newFixedThreadPool(100);
        CountDownLatch latch = new CountDownLatch(1000);
    
        for (int i = 0; i < 1000; i++) {
            executorService.execute(() -> {
                try {
                    int attempts = eventService.eventPointsOptimisticLock(id);
                    totalFailures.addAndGet(attempts);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executorService.shutdown();
    
        // then
        User updatedUser = userRepository.findById(id).orElseThrow();
        System.out.println("Total attempts: " + totalFailures.get());
        System.out.println("Final point count (optimistic): " + updatedUser.getPoint());
    
        // 동작 시간 측정 종료
        long endTime = System.currentTimeMillis();
        System.out.println("Execution time (Optimistic Lock): " + (endTime - startTime) + " ms");
    }
    
    결과
    
    Total attempts(재시도 횟수): 11239
    Final point count (optimistic): 1000000
    Execution time (Optimistic Lock): 11026 ms
    ```
    

낙관적 락을 통해 일관성 문제를 개선했지만, 충돌 발생 시 성능 저하가 있어 비관적 락으로 개선이 필요했습니다.

---

### 3. 비관적 락

비관적 락은 데이터에 접근할 때마다 락을 걸어 다른 스레드가 접근하지 못하도록 제어합니다. 데이터 일관성은 높아졌지만 락 대기로 인한 성능 저하가 발생할 수 있습니다.

- **스레드 활성화 시간**: 활성 스레드가 일정 수준으로 유지되며, 다른 스레드가 대기 상태로 줄어듭니다. **스레드 경합이 줄어 안정적인 경향**을 보입니다.
- **응답 시간**: 안정적이지만 락 대기로 인해 응답 시간이 일정 수준에서 제한되며, **평균 응답 시간이 상승**합니다.
- **트랜잭션 수**: 안정적이지만 락 대기 시간이 길어짐에 따라 처리 속도가 느려졌습니다.
- **포인트 지급 상태**: 데이터의 일관성은 보장되지만, **응답 지연 문제**가 발생하여 더욱 빠르고 효율적인 방법으로의 개선이 필요했습니다.

- 테스트 코드 및 결과
    
    ```
    @Test
    @DisplayName("비관락 테스트")
    public void testPessimistic() throws InterruptedException {
        // Final like count (pessimistic): 1300000
        //Execution time (Pessimistic Lock): 7452 ms
    
        long startTime = System.currentTimeMillis();
    
        // given
        Long id = 3L;
    
        // when
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(1000);
    
        for (int i = 0; i < 1000; i++) {
            executorService.execute(() -> {
                try {
                    eventService.eventPointsPessimisticLock(id);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executorService.shutdown();
    
        // then
        User updatedUser = userRepository.findById(id).orElseThrow();
        System.out.println("Final like count (pessimistic): " + updatedUser.getPoint());
    
        // 동작 시간 측정 종료
        long endTime = System.currentTimeMillis();
        System.out.println("Execution time (Pessimistic Lock): " + (endTime - startTime) + " ms");
    }
    
    결과
    
    Final like count (pessimistic): 1000000
    Execution time (Pessimistic Lock): 8182 ms
    ```
    

비관적 락은 데이터 일관성 측면에서는 효과적이지만, 락 대기로 인해 성능이 저하되어 더 나은 성능을 위해 Lua 스크립트를 도입했습니다.

---

### 4. Lua 스크립트

Lua 스크립트를 Redis에서 실행함으로써 빠르고 원자적인 처리와 효율적인 동시성 제어가 가능해졌습니다.

- **스레드 활성화 시간**: 스레드 활성화가 매우 빠르며 일정하게 유지됩니다. 이는 Redis 내부에서 원자적으로 실행되면서 **락 대기 없이 빠르게 처리**할 수 있기 때문입니다.
- **응답 시간**: 응답 시간이 비관적 락보다 빠르며 일정하게 유지됩니다. **동시성 제어와 빠른 응답 시간을 동시에 달성**할 수 있습니다.
- **트랜잭션 수**: Redis에서 원자적으로 처리되기 때문에 트랜잭션 처리 속도도 안정적이고 빠른 편입니다.
- **포인트 지급 상태**: 비관적 락에 비해 데이터 일관성 문제는 더 나아졌으나, Lua 스크립트는 단일 Redis 서버에 의존하기 때문에 **멀티 서버 환경에서의 확장성이 부족**합니다.

- 테스트 코드 및 결과
    
    ```
    @Test
    @DisplayName("루아 테스트")
    public void 루아_스크립트_사용() throws InterruptedException {
        // User points after event: 100000
        // Execution time (Lua Script): 3191 ms
    
        // 동작 시간 측정 시작
        long startTime = System.currentTimeMillis();
    
        Long id = 4L;
        ExecutorService executorService = Executors.newFixedThreadPool(100);
        CountDownLatch latch = new CountDownLatch(1000);
    
        for (int i = 0; i < 1000; i++) {
            executorService.execute(() -> {
                try {
                    eventService.luaScriptEventPoints(id);
                } catch (Exception e) {
                    System.out.println("Exception: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executorService.shutdown();
    
        User updatedUser = userRepository.findById(id).orElseThrow();
        System.out.println("User points after event: " + updatedUser.getPoint());
    
        // 동작 시간 측정 종료
        long endTime = System.currentTimeMillis();
        System.out.println("Execution time (Lua Script): " + (endTime - startTime) + " ms");
    }
    
    결과
    
    User points after event: 1000000
    Execution time (Lua Script): 3592 ms
    ```
    

Lua 스크립트를 통해 빠르고 효율적인 동시성 제어를 달성했지만, 멀티 서버 환경에서는 한계가 있다고 하여 최종적으로 분산 락을 적용하게 되었습니다.

---

### 5. 분산 락

분산 락은 Redisson의 분산 락 기능을 사용하여 멀티 서버 환경에서도 동시성 제어가 가능하도록 하였으며, 최종적으로 데이터 일관성과 성능을 동시에 달성할 수 있었습니다.

- **스레드 활성화 시간**: 스레드 수가 일정하게 유지되며 효율적으로 감소합니다. 락을 통해 경합이 제어되면서도 멀티 서버 환경에서 안정적인 스레드 수를 유지할 수 있습니다.
- **응답 시간**: 응답 시간이 가장 안정적이며, 락 대기 시간을 최적화하여 **일관성 있는 응답 시간을 제공합니다.**
- **트랜잭션 수**: 트랜잭션 수가 일정하게 유지되며, 스레드 수와 응답 시간 모두에서 효율적인 성능을 보여줍니다.
- **포인트 지급 상태**: 데이터 일관성을 완벽히 보장하면서도 성능과 응답 시간이 모두 안정적입니다.

- 테스트 코드 및 결과
    
    ```
    @Test
    @DisplayName("분산락 테스트")
    public void 분산락_사용() throws InterruptedException {
        // User points after event: 100000
        // Execution time (Distributed Lock): 4558 ms
    
        // 동작 시간 측정 시작
        long startTime = System.currentTimeMillis();
    
        Long id = 5L;
    
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(1000);
    
        for (int i = 0; i < 1000; i++) {
            executorService.submit(() -> {
                try {
                    eventService.eventPoints(id);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executorService.shutdown();
    
        User updatedUser = userRepository.findById(id).orElseThrow();
        System.out.println("User points after event: " + updatedUser.getPoint());
    
        // 동작 시간 측정 종료
        long endTime = System.currentTimeMillis();
        System.out.println("Execution time (Distributed Lock): " + (endTime - startTime) + " ms");
    }
    
    결과
    
    User points after event: 1000000
    Execution time (Distributed Lock): 5235 ms
    ```
    

분산 락을 통해 멀티 서버 환경에서의 동시성 문제와 데이터 일관성을 최적화할 수 있었으며, 가장 효과적인 성능을 보였습니다.

---

### 결론 및 인사이트

각 방식이 발전하면서 **성능 및 일관성 문제가 점진적으로 개선**되었습니다.

- **DB 바로 저장**: 성능과 일관성 문제가 모두 발생하여 개선 필요성이 있었습니다.
- **낙관적 락**: 충돌 시 성능 저하가 발생하여, 비관적 락으로 발전했습니다.
- **비관적 락**: 데이터 일관성은 개선되었으나 락 대기로 성능이 저하되었습니다.
- **Lua 스크립트**: 빠르고 원자적인 처리가 가능했으나, 단일 서버에 의존성이 있었습니다.
- **분산 락**: 멀티 서버 환경에서 성능과 일관성을 모두 최적화할 수 있었습니다.

최종적으로 **분산 락이 가장 안정적이고 성능이 우수**하여, 대규모 동시성 환경에서 적합 했습니다. 

Redis 분산 락은 데이터 충돌을 방지하고 빠른 성능을 제공하지만, 요청 순서가 보장되지 않는다는 한계가 있습니다. 이 단점은 **Redis Stream**을 활용한 작업 순서 관리, **타임스탬프 정렬**, 또는 **메시지 큐(Kafka 등)**를 도입해 추후 개선할 계획입니다. 현재 프로젝트에서는 **정확성과 성능**이 더 중요하다고 판단하여, Redis 분산 락이 가장 적합한 선택으로 평가되었습니다.
</details>

<details>
<summary><b>🍁 알람 기능 구현 1 (Redis Pub/Sub)</b></summary>

   <img width="519" alt="스크린샷 2024-11-05 오후 10 14 03" src="https://github.com/user-attachments/assets/411a030f-475f-4398-8308-a4d04378a2cf">

   ### 1. 내가 구현한 기능

Redis Pub/Sub를 활용하여 실시간 알림 기능을 구현했습니다. 댓글 작성 시 즉시 알림을 받아 사용자가 빠르게 응답할 수 있도록 하였으며, 알림을 DB에 저장하여 사용자가 전체 알림을 조회하거나 읽지 않은 알람만 필터링할 수 있는 기능도 추가했습니다. 이벤트 시작 시 모든 사용자에게 알림을 보내, 중요한 공지를 놓치지 않도록 설계했습니다.

### 2. 주요 로직

- **실시간 알림 전송**: Redis Pub/Sub에서 특정 채널을 통해 실시간 알림을 전송하고, 구독 중인 유저에게 알림을 전달합니다.
- **알림 상태 관리**: 알림 수신 시 이를 DB에 저장해 사용자가 언제든 확인할 수 있도록 하고, 알림 확인 여부를 DB에 업데이트할 수 있습니다.
- **시간 기록**: 알림 데이터에 타임스탬프를 저장하여 알림 발생 시간을 관리합니다.

### 3. 배경

이전에는 댓글 알림이나 이벤트 공지를 실시간으로 전송할 방법이 없어, 사용자 대응 속도가 느리고 중요한 정보를 놓칠 가능성이 있었습니다. 이러한 문제를 해결하기 위해 빠르고 안정적인 실시간 알림 시스템이 필요했습니다.

### 4. 요구사항

- **실시간 알림 제공**: 댓글 작성이나 이벤트 발생 시 사용자가 즉시 알림을 받을 수 있어야 합니다.
- **알림 관리**: 사용자에게 전체 알림 조회 및 읽지 않은 알림만 필터링할 수 있는 기능을 제공합니다. 또한 알림을 나중에도 조회할 수 있게 해야합니다.
- **이벤트 공지**: 전체 사용자에게 이벤트 알림을 전송해 중요한 공지를 전달합니다.

### 5. 선택지

1. **Redis Pub/Sub 사용** : 
    
    Redis의 Pub/Sub는 단순하고 빠른 메시징을 위한 방식입니다. 메시지가 브로커에 저장되지 않기 때문에 데이터 유실이 발생할 수 있으며, 주로 실시간 알림, 채팅 등 빠른 전송이 중요한 경우에 사용됩니다.
    
2. **RabbitMQ 사용**: 
    
    RabbitMQ는 메시지 큐 기반의 브로커로, 복잡한 라우팅과 다양한 전송 옵션을 지원합니다. 메시지 신뢰성과 정교한 라우팅이 중요한 업무에 유리합니다.
    
3. **Apache Kafka 사용** : 
    
    Kafka는 대용량 데이터 스트리밍에 적합하며, 데이터를 브로커에 영구 저장해 장애가 발생해도 재처리가 가능합니다. 데이터 분석과 실시간 처리에 유리하며, 확장성이 뛰어납니다.
    

### 6. 의사결정/사유

- Redis Pub/Sub을 선택하는 주요 사유는 **단순성**과 **저지연성**입니다. Redis는 기본적으로 메모리 기반으로 동작하여 매우 빠른 메시지 전달이 가능하며, 설정이 간단해 빠르게 구축할 수 있습니다. 또한 데이터가 즉시 소비되며 브로커에 저장되지 않기 때문에, 실시간 알림이나 채팅, 간단한 이벤트 전송 같은 **짧은 수명**의 메시징이 필요한 경우에 적합합니다. 메시지 유실 위험이 있었지만 알림 데이터를 DB와 연계해 관리할 수 있어 Redis의 유연성을 최대한 활용할 수 있었습니다.
- 또한, Redis Pub/Sub 구현 후에는 **Apache Kafka나 RabbitMQ**를 사용하여 더 고도화 할 예정입니다.

### 7. 회고

- **기술의 장단점**
    - Redis Pub/Sub는 빠르고 간편하게 실시간 메시지를 전달할 수 있지만, 메시지 전송의 신뢰성을 보장하기 어렵고 사용자가 알림을 놓치는 경우 이를 복구하는 데 한계가 있었습니다.
- **다시 시도한다면?**
    - **Redis를 외부화**하는 방안을 고려할 것입니다. Redis를 애플리케이션 외부에 배치하면 독립적인 확장 및 관리가 용이해져 시스템 안정성을 더 높일 수 있습니다.
    - **고도화를 위해 Kafka나 RabbitMQ를  사용해** 고도화를 시도하여 신뢰성 높은 메시징 시스템을 구축하고, 알림 유실 문제를 방지할 계획입니다. Kafka는 대용량 메시지 처리에 적합하고, 메시지의 보존이 가능해 안정성이 높아집니다. RabbitMQ는 메시지 보존 및 재전송 기능이 강력해 사용자가 알림을 놓치지 않도록 보장할 수 있습니다.

---

# 문제 해결 과정

### 성능 개선 / 코드 개선 요약

Redis Pub/Sub를 사용해 실시간 알림 기능을 구축함으로써 알림 전달 속도를 높이고 사용자 경험을 향상시켰습니다. 알림 데이터를 DB에 저장하여 사용자가 손쉽게 알림 기록을 관리할 수 있도록 했습니다.

### 문제 정의

이벤트 발생 시 사용자에게 알림을 실시간으로 제공하지 못해 사용자 대응 속도가 느리고, 사용자 경험이 저하되는 문제가 있었습니다. 이러한 문제를 해결하기 위해 실시간 알림 시스템이 필요했습니다.

### 가설

Redis Pub/Sub를 통해 빠르고 유연한 메시징을 구축하면 실시간 알림이 가능할 것으로 판단했습니다.

### 해결 방안

- **의사결정 과정**: Redis Pub/Sub와 Apache Kafka나 RabbitMQ를 비교하여 Redis Pub/Sub가 알림 전송에 더 적합하다고 결론을 내렸습니다.
- **해결 과정**: Pub/Sub 채널을 생성하여 댓글, 이벤트 알림을 실시간으로 전송하고, DB에 저장해 알림 기록과 읽음 상태를 관리할 수 있도록 했습니다.

### 해결 완료

- **결과**: 실시간 알림 기능 구현으로 사용자가 이벤트를 실시간으로 알 수 있고 댓글을 바로 확인할 수 있게 되었습니다.

---

# RabbitMQ와 Redis Pub/Sub의 차이점

- **메시지 신뢰성**: Redis Pub/Sub는 메시지 전송만을 제공하여 메시지가 즉시 수신되지 않으면 해당 메시지가 유실될 수 있습니다. 반면, RabbitMQ는 메시지를 큐에 저장하고 사용자가 메시지를 확인할 때까지 보관하므로, 메시지 손실 방지에 유리합니다.
- **메시지 보존 기능**: Redis Pub/Sub는 메시지 보존 기능이 없고, 구독자가 수신하지 않으면 메시지가 사라집니다. RabbitMQ는 메시지 보관 기능이 있어 사용자가 오프라인일 때에도 이후에 메시지를 수신할 수 있습니다.
- **확장성과 부하 관리**: Redis Pub/Sub는 가벼운 메시징 시스템으로 확장성에 유리하며 빠르지만, 대규모 메시징에 대한 부하 관리가 제한적입니다. 반면, RabbitMQ는 트래픽 관리 및 라우팅 기능이 우수하여 대규모 메시징 시스템에 적합합니다.
</details>

<details>
<summary><b>🍁 알람 기능 구현 2 (Redis Pub/Sub + Websocket)</b></summary>

   ### [내가 구현한 기능]

웹 애플리케이션에서 Redis Pub/Sub과 웹소켓을 통해 실시간 알림 기능을 구현했습니다.

### [주요 로직]

1. 웹소켓 연결: 클라이언트가 서버에 웹소켓 연결을 요청하여 실시간 연결을 유지합니다.
2. Redis Pub/Sub: 특정 이벤트가 발생하면 Redis Pub/Sub이 이벤트를 브로커로서 웹소켓을 통해 클라이언트에 전달합니다.

### [배경]

기존에는 Redis Pub/Sub을 웹소켓 없이 직접 구현하여 알림 기능을 사용했습니다. 하지만 웹소켓을 이용한 실시간 연결로 Redis Pub/Sub을 브로커로 활용하면서, 더 안정적이고 확장 가능한 알림 시스템을 제공하고자 웹소켓과 Pub/Sub을 결합했습니다.

### [요구사항]

- 클라이언트와 서버 간의 지속적인 연결로 실시간 알림 제공
- 서버 자원 최소화와 데이터 유실 방지

### [선택지]

1. **단독 Redis Pub/Sub**: 단순한 메시지 브로커로 사용하여 실시간 알림 가능하나, 실시간 연결 유지가 어려움.
2. **웹소켓 + Redis Pub/Sub**: Redis Pub/Sub은 브로커로, 웹소켓은 실시간 연결 유지에 활용.

### [의사결정/사유]

웹소켓의 지속적인 연결과 Redis Pub/Sub의 효율적인 메시지 전송이 결합되어 서버 리소스를 최소화하며 실시간 알림 시스템의 요구를 충족하기에 적합하다고 판단했습니다.

### [회고]

- **기술의 장단점**: Redis Pub/Sub과 웹소켓을 통한 연결로 성능이 매우 빠르지만, 메시지 영구 보관이 불가하여 일부 알림 유실 가능성 있어 DB에 저장하는 코드를 구현하지 않으면 알림을 놓치는 경우 이를 복구하는 데 한계가 있었습니다.
- **다시 시도한다면?**: 보완이 필요하다고 생각이 되어 Kafka와 같은 메시지 내구성을 갖춘 브로커 도입을 고려해보고 있습니다.

<img width="1402" alt="redis pub:sub websocket 사진" src="https://github.com/user-attachments/assets/0514e5b7-f4ca-4bd0-b86c-336265da912a">

</details>


<details>
<summary><b>🍁 알람 기능 구현 3(RabbitMQ)</b></summary>

[내가 구현한 기능]
사용자들이 게시글에 댓글을 작성할 때, 해당 댓글이 게시글 작성자에게 실시간으로 알림으로 전달되는 기능을 제공하기 위하여, 댓글 알림 기능을 WebSocket + Redis Pub/Sub에서 RabbitMQ와 WebSocket을 활용하여 실시간으로 전송하는 시스템을 구현했습니다.

[주요 로직]
댓글 생성 시 알림 발행: 댓글이 작성되면 해당 댓글 정보와 함께 게시글 작성자에게 알림을 보내도록 메시지를 발행합니다.
RabbitMQ를 통한 메시지 라우팅: 댓글 알림 메시지는 RabbitMQ의 Direct Exchange를 통해 commentQueue에 라우팅됩니다.
RabbitMQListener를 통해 WebSocket 전송: RabbitMQ의 commentQueue를 구독하는 RabbitMQListener가 메시지를 수신하여, WebSocket을 통해 게시글 작성자에게 실시간으로 알림을 전송합니다.

[배경]

**기존의 알림보다 안정적인 알림 전송**이 필요했습니다. **메시지 유실을 방지와**, 장애 발생 시에도 데이터를 복구할 수 있는 것을 안정적인 알림 전송이라고 생각해, 이 점을 충족할 수 있는 개선 방법이 필요했습니다. 현재는 실시간성은 있지만, 데이터 유실 위험이 존재하며 메시지 전달의 안정성을 보장하지 못하는 한계가 있었습니다.

[요구사항]
실시간 알림: 댓글 생성 시, 게시글 작성자에게 알림을 즉시 전달해야 합니다.
메시지 전송의 안정성: 알림이 누락되지 않고 게시글 작성자에게 전달되는 것이 중요합니다.

[선택지]
Pub/Sub를 통한 단순 알림 구현: 구독-발행 방식을 통해 게시글 작성자가 실시간으로 알림을 받을 수 있도록 구현.
RabbitMQ를 통한 메시지 큐 시스템 적용: RabbitMQ의 Direct Exchange를 활용하여 메시지를 특정 사용자에게 라우팅.
Kafka로의 고도화 가능성 고려: 일단은 RabbitMQ를 사용하되, 최종적으로 Kafka를 도입하여 대규모 트래픽 처리와 메시지 영속성을 강화할 계획.

[의사결정/사유]
RabbitMQ를 우선적으로 도입한 이유는 다음과 같습니다.
단계적 고도화 전략: 최종 목표는 Kafka와 같은 메시지 스트리밍 플랫폼을 활용하여 높은 트래픽과 확장성을 요구하는 상황에도 대비하는 것입니다. 하지만 Kafka는 설정 및 운영의 복잡도가 높아 초기 도입에 부담이 있기 때문에, 상대적으로 설정과 사용이 간단한 RabbitMQ를 우선 도입하여 실시간 알림 기능을 구현했습니다.
구독-발행 구조의 유연성: 구독-발행 모델을 사용하는 RabbitMQ는 각 메시지를 사용자 큐에 맞춰 라우팅할 수 있습니다. 이를 통해 댓글 작성 시 게시글 작성자에게만 알림을 보낼 수 있어, 초기 요구사항을 충족하기에 적합하다고 판단했습니다.
확장성 있는 구조: RabbitMQ는 Direct Exchange, Fanout Exchange 등 다양한 메시지 전송 방식을 지원하여 확장성이 높은 구조로 전환할 수 있습니다. 또한 Kafka로의 전환 시에도 Pub/Sub 구조가 유지되므로 큰 변경 없이 쉽게 고도화할 수 있습니다.
이와 같은 이유로, RabbitMQ를 먼저 도입한 후 향후 Kafka로 고도화하는 전략을 채택했습니다.

[회고]
기술의 장단점
RabbitMQ의 장점:
단순성과 설정의 용이성: RabbitMQ는 설정과 사용이 비교적 간단하며, 다양한 Exchange 타입을 통해 메시지를 손쉽게 큐에 라우팅할 수 있습니다.

**데이터 안전성과 신뢰성 확보**: RabbitMQ는 메시지를 큐에 저장하므로, 서버 장애가 발생해도 메시지를 잃지 않습니다. 이를 통해 알림 시스템의 신뢰성을 확보할 수 있습니다. 예를 들어, 선착순 이벤트 공지에서 메시지가 손실되면 공지 내용이 일부 사용자에게만 전달되는 문제를 방지할 수 있었습니다. 또한 Consumer가 가져갈 때까지 보관할 수 있어 안정적인 알림 전송이 가능합니다.

**유연한 메시지 라우팅**: RabbitMQ는 구독-발행 모델을 지원하여, 예를 들어 게시물 댓글 알림 시 작성자만 타겟팅하거나, 이벤트 공지 시 모든 사용자에게 메시지를 효율적으로 라우팅할 수 있었습니다.

RabbitMQ의 단점:
확장성의 한계: RabbitMQ는 고속 데이터 스트림 처리 및 대규모 트래픽에 한계가 있을 수 있어, 대용량 메시징 처리에는 Kafka보다 적합하지 않습니다.
복잡한 메시지 영속성 관리: Kafka는 기본적으로 메시지를 디스크에 저장하여 안정적인 메시지 영속성을 지원하지만, RabbitMQ는 Queue에 메시지를 저장하기 때문에 별도의 설정이 필요할 수 있습니다.

다시 시도한다면?
다시 구현한다면, 다음과 같은 사항을 고려할 것입니다.
Kafka로의 전환: 높은 트래픽 처리와 메시지 영속성을 고려하여 RabbitMQ에서 Kafka로 메시징 시스템을 전환해볼 수 있습니다. Kafka는 대규모 데이터 스트리밍을 효율적으로 처리할 수 있으며, 다양한 분석 및 실시간 데이터 처리가 가능합니다.
모니터링과 장애 대응 강화: RabbitMQ 또는 Kafka와 같은 메시징 시스템에 장애가 발생할 경우를 대비해 추가적인 모니터링 시스템을 구축하여 알림 전송의 신뢰성을 높이는 방안을 추가로 고려할 수 있습니다.

<img width="1426" alt="rabbitmq websocket 사진인데 댓글 알림 수정한 버전" src="https://github.com/user-attachments/assets/77377bf1-98dd-4675-96da-6fbe46f531c5">

</details>

<details>
<summary><b>🍁 실시간 채팅 구현 - Spring SimpleBroker(RabbitMQ)</b></summary>

# 📺 의사 결정 및 트러블 슈팅

- **프로토콜** : 웹소켓(WebSocket)
    
    > [왜 실시간 채팅 기능 구현 프로토콜로 웹 소켓을 사용했죠?](https://www.notion.so/131d82ce48568175943ef5e2e83744b6?pvs=21) 
    [웹소켓 동작 원리](https://www.notion.so/135d82ce485680b8bdcdf249ba284596?pvs=21)
    > 
    - **서브 프로토콜** : STOMP
- **메세지 브로커** : SimpleBroker
    
    > Spring WebSocket의 기본 내장 메세지 브로커
    인 메모리 메세지 브로커
    > 
- **메세징 패턴** : Pub/Sub
    
    > [메세징 패턴이 뭐에요? 왜 Pub/Sub을 선택했죠?](https://www.notion.so/Pub-Sub-136d82ce48568034bc17db5da707009f?pvs=21)

# 🌱 구현 과정

## 1. `build.gradle`에 WebSocket 의존성 추가

```java
implementation 'org.springframework.boot:spring-boot-starter-websocket'
implementation 'org.webjars:stomp-websocket:2.3.3' // STOMP 클라이언트
```

## 2. WebSocket 설정 클래스 작성

> **WebSocket**
> 
> - 지속적이고 실시간 양방향 통신을 위한 프로토콜
> - 헤더 없이 가벼운 프레임을 사용하여 데이터를 주고 받기 때문에 네트워크 자원을 절약하고 빠른 데이터 전송이 가능
>     - 데이터 전송의 효율성과 실시간성 확보를 위해 헤더X
- 코드
    
    ```java
    @Configuration
    @RequiredArgsConstructor
    @EnableWebSocketMessageBroker
    public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
        private final JwtUtil jwtUtil;
    
        @Override
        public void configureMessageBroker(MessageBrokerRegistry registry) {
            registry.enableSimpleBroker("/topic"); // 구독 경로
            registry.setApplicationDestinationPrefixes("/app"); // 송신 경로
        }
    
        @Override
        public void registerStompEndpoints(StompEndpointRegistry registry) {
            registry.addEndpoint("/ws-chat")  // STOMP 엔드포인트
                    .addInterceptors(new ChatHandshakeInterceptor(jwtUtil))
                    .setAllowedOriginPatterns("*");
        }
    }
    ```
    
- 구독(주제) 경로 : `/topic`
- 송신(애플이케이션) 경로 : `/app`
- STOMP 엔드포인트 : `/ws-chat`
    - STOMP 앤드포인트란?
        - WebSocket 연결을 초기화하는 경로
        - 클라이언트가 서버에 연결하기 위한 접점
        - STOMP 프로토콜을 통해 WebSocket 연결을 열고 닫는 역할
    - `@EnableWebSocketMessageBroker` 와 함께 설정
- 유저 인증을 위한 `ChatHandshakeInterceptor` 추가
    - `attributes.put("chatUser", <chatUser 객체>);`  : WebSocket 세션에 유저 정보 저장
    - 코드
        
        ```java
        @RequiredArgsConstructor
        public class ChatHandshakeInterceptor implements HandshakeInterceptor {
        
            private final JwtUtil jwtUtil;
        
            @Override
            public boolean beforeHandshake(
        			    ServerHttpRequest request, 
        			    ServerHttpResponse response, 
        			    WebSocketHandler wsHandler, 
        			    Map<String, Object> attributes
            ) {
                String token = this.extractTokenFromRequest(request);
        
                if (token != null && jwtUtil.validateToken(token)) {
                    AuthUser authUser = jwtUtil.getAuthUserFromToken(token);
        
        						// WebSocket 세션에 사용자 정보 저장
                    attributes.put("chatUser", WSChatUser.fromAuthUser(authUser));  
                    return true;
                }
        
                return false;
            }
        
            @Override
            public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
        
            }
        
            private String extractTokenFromRequest(ServerHttpRequest request) {
                String query = request.getURI().getQuery();
        
                if (query != null && query.contains("token=")) {
                    return query.split("token=")[1];
                }
                return null;
            }
        }
        ```
        
    - `WSController`의 메서드에서  `SimpMessageHeaderAccessor` 를 받아 메세지 발행자의 정보를 넣어 줌.
        - 코드
            
            ```java
            @MessageMapping("/chat-room/{roomId}/create")
                @SendTo("/topic/chat-room/{roomId}")
                public WSMessage handleCreateChatRoom(
                        @DestinationVariable Long roomId,
                        SimpMessageHeaderAccessor headerAccessor
                ) {
                    WSChatUser chatUser = this.getChatUser(headerAccessor);
                    log.info("채팅방 개설 : chat-room-{}, host : {}", roomId, chatUser.getId());
            
                    return this.wsMessageService.createChatRoom(chatUser, roomId);
                }
                
                
                 /**
                 * 헤더 엑세서에서 AuthUser getter
                 * @param headerAccessor : 인증 유저 정보를 얻기 위한 headerAccessor
                 * @return
                 */
                private WSChatUser getChatUser(SimpMessageHeaderAccessor headerAccessor) {
                    Map<String, Object> attributes = headerAccessor.getSessionAttributes();
            
                    if (attributes == null) {
                        throw new ApiException(ErrorStatus.UNAUTHORIZED_CHAT_USER);
                    }
            
                    WSChatUser chatUser = (WSChatUser) attributes.get("chatUser");
            
                    if (chatUser == null) {
                        throw new ApiException(ErrorStatus.UNAUTHORIZED_CHAT_USER);
                    }
            
                    return chatUser;
                }
            ```
            

- CORS 설정 일단은 ALL로 함(이후 서버가 확실히 정해지면 수정 예정)
- 여기서는 `withSockJs()`  옵션은 추가하지 않음 (넣었더니 에러가 발생해서 제외)
    - WebSocket을 지원하지 않을 경우 SockJS라는 폴백 메커니즘을 사용해 STOMP 메시지를 전달할 수 있도록 설정

## 3. 메세지 전송 및 수신 처리

- 코드
    
    ```java
        /**
         * 메세지 전송
         * @param roomId : 채팅방 Id
         * @param content : 채팅 메세지
         * @param headerAccessor : 인증 유저 정보를 얻기 위한 headerAccessor
         * @return
         */
        @MessageMapping("/chat-room/{roomId}")
        @SendTo("/topic/chat-room/{roomId}")
        public WSMessage handleSendMessage(
    			    @DestinationVariable Long roomId, 
    			    @Payload String content,
    			    SimpMessageHeaderAccessor headerAccessor
        ) {
            WSChatUser chatUser = this.getChatUser(headerAccessor);
            log.info("메세지 전송");
    
            return this.wsMessageService.sendMessage(
    			        chatUser, 
    			        roomId, 
    			        content
            );
        }
        
        
        /**
         * 채팅방 입장 (구독 요청)
         * @param roomId : 채팅방 ID
         * @param headerAccessor : 인증 유저 정보를 얻기 위한 headerAccessor
         * @return 입장 안내 메세지
         */
        @SubscribeMapping("/chat-room/{roomId}/enter")
        public WSMessage handleSubscription(
    			    @DestinationVariable Long roomId,
    			    SimpMessageHeaderAccessor headerAccessor
        ) {
            WSChatUser chatUser = this.getChatUser(headerAccessor);
            log.info("채팅방 입장");
    
            return this.wsMessageService.subscribeChatRoom(
    				        roomId, 
    				        chatUser.getEmail()
            );
        }
    ```
    

`@MessageMaping`  

- 클라이언트가 서버로 보낸 메세지를 특정 경로에 매핑해 처리하는 어노테이션
- HTTP의 `@RequestMapping`  과 유사
- 클라이언트가 전송할 WebSocket 경로를 지정
- 예를 들어 `/app/sendMessage` 로 메세지 전송
  : `/app` (송신 경로) + `/sendMessage` (매핑 경로)
 → `@MessageMapping("/sendMessage")` 로 매핑된 메서드 실행

`@SubscribeMapping`

- 클라이언트가 특정 주제를 구독할 때, 호출되는 메서드를 지정하는 어노테이션
- 초기 구독 시 한번만 데이터 전송
- 메세지 브로커를 거치지 않고 직접 전송
    - 빠른 데이터 로드
- 메시지 브로커를 통하지 않기 때문에 응답은 클라이언트가 구독 요청을 보낸 경로로 직접 돌아감
- 초기 데이터 로딩
    - 채팅방 입장 시, 방의 기존 메시지 목록을 한번만 전송하는 경우에 사용 가능
    - 실시간 대시보드에서 클라이언트가 구독할 때, 초기 데이터 세트
    (ex. 현재 상태, 초기 통계 등)을 제공할 때 적합
- 기본 설정 정보 전송
    - 사용자가 구독할 때, 초기 설정이나 구성 데이터를 한 번만 보내고, 
    이후 업데이트는 별도의 경로로 실시간 전송하는 구조를 구현할 때 유용
- **트러블 슈팅** : [@SubscribeMapping으로 이전 메세지 불러오기](https://www.notion.so/SubscribeMapping-135d82ce48568098b123ce2c707ef3a8?pvs=21)

`@SendTo`

- 서버가 클라이언트에게 메시지를 전달할 목적지 경로를 설정하는 어노테이션
- 처리된 메세지를 특정 주제에 발행
- 해당 주제를 구독하고 있는 모든 클라이언트가 메세지를 수신할 수 있도록 함
- 예을 들어 `@SendTo("/topic/messages")` 가 붙은 메서드 실행 시,
 :  `/topic` (구독 경로) + `/messages` (구독 주제(Topic) 경로)
→ `/topic/messages` 경로를 구독하고 있을 때 메세지를 받을 수 있음

# 서비스 흐름

![image (1)](https://github.com/user-attachments/assets/ec867dab-b3cd-4802-871a-5fb5e3d940f7)

- 채팅 입장
    - WebSocket `CONNECT` 요청
        
        ```java
        ws://localhost:8080/ws-chat?token =${token}
        ```
        
        - WebSocket은 헤더가 따로 없기 때문에 
        쿼리 파라미터로 JWT 토큰을 붙여 채팅 유저 정보 저장
    
- 채팅방 생성
    - 아래 경로를 destination으로  `SUBSCRIBE` 요청
        
        ```java
        @SubscribeMapping("/chat-room/{roomId}/create")
        @SendTo("/topic/chat-room/{roomId}")
        ```
        
        - 구독 경로 `/topic/chat-room/{roomId}`
        - 채팅방 개설 메세지 전송
- 채팅방 입장
    - 아래 경로를 destination으로  `SUBSCRIBE` 요청
        
        ```java
        @SubscribeMapping("/chat-room/{roomId}/enter")
        @SendTo("/topic/chat-room/{roomId}")
        ```
        
        - 이전 메세지 기록을 조회하여 클라이언트에게 전송

- 메세지 전송
    - 아래 경로를 destination으로  `SEND` 요청
        
        ```java
        @MessageMapping("/chat-room/{roomId}")
        @SendTo("/topic/chat-room/{roomId}")
        public WSMessage handleSendMessage(@DestinationVariable Long roomId, @Payload String content,
                                        SimpMessageHeaderAccessor headerAccessor
        ) {
            WSChatUser chatUser = this.getChatUser(headerAccessor);
            log.info("메세지 전송 : chat-room-{}, host : {} / message : {}", roomId, chatUser.getId(), content);
        
            return this.wsMessageService.sendMessage(chatUser, roomId, content);
        }
        ```
        
        - Payload로 메세지를 받아 `WSMessage` 를 반환
            - WSMessage 객체 필드
                - id : 메세지 고유번호
                - roomId : 입장한 채팅방 ID
                - sender : 메세지 전송한 유저 id와 email을 담은 `ChatUserResponse`  객체
                - contetnt : 메세지 내용
- createdAt : 작성일

- 채팅방 퇴장
    - 아래 경로를 destination으로 `UNSUBSCRIBE` 요청
        
        ```java
        @MessageMapping("/chat-room/{roomId}/leave")
        @SendTo("/topic/chat-room/{roomId}")
        ```
        
        - 현재 채팅방의 구독을 종료하고 안내 메세지 전송
- 채팅방 방장 승계
    - 아래 경로를 destination으로 `SEND` 요청
        
        ```java
        @MessageMapping("/chat-room/{roomId}/success")
        @SendTo("/topic/chat-room/{roomId}")
        ```
        
        - 방장이 퇴실 시, 방장을 다음 유저에게 승계하고 해당 요청을 하면 안내 메세지 전송

- 채팅 종료
    - `DISCONNECT` 요청을 하여 웹 소켓 연결 종료

---

## 📬 SimpleBroker의 메세지 내구성 문제를 보완

> 인 메모리 NoSQL DB인 Redis에 메세지 저장
> 
- 처음 메세지 발행/구독 기능 없이 채팅방 CRUD 기능 구현 시, MySQL 이용
    - [채팅 메세지 프로토콜 설정 없이 CRUD 구현](https://www.notion.so/CRUD-131d82ce4856818097c9fe03c39d2723?pvs=21)
### 기술적 의사 결정 과정

[왜 채팅 데이터 저장 DB를 변경했나요?](https://www.notion.so/DB-131d82ce4856804a92d4d15d14c9e05d?pvs=21) 

### 💥**트러블 슈팅**

[Redis CrudRepository를 사용하기위해 객체에 Getter, Setter, AllArgsConstructor를 붙여도 괜찮을까??](https://www.notion.so/Redis-CrudRepository-Getter-Setter-AllArgsConstructor-131d82ce485681168e63eb7c53232e92?pvs=21) 

[CrudRepository 사용시, Enum 객체의 직렬화/역직렬화](https://www.notion.so/CrudRepository-Enum-131d82ce48568195b98ff2e49e99a4dd?pvs=21) 

[RedisTemplate에서 key, hashKey를 사용할 때, 매직 문자열 사용 괜찮은가?](https://www.notion.so/RedisTemplate-key-hashKey-13ad82ce485680a791a7f29d39e67856?pvs=21)
</details>

<details>
<summary><b>🍁 알람 기능 구현 3(RabbitMQ)</b></summary>
</details>

<details>
<summary><b>🍁 알람 기능 구현 3(RabbitMQ)</b></summary>
</details>

<details>
<summary><b>🍁 알람 기능 구현 3(RabbitMQ)</b></summary>
</details>

---

## 역할 분담 및 협업 방식

### **Detail Role**

| 이름   | 포지션   | 담당(개인별 기여점)                                                                                                            | Github 링크                       |
|--------|----------|-----------------------------------------------------------------------------------------------------------------------------|-----------------------------------|
| 송민준 | 리더     | ▶ **ELK 스택**  <br> - [Elasticsearch] 제목&내용&카테고리별 게시글 검색 <br> - [Logstash] SearchService 로그 Elasticsearch에 저장 <br> - [Kibana] Elasticsearch 시각화 & 실시간 인기 검색어 랭킹 Top10(검색 횟수 내림차순) <br> ▶ **인덱싱 (레거시)** <br> - 게시글 부분인덱싱 <br> - 인덱스 검색 성능 테스트 <br> ▶ **댓글 CRUD** <br> - 댓글 CRUD <br> - 댓글 채택 <br> ▶ **카테고리 CRUD** <br> - 게시글 [프레임워크, 언어] CUD <br> - 유저 중간테이블 [프레임워크, 언어] CRD | [🍁 깃헙링크](https://github.com/Luta13) |
| 강이원 | 부리더   | ▶ **로그인/회원가입** <br> - Auth(user)-service CRUD <br> - JWT와 Spring Security를 이용한 보안 설정 <br> - Redis를 이용한 Refresh Token 구현 <br> ▶ **user 권한, 경고 구현** <br> - 관리자: 광고성, 코드리뷰 외 잡담 글 경고 부여 <br> - 경고 횟수: 계정 BLOCK 처리 <br> ▶ **Redis Sentinel 적용** <br> - Redis 초기 설정 및 연결 테스트 <br> - Redis master-slave 구조 및 Sentinel 구축 <br> ▶ **RabbitMQ 적용** <br> - RabbitMQ 초기 설정 및 연결 테스트 <br> ▶ **배포 및 CI/CD** <br> - docker-compose 파일, dockerfile 작성 <br> - EC2를 통한 서비스 배포 <br> - Docker Hub에 도커 이미지 업로드 <br> - GitHub Actions를 통한 CI/CD 설정 <br> ▶ **실시간 게시물 랭킹 조회** <br> - Redis Sort Set을 이용해 랭킹 관리 <br> - 스케줄러 <br> - 1시간 간격으로 Redis 캐시 초기화 <br> - **캐싱** <br> - Redis 캐싱을 통한 성능 최적화 <br> - Cacheable을 이용한 캐싱 <br> ▶ **실시간 알림 기능** <br> - 알림 전송 및 읽음 처리, 알람 조회, 읽지 않은 알람만 조회 기능 구현 <br> - 댓글 작성 시 해당 게시글 작성자에게 알림 전송 <br> - 이벤트 발생 시 알림 전송 <br> - Slack (최종) <br> - RabbitMQ + Websocket (최종) | [🍁 깃헙링크](https://github.com/KangIWon) |
| 홍정기 | 팀원     | ▶ **게시물 작성 CRUD** <br> ▶ **이벤트 기능** <br> - 동시성 제어 방식의 발전 과정 및 성능 분석 <br> - 최종: 분산락 사용 <br> ▶ **인프라 CI/CD (리팩토링)** <br> - 한 서버에 통합되었던 Elasticsearch, Redis, RabbitMQ를 분산 배치 | [🍁 깃헙링크](https://github.com/jki09871) |
| 나민수 | 팀원     | ▶ **포인트** <br> - 포인트 지급, 포인트 차감, 포인트 조회 <br> ▶ **포인트 랭킹 (Redis SortedSet, 캐싱)** <br> - 실시간 유저 랭킹 (현업자, 비현업자) <br> - 지난달 유저 랭킹 (현업자, 비현업자) <br> ▶ **CloudFront** <br> - 첨부파일을 S3에 저장 후 CloudFront로 반환 <br> ▶ **결제** <br> - 구독 결제 & 환불 | [🍁 깃헙링크](https://github.com/minsoo-hub) |
| 강민주 | 팀원     | ▶ **첨부파일 CRUD** <br> - S3 Bucket <br> ▶ **실시간 채팅** <br> - 프로토콜: WebSocket, STOMP <br> - 메시지 브로커: RabbitMQ <br> - 메세징 패턴: Pub/Sub <br> - DB: Redis | [🍁 깃헙링크](https://github.com/MinjuKang727) |

---

## 성과 및 회고

### 잘된 점
- 송민준 : Elasticsearch를 사용하여 게시글 검색 부문 최적화를 하였음.
- 강이원 : 프로젝트에 필요한 여러 기술들의 초기 설정과 배포 및 CI/CD를 맡아서 프로젝트의 발판을 마련함.
- 홍정기 : 모든 서비스를 하나의 EC2에서 분리하여 각각의 EC2에 배포해 관리와 확장성을 개선함.
- 나민수 : 레디스 캐싱 전략을 찾아보고 프로젝트에 적합한 전략을 적용함.
- 강민주 : 프로토콜, 메세징 패턴, 메세지 브로커 선정의 각 과정을 심도있게 고민하고 메세지 브로커를 단계적으로 도입한 점, 그동안 튜터님께 피드백 받은 내용을 참고하여 코딩에 녹여내려 했던 부분이 좋았음. 전 과정을 꼼꼼하게 문서화, 영상화 한 점이 좋았음.

### 아쉬운 점
- 송민준 : 유사 검색을 구현하지 못해 사용자가 검색을 할 때 관련된 문제를 찾기 힘듦.
- 강이원 : Kafka를 최종 목표로 구현을 하려 했지만 구현을 하지 못했음.
- 홍정기 : ECS, 로드 밸런서, 오토스케일링을 활용한 자동화와 확장성 구현을 하지 못함.
- 나민수 : 결제 부분을 여러가지 시나리오에 대해서 자세하게 구현하지 못함.
- 강민주 : 메세지 발행 및 구독 기능만 구현하여 RabbitMQ의 외부 메세지 브로커로서의 장점(ex. 확장성, 메세지 전송 보장 등)을 잘 살리지 못하였음. 외부 메세지를 도입하였지만 실시간 채팅과 알람 기능 구현에만 사용한 것도 조금 아쉬움.

### 향후 계획
- 송민준 : 기존 검색 서비스의 로그 말고도 다양한 서비스 레이어의 로그를 수집하여 전체 시스템의 성능, 병목 현상, 에러를 파악하고
  최적화, 개선하고 싶습니다.
- 강이원 : Kafka로의 전환: 높은 트래픽 처리와 메시지 영속성을 고려하여 RabbitMQ에서 Kafka로 메시징 시스템을 전환해볼 것 같습니다. 대규모 데이터 스트리밍을 효율적으로 처리하고 다양한 분석 및 실시간 데이터 처리가 가능하게 하기 위해서 입니다.
- 홍정기 : ECS와 Fargate를 도입해 컨테이너 기반 자동 확장성을 구현하고 서비스 관리 부담을 줄이는 한편, 로드 밸런서를 활용해 트래픽을 효율적으로 분산시키고 서비스 가용성과 안정성을 더욱 강화할 계획입니다.
- 나민수 : 구독 부분에 조금더 안정적으로 구독 서비스를 사용할수 있도록  결제& 환불시 일어날수 있는 상황들을 더 생각해보고,
 추가적으로 개선해고싶습니다.
- 강민주 : RabbitMQ를 외부 메세지 브로커로 다중 서버 환경에서 메세지 동기화 구현 및 최적화,
외부 메세지 브로커를 실시간 채팅과 이벤트 알람 용으로만 사용하고 있는데 팀원의 동시성 문제에서 순서 보장 문제 해결 부분에도 작업 큐로 도입해 보면 좋을 것 같음.
