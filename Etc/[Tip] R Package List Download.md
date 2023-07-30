[R] Package Donwload 방법
====

### R 모든 패키지 다운로드
```r 
pkg.list <- available.packages()
download.packages(pkgs = pkg.list, destdir ="/home/rstudio/share/R3.5.3_pkg/")
```

```r
pkg.list <- available.packages()
download.packages(pkgs = pkg.list, destdir="/home/rstudio/share/R3.6.3_pkg/")
```

### 다운받은 패키지 넥서스3 업로드
``` bash
$ find . -name "*.tar.gz" | awk 'BEGIN {FS="/"} {print "curl -v --user admin:tmaxtmax --upload-file " $0 " http://192.168.179.173:30475/repository/nps-r-hosted/src/contrib/"$3" &&"}' > move-pkg.sh

## 샘플 cmd
$ curl -v --user admin:tmaxtmax --upload-file ./R3.5.3_pkg/zyp_0.10-1.1.tar.gz http://192.168.179.173:30475/repository/nps-r-hosted/src/contrib/zyp_0.10-1.1.tar.gz


$ vi R_pkg_upload.sh
#!/bin/bash

FILEPATH=$1
count=1

pkg_list=$( ls -l $FILEPATH | awk '{print $9}' |grep -v "^\." )

for item in $pkg_list
do
   echo "====================="
   echo "[ $count ]Upload pkg $item"
   pkgPath="$FILEPATH/$item"

   uploadUrl="http://nexus3-svc.nps.svc.cluster.local:8081/repository/nps-r-hosted/src/contrib/$item"

   return=`curl -v --user admin:tmaxtmax --upload-file $pkgPath $uploadUrl`
   echo "$return"
   count=`expr $count + 1`
done 

```
#### 중복 업로드 해결
```bash
$ find ./R3.5.3_pkg/ -name "*" |grep -v "^./R3.5.3_pkg/$" | awk 'BEGIN {FS="/"} {print $3 "\t" $2}' > rpkg_list.txt 

$ find ./R3.6.3_pkg/ -name "*" |grep -v "^./R3.6.3_pkg/$" | awk 'BEGIN {FS="/"} {print $3 "\t" $2}' >> rpkg_list.txt

$ cat rpkg_list.txt | awk '!x[$1]++' |awk '{if($2!="R3.5.3_pkg") print $1}' > test.txt

$ cat ./test.txt | awk '{ print "cp ./R3.6.3_pkg/" $1 " ./uniq_r363_pkg/ &&"} ' > uniq_move.sh

$ cat uniq_move.sh |sed '$d' > uniq_move.sh

$ echo "cp ./R3.6.3_pkg/car_3.0-9.tar.gz ./uniq_r363_pkg/" >> ./uniq_move.sh

$ tail -n 3 uniq_move.sh

$ chmod +x ./uniq_move.sh

$ ./uniq_move.sh

$ nohup ./R_pkg_upload.sh ./uniq_r363_pkg > R_pkg_upload363.log &
```
nexus3-svc.nps.svc.cluster.local:8081/repository

