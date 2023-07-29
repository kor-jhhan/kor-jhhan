# Kubernetes v1.17.6 구성
## 구성정보

<details>
<summary> 구성 S/W 정보 </summary> 

- OS Version : xUbuntu_18.04
- Docker Version : 
- keepalive Version : 
- CRI-O Version : v1.17.6
- k8s Version : v1.17.6
- Calico Version : v3.14
- MetalLB Version : v0.8.2
- Node 5대 구성
    - 192.168.210.229 DockerSvr
    - 192.168.210.230 VIP
    - 192.168.210.231 k8s-master01 
    - 192.168.210.232 k8s-master02 
    - 192.168.210.233 k8s-master03 
    - 192.168.210.234 k8s-worker01 
    - 192.168.210.235 k8s-worker02 

</details>

## 설치 순서
- [Step1-Docker Image Registry 구성](#step1-docker-설치-및-docker-image-registry-구성)
- [Step2-Keeopalived 설치](#step2-vip-사용을-위한-keepalived-설치)
- [Step3-CRI-O 설치](#step3-docker-runtime-cri-o-설치)
- [Step4-kubernetes-v1.17.6 설치](#step4-kubernetes-v1176-설치)
- [Step5-Calico 설치](#step5-cni-설치)
- [Step6-MetalLB 설치](#step6-metallb-설치-선택)


## [Step1] Docker 설치 및 Docker Image Registry 구성

<details>
<summary> 설치대상 </summary> 

192.168.210.230 DockerSvr

</details>

1. Docker apt repository 추가
```bash
$ sudo apt update
$ sudo apt-get update

## 리포지터리 설정
### apt가 HTTPS 리포지터리를 사용할 수 있도록 해주는 패키지 설치
$ sudo apt-get install -y \
 apt-transport-https ca-certificates curl software-properties-common gnupg2

### Docker의 공식 GPG 키 추가
$ curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -

### Docker apt 리포지터리 추가.
$ sudo add-apt-repository \
  "deb [arch=amd64] https://download.docker.com/linux/ubuntu \
  $(lsb_release -cs) \
  stable"
```

2. Docker 설치

```bash
## 버전 지정 설치
$ apt-get install -y \
 docker-ce=5:19.03.5~3-0~ubuntu-bionic docker-ce-cli=5:19.03.5~3-0~ubuntu-bionic

# 서비스 등록 및 시작
$ systemctl enable docker
$ systemctl start docker
```

3. 개인 Docker registry 구성
```bash
## 폐쇄 환경에서 구성한다고 하면, 외부에서 docker save를 통하여 "registry" 이미지를 미리 준비해야한다.
## 준비된 이미지는 docker load 하면 된다.
$ docker run -dit --name my-registry \
--restart=always \
-eREGISTRY_STORAGE_DELETE_ENABLED=true \ # 이미지 삭제 CLI 사용 여부 (미 설정시 명령어로 삭제가 불가하다.)
-p5000:5000 \
-v/docker-repo/docker:/var/lib/registry \ # 용량관리를 위해서는 저장되는 이미지 정보를 별도 Mount된 Disk에 저장하는것이 좋다
registry:latest
```

4. 개인 Docker registry 연결
```bash
cat > /etc/docker/daemon.json
# -----------
{
"exec-opts": ["native.cgroupdriver=systemd"], "log-driver": "json-file",
"log-opts": {
"max-size": "100m" },
"storage-driver": "overlay2", "insecure-registries":
["192.168.210.229:5000"] ## 나의 Docker registry 주소를 입력한다.
}
```

## [Step2] VIP 사용을 위한 KeepAlived 설치

<details>
<summary> 설치대상 </summary> 

192.168.210.231 k8s-master01 <br>
192.168.210.232 k8s-master02 <br> 
192.168.210.233 k8s-master03 

</details>

``` bash
### 마스터 노드에만 설치하면됩니다.
$ sudo apt-get install keepalived

$ vi /etc/keepalived/keepalived.conf
    ##---------------------------##

    vrrp_instance VI_1 {
        state MASTER  ## master or backup으로 설정, 하나의 master에만 master를 설정하고 나머지 master에는 backup으로 설정
        interface eno1 ## network interface 이름 확인 (ip a 명령어로 확인) ex) enp0s8
        virtual_router_id 52 ## vritual router id ex) 50
        priority 100 ## Master 우선순위 (priority 값이 높으면 최우선적으로 Master 역할 수행, 각 Master마다 다른 priority 값으로 수정)
        advert_int 1
        authentication {
            auth_type PASS
            auth_pass 1111 ## 패스워드 설정
        }
        virtual_ipaddress {
            192.168.210.230 ## virtual ip(VIP) 설정
        }
    }

    ##----------------------------##

$ sudo systemctl restart keepalived
$ sudo systemctl enable keepalived
$ sudo systemctl status keepalived

## 서비스 정지 및 시작으로 정상적으로 VIP가 넘어가는지 확인한다.
## ip addr 명령어로 설정한 network로 VIP 설정이 되는지 확인한다.
```

## [Step3] Docker Runtime (CRI-O) 설치

<details>
<summary> 설치대상 </summary> 

192.168.210.231 k8s-master01 <br>
192.168.210.232 k8s-master02 <br> 
192.168.210.233 k8s-master03 <br> 
192.168.210.234 k8s-worker01 <br> 
192.168.210.235 k8s-worker02 

</details>

1. 기본 구성 패키지 설치
```bash
## 기본 구성을 위한 패키지 설치
$ sudo apt-get install -y libgpgme11-dev \
libassuan-dev \
libdevmapper-dev \
libglib2.0-dev \
libc6-dev \
libbtrfs-dev \
libseccomp-dev \
libgpg-error-dev \
go-md2man

## 불필요한 swap 기능 Off
sudo swapoff -a
vi /etc/fstab 수정

# OS 모드 추가
sudo modprobe overlay
sudo modprobe br_netfilter
lsmod |grep -e overlay -e br_netfilter

# CRI 셋팅
cat >  /etc/sysctl.d/99-kubernetes-cri.conf
# -----------
net.bridge.bridge-nf-call-iptables  = 1
net.ipv4.ip_forward                 = 1
net.bridge.bridge-nf-call-ip6tables = 1
# -----------

# 셋팅 적용
sudo sysctl --system
```

2. CRIO Apt repository 추가 및 설치
```bash
# 환경변수 설정
$ export CRIO_VERSION=1.17
$ export OS=xUbuntu_18.04

# apt 에 레파지토리 등록
$ echo "deb https://download.opensuse.org/repositories/devel:/kubic:/libcontainers:/stable/$OS/ /" > /etc/apt/sources.list.d/devel:kubic:libcontainers:stable.list
$ echo "deb http://download.opensuse.org/repositories/devel:/kubic:/libcontainers:/stable:/cri-o:/$CRIO_VERSION/$OS/ /" > /etc/apt/sources.list.d/devel:kubic:libcontainers:stable:cri-o:$CRIO_VERSION.list

# 키 등록
$ curl -L https://download.opensuse.org/repositories/devel:kubic:libcontainers:stable:cri-o:$CRIO_VERSION/$OS/Release.key | apt-key add -
$ curl -L https://download.opensuse.org/repositories/devel:/kubic:/libcontainers:/stable/$OS/Release.key | apt-key add -

# 업데이트 후 설치
$ apt-get update
$ apt-get install cri-o cri-o-runc

# 서비스 등록 및 시작
$ systemctl enable crio 
$ systemctl start crio 
$ systemctl status crio 

# 추후 설치예정인 network plugin(CNI)과 crio의 가 ㅡ상 인터페이스 충돌을 막기위해 cri-o의 default 인터페이스 설정을 제거한다.
$ rm -rf  /etc/cni/net.d/100-crio-bridge.conf
$ rm -rf  /etc/cni/net.d/200-loopback.conf

# pause_image,insecure_registry, registries, plugin_dirs 내용을 수정 [폐쇄]
# 잘못 설정시 kubeinit 시 image pull이 실패 한다.
$ vi /etc/crio/crio.conf
# 우분투 경우에는 conmon 셋팅을 필히 해줘야 한다.
# 안해주면 init 시 Host 명의 Agent를 찾지 못한다는 에러가 계속 난다.
# -----------
conmon = "/usr/bin/conmon"

pause_image = "192.168.210.229:5000/k8s.gcr.io/pause:3.1"

registries = ["192.168.210.229:5000"]

insecure_registries = ["192.168.210.229:5000"]

plugin_dirs = [ "/usr/libexec/cni","/opt/cni/bin/"]
# -----------

# 재시작
systemctl restart crio 
```

## [Step4] kubernetes-v1.17.6 설치

<details>
<summary> 설치대상 </summary> 

192.168.210.231 k8s-master01 <br>
192.168.210.232 k8s-master02 <br> 
192.168.210.233 k8s-master03 <br> 
192.168.210.234 k8s-worker01 <br> 
192.168.210.235 k8s-worker02 

</details>

1. k8s 설치
```bash
### apt 저장소 등록 및 키등록
$ sudo apt-get install -y apt-transport-https curl
$ curl -s https://packages.cloud.google.com/apt/doc/apt-key.gpg | sudo apt-key add -

$ cat <<EOF | sudo tee /etc/apt/sources.list.d/kubernetes.list
deb https://apt.kubernetes.io/ kubernetes-xenial main
EOF

#### 업데이트 후 설치
$ sudo apt-get update
$ sudo apt-get install -y kubeadm=1.17.6-00 kubelet=1.17.6-00 kubectl=1.17.6-00

## 자동업데이트 방지
### 우분투
$ sudo apt-mark hold kubelet kubeadm kubectl docker-ce $ docker-ce-cli cri-o cri-o-runc linux-image-generic linux-headers-generic

## cgroup-driver 설정 (미 설정 시 kubeadm init 시 에러가 발생할 수 있음.)
## 우분투 
$ vi /etc/default/kubelet
# -----------
KUBELET_EXTRA_ARGS=--cgroup-driver=systemd --container-runtime=remote --container-runtime-endpoint="unix:///var/run/crio/crio.sock"
# -----------
```

2. Main k8s Master01 서버 Init
```bash
# init을 위한 컨프 파일 생성
$ cat > kubeadm-config.yaml 
# -----------
piVersion: kubeadm.k8s.io/v1beta2
kind: InitConfiguration
localAPIEndpoint:
       advertiseAddress: 192.168.210.231
       bindPort: 6443
nodeRegistration:
       criSocket: /var/run/crio/crio.sock

---

apiVersion: kubeadm.k8s.io/v1beta2
kind: ClusterConfiguration
kubernetesVersion: v1.17.6
controlPlaneEndpoint: 192.168.210.230:6443
imageRepository: 192.168.210.229:5000/k8s.gcr.io # loadbalancer 할 IP 설정
networking:
       serviceSubnet: 10.96.0.0/16 # 사용할 가상 IP 대역대 설정
       podSubnet: 10.244.0.0/16

---

apiVersion: kubelet.config.k8s.io/v1beta1
kind: KubeletConfiguration
cgroupDriver: systemd
# -----------

$ kubeadm init --config=kubeadm-config.yaml --upload-certs 

$ mkdir -p $HOME/.kube
$ sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
$ sudo chown $(id -u):$(id -g) $HOME/.kube/config

```

3. Master Server Join

마스터로 조인할 Node에 접근하여 "kubeadm" 명령어로 클러스터에 조인한다.
```bash
$ kubeadm join 192.168.210.230:6443 \
--token so5sgg.kqz5bdexq4uhzxmc \
--discovery-token-ca-cert-hash sha256:13e9a28a69fed65740f3e488eee10053ed75f91e0fe858e52690f6b89d29ebea \
--control-plane --certificate-key 166052c2fa9dcc58d527e93bda14fad88cac6888aa6e89ed0c2843feb82109bc \
--cri-socket=/var/run/crio/crio.sock


### 참고정보 : 마스터 테인트 제거 방법
$ kubectl taint nodes k8s-master01 node-role.kubernetes.io/master:NoSchedule-
$ kubectl taint nodes k8s-master02 node-role.kubernetes.io/master:NoSchedule-
$ kubectl taint nodes k8s-master03 node-role.kubernetes.io/master:NoSchedule-

```

<details>
<summary> Token과 Hash값 가져오는 방법 </summary> 

```bash
$ kubeadm token create --print-join-command
```

</details>

<details>
<summary> certificate key 값 재설정 방법 </summary> 

```bash
$ kubeadm init phase upload-certs --upload-certs --config ./kubeadm-config.yaml 
```

</details>

4. Worker Server Join

워커로 조인할 Node에 접근하여 "kubeadm" 명령어로 클러스터에 조인한다.<br>
워커의 경우 "--control-plane" 속성값이 없다.

```bash
$ kubeadm join 192.168.230.225:6443 \
--token lo7nz2.qocb1f1afjwhuefh \
--discovery-token-ca-cert-hash sha256:dbbbb9f727a335cf5971018543849d8b413684a8b2789a34bdc35662f572d311 \
--cri-socket=/var/run/crio/crio.sock
```

## [Step5] CNI 설치
Calico는 컨테이너, VM 및 호스트 기반의 워크로드를 위한 오픈소스 네트워킹 및 네트워크 보안 솔루션입니다. 쿠버네티스 같은 클라우드 네이티브 환경에서는 네트워킹과 네트워크 정책 제어를 위해 주로 사용됩니다.
```bash
## 필요한 매니페스트 파일 다운로드
$ curl https://docs.projectcalico.org/v3.14/manifests/calico.yaml -O

## 배포
$ kubectl apply -f calico.yaml

## 확인
$ kubectl get -n kube-system po -l k8s-app=calico-node
NAME                READY   STATUS    RESTARTS   AGE
calico-node-6sms2   1/1     Running   0          29d
calico-node-hd5j7   1/1     Running   0          29d
calico-node-vgvlx   1/1     Running   0          30d
calico-node-wkdnj   1/1     Running   0          29d
calico-node-xq8th   1/1     Running   0          29d
```

## [Step6] MetalLB 설치 (선택)
MetalLB는 쿠버네티스(Kubernetes) 환경에서 로드 밸런서 서비스 유형을 구현하기 위한 네트워크 로드 밸런서를 위해 설치
```bash
## 야물의 이미지 레파지토리 주소 일괄 변경
$ curl  https://raw.githubusercontent.com/metallb/metallb/v0.8.2/manifests/metallb.yaml -O

## 가용한 IP 대역대로 설정해줌
vi  metallb_cidr.yaml
#-------------------
apiVersion: v1
kind: ConfigMap
metadata:
  namespace: metallb-system
  name: config
data:
  config: |
    address-pools:
    - name: default
      protocol: layer2
      addresses:
      - xxx.xxx.xxx.xxx-xxx.xxx.xxx.xxx # 사용 가능한 범위로 작성
#-------------------

## 배포
$ kubectl apply -f metallb.yaml 

## 확인
$ kubectl get pod -n metallb-system
NAME                              READY   STATUS    RESTARTS   AGE
pod/controller-65974f684c-lqvn6   1/1     Running   0          32d
pod/speaker-57rk4                 1/1     Running   1          399d
pod/speaker-dg279                 1/1     Running   2          397d
pod/speaker-j8hwn                 1/1     Running   0          399d
pod/speaker-pnk9p                 1/1     Running   5          399d
pod/speaker-rc4md                 1/1     Running   6          154d

## LoadBalancer IP 할당
$ kubectl apply -f metallb_cidr.yaml
```

