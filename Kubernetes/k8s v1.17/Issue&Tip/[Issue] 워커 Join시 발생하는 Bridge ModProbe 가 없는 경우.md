# 워커 Join시 발생하는 Bridge ModProbe 가 없는 경우
## [현상] 워커 Join시 발생하는 Bridge ModProbe 가 없는 경우
```bash
$ kubeadm join 192.168.210.212:6443 --token qq7hv4.aurm3zuoo2qatuhb --discovery-token-ca-cert-hash sha256:76ef3284f836faa029c4704128986dbdf6683b8d95a55cfbe1f0f8f66bb2067c --cri-socket=/var/run/crio/crio.sock
W0312 16:57:42.999792    2672 join.go:346] [preflight] WARNING: JoinControlPane.controlPlane settings will be ignored when control-plane flag is not set.
[preflight] Running pre-flight checks
error execution phase preflight: [preflight] Some fatal errors occurred:
	[ERROR FileContent--proc-sys-net-bridge-bridge-nf-call-iptables]: /proc/sys/net/bridge/bridge-nf-call-iptables does not exist
[preflight] If you know what you are doing, you can make a check non-fatal with `--ignore-preflight-errors=...`
To see the stack trace of this error execute with --v=5 or higher

```

## 해결
```bash
# 추가 해주면 됨
$ sudo modprobe br_netfilter
# 확인
$ lsmod |grep -e overlay -e br_netfilter
br_netfilter           22256  0 
bridge                151336  1 br_netfilter
overlay                91659  0 

```