# kubeflow
## \# kubeflow 소개
Kubeflow는 기계 학습 워크플로우를 쿠버네티스(Kubernetes) 상에서 간편하게 구축하고 실행하기 위한 오픈소스 프로젝트입니다. 이 프로젝트는 Google Cloud에서 시작되었으며, 쿠버네티스에서 동작하는 다양한 기계 학습 시스템을 통합하였습니다. Kubeflow는 데이터 과학자들과 ML 엔지니어들이 모델을 빠르게 개발하고 배포하는데 많은 도움을 주고 있습니다.

- **포괄적인 기계 학습 스택** 
    
    Kubeflow는 데이터 전처리, 모델 훈련, 모델 서빙, 모델 관리 등 기계 학습의 전체 주기를 수행하는데 필요한 도구들을 제공합니다.

- **쿠버네티스와의 밀접한 통합** 

    Kubeflow는 쿠버네티스 상에서 동작하며, 쿠버네티스의 기능을 활용하여 워크플로우를 자동화하고, 각 컴포넌트를 스케일링하며, 리소스를 관리할 수 있습니다.

- **다양한 프레임워크와 도구 지원** 

    Kubeflow는 TensorFlow, PyTorch, MXNet 등 다양한 머신러닝 프레임워크와, Jupyter Notebook, Katib (하이퍼파라미터 튜닝 도구), Seldon (오픈 소스 MLOps 프레임워크) 등의 도구를 지원합니다.

- **커뮤니티 및 생태계 지원** 

    Kubeflow는 활발한 오픈소스 커뮤니티를 가지고 있어, 여러 기업과 개발자들이 참여하고 있습니다. 이를 통해 주기적인 업데이트와 새로운 기능 개발, 버그 수정 등이 지속적으로 이루어지며, 또한 문제 해결을 위한 다양한 자료와 도움을 받을 수 있습니다.

## \# kubeflow 구성요소
 - **Kubeflow Pipelines**

    ML 워크플로우를 실험, 재현, 공유 및 배포를 용이하게 하는 도구입니다. 각 단계는 독립적인 컨테이너로서 실행되며, 사용자는 파이프라인을 정의하는 Python SDK를 사용할 수 있습니다.

 - **Katib**

    Kubeflow의 하이퍼 파라미터 튜닝 컴포넌트입니다. 사용자는 하이퍼 파라미터의 검색 공간을 정의하고, Katib는 그 공간에서 최적의 파라미터를 찾아냅니다.

 - **KFServing**
 
    기계 학습 모델을 서빙하기 위한 프레임워크입니다. KFServing은 모델의 롤아웃, 롤백, 스케일링 등을 자동화하고, 여러 프레임워크에 대한 모델 서빙을 지원합니다.

 - **TFJob and PyTorchJob**

    Kubeflow에서 제공하는 특정 프레임워크를 위한 훈련 작업 컴포넌트입니다. 각각 TensorFlow와 PyTorch를 위해 설계되었습니다.

 - **Jupyter Notebook**

    사용자가 코드를 작성하고 실험을 수행할 수 있게 해주는 웹 기반의 인터랙티브 개발 환경입니다. Kubeflow는 사용자가 쿠버네티스 클러스터에 쉽게 Jupyter 서버를 배포하고 사용할 수 있게 지원합니다.

 - **Central Dashboard**

    사용자가 Kubeflow의 여러 기능을 쉽게 사용하고 관리할 수 있도록 도와주는 웹 기반 인터페이스입니다. 사용자는 대시보드를 통해 파이프라인을 관리하거나, Jupyter 노트북을 생성하거나, Katib 실험을 실행할 수 있습니다.

## \# kubeflow 설치
``` bash
# 기본 스토리지 설정 확인 후 미설정시 설정
kubectl get storageclass 
kubectl patch storageclass csi-cephfs-sc -p '{"metadata": {"annotations":{"storageclass.kubernetes.io/is-default-class":"true"}}}'
kubectl patch storageclass nutanix-file-csi

### 쿠베플로우 ctl 설치
tar -xvf kfctl_v1.0.2-0-ga476281_linux.tar.gz \
&& mv kfctl /usr/bin

export KF_NAME=my-kubeflow
export BASE_DIR=/root/kubeflow-v1.0.2
export KF_DIR=${BASE_DIR}/${KF_NAME}
export CONFIG_FILE=${KF_DIR}/kfctl_k8s_istio.v1.0.2.yaml

## 야물의 이미지 레파지토리 주소 일괄 변경
cd /data/kubeflow-v1.0.2/my-kubeflow/kustomize
find ./ -name "*.yaml" -exec sed -i 's/56.150/210.218/g' {} \;
cd 
## kubeflow 배포
kfctl apply -V -f ${CONFIG_FILE}
```

### 설치 이슈 대응
```bash
## 정상 배포가 안되면 kubeflow와 istio-system 삭제 후 네임스페이스 삭제
## 완벽히 삭제가 잘 안되어 마지막 단계에서 네임스페이스를 수동으로 삭제 해줘야함 
cd /data/kubeflow-v1.0.2/utile_sh
./9.del_kubeflow.sh


## AIP사용을 위한 Proxy 활성화
$ kubectl proxy

## 비정상 삭제시 아래 수행
### knative-serving 네임스페이스 삭제
kubectl get namespace knative-serving -o json > knative-serving.json
sed -i '/"kubernetes"/d' ./knative-serving.json
curl -k -H "Content-Type: application/json" -X PUT --data-binary @knative-serving.json 127.0.0.1:8001/api/v1/namespaces/knative-serving/finalize

### istio-system  네임스페이스 삭제
kubectl get namespace istio-system -o json > istio-system.json
sed -i '/"kubernetes"/d' ./istio-system.json
curl -k -H "Content-Type: application/json" -X PUT --data-binary @istio-system.json 127.0.0.1:8001/api/v1/namespaces/istio-system/finalize

###  kubeflow 네임스페이스 삭제
kubectl get namespace kubeflow  -o json > kubeflow.json
sed -i '/"kubernetes"/d' ./kubeflow.json 
curl -k -H "Content-Type: application/json" -X PUT --data-binary @kubeflow.json 127.0.0.1:8001/api/v1/namespaces/kubeflow/finalize

### cert-manager  네임스페이스 삭제
kubectl get namespace cert-manager -o json > cert-manager.json
sed -i '/"kubernetes"/d' ./cert-manager.json
curl -k -H "Content-Type: application/json" -X PUT --data-binary @cert-manager.json 127.0.0.1:8001/api/v1/namespaces/cert-manager/finalize
```