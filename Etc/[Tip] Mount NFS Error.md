## 현상
```sh
### NFS PV 사용시 신규 Worker Server에서 Pod 생성시 다음과 같은 현상이 발생될 수 있음.
  Warning  FailedMount  102s  kubelet  (combined from similar events): MountVolume.SetUp failed for volume "pvc-ee9e8033-578c-4172-937e-f3cd1d78e473" : mount failed: exit status 32
Mounting command: systemd-run
Mounting arguments: --description=Kubernetes transient mount for /var/lib/kubelet/pods/fed42b8e-f850-4e44-a0a8-a1f37e9d7e20/volumes/kubernetes.io~nfs/pvc-ee9e8033-578c-4172-937e-f3cd1d78e473 --scope -- mount -t nfs 192.168.220.220:/home/nfs/test/data-airflow-postgresql-0 /var/lib/kubelet/pods/fed42b8e-f850-4e44-a0a8-a1f37e9d7e20/volumes/kubernetes.io~nfs/pvc-ee9e8033-578c-4172-937e-f3cd1d78e473
Output: Running scope as unit: run-r01a0414e36dd47059a1b9ad1a10bf489.scope
mount: /var/lib/kubelet/pods/fed42b8e-f850-4e44-a0a8-a1f37e9d7e20/volumes/kubernetes.io~nfs/pvc-ee9e8033-578c-4172-937e-f3cd1d78e473: bad option; for several filesystems (e.g. nfs, cifs) you might need a /sbin/mount.<type> helper program.
```

## CentOS 조치
```sh
yum install nfs-utils
```

## 우분투 조치
```sh
apt-get install nfs-common 
```