# Flink
Apache Flink는 대용량 데이터 처리를 위한 오픈 소스 스트림 처리 프레임워크입니다. Flink는 높은 처리량과 낮은 지연 시간을 보장하면서 실시간 스트림 처리와 배치 처리 모두를 지원합니다.
## 목차
- [주요 특징](#주요-특징)
- [구성 정보](#구성-정보)
- [구성 방법(k8s)](#구성-방법)

<br>

## \# 주요 특징
**1. 이벤트 시간 처리와 느슨한 이벤트 시간**

실시간 데이터 처리에서는 종종 "이벤트 시간(event time)" 처리가 중요합니다. 즉, 이벤트가 실제로 발생한 시간에 따라 이벤트를 처리합니다. Flink는 이벤트 시간 처리를 완벽하게 지원하며, 데이터 레코드가 불규칙하게 지연되거나 재정렬될 때도 정확한 결과를 보장합니다.

**2. 상태 및 오류 복구**

Flink는 장애 복구를 위한 내장 메커니즘이 있으며, 이를 통해 장애가 발생했을 때 작업을 복구할 수 있습니다. 이는 Flink가 처리 중인 데이터의 상태를 정기적으로 체크포인트로 백업하는 방식으로 수행됩니다.

**3. 분산 및 확장성**

Flink는 수천 개의 노드에서 동시에 실행될 수 있으며, PB 단위의 데이터를 처리할 수 있습니다. 이를 통해 Flink는 매우 큰 규모의 데이터 세트를 처리할 수 있습니다.

**4. 다양한 소스 및 싱크**

Flink는 다양한 데이터 소스(예: Kafka, RabbitMQ, Kinesis 등)와 데이터 싱크(예: Elasticsearch, HDFS, JDBC 등)를 지원합니다. 이는 Flink가 다양한 데이터 파이프라인과 통합될 수 있음을 의미합니다.

**5. 사용하기 쉬운 API**
Flink는 Java와 Scala를 위한 강력하면서도 사용하기 쉬운 API를 제공합니다. 또한, SQL 쿼리를 지원하여 기존의 SQL 기반 애플리케이션과 통합이 용이합니다.

사용 사례
Flink는 이벤트 기반 애플리케이션, 실시간 분석, 데이터 파이프라인, 검색 및 콘텐츠 순위 매기기 등의 다양한 상황에서 사용될 수 있습니다. 유명한 사용자로는 Alibaba, Uber, Netflix 등이 있습니다.

<br>

## \# 구성 정보
Apache Flink은 큰 데이터 처리를 위한 오픈소스 스트림 처리 프레임워크입니다. 복잡한 데이터 파이프라인을 구성하고, 이벤트 시간 처리, 정확한 상태 관리 등을 수행할 수 있습니다. Apache Flink은 다음과 같이 구성됩니다.

### **JobManager**
JobManager는 Flink 시스템의 마스터 노드로 작동합니다. 주요 역할은 프로그램 실행 및 복구, 스케줄링, 태스크 관리 등입니다.

### **TaskManager**
TaskManager는 Flink 시스템의 워커 노드로 작동합니다. JobManager로부터 할당받은 태스크를 실행하며, 연산과 데이터 저장 등의 역할을 담당합니다.

### **Dispatcher**
Dispatcher는 JobManager를 생성하고 클라이언트와 통신하는 역할을 합니다.

### **ResourceManager**
ResourceManager는 TaskManager의 수명주기를 관리하며, 효율적인 리소스 분배를 담당합니다.

### **Client**
Flink 클라이언트는 Job을 JobManager에 제출하는 역할을 합니다. Job 제출 후에는 즉시 종료되거나 Job의 수행 상태를 모니터링할 수 있습니다.

### **Flink UI**
Flink UI는 사용자가 작업을 모니터링하고, 실행 중인 Job의 통계를 볼 수 있도록 합니다.

<br>

## \# 구성 방법
### [Step1] Flink 다운로드 및 압축 해제
```bash
# 다운로드
$ wget https://downloads.apache.org/flink/flink-1.13.2/flink-1.13.2-bin-scala_2.11.tgz
# 압축 해제
$ tar xzf flink-1.13.2-bin-scala_2.11.tgz
```
### [Step2] Kubernetes에 대한 Flink 설정
conf/flink-conf.yaml 파일을 수정
```yaml
kubernetes.cluster-id: <ClusterId>
high-availability: org.apache.flink.kubernetes.highavailability.KubernetesHaServicesFactory
high-availability.storageDir: hdfs:///flink/recovery
restart-strategy: fixed-delay
jobmanager.memory.process.size: 1024m
kubernetes.taskmanager.memory.process.size: 4096m
kubernetes.taskmanager.cpu: 2
taskmanager.numberOfTaskSlots: 4
```
속성정보 : https://nightlies.apache.org/flink/flink-docs-release-1.17/docs/deployment/config/

### [Step3] Flink 이미지 준비
도커파일을 만들고
```dockerfile
FROM flink
ADD target/my-flink-job.jar /opt/flink/usrlib/my-flink-job.jar
```
만들 파일로 빌드하여 저장소에 push
```bash
$ docker build -t <DockerImageName> .
$ docker push <DockerImageName>
```

### [Step4] Flink 배포
```bash
$ ./bin/kubernetes-session.sh -Dkubernetes.container.image=<DockerImageName>
```

## \# Source Connectors
Source Connectors는 다양한 데이터 스토리지 시스템에서 데이터를 읽어들입니다. Apache Flink에서 제공하는 Source Connectors는 다음과 같습니다:

 - Apache Kafka: 실시간 분산 스트리밍 플랫폼인 Apache Kafka에서 데이터를 읽어들입니다.
 - Apache Cassandra: 분산 NoSQL 데이터베이스인 Cassandra에서 데이터를 읽어들입니다.
 - Amazon Kinesis Streams: 실시간 데이터 스트리밍 서비스인 Amazon Kinesis Streams에서 데이터를 읽어들입니다.
 - Filesystem: HDFS나 로컬 파일시스템 등 다양한 파일시스템에서 데이터를 읽어들입니다.
 - RabbitMQ: 메시지 큐 서비스인 RabbitMQ에서 데이터를 읽어들입니다.
 - NiFi: 실시간 데이터 플로우와 처리 시스템인 Apache NiFi에서 데이터를 읽어들입니다.

## \# Sink Connectors
Sink Connectors는 처리된 데이터를 다양한 데이터 스토리지 시스템에 쓰는 역할을 합니다. Apache Flink에서 제공하는 Sink Connectors는 다음과 같습니다:

 - Apache Kafka: 실시간 분산 스트리밍 플랫폼인 Apache Kafka에 데이터를 쓰는 역할을 합니다.
 - Apache Cassandra: 분산 NoSQL 데이터베이스인 Cassandra에 데이터를 쓰는 역할을 합니다.
 - Amazon Kinesis Streams: 실시간 데이터 스트리밍 서비스인 Amazon Kinesis Streams에 데이터를 쓰는 역할을 합니다.
 - Filesystem: HDFS나 로컬 파일시스템 등 다양한 파일시스템에 데이터를 쓰는 역할을 합니다.
 - Elasticsearch: 실시간 분산 검색 및 분석 엔진인 Elasticsearch에 데이터를 쓰는 역할을 합니다.
 - RabbitMQ: 메시지 큐 서비스인 RabbitMQ에 데이터를 쓰는 역할을 합니다.
 - NiFi: 실시간 데이터 플로우와 처리 시스템인 Apache NiFi에 데이터를 쓰는 역할을 합니다.
 - JDBC: JDBC를 통해 다양한 RDBMS에 데이터를 쓰는 역할을 합니다.