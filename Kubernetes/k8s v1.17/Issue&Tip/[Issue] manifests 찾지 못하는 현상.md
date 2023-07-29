# [Issue] manifests 찾지 못하는 현상
## [현상]
```bash
journalctl -f
## 아래와 같은 manifests 를 못찾는 다는 에러가 발생한다.
 read config path "/etc/kubernetes/manifests"
```

## [해결]
```bash
# 워커 노드에 저 경로 만들어주면 해결
mkdir -p /etc/kubernetes/manifests
```