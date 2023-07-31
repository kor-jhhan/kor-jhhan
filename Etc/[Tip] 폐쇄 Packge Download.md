[출처] https://blog.naver.com/cestlavie_01/220281004758

## 패키지 다운로드 (centos)
yum install -y --downloadonly --downloaddir=/opt {패키지명-버전}


sudo rpm -i cuda-repo-rhel7-10-0-local-10.0.130-410.48-1.0-1.x86_64.rpm

yum install -y --downloadonly --downloaddir=/root/GPU cuda
## 패키지 버전 체크
$ apt-cache madison calicoctl

## 패키지 다운로드 (우분투)
$ sudo apt-get install -d -o=dir::cache=<dir> <package>
$ sudo apt-get install -d --reinstall -o=dir:cache=<dir> <package> 



apt-get install -y gnupg gnupg2 gnupg1


sudo apt-get install -d -o=dir::cache=/home/jovyan/GPU_10.0 gnupg gnupg2 gnupg1
sudo apt-get install -d -o=dir::cache=/home/jovyan/GPU_10.0 cuda-10-0

sudo apt-get install -d -o=dir::cache=/home/jovyan/GPU_10.1 cuda-10-1
sudo apt-get install -d --reinstall -o=dir:cache=/home/jovyan/GPU_10.1 cuda-10-1