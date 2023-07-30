# Spark ON kubernetes 설치!
## **# Spark란?**
- java, Python, Scala, R등 언어 처리 가능
- mapreduce보다 빠르게 처리 (메모리에서 처리, 네트워크IO 감소 / Disk IO 감소)
- RDD (Resilient Distributed Dataset) (참고:https://bomwo.cc/posts/spark-rdd/)
- Master node 와 worker 노드로 구성
![온프레미스환경](./%EC%8A%A4%ED%8C%8C%ED%81%AC%EA%B5%AC%EC%84%B1%EB%8F%84.png)

## **# Spark on Kubernetes는 어떻게 다른가요?**
```
일반적으로 YARN을 통해서 client모드로 Spark job을 실행시켜보면 master node에서 driver가 동작하고 worker node에서 executor들이 동작하게 됩니다. 물론 cluster모드라면 driver 또한 worker node어딘가에서 동작할테지요.

Kubernetes에서는 driver가 뜨고 이 driver가 executor pod들을 실행시키게 되는데요. client모드로 Spark job을 실행하면 실행하고자 하는 pod이 driver가 되고 executor pod들이 새로 뜨게 됩니다. cluster 모드로 실행하면 실행한 pod과는 별개로 driver pod이 새로 뜨게되며 새로 뜬 driver pod이 executor pod들을 띄우게 되는 구조입니다.
```
참고 : https://blog.banksalad.com/tech/spark-on-kubernetes/ <br>
참고 : https://techblog.woowahan.com/10291/

## **# AWS 의 경우 간단한 처리 구조**
보관 > 통합 > 처리 구조 <br>
AWS S3 > AWS Glue > Spark on AWS EMR <br>
 - AWS S3 : 확장성, 데이터 가용성, 보안 및 성능을 제공하는 객체 스토리지 서비스
 - AWS Glue : 분석, 기계 학습(ML) 및 애플리케이션 개발을 위해 여러 소스에서 데이터를 쉽게 탐색, 준비, 이동 및 통합할 수 있도록 하는 확장 가능한 서버리스 데이터 통합 서비스
 - AWS EMR :  Apache Spark, Apache Hive 및 Presto와 같은 오픈 소스 프레임워크를 사용하여 페타바이트급 데이터 처리, 대화
 
### **# [Spark on Kubernetes 구성]**
1. Spark 2.3 이상 binary에는 k8s 배포 관련 util 이 포함되어 있다.
2. spark app을 Image로 만들어준다.
3. 만든 Image를 submin-commit 으로 k8s cluster를 지정해서 보낸다.
4. 그럼 알아서 Driver 생성 및 Executor 생성 한다.

### **[1] Spark 이미지 준비**
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

```

### **[2] Spark 수행 환경 구성**
```bash
# Hadoop Binary가 필요 (native-hadoop library 를 필요로 함)
# 압축을 해제한 경로를 잘 기록해 두자 (ex. /data/jhhan/spark/hadoop-3.3.4/lib/native)
$ wget https://dlcdn.apache.org/hadoop/common/hadoop-3.3.4/hadoop-3.3.4.tar.gz
$ tar -zxvf hadoop-3.3.4.tar.gz

# https://stackoverflow.com/questions/55498702/how-to-fix-forbiddenconfigured-service-account-doesnt-have-access-with-spark
# spark 가 k8s를 사용하기 위한 권한을 부여해야한다.
# 아래와 같이 생성후 사용하도록 conf를 지정한다.
$ vi spark_base.yaml
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

$ kubectl apply -f spark_base.yaml
```

### **[3] Spark 수행**
```bash
# sumit shell 작성해보자
## 마스터에서 클러스터 정보 추출
## 아래 정보에서 내 k8s의 마스터 API Server 호출 주소를 알아낼 수 있다.
$ kubectl cluster-info
Kubernetes master is running at https://192.168.220.225:6443
KubeDNS is running at https://192.168.220.225:6443/api/v1/namespaces/kube-system/services/kube-dns:dns/proxy
Metrics-server is running at https://192.168.220.225:6443/api/v1/namespaces/kube-system/services/https:metrics-server:/proxy

# Spark Binary에서 아래 경로에 spark-env.sh 을 수정한다.
# 환경 구성에서 구성한 Hadoop의 경로를 작성해 줘야 한다.
$ cp ./spark-3.3.2-bin-hadoop3/conf/spark-env.sh.template ./spark-3.3.2-bin-hadoop3/conf/spark-env.sh
$ vi ./spark-3.3.2-bin-hadoop3/conf/spark-env.sh
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:/data/jhhan/spark/hadoop-3.3.4/lib/native

# sumit shell 작성해보자
# 마지막 local 경로의 jar가 내가 처리하고자 하는 Source 파일이다.
# 해당 경로는 위 Image 안에 포함된 경로이다. 즉 배포된 Container안에 "/opt/spark/examples/jars/spark-examples_2.12-3.3.2.jar" 경로에 파일이 존재한다.
# 지속적인 Base 환경 활용 방법으로는 PVC를 연계하여 경로를 지정하여 수행하는 방법이다.
#!/bin/bash
$ vi k8s_submit.sh
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


### **[k8s_submit.sh]**
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