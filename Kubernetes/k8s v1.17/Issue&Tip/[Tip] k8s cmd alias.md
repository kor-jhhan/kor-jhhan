## k8s 환경에서 원활한 작업 환경 구축을 위한 Alias 설정

``` bash
$ vi ~/.bashrc

### 아래 내용을 입력
#--------------------------------
# k8s alias
alias k='kubectl'
alias kg='kubectl get'
alias kgpo='kubectl get pod -o wide'
alias kgpoall='kubectl get pods --all-namespaces'
alias krm='kubectl delete'
alias kc='kubectl create'
alias kd='kubectl delete'
alias klo='kubectl logs -f'
alias klop='kubectl logs -f -p'
alias kd='kubectl describe'
alias ns='namespace'
alias kex='kubectl exec -it'
alias krmf='kubectl delete --grace-period=0 --force'


alias kuse='kubectl describe node |grep -e cpu -e Hostname -e memory -e Resource|grep -v Memory'
alias ceph-stat="kubectl exec -it -n rook-ceph  $(kubectl get po -n rook-ceph |grep tools | awk '{print $1}') -- /usr/bin/ceph status"
alias ceph-use="kubectl exec -it -n rook-ceph  $(kubectl get po -n rook-ceph |grep tools | awk '{print $1}') -- /usr/bin/ceph osd status"
#--------------------------------

$ . ~/.bashrc

```