# AppLogProducerJob
AppLogProducerJob은 Flink 스트리밍 잡입니다. 
이 잡은 주어진 파일에서 데이터를 읽어와 처리한 후 Kafka 토픽으로 데이터를 전송합니다. 
데이터의 정확한 한 번의 전달 보장 (Exactly-once semantics)을 가지도록 구성되어 있습니다.


## 기능
- 파일에서 CSV 형식으로 된 데이터를 읽어옵니다.
- 데이터를 처리하여 파티션 키를 추출하고 AppLogRecord 객체를 생성합니다.
- Kafka 토픽으로 데이터를 전송합니다.
- 트랜잭션 기능을 활성화하여 중복 제거 (Idempotence)를 보장합니다.


## 설정
아래와 같은 설정을 제공합니다:
- `bootstrap.servers`: Kafka 브로커 서버들의 주소를 설정합니다. 기본값은 "localhost:9092,localhost:9093,localhost:9094" 입니다.
- `filePath`: 입력 데이터 파일의 경로를 설정합니다. 기본값은 "C:\\Users\\jhhan\\Desktop\\VSCODE\\00.Coding\\KakaoBank\\source\\app.log" 입니다.
- `topic`: 데이터를 전송할 Kafka 토픽 이름을 설정합니다. 기본값은 "test5" 입니다.

## 데이터 처리
1. 로그 파일은 CSV 형식으로 라인단위로 실시간 입력된다고 가정합니다.
2. 실시간으로 발생되는 로그파일을 증분 데이터만을 읽어 들입니다.
3. 첫 번째 컬럼이 "거래내역" 또는 "상세거래내역"인 경우, 두 번째와 세 번째 컬럼을 하이픈으로 붙여서 파티션 키로 사용합니다. 그 외의 경우 두 번째 컬럼을 파티션 키로 사용합니다.
4. 파티션 키, 값 으로 구분된 데이터를 AppLogRecord 객체로 담아 직렬화 처리합니다.
5. 직렬화된 Stream 데이터를 Kafka Sink를 통하여 지정된 Topic으로 전달합니다.
6. 1~5 과정을 1.5초 간격으로 발생된 데이터를 대상으로 이루어 집니다.

## 의존성
- Flink 1.17.1 

## 실행방법
```shell
# flink jobmanager Container에 접근하여 다음과 같은 명령어를 수행합니다.
flink run -c com.g1.flink.producer.AppLogProducerJob <jar 파일 경로> --bootstrap.servers <Kafka 브로커 주소> --filePath <데이터 파일 경로> --topic <Kafka 토픽 이름>
```
위 명령어에서 <jar 파일 경로> 는 빌드된 JAR 파일의 경로를, <Kafka 브로커 주소> 는 Kafka 브로커 서버들의 주소를, <데이터 파일 경로> 는 입력 데이터 파일의 경로를, <Kafka 토픽 이름> 은 데이터를 전송할 Kafka 토픽의 이름을 각각 지정해야 합니다.

또는 http://localhost:8081/ 접근하여 [Submit New Job] 기능을 활용해서 배포 할 수 있습니다.