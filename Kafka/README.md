# Kafka
Apache Kafka는 오픈 소스 분산 스트리밍 플랫폼입니다. Kafka는 실시간 데이터 피드를 기반으로 설계되었으며, 이를 통해 실시간 분석이 가능하게 만들어 줍니다. 이는 많은 양의 데이터를 실시간으로 빠르게 처리하도록 최적화되어 있습니다. Kafka는 LinkedIn에서 개발되었으며, 이후 오픈소스로 제공되게 되었습니다.
Kafka는 실시간 분석, 로깅, 이벤트 소싱, 메시징, 스트리밍 등 다양한 분야에서 활용되며, 많은 기업들이 대용량 데이터 스트림 처리를 위해 Kafka를 이용하고 있습니다.

## 목차
- [주요 기능](#contents1)
- [구성요소](#contents2)
- [토픽(Topic) 생성시 고려할 점](#contents3)
- [브로커 수와 파티션 수](#contents4)

<div id="contents1"></div>

## \# 주요 기능
- **파티셔닝:** 데이터를 분산 저장합니다. 파티셔닝은 스케일링을 가능하게 합니다.

- **복제:** 장애 대비 복제를 지원합니다. 이를 통해 데이터의 신뢰성과 내구성이 확보됩니다.

- **실시간 처리:** Kafka는 실시간 스트림 데이터 처리를 지원하며, 데이터를 게시하자마자 구독자에게 전달합니다.

- **고성능:** Kafka는 메시지 브로커 시스템에 비해 높은 스루풋과 작은 지연 시간을 제공합니다.

<div id="contents2"></div>

## \# 구성요소
 - **Producer:** Producer는 Kafka에 메시지(일명 events 또는 records)를 게시(publish)하는 역할을 합니다. 프로듀서는 데이터를 생성하고 Kafka 브로커에 전송합니다. 이 데이터는 웹 서버 로그, 사용자 이벤트, 주식 거래 등 다양한 출처에서 나올 수 있습니다.

 - **Consumer:** Consumer는 Kafka에서 메시지를 읽는 역할을 합니다. Kafka에서 데이터를 가져와 실시간 분석이나 배치 처리를 수행하는 등의 작업을 합니다. 소비자는 특정 "토픽"을 구독(subscribe)하고, 그 토픽으로부터 메시지를 가져옵니다.

 - **Topics:** Kafka의 핵심은 "토픽"입니다. 토픽은 Kafka에 저장되는 메시지 스트림의 이름입니다. 프로듀서는 토픽에 메시지를 게시하고, 컨슈머는 토픽을 구독합니다.

 - **Broker:** Broker는 Kafka의 서버입니다. 브로커는 클라이언트의 요청을 처리하고, 메시지를 저장하는 역할을 합니다. Kafka 클러스터는 하나 이상의 브로커로 구성되며, 이들 브로커는 메시지의 분산 처리와 복제를 담당합니다.

 - **Partitions:** Kafka 토픽은 파티션으로 세분화 될 수 있습니다. 파티션은 토픽 내에서 메시지를 분산 저장하는 논리적인 단위입니다. 이는 Kafka가 높은 병렬성과 확장성을 가질 수 있도록 합니다.

 - **Replicas:** Kafka는 복제를 지원하여 데이터의 내구성을 높입니다. 각 파티션은 여러 복제본(replica)를 가질 수 있으며, 이 복제본은 다른 브로커에 분산되어 있습니다.

 - **ZooKeeper:** ZooKeeper는 Kafka 클러스터의 메타데이터 정보를 관리하며, 브로커 간의 동기화를 담당합니다. 이는 Kafka의 높은 가용성을 보장하는 데 중요합니다.

<div id="contents3"></div>

## \# 토픽(Topic) 생성시 고려할 점
1. 토픽의 이름 <br>
     토픽의 이름은 중복되지 않아야 하며, 데이터의 내용을 명확히 설명할 수 있어야 합니다. 가능하면 짧고 간결한 이름을 사용하는 것이 좋습니다.

2. 파티션의 수 <br>
     토픽의 파티션 수는 토픽의 병렬 처리 능력에 큰 영향을 미칩니다. 파티션의 수가 많을수록 더 많은 컨슈머를 활용하여 데이터를 병렬로 처리할 수 있습니다. 하지만 너무 많은 파티션은 ZooKeeper에 부담을 줄 수 있으니, 적절한 수준에서 설정해야 합니다.

3. 복제 팩터 <br> 
     토픽의 복제 팩터는 토픽의 내구성과 가용성에 영향을 미칩니다. 복제 팩터를 늘리면 브로커의 장애에 대한 내성이 향상되지만, 더 많은 디스크 공간과 네트워크 트래픽을 요구합니다.

4. 삭제 정책과 보존 기간 <br> 
     Kafka는 토픽의 메시지를 영구히 보존하도록 설정할 수도 있고, 특정 기간 후에 자동으로 삭제하도록 설정할 수도 있습니다. 이 설정은 토픽의 사용 사례와 저장 공간, 법규 준수 요구 사항 등에 따라 달라집니다.

5. 토픽의 세그먼트 크기 <br> 
     Kafka는 토픽의 메시지를 여러 세그먼트로 나누어 저장합니다. 세그먼트 크기를 적절히 설정하면 디스크 I/O 효율과 가비지 컬렉션 효율을 높일 수 있습니다.

6. 압축 설정 <br>
    토픽의 메시지를 압축하면 디스크 공간 사용량을 줄일 수 있습니다. 그러나 압축 및 압축 해제에는 CPU 자원이 필요하므로, 압축 설정은 디스크 공간과 CPU 사용량 사이의 트레이드오프를 고려해야 합니다.
```bash
# 예시
$ ./kafka-topics.sh --create --bootstrap-server localhost:9092 \
--replication-factor 3 \
--partitions 6 \
--topic my_topic \
--config min.insync.replicas=2 \
--config delete.retention.ms=10000 \
--config file.delete.delay.ms=60000 \
--config max.message.bytes=128000 \
--config min.compaction.lag.ms=30000 \
--config message.timestamp.type=LogAppendTime \
--config segment.bytes=1073741824
```

<div id="contents4"></div>

## \# 브로커 수와 파티션 수
### 브로커의 수
    Kafka 브로커는 클러스터 내의 서버로 각 브로커는 하나 이상의 토픽 파티션을 호스트할 수 있습니다. 브로커의 수가 많을수록 더 많은 데이터와 트래픽을 처리할 수 있습니다. 또한, 브로커의 수가 많을수록 고가용성과 내결함성이 향상됩니다. 예를 들어, 복제 인수가 브로커 수보다 작거나 같아야 하므로 브로커가 3개 있으면 각 토픽 파티션의 최대 복제 인수는 3이 됩니다.

### 파티션의 수
    파티션은 토픽을 더 작은 부분으로 분할하는 데 사용됩니다. 파티션의 수는 토픽의 병렬처리 수준을 결정합니다. 즉, 파티션의 수는 동시에 데이터를 읽고 쓸 수 있는 컨슈머의 최대 수를 결정합니다. 따라서 파티션의 수가 많을수록 처리량이 향상될 수 있습니다. 하지만 너무 많은 파티션은 브로커에 추가적인 오버헤드를 초래하고 전체 시스템의 성능을 저하시킬 수 있습니다.

<div id="contents5"></div>

## \# kafka on k8s
1. NameSpace생성
    ```bash
    $ kubectl create namesapce kafka
    ```
1. **Zookeeper 설치** 

    [:clown_face: zookeeper 설치](./Install/zookeeper.yaml)

2. **Kafka 설치** 

    [:clown_face: kafka 설치](./Install/kafka.yaml)

3. **Topic 생성**
    ```bash
    # kubectl exec 명령어로 Container 접근 후
    $ ./kafka-topics.sh --create --bootstrap-server localhost:9092 \
    --replication-factor 3 \
    --partitions 6 \
    --topic my_topic \
    --config min.insync.replicas=2 
    ```