# 노드 강제 재기동시 Journal log에서 아래와 같은 에러 발생
## [현상] 노드 강제 재기동시 Journal log에서 아래와 같은 에러 발생
```bash
 3월 31 16:03:01 k8s-worker06 kubelet[21294]: E0331 16:03:01.042616   21294 kubelet_volumes.go:154] orphaned pod "f7c4c771-f564-484f-997d-b2a60ad042fd" found, but volume paths are still present on disk : There were a total of 1 errors similar to this. Turn up verbosity to see them.
 3월 31 16:03:03 k8s-worker06 kubelet[21294]: E0331 16:03:03.042850   21294 kubelet_volumes.go:154] orphaned pod "f7c4c771-f564-484f-997d-b2a60ad042fd" found, but volume paths are still present on disk : There were a total of 1 errors similar to this. Turn up verbosity to see them.
 3월 31 16:03:05 k8s-worker06 kubelet[21294]: E0331 16:03:05.040549   21294 kubelet_volumes.go:154] orphaned pod "f7c4c771-f564-484f-997d-b2a60ad042fd" found, but volume paths are still present on disk : There were a total of 1 errors similar to this. Turn up verbosity to see them.
```

## [해결]
참고 : https://jx2lee.github.io/cloud_orphaned_pod_error/
``` bash
# 이동하여 에러나는 파드 찌꺼기를 제거해줌
$ cd /var/lib/kubelet/pods
$ rm -rf ./f7c4c771-f564-484f-997d-b2a60ad042fd

## 로그를 확인하면 더이상 발생하지 않는것을 확인 할 수 있다
$ journalctl -f 
```