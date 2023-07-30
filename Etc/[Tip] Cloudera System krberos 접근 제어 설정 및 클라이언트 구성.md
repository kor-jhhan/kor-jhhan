Cloudera System krberos 접근 제어 설정 및 클라이언트 구성 방법
===

# 참고
[kerberos 설치]     
https://www.howtoforge.com/how-to-setup-kerberos-server-and-client-on-ubuntu-1804-lts/    


[kerberos와 CDH 연계]     
https://kylo.readthedocs.io/en/v0.8.2/installation/KerberosInstallationExample-Cloudera.html     


[kerberos 사용 방법]    
- hadoop 통신을 위한 keytab 생성 및 적용 <br>
  https://community.cloudera.com/t5/Support-Questions/HDFS-is-not-accessible-from-an-user-after-kerberos/td-p/268681    
<br>
- Kerberos 패스워드 변경 시 새로 키탭 부여하기 <br>
  https://dalpo0814.tistory.com/2   
<br>
- kerberos 명령어 <br>
https://web.mit.edu/kerberos/krb5-1.12/doc/admin/admin_commands/kadmin_local.html   

-------------------------
=============== 클라이언트 구성 ===================
``` bash
root@cdh1:/etc# kadmin   
kadmin: unable to get default realm    

root@cdh1:/etc# vi /etc/krb5.conf    
[libdefaults]    
       default_realm = quickstart.cloudera    

[realms]
        quickstart.cloudera = {
                kdc = quickstart.cloudera
                admin_server = cdh3.quickstart.cloudera
        }

root@cdh1:/etc# kadmin
Authenticating as principal root/admin@quickstart.cloudera with password.
Password for root/admin@quickstart.cloudera:

kadmin:  addprinc -randkey host/cdh1.quickstart.cloudera
WARNING: no policy specified for host/cdh1.quickstart.cloudera@quickstart.cloudera; defaulting to no policy
Principal "host/cdh1.quickstart.cloudera@quickstart.cloudera" created.

kadmin:  ktadd host/cdh1.quickstart.cloudera
Entry for principal host/cdh1.quickstart.cloudera with kvno 2, encryption type aes256-cts-hmac-sha1-96 added to keytab FILE:/etc/krb5.keytab.
Entry for principal host/cdh1.quickstart.cloudera with kvno 2, encryption type aes128-cts-hmac-sha1-96 added to keytab FILE:/etc/krb5.keytab.
kadmin:  quit
root@cdh2:/etc# kadmin
Authenticating as principal root/admin@quickstart.cloudera with password.
Password for root/admin@quickstart.cloudera:

kadmin:  addprinc -randkey host/cdh2.quickstart.cloudera
WARNING: no policy specified for host/cdh2.quickstart.cloudera@quickstart.cloudera; defaulting to no policy
Principal "host/cdh2.quickstart.cloudera@quickstart.cloudera" created.

kadmin:  ktadd host/cdh2.quickstart.cloudera
Entry for principal host/cdh2.quickstart.cloudera with kvno 2, encryption type aes256-cts-hmac-sha1-96 added to keytab FILE:/etc/krb5.keytab.
Entry for principal host/cdh2.quickstart.cloudera with kvno 2, encryption type aes128-cts-hmac-sha1-96 added to keytab FILE:/etc/krb5.keytab.

kadmin:  quit
```
============== 클라이언트 구성 끝 ===========

============= 추가 hdfs 연계를 위한 클라이언트 구성 =====
#  계정 접근 권한 부여 방법.
@ host 설정 추가
``` bash
root@rserver-3:/# vi /etc/hosts 

# CDH 6.3 Cluster
192.168.179.181 cdh1.quickstart.cloudera cdh1
192.168.179.182 cdh2.quickstart.cloudera cdh2
192.168.179.183 cdh3.quickstart.cloudera quickstart.cloudera cdh3
```
@ 패키지 설치 os package install (  krb5-config,  krb5-user )
``` bash
root@rserver-3:/# apt-get install -y krb5-config krb5-user
```
@ krb5.conf 수정
``` bash
root@rserver-3:/# vi /etc/krb5.conf
[libdefaults]
    default_realm = quickstart.cloudera
    dns_lookup_kdc = false
    dns_lookup_realm = false
    ticket_lifetime = 8553600
    renew_lifetime = 604800
    forwardable = true
    default_tgs_enctypes = aes256-cts-hmac-sha1-96
    default_tkt_enctypes = aes256-cts-hmac-sha1-96
    permitted_enctypes = aes256-cts-hmac-sha1-96
    udp_preference_limit = 1
    kdc_timeout = 3000
[realms]
    quickstart.cloudera = {
        kdc = quickstart.cloudera
        admin_server = cdh3.quickstart.cloudera
    }
```
@ 접근 가능한지 확인 - 아래와 같이 kadmin 명령어 실행시 password를 묻는것이 나옴. 정상 접근되면 q로 나옴.
``` bash
root@rserver-3:/# kadmin
Authenticating as principal root/admin@quickstart.cloudera with password.
Password for root/admin@quickstart.cloudera:
kadmin: q
```
@ 여기까지 기본적인 클라이언트 설정 완료

@ 사용자고자 하는 계정을 kerberos 클라이언트의 server 또는 container에 생성(rstudio 계정 생성함)    
@ 생성된 계정으로 kadmin 시 관련된 정보가 없다고 뜸.      
``` bash
rstudio@rserver-3:/# kadmin
Authenticating as principal rstudio/admin@quickstart.cloudera with password.
kadmin: Client 'rstudio/admin@quickstart.cloudera' not found in Kerberos database while initializing kadmin interface
```
@ root로 접근하여 kerberos 사용자 등록
``` bash
root@rserver-3:/# kinit root/admin@quickstart.cloudera
Password for root/admin@quickstart.cloudera:

root@rserver-3:/# kadmin -q 'addprinc rstudio/user'
Authenticating as principal root/admin@quickstart.cloudera with password.
Password for root/admin@quickstart.cloudera:
WARNING: no policy specified for rstudio/user@quickstart.cloudera; defaulting to no policy
Enter password for principal "rstudio/user@quickstart.cloudera":
Re-enter password for principal "rstudio/user@quickstart.cloudera":
Principal "rstudio/user@quickstart.cloudera" created.
```

### 사용 계정으로 스위칭
```bash
root@rserver-3:/# su - rstudio
```

### 키인잇
```bash
rstudio@rserver-3:/# kinit rstudio/user@quickstart.cloudera
Password for rstudio/user@quickstart.cloudera:
```

### 확인
```bash
rstudio@rserver-3:/# klist
Ticket cache: FILE:/tmp/krb5cc_1000
Default principal: rstudio/user@quickstart.cloudera

Valid starting       Expires              Service principal
09/14/2020 05:36:21  09/14/2020 15:36:21  krbtgt/quickstart.cloudera@quickstart.cloudera
        renew until 09/21/2020 05:36:21
```

### Hue 접근하여 계정 생성
 URL : http://cdh3.quickstart.cloudera:8889/hue/accounts/login?next=/ <br>
 관리자 : admin / admin <br>
 ``` txt
 [사용자관리] 메뉴에서 > [사용자 추가] > 계정 및 PW 설정 (kerberos와 동일하게) > [2단계] > analysis 그룹 설정 > [사용자 추가] 함     
```

### 클라우데라 매니저 접근
URL : http://192.168.179.183:7180/cmf/login?logout <br>
관리자 : admin/admin <br>
``` txt
[HDFS] 선택하여 > [작업] 버튼을 눌러 > [클라이언트 구성 배포] 파일을 다운로드 받음.
다운 받은 " hdfs-clientconfig.zip " 파일을 kerberos 클라이언트의 server 또는 container 의 HDFS 클라이언트 구성값으로 넣음.
```

### 다운받은 구성파일의 정보를 $HADOOP_HOME/etc/hadoop 경로에 덮어씀
```bash
rstudio@rserver-3:/# unzip hdfs-clientconfig.zip -d $HADOOP_HOME/etc/hadoop 경로에 덮어씀.
```

### ls 명령어도 디렉토리 및 데이터 리스트 조회
```bash
rstudio@rserver-3:/# hdfs dfs -ls /user
Found 8 items
drwxr-xr-x   - admin  admin               0 2020-09-14 04:19 /user/admin
drwxrwxrwx   - mapred hadoop              0 2020-09-11 05:19 /user/history
drwxrwxr-t   - hive   hive                0 2020-09-14 05:01 /user/hive
drwxrwxr-x   - hue    hue                 0 2020-09-11 05:19 /user/hue
drwxrwxr-x   - impala impala              0 2020-09-11 05:17 /user/impala
drwxrwxr-x   - oozie  oozie               0 2020-09-11 05:18 /user/oozie
drwxr-xr-x   - root   root                0 2020-09-14 02:36 /user/root
drwxr-xr-x   - hdfs   supergroup          0 2020-09-11 05:18 /user/yarn

rstudio@rserver-3:/# hdfs dfs -ls /user/root
Found 9 items
-rw-r--r--   3 root root  152152707 2020-09-14 02:35 /user/root/lineitem.tbl.1
-rw-r--r--   3 root root  153252503 2020-09-14 02:35 /user/root/lineitem.tbl.2
-rw-r--r--   3 root root  153438152 2020-09-14 02:35 /user/root/lineitem.tbl.3
-rw-r--r--   3 root root  153314626 2020-09-14 02:35 /user/root/lineitem.tbl.4
-rw-r--r--   3 root root  153247460 2020-09-14 02:35 /user/root/lineitem.tbl.5
-rw-r--r--   3 root root  153022835 2020-09-14 02:35 /user/root/lineitem.tbl.6
-rw-r--r--   3 root root  153436347 2020-09-14 02:35 /user/root/lineitem.tbl.7
-rw-r--r--   3 root root  153345670 2020-09-14 02:36 /user/root/lineitem.tbl.8
-rw-r--r--   3 root root  153810762 2020-09-14 02:36 /user/root/lineitem.tbl.9
```
### Get 명령어로 데이터를 가져 올수 있음.
```bash
rstudio@rserver-3:/# hdfs dfs -get /user/root/lineitem.tbl.1 ./
```




# 트러블 이슈

### 장애 발생시 

kdb5_util: Cannot open DB2 database '/var/lib/krb5kdc/principal': File exists while creating database '/var/lib/krb5kdc/principal'

### db 지우고
```bash
$  kdb5_util destroy -f /var/lib/krb5kdc/principal
```

### 새로 생성
```bash
$  krb5_newrealm
```
### 서비스 재시작
```bash
$  service krb5-admin-server restart
$  service krb5-kdc restart
```

### root 생성
```bash
$ kadmin.local

 kadmin.local : addprinc root/admin
 kadmin.local : addprinc -randkey host/cdh3.quickstart.cloudera
 kadmin.local : ktadd host/cdh3.quickstart.cloudera
```

### root 설정
```bash
$ vim /etc/krb5kdc/kadm5.acl
~~~
root/admin *
~~~
```
### 재시작
```bash
$ servie restart krb5-admin-server
```