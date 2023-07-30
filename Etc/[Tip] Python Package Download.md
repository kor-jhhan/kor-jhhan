[Python] Package Donwload 방법
====
## Python Package 다운로드
### 파이썬 내의 패키지 지우기
``` python
# 전체 패키지 목록 삭제 방법
pip freeze > requirement.txt
pip uninstall -y -r ./requirement.txt
```

### 패키지 다운로드를 위한 쉘작성
- 쉘 동작을 위해서는 다운로드를 위한 패키지 리스트 파일 필요
- 리스트 파일과 다운로드 경로를 인자값을 동작함.
``` bash
root@jupyter-3:~# cat pip_package_download.sh 
#!/bin/bash
FILE=$1
DOWN_DIR=$2

if [ -f $FILE ] ; then
   while read line
   do
      echo "[ "$line" ] install!!"
      pip download -d $DOWN_DIR $line
#      pip3 download -i https://pypi.org/pypi --only-binary=:all: -d /home/jovyan/packages/python3.7_pkg $line
#      pip3 download -i https://pypi.org/pypi --no-binary=:all: -d /home/jovyan/packages/python3.7_pkg $line
      echo "------------------------------------------------------"
   done < $FILE
else
   echo "$FILE not exitsts!!!!!"
fi

```

## 다운로드 체크를 위한 쉘
``` bash
root@jupyter-3:~# cat package_download_check.sh
#!/bin/bash
PKG_DIR=$1
PKG_LIST=$2

## 인자값 확인 후 존재 여부 확인
if [ -f $PKG_LIST ] ; then
   while read line
   do
      echo "[ $line ] download check"
      CHECK=`ls -a $PKG_DIR | grep -i ^$line | wc -l`
      if [ $CHECK -eq 0 ] ; then
          col1=`echo $line | cut -f 1 -d '-'`
          col2=`echo $line | cut -f 2 -d '-'`
          CHECK2=`ls -a $PKG_DIR | grep -i ^$col1 |grep -i $col2 | wc -l`
 
          if [ $CHECK2 -eq 0 ] ; then
              echo "$line not Download"
          elif [ $CHECK2 -gt 1 ] ; then
              echo "$line to many Download"
              echo `ls -a $PKG_DIR | grep -i ^$col1 |grep -i $col2`
          else
              echo "$line Success Download"
          fi 
      elif [ $CHECK -gt 1 ] ; then
          echo "$line to many Download"
          echo `ls -a $PKG_DIR | grep -i ^$line`
      else
          echo "$line Success Download"
      fi   
      echo "================================" 
   done < $PKG_LIST
else
   echo "$PKG_LIST not exitsts!!!!"
fi 

```


## 넥서스3 업로드
### 넥서스 접근을 의한 설정
``` bash
root@jupyter-3:~# pip install twine

root@jupyter-3:~/work# cat .pypirc
[pypi]
repository: http://nexus3-svc.nps.svc.cluster.local:8081/repository/nps-pypi-hosted/
username: admin
password: tmaxtmax
```

### 넥서스 업로드를 위한 쉘 작성
- 파일 업로드를 위한 쉘로 파일 리스트 파일을 인자로 받는다.
``` bash

root@jupyter-3:~/work$ ls -al |awk {'print $9'} |grep -v "^\." > pkg_list.txt

root@jupyter-3:~/work$ cat nexus3_pkg_upload.sh 
#!/bin/bash

FILELIST=$1
count=1
while read line
do
   echo "====================="
   echo "[ $count ]Upload pkg $line"
   pkgPath="/home/jovyan/packages/python3.7_pkg/$line"
   return=`twine upload --config-file .pypirc -r pypi $pkgPath`
   echo "$return"
   count=`expr $count + 1`
done < $FILELIST

```


총 946 개 (현재 2개 )
