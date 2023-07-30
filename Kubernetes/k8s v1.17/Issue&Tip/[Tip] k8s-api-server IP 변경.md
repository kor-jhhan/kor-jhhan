# [Tip] k8s-api-server IP 변경
## 현상
``` bash
## 전체 IP 변경이 있다면 Master Node IP 변경 작업이 선행된 후 수행해야 한다. 동시 수행시 연결이 되돌릴수 없다.
## keepalived.service 구성된 경우의 변경 방법
## 작업 순서가 중요함.
```

## 조치방법
### Step1 : kubeadm-conf.yaml 생성
```sh
$ cd /etc/kubernetes
# 현 configmap yaml로 추출
$ kubectl get configmap kubeadm-config -n kube-system -o jsonpath='{.data.ClusterConfiguration}' > /etc/kubernetes/kubeadm-conf.yaml

## 아래 certSANs: 내용 추가 및 변경
$ vi /etc/kubernetes/kubeadm-conf.yaml
apiServer:
  certSANs: # 추가사항
  - 192.123.123.225 ## 변경할 VIP
  - 192.123.123.214 ## Master01 IP
  - 192.123.123.215 ## Master02 IP
  - 192.123.123.216 ## Master03 IP
  extraArgs:
    authorization-mode: Node,RBAC
  timeoutForControlPlane: 4m0s
apiVersion: kubeadm.k8s.io/v1beta2
certificatesDir: /etc/kubernetes/pki
clusterName: kubernetes
controlPlaneEndpoint: 192.123.123.225:6443 # 변경할 VIP
controllerManager: {}
dns:
  type: CoreDNS
etcd:
  local:
    dataDir: /var/lib/etcd
imageRepository: k8s.gcr.io
kind: ClusterConfiguration
kubernetesVersion: v1.19.4
networking:
  dnsDomain: cluster.local
  podSubnet: 10.10.0.0/16
  serviceSubnet: 10.244.0.0/16
scheduler: {}
```

### Step2 : Master03 번 먼저 작업
```sh
# api server cert key 생성을 위해 백업 및 생성 작업
$ cd /etc/kubernetes/pki
$ mkdir backup
$ mv apiserver.* backup

# key 생성
$ kubeadm init phase certs apiserver --config /etc/kubernetes/kubeadm-conf.yaml

W0331 17:24:44.561374  355601 configset.go:348] WARNING: kubeadm cannot validate component configs for API groups [kubelet.config.k8s.io kubeproxy.config.k8s.io]
[certs] Generating "apiserver" certificate and key
[certs] apiserver serving cert is signed for DNS names [k8s-19-master03 kubernetes kubernetes.default kubernetes.default.svc kubernetes.default.svc.cluster.local] and IPs [10.244.0.1 192.123.123.216 192.123.123.212 192.168.210.212 192.123.123.214 192.123.123.215]

# key 생성 확인
$ ls -al /etc/kubernetes/pki/apiserver.*
-rw-r--r-- 1 root root 1310  3월 31 17:24 /etc/kubernetes/pki/apiserver.crt
-rw------- 1 root root 1679  3월 31 17:24 /etc/kubernetes/pki/apiserver.key

``` 

```sh
# 변경 내역 kubeadm 적용
$ kubeadm init phase upload-config kubelet --config /etc/kubernetes/kubeadm-conf.yaml

W0331 17:25:12.676054  355806 configset.go:348] WARNING: kubeadm cannot validate component configs for API groups [kubelet.config.k8s.io kubeproxy.config.k8s.io]
[kubelet] Creating a ConfigMap "kubelet-config-1.19" in namespace kube-system with the configuration for the kubelets in the cluster
```

```sh
## kubeadm-config k8s config Map 수정
$ kubectl edit cm -n kube-system kube-proxy

# --- 생략
    apiVersion: v1
    kind: Config
    clusters:
    - cluster:
        certificate-authority: /var/run/secrets/kubernetes.io/serviceaccount/ca.crt
        server: https://192.123.123.225:6443 ## 변경할 IP 변경
# --- 생략


## kubeadm-config k8s config Map 수정
$ kubectl edit cm -n kube-system kubeadm-config

# --- 생략
    apiVersion: kubeadm.k8s.io/v1beta2
    certificatesDir: /etc/kubernetes/pki
    clusterName: kubernetes
    controlPlaneEndpoint: 192.123.123.225:6443 ## 변경할 IP 변경
    controllerManager: {}
# --- 생략

## cluster-info config Map 수정
$ kubectl edit cm -n kube-public cluster-info 

# --- 생략
    clusters:
    - cluster:
        certificate-authority-data: LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUM1ekNDQWMrZ0F3SUJBZ0lCQURBTkJna3Foa2lHOXcwQkFRc0ZBREFWTVJNd0VRWURWUVFERXdwcmRXSmwKY201bGRHVnpNQjRYRFRJeU1ETXpNVEF4TVRRd01Wb1hEVE15TURNeU9EQXhNVFF3TVZvd0ZURVRNQkVHQTFVRQpBeE1LYTNWaVpYSnVaWFJsY3pDQ0FTSXdEUVlKS29aSWh2Y05BUUVCQlFBRGdnRVBBRENDQVFvQ2dnRUJBS3A1CnJmZEdJNExDTDJlOGUwZFd3UXA4YmFqaEl5UHU2YmZGNTI1ZDJybGQxcXFIL3hvbGplZE16OFd2RXdXeUE2TWQKYU84RVhiSDc3Rm9UL2ZtNElvV2dpL2duTExMbWM3STB0ZmNwR2lrMmkxa0hxajZhMjRlTnVPS2JMejU0VEx1bgplMk5vQ0plVC9OcUJvdTFpSXN6TGFTV0R0dlpUSnYrdGhYMzVKb0tUMGliUWwvRi9GT0svZmJLazQyb0tteFNxCks2U0FSc1JzcWNTNFc1bUxXTzJCY1AwallWOU5yY05KNVlkcXJxeTVWc3F2VUw2dGpQenJNWUl0eWhJR1J3V3MKT3FyMitSc2p0RWdEaXVLejJ6QTZaNlBKNEdocGxxcmZrWWFVSTRHTFFwRzM1QklmUXhZZ2YwMVRUbTF3bXdXaAo1WTBJTC94R3AyZEpiaFFZREE4Q0F3RUFBYU5DTUVBd0RnWURWUjBQQVFIL0JBUURBZ0trTUE4R0ExVWRFd0VCCi93UUZNQU1CQWY4d0hRWURWUjBPQkJZRUZBTnJEODFPTHBMN0JWeDNLUlYyVWhjNW1iNjVNQTBHQ1NxR1NJYjMKRFFFQkN3VUFBNElCQVFCWWR0c0tqV1RGYmErWDlpK3dWdTFUNHlyMDhGZWJKSkJkVFhwREdiSXhxSXFPY2RUSQpNT1ZhMk5jTXlPbDlQTzlKY3k5aEpGcEJCN0Vpem9aak9tV1U4eGpiTHp1ZkhoMGJMejNuTHVWRzh1cUFVUnVMCjUvTS9NSjRDQUN4M2xCK0NDakVMcnZIczZhbEZRdm4rdE8xeFBiblVMYkdKZUtoQUJld3RqdkFLR0pVbGcwbHUKenlpeFZJTGQ4SjNHVTY0VWM0LzJVdkkydUR4a3lqdkNVUmJwNnVtUFdkY0RSKzArUnhPL3VVeDROdXBEQlVBbQoxOU1kUkNDcW1CUjNRcmJBcmE5RytpUTNiZ0pYbmovRkVlSnN6U1ZsVjgxVW1ZdjBDcXo2TTVVcE1XeXUvS08zCnFPZEdzT3pvb1dsWjlYRm55dldFRkdyU1ZmY2FwT1F6L2k4YgotLS0tLUVORCBDRVJUSUZJQ0FURS0tLS0tCg==
        server: https://192.123.123.225:6443 ## 여기 변경
      name: ""
    contexts: null
    current-context: ""
    kind: Config
    preferences: {}

# --- 생략

```

```sh
# keepalived IP 변경
$ vi /etc/keepalived/keepalived.conf
vrrp_instance VI_1 {
state BACKUP
interface enp3s0
virtual_router_id 51
priority 98
advert_int 1
nopreempt
authentication {
        auth_type PASS
        auth_pass tmax1234
        }
virtual_ipaddress {
        192.123.123.225 ## 변경할 IP 변경
        }
}

# keepalived 재기동
$ systemctl restart keepalived

# 변경 IP 확인
$ ip addr
```

```sh
$ cd /etc/kubernetes/

# 변경 /이전IP / 변경 IP/
$ sed -i "s/210.212/220.225/" ./*.conf
$ sed -i "s/210.212/220.225/" ./manifests/*

# 변경 확인
$ grep -r 220.225 ./*

# k8s Service 재기동
$ systemctl restart kubelet
$ systemctl restart crio

# 정상 확인 (모든 노드가 정상화 될때 까지 기다려야 한다.)
$ kubectl get node -o wide
NAME              STATUS   ROLES    AGE   VERSION
k8s-19-master01   Ready    master   21h   v1.19.4
k8s-19-master02   Ready    master   19h   v1.19.4
k8s-19-master03   Ready    master   19h   v1.19.4

$ kubectl get po -n kube-system
NAME                                       READY   STATUS    RESTARTS   AGE
calico-kube-controllers-7574d55fcb-4l7dt   1/1     Running   0          19h
calico-node-2kgwg                          1/1     Running   0          19h
calico-node-b8d7c                          1/1     Running   0          19h
calico-node-nfdnf                          1/1     Running   0          21h
coredns-f9fd979d6-796b6                    1/1     Running   0          16h
coredns-f9fd979d6-kkb4v                    1/1     Running   0          19h
etcd-k8s-19-master01                       1/1     Running   0          16h
etcd-k8s-19-master02                       1/1     Running   0          16h
etcd-k8s-19-master03                       1/1     Running   0          19h
kube-apiserver-k8s-19-master01             1/1     Running   1          21h
kube-apiserver-k8s-19-master02             1/1     Running   0          19h
kube-apiserver-k8s-19-master03             1/1     Running   0          19h
kube-controller-manager-k8s-19-master01    1/1     Running   0          21h
kube-controller-manager-k8s-19-master02    1/1     Running   0          19h
kube-controller-manager-k8s-19-master03    1/1     Running   0          19h
kube-proxy-mff8g                           1/1     Running   0          19h
kube-proxy-sv72k                           1/1     Running   0          19h
kube-proxy-x6z4f                           1/1     Running   0          21h
kube-scheduler-k8s-19-master01             1/1     Running   0          21h
kube-scheduler-k8s-19-master02             1/1     Running   0          19h
kube-scheduler-k8s-19-master03             1/1     Running   0          19h

```


### Step3 : Master02 번 작업
```sh
## Setp2 과정과 동일
```

### Step4 : Master01 번 작업
```sh
## 보통의 마스터01의 경우 Docker가 같이 설치되어 kubeadm init시 --cri-socket 를 선택하라고 하면서 선택시 없는 옵션이라고 한다.
## 이때는 Step2 kubeadm init 시 사용하는 Config 가 아닌 최초 init 시 사용한 kubeadm-config.yaml 파일을 수정하여 사용하면 된다.
## 샘플 

apiVersion: kubeadm.k8s.io/v1beta2
kind: InitConfiguration
localAPIEndpoint:
  advertiseAddress: 192.123.123.214
  bindPort: 6443
nodeRegistration:
  criSocket: /var/run/crio/crio.sock

---

apiVersion: kubeadm.k8s.io/v1beta2
kind: ClusterConfiguration
kubernetesVersion: v1.19.4
controlPlaneEndpoint: 192.123.123.225:6443 ## 변경 API 주소
imageRepository: k8s.gcr.io
networking:
  serviceSubnet: 10.244.0.0/16
  podSubnet: 10.10.0.0/16

---

apiVersion: kubelet.config.k8s.io/v1beta1
kind: KubeletConfiguration
cgroupDriver: systemd


## 그외 절차는 모두 동일하다.
```


