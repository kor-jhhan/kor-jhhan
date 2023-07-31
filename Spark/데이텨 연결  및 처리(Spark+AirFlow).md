# 데이터 처리 Spark + Airflow
## **# AWS 의 경우 간단한 처리 구조**
보관 > 통합 > 처리 구조 <br>
AWS S3 > AWS Glue > Spark on AWS EMR <br>
 - AWS S3 : 확장성, 데이터 가용성, 보안 및 성능을 제공하는 객체 스토리지 서비스
 - AWS Glue : 분석, 기계 학습(ML) 및 애플리케이션 개발을 위해 여러 소스에서 데이터를 쉽게 탐색, 준비, 이동 및 통합할 수 있도록 하는 확장 가능한 서버리스 데이터 통합 서비스
 - AWS EMR :  Apache Spark, Apache Hive 및 Presto와 같은 오픈 소스 프레임워크를 사용하여 페타바이트급 데이터 처리, 대화식 분석 및 기계 학습을 위한 클라우드 빅 데이터 솔루션

## **Spark on Kubernetes는 어떻게 다른가요?**
```
일반적으로 YARN을 통해서 client모드로 Spark job을 실행시켜보면 master node에서 driver가 동작하고 worker node에서 executor들이 동작하게 됩니다. 물론 cluster모드라면 driver 또한 worker node어딘가에서 동작할테지요.

Kubernetes에서는 driver가 뜨고 이 driver가 executor pod들을 실행시키게 되는데요. client모드로 Spark job을 실행하면 실행하고자 하는 pod이 driver가 되고 executor pod들이 새로 뜨게 됩니다. cluster 모드로 실행하면 실행한 pod과는 별개로 driver pod이 새로 뜨게되며 새로 뜬 driver pod이 executor pod들을 띄우게 되는 구조입니다.
```

https://blog.banksalad.com/tech/spark-on-kubernetes/ <br>
https://techblog.woowahan.com/10291/


### **[Spark on Kubernetes 구성]**
1. Spark 2.3 이상 binary에는 k8s 배포 관련 util 이 포함되어 있다.
2. spark app을 Image로 만들어준다.
3. 만든 Image를 submin-commit 으로 k8s cluster를 지정해서 보낸다.
4. 그럼 알아서 Driver 생성 및 Executor 생성

### **[작업돌려보기]**
```sh
# 다운로드 받는다 (Ozone은 hadoop3 기준이기때문에 v3.3.x 쵸이스 )
$ wget https://dlcdn.apache.org/spark/spark-3.3.2/spark-3.3.2-bin-hadoop3.tgz
# 압축을 풀고
$ tar -zxvf spark-3.3.2-bin-hadoop3.tgz
# 빌드해본다.
$ ./spark-3.3.2-bin-hadoop2.7/bin/docker-image-tool.sh -r 192.168.220.202:5000 -t v1.0 build
./spark-3.2.3-bin-hadoop2.7/bin/docker-image-tool.sh: line 177: docker: command not found
Failed to build Spark JVM Docker image, please refer to Docker build output for details.
### > 안된다.... Docker환경이 구축된 곳에서 할수 있는것 같다.

# 다시 환경을(Docker설치된 서버) 옮겨서 빌드해본다.
## Docker version 20.10.14, build a224086
$ ./spark-3.3.2-bin-hadoop3/bin/docker-image-tool.sh -r 192.168.220.202:5000 -t v1.0 build
Sending build context to Docker daemon  337.7MB
Step 1/18 : ARG java_image_tag=11-jre-slim
Step 2/18 : FROM openjdk:${java_image_tag}
11-jre-slim: Pulling from library/openjdk

## 생략 ##

Step 18/18 : USER ${spark_uid}
 ---> Running in 23bbcf422aa4
Removing intermediate container 23bbcf422aa4
 ---> 9fcf542b4d4a
Successfully built 9fcf542b4d4a
Successfully tagged 192.168.220.202:5000/spark:v1.0

# build 확인
$ docker images |grep spark 
# push 해본다.
$ ./spark-3.3.2-bin-hadoop3/bin/docker-image-tool.sh -r 192.168.220.202:5000 -t v1.0 push
The push refers to repository [192.168.220.202:5000/spark]
b9ca6c484aa7: Pushed 
f775e56e0fcd: Pushed 
d00b1260ea67: Pushed 
acad6e80a726: Pushed 
481ab0385b18: Pushed 
15a249d53e5c: Pushed 
0fb02ac6bd62: Pushed 
8543b7b8afe5: Pushed 
e501578f6db2: Pushed 
f309a4cd9a46: Pushed 
d7802b8508af: Pushed 
e3abdc2e9252: Pushed 
eafe6e032dbd: Pushed 
92a4e8a3140f: Pushed 
v1.0: digest: sha256:f2bf057b9d3c98036bc0b94dc9221faecdb334d6ad16c64b563f01f31847593e size: 3459
192.168.220.202:5000/spark-py:v1.0 image not found. Skipping push for this image.
192.168.220.202:5000/spark-r:v1.0 image not found. Skipping push for this image.

# push 확인
$ curl 192.168.220.202:5000/v2/_catalog?n=1000 |grep spark
$ curl 192.168.220.202:5000/v2/spark/tags/list
{"name":"spark","tags":["v1.0","base","spark-crystal"]}


# sumit shell 작성해보자
## 마스터에서 클러스터 정보 추출
$ kubectl cluster-info
Kubernetes master is running at https://192.168.220.225:6443
KubeDNS is running at https://192.168.220.225:6443/api/v1/namespaces/kube-system/services/kube-dns:dns/proxy
Metrics-server is running at https://192.168.220.225:6443/api/v1/namespaces/kube-system/services/https:metrics-server:/proxy

$ vi ./k8s_submit.sh
#!/bin/bash
IMAGE_REPO=192.168.220.202:5000
export SPARK_HOME=./spark-3.3.2-bin-hadoop3

$SPARK_HOME/bin/spark-submit \
    --master k8s://https://192.168.220.225:6443 \
    --deploy-mode cluster \
    --name spark-example \
    --class org.apache.spark.examples.SparkPi \
    --conf spark.executor.instances=3 \
    --conf spark.kubernetes.container.image=$IMAGE_REPO/spark:v1.0 \
    $SPARK_HOME/examples/jars/spark-examples_2.12-3.3.2.jar

## 1차 시도
## 에러난다. https://m.blog.naver.com/firstpcb/221762669146
$ ./k8s_submit.sh 
23/02/26 15:01:40 WARN NativeCodeLoader: Unable to load native-hadoop library for your platform... using builtin-java classes where applicable
### 생략 ### 
### 아마도 Hadoop Binary가 필요해 보인다.
$ wget https://dlcdn.apache.org/hadoop/common/hadoop-3.3.4/hadoop-3.3.4.tar.gz
$ tar -zxvf hadoop-3.3.4.tar.gz
$ cp ./spark-3.3.2-bin-hadoop3/conf/spark-env.sh.template ./spark-3.3.2-bin-hadoop3/conf/spark-env.sh
$ vi ./spark-3.3.2-bin-hadoop3/conf/spark-env.sh
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:/data/jhhan/spark/hadoop-3.3.4/lib/native # 추가

## 2차 시도
# 또 에러난다....
$ ./k8s_submit.sh
	23/02/26 15:26:43 INFO SparkKubernetesClientFactory: Auto-configuring K8S client using current context from users K8S config file
	23/02/26 15:26:43 INFO KerberosConfDriverFeatureStep: You have not specified a krb5.conf file locally or via a ConfigMap. Make sure that you have the krb5.conf locally on the driver image.
	Exception in thread "main" org.apache.spark.SparkException: Please specify spark.kubernetes.file.upload.path property.
		at org.apache.spark.deploy.k8s.KubernetesUtils$.uploadFileUri(KubernetesUtils.scala:330)
		at org.apache.spark.deploy.k8s.KubernetesUtils$.$anonfun$uploadAndTransformFileUris$1(KubernetesUtils.scala:276)
		at scala.collection.TraversableLike.$anonfun$map$1(TraversableLike.scala:286)
		at scala.collection.mutable.ResizableArray.foreach(ResizableArray.scala:62)
		at scala.collection.mutable.ResizableArray.foreach$(ResizableArray.scala:55)
		at scala.collection.mutable.ArrayBuffer.foreach(ArrayBuffer.scala:49)
		at scala.collection.TraversableLike.map(TraversableLike.scala:286)
		at scala.collection.TraversableLike.map$(TraversableLike.scala:279)
		at scala.collection.AbstractTraversable.map(Traversable.scala:108)
		at org.apache.spark.deploy.k8s.KubernetesUtils$.uploadAndTransformFileUris(KubernetesUtils.scala:275)
		at org.apache.spark.deploy.k8s.features.BasicDriverFeatureStep.$anonfun$getAdditionalPodSystemProperties$1(BasicDriverFeatureStep.scala:188)
		at scala.collection.immutable.List.foreach(List.scala:431)
		at org.apache.spark.deploy.k8s.features.BasicDriverFeatureStep.getAdditionalPodSystemProperties(BasicDriverFeatureStep.scala:178)
		at org.apache.spark.deploy.k8s.submit.KubernetesDriverBuilder.$anonfun$buildFromFeatures$5(KubernetesDriverBuilder.scala:86)
		at scala.collection.LinearSeqOptimized.foldLeft(LinearSeqOptimized.scala:126)
		at scala.collection.LinearSeqOptimized.foldLeft$(LinearSeqOptimized.scala:122)
		at scala.collection.immutable.List.foldLeft(List.scala:91)
		at org.apache.spark.deploy.k8s.submit.KubernetesDriverBuilder.buildFromFeatures(KubernetesDriverBuilder.scala:84)
		at org.apache.spark.deploy.k8s.submit.Client.run(KubernetesClientApplication.scala:104)
		at org.apache.spark.deploy.k8s.submit.KubernetesClientApplication.$anonfun$run$5(KubernetesClientApplication.scala:248)
		at org.apache.spark.deploy.k8s.submit.KubernetesClientApplication.$anonfun$run$5$adapted(KubernetesClientApplication.scala:242)
		at org.apache.spark.util.Utils$.tryWithResource(Utils.scala:2764)
		at org.apache.spark.deploy.k8s.submit.KubernetesClientApplication.run(KubernetesClientApplication.scala:242)
		at org.apache.spark.deploy.k8s.submit.KubernetesClientApplication.start(KubernetesClientApplication.scala:214)
		at org.apache.spark.deploy.SparkSubmit.org$apache$spark$deploy$SparkSubmit$$runMain(SparkSubmit.scala:958)
		at org.apache.spark.deploy.SparkSubmit.doRunMain$1(SparkSubmit.scala:180)
		at org.apache.spark.deploy.SparkSubmit.submit(SparkSubmit.scala:203)
		at org.apache.spark.deploy.SparkSubmit.doSubmit(SparkSubmit.scala:90)
		at org.apache.spark.deploy.SparkSubmit$$anon$2.doSubmit(SparkSubmit.scala:1046)
		at org.apache.spark.deploy.SparkSubmit$.main(SparkSubmit.scala:1055)
		at org.apache.spark.deploy.SparkSubmit.main(SparkSubmit.scala)
	23/02/26 15:26:43 INFO ShutdownHookManager: Shutdown hook called
	23/02/26 15:26:43 INFO ShutdownHookManager: Deleting directory /tmp/spark-7aa1d18a-8834-42d4-9831-15d80d9779af

## 3차 시도
## $ ./k8s_submit.sh 에서 실행 jar 파일 Paht 앞에 local:// 설정
## 결과 아래와 같이
#!/bin/bash
IMAGE_REPO=192.168.220.202:5000
export SPARK_HOME=/data/jhhan/spark/spark-3.3.2-bin-hadoop3

$SPARK_HOME/bin/spark-submit \
    --master k8s://https://192.168.220.225:6443 \
    --deploy-mode cluster \
    --name spark-example \
    --class org.apache.spark.examples.SparkPi \
    --conf spark.executor.instances=3 \
    --conf spark.kubernetes.container.image=$IMAGE_REPO/spark:v1.0 \
    local://$SPARK_HOME/examples/jars/spark-examples_2.12-3.3.2.jar

## Drive가 배포는 되었으나 Error로 끝이난다.
$ ./k8s_submit.sh 
23/02/26 15:46:38 INFO SparkKubernetesClientFactory: Auto-configuring K8S client using current context from users K8S config file
23/02/26 15:46:38 INFO KerberosConfDriverFeatureStep: You have not specified a krb5.conf file locally or via a ConfigMap. Make sure that you have the krb5.conf locally on the driver image.
23/02/26 15:46:39 INFO KubernetesClientUtils: Spark configuration files loaded from Some(/data/jhhan/spark/spark-3.3.2-bin-hadoop3/conf) : spark-env.sh
23/02/26 15:46:39 INFO LoggingPodStatusWatcherImpl: State changed, new state: 
	 pod name: spark-example-e4f249868c793199-driver
	 namespace: default
	 labels: spark-app-name -> spark-example, spark-app-selector -> spark-74dc1638e90149b893ef42bd776f3668, spark-role -> driver, spark-version -> 3.3.2
	 pod uid: f82c4e16-3aea-4a48-8220-1bfe06e47d0b
	 creation time: 2023-02-26T06:46:39Z
	 service account name: default
	 volumes: spark-local-dir-1, spark-conf-volume-driver, default-token-2dxpc
	 node name: N/A
	 start time: N/A
	 phase: Pending
	 container status: N/A
23/02/26 15:46:39 INFO LoggingPodStatusWatcherImpl: State changed, new state: 
	 pod name: spark-example-e4f249868c793199-driver
	 namespace: default
	 labels: spark-app-name -> spark-example, spark-app-selector -> spark-74dc1638e90149b893ef42bd776f3668, spark-role -> driver, spark-version -> 3.3.2
	 pod uid: f82c4e16-3aea-4a48-8220-1bfe06e47d0b
	 creation time: 2023-02-26T06:46:39Z
	 service account name: default
	 volumes: spark-local-dir-1, spark-conf-volume-driver, default-token-2dxpc
	 node name: N/A
	 start time: N/A
	 phase: Pending
	 container status: N/A
23/02/26 15:46:39 INFO LoggingPodStatusWatcherImpl: Waiting for application spark-example with submission ID default:spark-example-e4f249868c793199-driver to finish...
23/02/26 15:46:39 INFO LoggingPodStatusWatcherImpl: State changed, new state: 
	 pod name: spark-example-e4f249868c793199-driver
	 namespace: default
	 labels: spark-app-name -> spark-example, spark-app-selector -> spark-74dc1638e90149b893ef42bd776f3668, spark-role -> driver, spark-version -> 3.3.2
	 pod uid: f82c4e16-3aea-4a48-8220-1bfe06e47d0b
	 creation time: 2023-02-26T06:46:39Z
	 service account name: default
	 volumes: spark-local-dir-1, spark-conf-volume-driver, default-token-2dxpc
	 node name: k8s-19-worker09
	 start time: N/A
	 phase: Pending
	 container status: N/A
23/02/26 15:46:39 INFO LoggingPodStatusWatcherImpl: State changed, new state: 
	 pod name: spark-example-e4f249868c793199-driver
	 namespace: default
	 labels: spark-app-name -> spark-example, spark-app-selector -> spark-74dc1638e90149b893ef42bd776f3668, spark-role -> driver, spark-version -> 3.3.2
	 pod uid: f82c4e16-3aea-4a48-8220-1bfe06e47d0b
	 creation time: 2023-02-26T06:46:39Z
	 service account name: default
	 volumes: spark-local-dir-1, spark-conf-volume-driver, default-token-2dxpc
	 node name: k8s-19-worker09
	 start time: 2023-02-26T06:46:40Z
	 phase: Pending
	 container status: 
		 container name: spark-kubernetes-driver
		 container image: 192.168.220.202:5000/spark:v1.0
		 container state: waiting
		 pending reason: ContainerCreating
23/02/26 15:46:40 INFO LoggingPodStatusWatcherImpl: Application status for spark-74dc1638e90149b893ef42bd776f3668 (phase: Pending)
23/02/26 15:46:41 INFO LoggingPodStatusWatcherImpl: Application status for spark-74dc1638e90149b893ef42bd776f3668 (phase: Pending)
23/02/26 15:46:41 INFO LoggingPodStatusWatcherImpl: State changed, new state: 
	 pod name: spark-example-e4f249868c793199-driver
	 namespace: default
	 labels: spark-app-name -> spark-example, spark-app-selector -> spark-74dc1638e90149b893ef42bd776f3668, spark-role -> driver, spark-version -> 3.3.2
	 pod uid: f82c4e16-3aea-4a48-8220-1bfe06e47d0b
	 creation time: 2023-02-26T06:46:39Z
	 service account name: default
	 volumes: spark-local-dir-1, spark-conf-volume-driver, default-token-2dxpc
	 node name: k8s-19-worker09
	 start time: 2023-02-26T06:46:40Z
	 phase: Pending
	 container status: 
		 container name: spark-kubernetes-driver
		 container image: 192.168.220.202:5000/spark:v1.0
		 container state: waiting
		 pending reason: ContainerCreating
23/02/26 15:46:42 INFO LoggingPodStatusWatcherImpl: Application status for spark-74dc1638e90149b893ef42bd776f3668 (phase: Pending)
23/02/26 15:46:43 INFO LoggingPodStatusWatcherImpl: State changed, new state: 
	 pod name: spark-example-e4f249868c793199-driver
	 namespace: default
	 labels: spark-app-name -> spark-example, spark-app-selector -> spark-74dc1638e90149b893ef42bd776f3668, spark-role -> driver, spark-version -> 3.3.2
	 pod uid: f82c4e16-3aea-4a48-8220-1bfe06e47d0b
	 creation time: 2023-02-26T06:46:39Z
	 service account name: default
	 volumes: spark-local-dir-1, spark-conf-volume-driver, default-token-2dxpc
	 node name: k8s-19-worker09
	 start time: 2023-02-26T06:46:40Z
	 phase: Running
	 container status: 
		 container name: spark-kubernetes-driver
		 container image: 192.168.220.202:5000/spark:v1.0
		 container state: running
		 container started at: 2023-02-26T06:46:42Z
23/02/26 15:46:43 INFO LoggingPodStatusWatcherImpl: Application status for spark-74dc1638e90149b893ef42bd776f3668 (phase: Running)
23/02/26 15:46:44 INFO LoggingPodStatusWatcherImpl: State changed, new state: 
	 pod name: spark-example-e4f249868c793199-driver
	 namespace: default
	 labels: spark-app-name -> spark-example, spark-app-selector -> spark-74dc1638e90149b893ef42bd776f3668, spark-role -> driver, spark-version -> 3.3.2
	 pod uid: f82c4e16-3aea-4a48-8220-1bfe06e47d0b
	 creation time: 2023-02-26T06:46:39Z
	 service account name: default
	 volumes: spark-local-dir-1, spark-conf-volume-driver, default-token-2dxpc
	 node name: k8s-19-worker09
	 start time: 2023-02-26T06:46:40Z
	 phase: Failed
	 container status: 
		 container name: spark-kubernetes-driver
		 container image: 192.168.220.202:5000/spark:v1.0
		 container state: terminated
		 container started at: 2023-02-26T06:46:42Z
		 container finished at: 2023-02-26T06:46:44Z
		 exit code: 101
		 termination reason: Error
23/02/26 15:46:44 INFO LoggingPodStatusWatcherImpl: Application status for spark-74dc1638e90149b893ef42bd776f3668 (phase: Failed)
23/02/26 15:46:44 INFO LoggingPodStatusWatcherImpl: Container final statuses:
	 container name: spark-kubernetes-driver
	 container image: 192.168.220.202:5000/spark:v1.0
	 container state: terminated
	 container started at: 2023-02-26T06:46:42Z
	 container finished at: 2023-02-26T06:46:44Z
	 exit code: 101
	 termination reason: Error
23/02/26 15:46:44 INFO LoggingPodStatusWatcherImpl: Application spark-example with submission ID default:spark-example-e4f249868c793199-driver finished
23/02/26 15:46:44 INFO ShutdownHookManager: Shutdown hook called
23/02/26 15:46:44 INFO ShutdownHookManager: Deleting directory /tmp/spark-86286d2a-0416-43ff-990a-6b1bcf5ccebc

### Log를 찍어보면 아래와 같다.
$ kubectl logs spark-example-e4f249868c793199-driver
	## 생략 ##
	+ CMD=("$SPARK_HOME/bin/spark-submit" --conf "spark.driver.bindAddress=$SPARK_DRIVER_BIND_ADDRESS" --deploy-mode client "$@")
	+ exec /usr/bin/tini -s -- /opt/spark/bin/spark-submit --conf spark.driver.bindAddress=10.10.192.30 --deploy-mode client --properties-file /opt/spark/conf/spark.properties --class org.apache.spark.examples.SparkPi local:///data/jhhan/spark/spark-3.3.2-bin-hadoop3/examples/jars/spark-examples_2.12-3.3.2.jar
	23/02/26 06:46:44 WARN NativeCodeLoader: Unable to load native-hadoop library for your platform... using builtin-java classes where applicable
	23/02/26 06:46:44 WARN DependencyUtils: Local jar /data/jhhan/spark/spark-3.3.2-bin-hadoop3/examples/jars/spark-examples_2.12-3.3.2.jar does not exist, skipping.
	23/02/26 06:46:44 WARN DependencyUtils: Local jar /data/jhhan/spark/spark-3.3.2-bin-hadoop3/examples/jars/spark-examples_2.12-3.3.2.jar does not exist, skipping.
	Error: Failed to load class org.apache.spark.examples.SparkPi.
	23/02/26 06:46:44 INFO ShutdownHookManager: Shutdown hook called
	23/02/26 06:46:44 INFO ShutdownHookManager: Deleting directory /tmp/spark-39d25f0b-a40d-4156-98d3-a734916afd9d

## 아마도 Log 내용으로 보아 내가 설정한 local이 sumit을 실행한 곳이나 Pod가 배포된 k8s Worker 서버가 아닌 Pod 내부를 말하는것 같다.
## https://github.com/GoogleCloudPlatform/spark-on-k8s-operator/issues/1473
## 즉 수행할 이미지를 만들어 쓰거나 수행할 app jar를 모아놓는 pvc를 만들어서 거글 통해서 하면 편리할듯 하다.

## 4차 시도
$ vi k8s_submit.sh
#!/bin/bash
IMAGE_REPO=192.168.220.202:5000
export SPARK_HOME=/data/jhhan/spark/spark-3.3.2-bin-hadoop3

$SPARK_HOME/bin/spark-submit \
    --master k8s://https://192.168.220.225:6443 \
    --deploy-mode cluster \
    --name spark-example \
    --class org.apache.spark.examples.SparkPi \
    --conf spark.executor.instances=3 \
    --conf spark.kubernetes.container.image=$IMAGE_REPO/spark:v1.0 \
    local:///opt/spark/examples/jars/spark-examples_2.12-3.3.2.jar ## 경로 변경 (위에서 Build시에 쓰는 Dockerfile을 열어보니 존재할듯 하다)

## cat /home/data/hanjh/spark/spark-3.3.2-bin-hadoop3/kubernetes/dockerfiles/spark/Dockerfile

$ ./k8s_submit.sh
## Driver는 떠있지만 Log를 보니 Service 접근이 안되는듯 하다.
$ kubectl logs pod/spark-example-086b82868cb4ec97-driver

23/02/26 07:51:58 ERROR SparkContext: Error initializing SparkContext.
org.apache.spark.SparkException: External scheduler cannot be instantiated
	at org.apache.spark.SparkContext$.org$apache$spark$SparkContext$$createTaskScheduler(SparkContext.scala:3002)
	at org.apache.spark.SparkContext.<init>(SparkContext.scala:573)
	at org.apache.spark.SparkContext$.getOrCreate(SparkContext.scala:2714)
	at org.apache.spark.sql.SparkSession$Builder.$anonfun$getOrCreate$2(SparkSession.scala:953)
	at scala.Option.getOrElse(Option.scala:189)
	at org.apache.spark.sql.SparkSession$Builder.getOrCreate(SparkSession.scala:947)
	at org.apache.spark.examples.SparkPi$.main(SparkPi.scala:30)
	at org.apache.spark.examples.SparkPi.main(SparkPi.scala)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(Unknown Source)
	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(Unknown Source)
	at java.base/java.lang.reflect.Method.invoke(Unknown Source)
	at org.apache.spark.deploy.JavaMainApplication.start(SparkApplication.scala:52)
	at org.apache.spark.deploy.SparkSubmit.org$apache$spark$deploy$SparkSubmit$$runMain(SparkSubmit.scala:958)
	at org.apache.spark.deploy.SparkSubmit.doRunMain$1(SparkSubmit.scala:180)
	at org.apache.spark.deploy.SparkSubmit.submit(SparkSubmit.scala:203)
	at org.apache.spark.deploy.SparkSubmit.doSubmit(SparkSubmit.scala:90)
	at org.apache.spark.deploy.SparkSubmit$$anon$2.doSubmit(SparkSubmit.scala:1046)
	at org.apache.spark.deploy.SparkSubmit$.main(SparkSubmit.scala:1055)
	at org.apache.spark.deploy.SparkSubmit.main(SparkSubmit.scala)
Caused by: java.lang.reflect.InvocationTargetException
	at java.base/jdk.internal.reflect.NativeConstructorAccessorImpl.newInstance0(Native Method)
	at java.base/jdk.internal.reflect.NativeConstructorAccessorImpl.newInstance(Unknown Source)
	at java.base/jdk.internal.reflect.DelegatingConstructorAccessorImpl.newInstance(Unknown Source)
	at java.base/java.lang.reflect.Constructor.newInstance(Unknown Source)
	at org.apache.spark.scheduler.cluster.k8s.KubernetesClusterManager.makeExecutorPodsAllocator(KubernetesClusterManager.scala:158)
	at org.apache.spark.scheduler.cluster.k8s.KubernetesClusterManager.createSchedulerBackend(KubernetesClusterManager.scala:113)
	at org.apache.spark.SparkContext$.org$apache$spark$SparkContext$$createTaskScheduler(SparkContext.scala:2996)
	... 19 more
Caused by: io.fabric8.kubernetes.client.KubernetesClientException: Failure executing: GET at: https://kubernetes.default.svc/api/v1/namespaces/default/pods/spark-example-086b82868cb4ec97-driver. Message: Forbidden!Configured service account doesn't have access. Service account may have been revoked. pods "spark-example-086b82868cb4ec97-driver" is forbidden: User "system:serviceaccount:default:default" cannot get resource "pods" in API group "" in the namespace "default".
	at io.fabric8.kubernetes.client.dsl.base.OperationSupport.requestFailure(OperationSupport.java:682)
	at io.fabric8.kubernetes.client.dsl.base.OperationSupport.requestFailure(OperationSupport.java:661)
	at io.fabric8.kubernetes.client.dsl.base.OperationSupport.assertResponseCode(OperationSupport.java:610)
	at io.fabric8.kubernetes.client.dsl.base.OperationSupport.handleResponse(OperationSupport.java:555)
	at io.fabric8.kubernetes.client.dsl.base.OperationSupport.handleResponse(OperationSupport.java:518)
	at io.fabric8.kubernetes.client.dsl.base.OperationSupport.handleGet(OperationSupport.java:487)
	at io.fabric8.kubernetes.client.dsl.base.OperationSupport.handleGet(OperationSupport.java:457)
	at io.fabric8.kubernetes.client.dsl.base.BaseOperation.handleGet(BaseOperation.java:698)
	at io.fabric8.kubernetes.client.dsl.base.BaseOperation.getMandatory(BaseOperation.java:184)
	at io.fabric8.kubernetes.client.dsl.base.BaseOperation.get(BaseOperation.java:151)
	at io.fabric8.kubernetes.client.dsl.base.BaseOperation.get(BaseOperation.java:83)
	at org.apache.spark.scheduler.cluster.k8s.ExecutorPodsAllocator.$anonfun$driverPod$1(ExecutorPodsAllocator.scala:79)
	at scala.Option.map(Option.scala:230)
	at org.apache.spark.scheduler.cluster.k8s.ExecutorPodsAllocator.<init>(ExecutorPodsAllocator.scala:78)
	... 26 more
23/02/26 07:51:58 INFO SparkUI: Stopped Spark web UI at http://spark-example-086b82868cb4ec97-driver-svc.default.svc:4040
23/02/26 07:51:58 INFO MapOutputTrackerMasterEndpoint: MapOutputTrackerMasterEndpoint stopped!'

## 5차 시도
# https://stackoverflow.com/questions/55498702/how-to-fix-forbiddenconfigured-service-account-doesnt-have-access-with-spark
# spark 가 k8s를 사용하기 위한 권한을 부여해야한다.
# 아래와 같이 생성후 사용하도록 conf를 지정한다.
$ vi spark_base.yaml
apiVersion: v1
kind: ServiceAccount
metadata:
  name: spark
---
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRole
metadata:
  name: spark-cluster-role
rules:
- apiGroups: [""] # "" indicates the core API group
  resources: ["pods"]
  verbs: ["get", "watch", "list", "create", "delete"]
- apiGroups: [""] # "" indicates the core API group
  resources: ["services"]
  verbs: ["get", "create", "delete"]
- apiGroups: [""] # "" indicates the core API group
  resources: ["configmaps"]
  verbs: ["get", "create", "delete"]
---
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRoleBinding
metadata:
  name: spark-cluster-role-binding
subjects:
- kind: ServiceAccount
  name: spark
  namespace: default
roleRef:
  kind: ClusterRole
  name: spark-cluster-role
  apiGroup: rbac.authorization.k8s.io

$ kubectl apply -f spark_base.yaml

$ vi k8s_submit.sh
## 아래 컨피그 추가
--conf spark.kubernetes.authenticate.driver.serviceAccountName=spark

$ ./k8s_submit.sh
## 성공!!!!!!

## 수행된후 확인하면 이렇게 driver의 상태가 completed가 된다.
NAME                                        READY   STATUS      RESTARTS   AGE     IP             NODE              NOMINATED NODE   READINESS GATES
pod/pod-1                                   1/1     Running     0          3d15h   10.10.220.66   k8s-19-master02   <none>           <none>
pod/spark-example-2a262386904c5997-driver   0/1     Completed   0          42s     10.10.192.33   k8s-19-worker09   <none>           <none>

NAME                                                TYPE        CLUSTER-IP	EXTERNAL-IP   PORT(S)                      AGE    SELECTOR
service/kubernetes                         	    ClusterIP   10.244.0.1      <none>        443/TCP                      332d   <none>
service/spark-example-2a262386904c5997-driver-svc   ClusterIP   None      	<none>        7078/TCP,7079/TCP,4040/TCP   42s    spark-app-name=spar
k-example,spark-app-selector=spark-0f7944e3ad1f40758cdfcac6c1d9b2d9,spark-role=driver,spark-version=3.3.2
service/svc-1                                       NodePort    10.244.117.41   <none>        9000:31788/TCP               5d9h   app=pod

```

### **[k8s_submit.sh]**
 - NameSpace를 추가로 지정해 주었다.
```bash
#!/bin/bash
IMAGE_REPO=192.168.220.202:5000
export SPARK_HOME=/data/jhhan/spark/spark-3.3.2-bin-hadoop3

$SPARK_HOME/bin/spark-submit \
    --master k8s://https://192.168.220.225:6443 \
    --deploy-mode cluster \
    --name spark-example \
    --class org.apache.spark.examples.SparkPi \
    --conf spark.executor.instances=3 \
    --conf spark.kubernetes.container.image=$IMAGE_REPO/spark:v1.0 \
    --conf spark.kubernetes.authenticate.driver.serviceAccountName=spark \
    --conf spark.kubernetes.namespace=test \
    local:///opt/spark/examples/jars/spark-examples_2.12-3.3.2.jar
```

### **[spark_base.yaml]**
 - NameSpace를 추가로 지정해 주었다.
```yaml
apiVersion: v1
kind: ServiceAccount
metadata:
  name: spark
  namespace: test
---
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRole
metadata:
  name: spark-cluster-role
rules:
- apiGroups: [""] # "" indicates the core API group
  resources: ["pods"]
  verbs: ["get", "watch", "list", "create", "delete"]
- apiGroups: [""] # "" indicates the core API group
  resources: ["services"]
  verbs: ["get", "create", "delete"]
- apiGroups: [""] # "" indicates the core API group
  resources: ["configmaps"]
  verbs: ["get", "create", "delete"]
---
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRoleBinding
metadata:
  name: spark-cluster-role-binding
subjects:
- kind: ServiceAccount
  name: spark
  namespace: test
roleRef:
  kind: ClusterRole
  name: spark-cluster-role
  apiGroup: rbac.authorization.k8s.io
```

## **# AirFlow 를 설치해보자**
버전 확인 결과 v1.4.0 부터는 k8s 1.20 이상의 클러스터를 원한다. 우린 v1.3.0
- https://airflow.apache.org/docs/helm-chart/1.3.0/index.html
```
# Kubernetes 1.14+ cluster
# Helm 3.0+
# PV provisioner support in the underlying infrastructure (optionally)
```

### **[포인트]** 
Airflow를 사용하는 이유<br>
Airflow에서는 KubernetesPodOperator라는 operator를 지원함. <br>
KubernetesPodOperator를 사용하여 Spark 이미지의 spark submit ... 을 실행하면 Airflow에서 Spark job을 손쉽게 돌릴 수 있다고함.

AirFlow로 Spark_Submit 관리 방법은 아래 3가지 방안 (참고 : https://mightytedkim.tistory.com/43)
1. KubernetePodOperator(KPO) : dag
	- Spark_Submit Instance 여러개여도 Pod 1개만 동작
2. SparkApplication(CRD) : yaml
3. SparkKubernetesOperator(SKO) : yaml + dag

### **[설치과정]**
```sh
$ helm repo add apache-airflow https://airflow.apache.org
$ helm pull airflow apache-airflow/airflow --version 1.3.0
$ tar -zxvf airflow-1.3.0.tgz
$ helm install airflow ./airflow --namespace test --version 1.3.0
$ helm delete airflow apache-airflow/airflow --namespace test
$ watch kubectl get all -n test

NAME                                       READY   STATUS              RESTARTS   AGE
pod/airflow-flower-7c87f95f46-zw4bw        0/1     Running             1          2m16s
pod/airflow-postgresql-0                   0/1     ContainerCreating   0          2m17s
pod/airflow-redis-0                        0/1     ContainerCreating   0          2m16s
pod/airflow-run-airflow-migrations-7t7pk   0/1     ContainerCreating   0          2m14s
pod/airflow-scheduler-5c5796cb67-xqwdc     0/2     Init:0/1            0          2m15s
pod/airflow-statsd-84f4f9898-czbfj         0/1     ContainerCreating   0          2m14s
pod/airflow-triggerer-6857cb49cc-5wcfm     0/1     Init:0/1            0          2m15s
pod/airflow-webserver-77b74bd7d7-777bm     0/1     Init:0/1            0          2m16s
pod/airflow-worker-0                       0/2     Init:0/1            0          2m17s

NAME                                  TYPE        CLUSTER-IP	   EXTERNAL-IP   PORT(S)             AGE
service/airflow-flower                ClusterIP   10.244.255.235   <none>        5555/TCP            2m18s
service/airflow-postgresql            ClusterIP   10.244.232.255   <none>        5432/TCP            2m18s
service/airflow-postgresql-headless   ClusterIP   None             <none>        5432/TCP            2m18s
service/airflow-redis                 ClusterIP   10.244.157.245   <none>        6379/TCP            2m18s
service/airflow-statsd                ClusterIP   10.244.25.137    <none>        9125/UDP,9102/TCP   2m18s
service/airflow-webserver             ClusterIP   10.244.96.211    <none>        8080/TCP            2m18s
service/airflow-worker                ClusterIP   None             <none>        8793/TCP            2m18s

NAME                                READY   UP-TO-DATE   AVAILABLE   AGE
deployment.apps/airflow-flower      0/1     1            0           2m19s
deployment.apps/airflow-scheduler   0/1     1            0           2m19s
deployment.apps/airflow-statsd      0/1     1            0           2m19s
deployment.apps/airflow-triggerer   0/1     1            0           2m19s
deployment.apps/airflow-webserver   0/1     1            0           2m19s

NAME                                           DESIRED   CURRENT   READY   AGE
replicaset.apps/airflow-flower-7c87f95f46      1         1         0	   2m17s
replicaset.apps/airflow-scheduler-5c5796cb67   1         1         0	   2m17s
replicaset.apps/airflow-statsd-84f4f9898       1         1         0	   2m19s
replicaset.apps/airflow-triggerer-6857cb49cc   1         1         0	   2m17s
replicaset.apps/airflow-webserver-77b74bd7d7   1         1         0	   2m17s

NAME                                  READY   AGE
statefulset.apps/airflow-postgresql   0/1     2m19s
statefulset.apps/airflow-redis        0/1     2m19s
statefulset.apps/airflow-worker       0/1     2m19s

NAME                                       COMPLETIONS   DURATION   AGE
job.batch/airflow-run-airflow-migrations   0/1           2m15s      2m19s

## 폐쇄 환경으로 설치가 안됨.
$ docker pull bitnami/postgresql:11.12.0-debian-10-r44
$ docker pull bitnami/bitnami-shell:10-debian-10-r125
$ docker pull bitnami/postgres-exporter:0.9.0-debian-10-r108

$ docker tag docker.io/bitnami/postgresql:11.12.0-debian-10-r44        192.168.220.202:5000/bitnami/postgresql:11.12.0-debian-10-r44
$ docker tag docker.io/bitnami/bitnami-shell:10-debian-10-r125         192.168.220.202:5000/bitnami/bitnami-shell:10-debian-10-r125
$ docker tag docker.io/bitnami/postgres-exporter:0.9.0-debian-10-r108  192.168.220.202:5000/bitnami/postgres-exporter:0.9.0-debian-10-r108

$ docker push 192.168.220.202:5000/bitnami/postgresql:11.12.0-debian-10-r44
$ docker push 192.168.220.202:5000/bitnami/bitnami-shell:10-debian-10-r125
$ docker push 192.168.220.202:5000/bitnami/postgres-exporter:0.9.0-debian-10-r108

# 템플릿 이미지 변경
$ vi /data/jhhan/airflow/airflow/charts/postgresql/values.yaml
## > registry: docker.io를 폐쇄환경 docker repo 주소로 변경


## 이미지 준비
docker pull apache/airflow:airflow-pgbouncer-2021.04.28-1.14.0
docker pull redis:6-buster
docker pull apache/airflow:airflow-pgbouncer-2021.04.28-1.14.0
docker pull apache/airflow:airflow-pgbouncer-exporter-2021.09.22-0.12.0
docker pull k8s.gcr.io/git-sync/git-sync:v3.3.0

docker tag apache/airflow:airflow-pgbouncer-2021.04.28-1.14.0           192.168.220.202:5000/apache/airflow:airflow-pgbouncer-2021.04.28-1.14.0
docker tag redis:6-buster                                               192.168.220.202:5000/redis:6-buster
docker tag apache/airflow:airflow-pgbouncer-2021.04.28-1.14.0           192.168.220.202:5000/apache/airflow:airflow-pgbouncer-2021.04.28-1.14.0
docker tag apache/airflow:airflow-pgbouncer-exporter-2021.09.22-0.12.0  192.168.220.202:5000/apache/airflow:airflow-pgbouncer-exporter-2021.09.22-0.12.0
docker tag k8s.gcr.io/git-sync/git-sync:v3.3.0                          192.168.220.202:5000/k8s.gcr.io/git-sync/git-sync:v3.3.0

docker push 192.168.220.202:5000/apache/airflow:airflow-pgbouncer-2021.04.28-1.14.0
docker push 192.168.220.202:5000/redis:6-buster
docker push 192.168.220.202:5000/apache/airflow:airflow-pgbouncer-2021.04.28-1.14.0
docker push 192.168.220.202:5000/apache/airflow:airflow-pgbouncer-exporter-2021.09.22-0.12.0
docker push 192.168.220.202:5000/k8s.gcr.io/git-sync/git-sync:v3.3.0

## 변경사항 없음.
$ vi /data/jhhan/airflow/airflow/values.yaml
### > webserver service type 값 NodePort로 변경

## 설치
$ helm install -n test airflow ./airflow
NAME: airflow
LAST DEPLOYED: Sat Feb 25 15:30:57 2023
NAMESPACE: test
STATUS: deployed
REVISION: 1
TEST SUITE: None
NOTES:
Thank you for installing Apache Airflow 2.2.1!

Your release is named airflow.
You can now access your dashboard(s) by executing the following command(s) and visiting the corresponding port at localhost in your browser:

Airflow Webserver:     kubectl port-forward svc/airflow-webserver 8080:8080 --namespace test
Flower dashboard:      kubectl port-forward svc/airflow-flower 5555:5555 --namespace test
Default Webserver (Airflow UI) Login credentials:
    username: admin
    password: admin
Default Postgres connection credentials:
    username: postgres
    password: postgres
    port: 5432

You can get Fernet Key value by running the following:

    echo Fernet Key: $(kubectl get secret --namespace test airflow-fernet-key -o jsonpath="{.data.fernet-key}" | base64 --decode)

###########################################################
#  WARNING: You should set a static webserver secret key  #
###########################################################

You are using a dynamically generated webserver secret key, which can lead to
unnecessary restarts of your Airflow components.

Information on how to set a static webserver secret key can be found here:
https://airflow.apache.org/docs/helm-chart/stable/production-guide.html#webserver-secret-key

## 설정 변경
$ vi values.yaml 
### > executor: "KubernetesExecutor" 로 변경
$ helm ls -n test
NAME   	NAMESPACE	REVISION	UPDATED                                	STATUS  	CHART        	APP VERSION
airflow	test     	1       	2023-02-25 15:30:57.643731525 +0900 KST	deployed	airflow-1.3.0	2.2.1      
$ helm upgrade --install airflow ./airflow -n test -f ./airflow/^Clues.yaml
$ cd ..
$ pwd
/data/jhhan/airflow
$ helm upgrade --install airflow ./airflow -n test -f ./airflow/values.yaml


## 제거 
$ helm delete -n test airflow
```

### **[설치된 모습]**
```sh
Every 1.0s: kubectl get all -n test -o wide                                                                                                                         k8s-19-master03: Sat Feb 25 14:59:43 2023

NAME                                       READY   STATUS      RESTARTS   AGE     IP             NODE              NOMINATED NODE   READINESS GATES
pod/airflow-flower-7c87f95f46-srhr5        1/1     Running     5          8m35s   10.10.192.12   k8s-19-worker09   <none>           <none>
pod/airflow-postgresql-0                   1/1     Running     0          8m37s   10.10.192.17   k8s-19-worker09   <none>           <none>
pod/airflow-redis-0                        1/1     Running     0          8m35s   10.10.192.16   k8s-19-worker09   <none>           <none>
pod/airflow-run-airflow-migrations-x2nw6   0/1     Completed   2          8m35s   10.10.192.14   k8s-19-worker09   <none>           <none>
pod/airflow-scheduler-5c5796cb67-lcnnw     2/2     Running     0          8m35s   10.10.192.13   k8s-19-worker09   <none>           <none>
pod/airflow-statsd-84f4f9898-k82ds         1/1     Running     0          8m35s   10.10.192.10   k8s-19-worker09   <none>           <none>
pod/airflow-triggerer-6857cb49cc-p55db     1/1     Running     0          8m35s   10.10.192.9    k8s-19-worker09   <none>           <none>
pod/airflow-webserver-6675d7dbdf-8wfcc     1/1     Running     0          8m35s   10.10.192.11   k8s-19-worker09   <none>           <none>
pod/airflow-worker-0                       2/2     Running     0          8m35s   10.10.192.15   k8s-19-worker09   <none>           <none>

NAME                                  TYPE        CLUSTER-IP	  EXTERNAL-IP   PORT(S)             AGE     SELECTOR
service/airflow-flower                ClusterIP   10.244.3.219    <none>        5555/TCP            8m38s   component=flower,release=airflow,tier=airflow
service/airflow-postgresql            ClusterIP   10.244.126.12   <none>        5432/TCP            8m38s   app.kubernetes.io/instance=airflow,app.kubernetes.io/name=postgresql,role=primary
service/airflow-postgresql-headless   ClusterIP   None            <none>        5432/TCP            8m38s   app.kubernetes.io/instance=airflow,app.kubernetes.io/name=postgresql
service/airflow-redis                 ClusterIP   10.244.128.47   <none>        6379/TCP            8m38s   component=redis,release=airflow,tier=airflow
service/airflow-statsd                ClusterIP   10.244.135.75   <none>        9125/UDP,9102/TCP   8m38s   component=statsd,release=airflow,tier=airflow
service/airflow-webserver             ClusterIP   10.244.154.25   <none>        8080/TCP            8m38s   component=webserver,release=airflow,tier=airflow
service/airflow-worker                ClusterIP   None            <none>        8793/TCP            8m38s   component=worker,release=airflow,tier=airflow

NAME                                READY   UP-TO-DATE   AVAILABLE   AGE     CONTAINERS                        IMAGES                                                      SELECTOR
deployment.apps/airflow-flower      1/1     1            1           8m38s   flower                            apache/airflow:2.2.1                                        component=flower,release=airflow,t
ier=airflow
deployment.apps/airflow-scheduler   1/1     1            1           8m38s   scheduler,scheduler-log-groomer   apache/airflow:2.2.1,apache/airflow:2.2.1                   component=scheduler,release=airflo
w,tier=airflow
deployment.apps/airflow-statsd      1/1     1            1           8m38s   statsd                            apache/airflow:airflow-statsd-exporter-2021.04.28-v0.17.0   component=statsd,release=airflow,t
ier=airflow
deployment.apps/airflow-triggerer   1/1     1            1           8m38s   triggerer                         apache/airflow:2.2.1                                        component=triggerer,release=airflo
w,tier=airflow
deployment.apps/airflow-webserver   1/1     1            1           8m38s   webserver                         apache/airflow:2.2.1                                        component=webserver,release=airflo
w,tier=airflow

NAME                                           DESIRED   CURRENT   READY   AGE     CONTAINERS                        IMAGES                                                      SELECTOR
replicaset.apps/airflow-flower-7c87f95f46      1         1         1	   8m38s   flower                            apache/airflow:2.2.1                                        component=flower,pod-templat
e-hash=7c87f95f46,release=airflow,tier=airflow
replicaset.apps/airflow-scheduler-5c5796cb67   1         1         1	   8m38s   scheduler,scheduler-log-groomer   apache/airflow:2.2.1,apache/airflow:2.2.1                   component=scheduler,pod-temp
late-hash=5c5796cb67,release=airflow,tier=airflow
replicaset.apps/airflow-statsd-84f4f9898       1         1         1	   8m38s   statsd                            apache/airflow:airflow-statsd-exporter-2021.04.28-v0.17.0   component=statsd,pod-templat
e-hash=84f4f9898,release=airflow,tier=airflow
replicaset.apps/airflow-triggerer-6857cb49cc   1         1         1	   8m38s   triggerer                         apache/airflow:2.2.1                                        component=triggerer,pod-temp
late-hash=6857cb49cc,release=airflow,tier=airflow
replicaset.apps/airflow-webserver-6675d7dbdf   1         1         1	   8m38s   webserver                         apache/airflow:2.2.1                                        component=webserver,pod-temp
late-hash=6675d7dbdf,release=airflow,tier=airflow

NAME                                  READY   AGE     CONTAINERS                  IMAGES
statefulset.apps/airflow-postgresql   1/1     8m38s   airflow-postgresql          192.168.220.202:5000/bitnami/postgresql:11.12.0-debian-10-r44
statefulset.apps/airflow-redis        1/1     8m38s   redis                       redis:6-buster
statefulset.apps/airflow-worker       1/1     8m38s   worker,worker-log-groomer   apache/airflow:2.2.1,apache/airflow:2.2.1

NAME                                       COMPLETIONS   DURATION   AGE     CONTAINERS               IMAGES                 SELECTOR
job.batch/airflow-run-airflow-migrations   1/1           5m28s      8m39s   run-airflow-migrations   apache/airflow:2.2.1   controller-uid=e3a49c1c-9394-4e70-9b1f-387b5a95d513

```
### **[설치확인]**
```sh
$ kubectl svc -n test
## airflow-webserver 의 NodePort 값으로 접근

## 브라우저로 http://192.168.220.214:32410/home 접근
```

### **[구성 요소의 역할]**
#### # DAG (Directed Acyclic Graph)
- 비순환 그래프
- 비순환 Flow를 의미함
- 뛰어난 트랜잭션 처리속도, 확장성, 저렴한 수수료의 장점
- Python, Scala 사용
#### # AirFlow Webserver
- AirFlow Log, Scheduler, DAG 목록, Task상태 등 모니터링 기능
#### # AirFlow Scheduler
- work들의 스케줄링을해주는 Component
- executor에게 task를 제공하는 역할
#### # AirFlow Executor
- 실행중인 Task를 Handling하는 Component
- Executor는 Worker에게 Task를 Push
#### # AirFlow Worker
- 실제 Task를 실행하는 주체
#### # AirFlow Database
- Airflow에 있는 DAG, Task 등의 Metadata를 저장하고 관리


### **[Spark 수행 DAG 작성 방법]**
```py
from airflow import DAG
from airflow.contrib.operators.spark_submit_operator import SparkSubmitOperator
from datetime import datetime, timedelta

# DAG 설정
default_args = {
    'owner': 'airflow',
    'depends_on_past': False,
    'start_date': datetime(2023, 7, 27),
    'email': ['your-email@example.com'],
    'email_on_failure': False,
    'email_on_retry': False,
    'retries': 1,
    'retry_delay': timedelta(minutes=5),
}

dag = DAG(
    'spark_submit_job', default_args=default_args,
    schedule_interval=timedelta(days=1))

# Spark 작업 실행 task
submit_task = SparkSubmitOperator(
    task_id='spark_submit',
    conn_id='spark_default',
    application='/path/to/your/sparkjob.py',
    executor_memory='2g',
    total_executor_cores=2,
    dag=dag)


```

###