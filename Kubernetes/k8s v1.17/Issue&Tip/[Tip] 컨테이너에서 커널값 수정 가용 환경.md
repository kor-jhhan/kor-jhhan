# [Tip] 컨테이너에서 커널값 수정 가용 환경

1. 모든 노드에 kernel 설정을 변경

``` bash
#kernel 설정
$ vi /etc/sysctl.conf
    ----------------------------
    fs.aio-max-nr=1048576
    fs.file-max=6815744
    net.core.rmem_max=67108864
    net.core.wmem_max=67108864 
    ----------------------------
#kernnel 적용
$ sysctl -p

$ vi /etc/security/limits.conf
    ----------------------------
    root soft memlock unlimited
    root hard memlock unlimited
    root soft nofile 65536
    root hard nofile 65536
    root soft nproc unlimited
    root hard nproc unlimited
    root soft stack 10240
    ----------------------------

## 커널 셋팅 후에는 세션을 끊고 root로 재접근한다.
## 재접근하여 ulimit -a 로 보면 변경된 커널 정보를 확인 할 수 있다.

## 이미 기동중인 서비스에 커널 변경된 값으로 재기동을 해야 적용된다.
$ systemctl restart crio
$ systemctl daemon-reload
$ systemctl restart kubelet
$ systemctl restart docker
```

2. k8s 설정 변경

```bash
# --allowed-unsafe-sysctls=kernel.sem  값을 추가해줌.
$ vi /etc/systemd/system/kubelet.service.d/10-kubeadm.conf
#[예시]
# ExecStart=/usr/bin/kubelet --allowed-unsafe-sysctls=kernel.sem $KUBELET_KUBECONFIG_ARGS $KUBELET_CONFIG_ARGS $KUBELET_KUBEADM_ARGS $KUBELET_EXTRA_ARGS

# 설정을 적용합니다.
systemctl daemon-reload # daemon에 내용 즉시 반영
systemctl restart kubelet # kubelet 재시작
```