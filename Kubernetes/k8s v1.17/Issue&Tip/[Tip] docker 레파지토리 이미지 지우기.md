
## docker 레파지토리 이미지 지우기
 - 주의 할 점은 docker 이미지가 깨지는 경우가 생김. 결국 재구축을 해야함.
 - 정상적인 curl 명령어를 통한 삭제가 안전하다. 단, 삭제를 하기 위해서는 Docker Registry 기동시 Delete 에 대한 Config 설정이 필요하다.
```bash
### 1. 도커 레파지토리의 아래와 같은 디렉토리로 이동한다.
cd /data/bips-repo/docker/registry/v2/repositories/hdml/mllab_notebook/_manifests/tags

---------------
[root@k8s-master01 tags]# ls -al
합계 24
drwxr-xr-x. 6 root root 4096  1월 28 21:57 .
drwxr-xr-x. 4 root root 4096  1월 25 13:56 ..
drwxr-xr-x. 4 root root 4096  1월 27 18:00 tf_v1.14.0
drwxr-xr-x. 4 root root 4096  1월 28 21:56 tf_v1.15.2
drwxr-xr-x. 4 root root 4096  1월 25 13:56 tf_v2.1.0
drwxr-xr-x. 4 root root 4096  1월 28 21:57 torch_v1.6.0
----------------

### 2. 지우고자 하는 태그의 디렉토리를 지운다.
rm -rf ./tf_v1.15.2

### 3. docker registry 컨테이너 아이디를 찾는다. 명령어를 수행한다.
docker ps |grep registry

#> 찾은 ID 값으로 registry garbage-collect 명령어를 수행한다.
docker exec -it 6eaa4b3a69cb registry garbage-collect /etc/docker/registry/config.yml

#> 간단히 한줄로 요약하면 아래와 같은 명령어로 수행 가능하다.
docker exec -it $(docker ps |grep registry |awk '{print $1}') registry garbage-collect /etc/docker/registry/config.yml
```
