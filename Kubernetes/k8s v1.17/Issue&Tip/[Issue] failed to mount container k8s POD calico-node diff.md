# failed to mount container k8s POD calico-node diff
## [현상] failed to mount container k8s POD calico-node diff
```bash
# 워커 노드 Join시 계속 NotReady 상태로 해당 노드의 저널로그 확인
journalctl -f

# --------------------
1월 28 20:44:08 hd-hc05 kubelet[6680]: E0128 20:44:08.656991    6680 kuberuntime_manager.go:729] createPodSandbox for pod "calico-node-4gsqh_kube-system(73a13869-21ab-4e15-b1ea-c58b081b1304)" failed: rpc error: code = Unknown desc = failed to mount container k8s_POD_calico-node-4gsqh_kube-system_73a13869-21ab-4e15-b1ea-c58b081b1304_0 in pod sandbox k8s_calico-node-4gsqh_kube-system_73a13869-21ab-4e15-b1ea-c58b081b1304_0(3dc8d3af638b0cbcfd83f448ce2a008bd6948b5d062336209d6269ab21d7ce70): error recreating the missing symlinks: symlink ../0a49e9277810dd3e17840f8491ed2159c687165bdd49f4e7dd4563917d4987d4/diff /var/lib/containers/storage/overlay/l/c5c678ed2546: file exists
 1월 28 20:44:08 hd-hc05 kubelet[6680]: E0128 20:44:08.657085    6680 pod_workers.go:191] Error syncing pod 73a13869-21ab-4e15-b1ea-c58b081b1304 ("calico-node-4gsqh_kube-system(73a13869-21ab-4e15-b1ea-c58b081b1304)"), skipping: failed to "CreatePodSandbox" for "calico-node-4gsqh_kube-system(73a13869-21ab-4e15-b1ea-c58b081b1304)" with CreatePodSandboxError: "CreatePodSandbox for pod \"calico-node-4gsqh_kube-system(73a13869-21ab-4e15-b1ea-c58b081b1304)\" failed: rpc error: code = Unknown desc = failed to mount container k8s_POD_calico-node-4gsqh_kube-system_73a13869-21ab-4e15-b1ea-c58b081b1304_0 in pod sandbox k8s_calico-node-4gsqh_kube-system_73a13869-21ab-4e15-b1ea-c58b081b1304_0(3dc8d3af638b0cbcfd83f448ce2a008bd6948b5d062336209d6269ab21d7ce70): error recreating the missing symlinks: symlink ../0a49e9277810dd3e17840f8491ed2159c687165bdd49f4e7dd4563917d4987d4/diff /var/lib/containers/storage/overlay/l/c5c678ed2546: file exists"
# --------------------

```

## [해결]
```bash
# 오!!! 난 해결했어 훗~
# 1. kubelet 서비스를 정지
systemctl stop kubelet
# 2. 과거 이력이 있는 pod 껍데기를 모두 지운다.
cd /var/lib/containers/storage/overlay
## 주의 rm -rf ./*

# 3. 간단하다!!!!  후 crio에 등록된 이미지를 모두 삭제한다.
crictl rmi $(crictl images |grep -v IMAGE |awk '{print $3}')

# 4. 다시 kubelet 재기동
```
참고 : https://github.com/projectcalico/calico/issues/3053 <br>
참고 : https://crystalcube.co.kr/202

