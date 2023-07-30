### [방법] : k8s kubeadm image Repository 설정 변경 방법
참고 : https://github.com/kubernetes/kubeadm/issues/2069 <br>
참고 : https://kubernetes.io/docs/reference/setup-tools/kubeadm/kubeadm-upgrade/<br>

``` bash
# 이작업은 Master Node에 해당되는 내용이다.

## 현재 구성된 kubeadm 설정을 파일로 내린다.
$ kubeadm config view > kubeadm-config.yaml
## imageRepository 주소 변경한다.
$ vi  kubeadm-config.yaml 
## kube cluster의 기본 정보를 수정한다.
$ kubeadm upgrade apply --config kubeadm-config.yaml

## 변경 확인
$ kubectl get cm -n kube-system kubeadm-config -o yaml

## 참고링크 이슈를 보면 etcd 주소는 자동으로 변경이 안된다. 현재 "v1.17.6", 에서는 동일한 이슈가 존재한다.
## etcd.yaml 만 변경 안된것을 확인 할 수 있다.
$ cd /etc/kubernetes/manifests/
$ ls -al 
합계 16
-rw-------. 1 root root 1920  1월 01 14:33 etcd.yaml
-rw-------. 1 root root 2631  6월 21 14:29 kube-apiserver.yaml
-rw-------. 1 root root 2554  6월 21 14:29 kube-controller-manager.yaml
-rw-------. 1 root root 1141  6월 21 14:29 kube-scheduler.yaml
## 수동으로 이미지 주소를 변경해준다.
$ vi etcd.yaml 

## 작업을 수행한 Master Node의 etcd 파드를 삭제해주면 정상적으로 다시 재 생성 된다.
$ kubectl delete pod -n kube-system etcd-[노드명]

## 나머지 마스터 노드들은 아래와 같은 cmd로 변경된 내용을 업그레이드 가능하다.
$ kubeadm upgrade node
$ vi /etc/kubernetes/manifests/etcd.yaml
```