# Flink
Apache Flink는 대용량 데이터 처리를 위한 오픈 소스 스트림 처리 프레임워크입니다. Flink는 높은 처리량과 낮은 지연 시간을 보장하면서 실시간 스트림 처리와 배치 처리 모두를 지원합니다.
## 목차
- [주요 특징](#contents1)
- [구성 정보](#contents2)
- [구성 방법(k8s)](#contents3)
- [Job 구성](#contents4)
    - [Source](#contents4-1)
    - [transformation](#contents4-2)
    - [Sink](#contents4-3)
- [Stream 처리방식](#contents5)
- [Job 개발](#contents6)

<br>

<div id="contents1"></div>

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

<div id="contents2"></div>

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

<div id="contents3"></div>

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
상세 속성 정보 : https://nightlies.apache.org/flink/flink-docs-release-1.17/docs/deployment/config/

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

<div id="contents4"></div>

## \# Filnk Job 구조
Apache Flink 작업(job)의 구조는 크게 다음의 3가지 주요 컴포넌트로 구성되어 있습니다.

 - **Source**: 데이터를 Flink 작업으로 가져오는 역할을 합니다. 데이터는 다양한 소스로부터 가져올 수 있으며, 이에는 Apache Kafka, Pravega, AWS Kinesis Streams, RabbitMQ, Apache NiFi 등의 메시징 시스템, 파일 시스템, 소켓 등이 포함됩니다.

 - **Transformation**: Source에서 가져온 데이터에 대해 처리를 수행합니다. 여러 가지 유형의 변환을 수행할 수 있는데, 이에는 Map, Filter, Reduce, Join, Aggregation, Windowing 등이 포함됩니다. 이렇게 변환된 데이터는 다른 연산을 위해 사용되거나, 결과를 생성하기 위해 Sink로 전송됩니다.

 - **Sink**: 처리된 데이터의 최종 목적지를 나타냅니다. Sink는 다양한 유형의 저장소 또는 시스템이 될 수 있습니다. 예를 들어, Apache Kafka, AWS Kinesis Streams, Pravega, HDFS, AWS S3, RDBMS 등으로 데이터를 내보낼 수 있습니다.

이러한 구조를 통해 Apache Flink는 높은 수준의 유연성과 확장성을 제공합니다. 데이터 처리 파이프라인의 각 단계는 분리되어 있으며, 별도로 구성하고 조정할 수 있기 때문입니다. 이는 Apache Flink를 다양한 시나리오와 요구 사항에 적합하게 만듭니다.

또한, Apache Flink는 고유의 분산 데이터 플로우 런타임을 기반으로 하며, 이를 통해 각 작업(job)이 병렬로 처리될 수 있게 해줍니다. 이는 대용량 데이터 스트림을 실시간으로 처리하는 데 필요한 높은 처리량과 낮은 지연 시간을 가능하게 합니다.

<div id="contents4-1"></div>

### \@ Source Connectors
Source Connectors는 다양한 데이터 스토리지 시스템에서 데이터를 읽어들입니다. Apache Flink에서 제공하는 Source Connectors는 다음과 같습니다:

 - Apache Kafka: 실시간 분산 스트리밍 플랫폼인 Apache Kafka에서 데이터를 읽어들입니다.

 - Apache Cassandra: 분산 NoSQL 데이터베이스인 Cassandra에서 데이터를 읽어들입니다.

 - Amazon Kinesis Streams: 실시간 데이터 스트리밍 서비스인 Amazon Kinesis Streams에서 데이터를 읽어들입니다.

 - Filesystem: HDFS나 로컬 파일시스템 등 다양한 파일시스템에서 데이터를 읽어들입니다.

 - RabbitMQ: 메시지 큐 서비스인 RabbitMQ에서 데이터를 읽어들입니다.

 - NiFi: 실시간 데이터 플로우와 처리 시스템인 Apache NiFi에서 데이터를 읽어들입니다.

<div id="contents4-2"></div>

### \@ Transformation 
Apache Flink은 데이터 스트림을 처리하고 변환하기 위한 다양한 종류의 연산자들을 제공합니다. 이 연산자들을 이용하면 풍부한 변환 기능을 수행할 수 있습니다. 여기서는 Flink가 지원하는 주요 변환(Transformation)의 종류와 그 사용 방법에 대해 간략하게 설명하겠습니다.

 - Map: 각 요소를 함수에 적용하고 결과를 출력합니다. Map 연산자는 한 유형의 요소를 다른 유형의 요소로 변환하는 데 사용됩니다.

 - FlatMap: 각 요소를 함수에 적용하고 0개 이상의 결과를 출력합니다. 이 연산자는 Map과 유사하지만, 여러 결과를 생성할 수 있다는 점이 다릅니다.

 - Filter: 각 요소를 함수에 적용하고 함수가 참을 반환하는 요소만 출력합니다.

 - KeyBy: 특정 키 함수를 기반으로 데이터 스트림을 그룹화합니다. 이 연산자는 스트림에 있는 요소를 같은 키를 갖는 여러 스트림으로 나눕니다.

 - Reduce: KeyBy로 그룹화된 스트림에 대해 reduce 연산을 수행합니다. 이 연산자는 그룹 내의 요소를 단일 요소로 줄입니다.

 - Window: KeyBy 후에 window 함수를 적용하여 그룹화된 스트림의 특정 시간 범위 또는 길이 범위에 대한 연산을 수행합니다.

<div id="contents4-3"></div>

### \@ Sink Connectors
Sink Connectors는 처리된 데이터를 다양한 데이터 스토리지 시스템에 쓰는 역할을 합니다. Apache Flink에서 제공하는 Sink Connectors는 다음과 같습니다:

 - Apache Kafka: 실시간 분산 스트리밍 플랫폼인 Apache Kafka에 데이터를 쓰는 역할을 합니다.

 - Apache Cassandra: 분산 NoSQL 데이터베이스인 Cassandra에 데이터를 쓰는 역할을 합니다.

 - Amazon Kinesis Streams: 실시간 데이터 스트리밍 서비스인 Amazon Kinesis Streams에 데이터를 쓰는 역할을 합니다.

 - Filesystem: HDFS나 로컬 파일시스템 등 다양한 파일시스템에 데이터를 쓰는 역할을 합니다.

 - Elasticsearch: 실시간 분산 검색 및 분석 엔진인 Elasticsearch에 데이터를 쓰는 역할을 합니다.

 - RabbitMQ: 메시지 큐 서비스인 RabbitMQ에 데이터를 쓰는 역할을 합니다.

 - NiFi: 실시간 데이터 플로우와 처리 시스템인 Apache NiFi에 데이터를 쓰는 역할을 합니다.

 - JDBC: JDBC를 통해 다양한 RDBMS에 데이터를 쓰는 역할을 합니다.

<div id="contents5"></div>

## \# Stream 처리 방식
### **Event Time and Watermarks**
이벤트 시간과 워터마크는 시간에 대한 진보를 추적하고, 이를 기반으로 윈도우 연산과 같은 시간 종속적인 연산을 가능하게 합니다. 이벤트 시간은 데이터 요소가 발생한 실제 시간을 나타내며, 워터마크는 특정 시점 이후에 더 이상 지연된 이벤트를 기대하지 않는다는 시스템의 가정을 나타냅니다.

```java
DataStream<MyEvent> stream = env.addSource(new FlinkKafkaConsumer<>("topic", new MyEventSchema(), props));

stream
    .assignTimestampsAndWatermarks(
        WatermarkStrategy.<MyEvent>forBoundedOutOfOrderness(Duration.ofSeconds(20))
            .withTimestampAssigner((event, timestamp) -> event.getCreationTime()));

```

### **Windowing**
윈도우는 데이터 스트림에 대한 연산을 수행하는 데 사용되는 기본적인 구성 요소입니다. Flink은 시간 기반(예: 슬라이딩 윈도우, 텀블링 윈도우, 세션 윈도우) 또는 데이터 기반(예: 카운트 윈도우)의 윈도우를 지원합니다.

```java
DataStream<Tuple2<String, Integer>> input = ...;

input
    .keyBy(value -> value.f0)
    .window(TumblingEventTimeWindows.of(Time.seconds(5)))
    .reduce((value1, value2) -> new Tuple2<>(value1.f0, value1.f1 + value2.f1));

```

### **Checkpointing and Savepoints**
Flink은 스트림 처리의 내결함성을 보장하기 위해 체크포인트와 세이브포인트를 지원합니다. 체크포인트는 자동적으로 생성되고, 실패 시 시스템 상태를 복구하는 데 사용됩니다. 반면에 세이브포인트는 사용자가 수동으로 생성하고 관리하는 것으로, 장기적인 백업이나 버전 업그레이드시에 사용됩니다.

```java
env.enableCheckpointing(5000); // checkpoint every 5000 msecs

```

### **State and Fault Tolerance**
Flink은 상태 관리 및 내결함성 기능을 제공하여, 스트림 처리 시 발생할 수 있는 오류에 대비하고, 처리 과정 중에 필요한 정보를 유지할 수 있게 합니다. Flink의 상태는 작업의 진행상태나 결과를 저장하며, 장애 발생 시 체크포인트에서 복구할 수 있습니다.

```java
public class CountWindowAverage extends RichFlatMapFunction<Tuple2<Long, Long>, Tuple2<Long, Long>> {
    private transient ValueState<Tuple2<Long, Long>> sum;

    @Override
    public void flatMap(Tuple2<Long, Long> input, Collector<Tuple2<Long, Long>> out) throws Exception {
        Tuple2<Long, Long> currentSum = sum.value();
        currentSum.f0 += 1;
        currentSum.f1 += input.f1;
        sum.update(currentSum);
        if (currentSum.f0 >= 2) {
            out.collect(new Tuple2<>(input.f0, currentSum.f1 / currentSum.f0));
            sum.clear();
        }
    }
}

```

### **CEP (Complex Event Processing)**
복잡한 이벤트 처리는 스트림에서 복잡한 패턴을 탐지하는 기능을 제공합니다. 이 기능을 사용하면 이벤트 스트림에서 특정 조건을 만족하는 이벤트의 시퀀스를 감지하고 처리할 수 있습니다.

```java
Pattern<LoginEvent, ?> loginFailPattern = Pattern.<LoginEvent>begin("firstFail").where(new SimpleCondition<LoginEvent>() {
    @Override
    public boolean filter(LoginEvent loginEvent) {
        return loginEvent.getEventType().equals("fail");
    }
}).next("secondFail").within(Time.seconds(2));

PatternStream<LoginEvent> patternStream = CEP.pattern(loginEventStream.keyBy(LoginEvent::getUserId), loginFailPattern);

```

### **Exactly-Once Semantics**
Flink은 Exactly-Once Semantics를 보장하여, 처리 중에 오류가 발생하더라도 각 레코드가 한 번만 처리되도록 합니다. 이 기능은 체크포인트와 함께 사용되어 데이터 유실이나 중복 처리를 방지합니다.
```java
env.enableCheckpointing(5000); // checkpoint every 5000 msecs
env.getCheckpointConfig().setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);
```

<div id="contents6"></div>

## \# Flink Job 개발 
실시간으로 파일을 읽어 Steam 처리 후 DBMS에 저장하는 구조 <br>
[:clown_face: File to Kafka Producer](./flink-consumer/) <br>
[:clown_face: Kafka to Mysql Consumer](./flink-producer/)