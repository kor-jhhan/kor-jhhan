## [이슈] wthr_create (pthread_create)
### [일자] : 2020.11.09

### [환경] 
- k8s v1.17 
- CRIO v1.17 
- Docker version 19.03.5

### [Error 내용(반복 발생)] 
파드 배포 시 "wthr_create (pthread_create): 자원이 일시적으로 사용 불가능함" 에러 발생

### [원인]   
Docker Container Engine이 아닌 CRIO Container Engine 사용 시 pids_limit 기본 값이 1024 로 설정됨.
Container 별 Max Pid 값이 1024로 Pid Thread 갯수 설정임. 그래서 결국 Tibero MAX_SESSION_COUNT 값이 300 이상치 부터 Thread 갯수가 1024를 넘기게 됨으로 기동이 안되며 위와 같은 에러 발생.    
k8s v1.15 에서 동일한 장애가 발생되지 않은 이유는 Docker Container Engine을 사용해서 그러함. Docker Container Engine은 반대로 host의 Limit Setting을 따라가며, 기본 설정이 제한 없음으로 설정되어 있고,--pod-max-pids 와 같은 설정을 사용하여 제한을 줄수 있는 설정이다.

### [조치방법]
#### @ Step1
모든 Master, Worker Node에서 Cri-O 설정 파일 변경 후 재기동
``` bash
# Cri-O 설정 파일 변경
$ vi /etc/crio/crio.conf
----- 생략 ------
# Default 값 변경 1024 > 2048
pid_limit=2048
----- 생략 ------

$ systemctl restart crio

## 메인 마스터 서버는 Cri-O 재기동 후 에러가 발생 함으로 Kubelet 도 재기동 필요
$ systemctl restart kubelet
```

#### @ Step2
장애가 발생한 Pod 재생성 
``` bash
# 리플리카 Object가 존재 한다는 조건하. 삭제 후 자동 재생성
$ kubectl delete -n [namespace 명] po [pod 명]
```