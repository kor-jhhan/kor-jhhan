# kubeflow 설치
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