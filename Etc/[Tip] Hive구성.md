# [Tip] Hive구성

## STEP1 : 환경 구성
```sh
# Repo 변경 (CentOS 8 EOS 로  Mirror site 를 Vault 로 전환)
[root@gpu-server]$ sed -i 's/mirrorlist/#mirrorlist/g' /etc/yum.repos.d/CentOS-Linux-*
[root@gpu-server]$ sed -i 's|#baseurl=http://mirror.centos.org|baseurl=http://vault.centos.org|g' /etc/yum.repos.d/CentOS-Linux-*
[root@gpu-server]$ dnf repolist
리포지터리 ID                                                           리포지터리 이름
appstream                                                               CentOS Linux 8 - AppStream
baseos                                                                  CentOS Linux 8 - BaseOS
extras                                                                  CentOS Linux 8 - Extras

# yum 업데이트
[root@gpu-server]$ yum update -y

# HOST명 변경
[root@gpu-server]$ hostname gpu-server

[root@gpu-server]$ echo 'HOSTNAME=gpu-server' >> /etc/sysconfig/network
[root@gpu-server]$ cat /etc/sysconfig/network
# Created by anaconda
HOSTNAME=gpu-server

# 필요시 작업
[root@gpu-server]$ vi /etc/sysctl.conf
----------------------------
fs.aio-max-nr=1048576
fs.file-max=6815744
net.core.rmem_max=67108864
net.core.wmem_max=67108864 
----------------------------
#kernnel 적용
sysctl -p

[root@gpu-server]$ vi /etc/security/limits.conf
----------------------------
root soft memlock unlimited
root hard memlock unlimited
root soft nofile 65536
root hard nofile 65536
root soft nproc unlimited
root hard nproc unlimited
root soft stack 10240
----------------------------
```

## STEP2 : Hadoop 구성
### 환경구성
```sh
# 설치된 Package 체크
[root@gpu-server]$ yum list installed

# JDK 설치 파일 준비
[root@gpu-server]$ mkdir 01.java-jdk
[root@gpu-server]$ cd 01.java-jdk
[root@gpu-server]$ yum install -y --downloadonly --downloaddir=/data/IBK_hive/centos-repo/01.java-jdk openjdk-8-jdk
[root@gpu-server]$ yum install -y --downloadonly --downloaddir=/data/IBK_hive/centos-repo/01.java-jdk openjdk-8-jdk-devel

# JDK 설치
[root@gpu-server]$ rpm -i ./*

# JDK 버전 확인
[root@gpu-server]$ java -version
openjdk version "1.8.0_312"
OpenJDK Runtime Environment (build 1.8.0_312-b07)
OpenJDK 64-Bit Server VM (build 25.312-b07, mixed mode)

# 환경변수 설정
## JAVA 경로 찾기
[root@gpu-server]$ which java
/usr/bin/java
[root@gpu-server]$ readlink /usr/bin/java
/etc/alternatives/java
[root@gpu-server jre]# readlink /etc/alternatives/java
/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.312.b07-2.el8_5.x86_64/jre/bin/java

## OS 전체 환경변수 설정
[root@gpu-server]$ vi /etc/profile
JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.312.b07-2.el8_5.x86_64
PATH=$PATH:$JAVA_HOME/bin
CLASSPATH=$JAVA_HOME/jre/lib:$JAVA_HOME/lib/tools.jar

export JAVA_HOME PATH CLASSPATH
## 환경변수 적용
[root@gpu-server]$ source /etc/profile

## 체크
[root@gpu-server]$ java -version


## Host 지정
[root@gpu-server]$ vi /etc/hosts
192.168.210.244 gpu-server localhost

[root@gpu-server]$ touch /etc/hosts

## SSH Key 생성
[root@gpu-server]$ ssh-keygen
Generating public/private rsa key pair.
Enter file in which to save the key (/root/.ssh/id_rsa): 
Enter passphrase (empty for no passphrase): 
Enter same passphrase again: 
Your identification has been saved in /root/.ssh/id_rsa.
Your public key has been saved in /root/.ssh/id_rsa.pub.
The key fingerprint is:
SHA256:MiVf+iCgsfcdjpY1fFvW8hgpVUNNZyBgLMC37afh+OE root@gpu-server
The keys randomart image is:
+---[RSA 3072]----+
|     ... .o...=++|
|      . o..  o oo|
|  . . ...+. .    |
|   + . =.o.. o   |
|  o . + S.o * .  |
|   . . X *o=.=   |
|      = oo++. .  |
|     .  ..o.     |
|         .E      |
+----[SHA256]-----+

## SSH Key 적용
[root@gpu-server]$ ssh-copy-id root@gpu-server -p 4022
```

### Hadoop 설치
```sh
# Hdoop Binary 
[root@gpu-server]$ wget https://dlcdn.apache.org/hadoop/common/hadoop-2.10.1/hadoop-2.10.1.tar.gz
--2022-03-09 17:00:01--  https://dlcdn.apache.org/hadoop/common/hadoop-2.10.1/hadoop-2.10.1.tar.gz
Resolving dlcdn.apache.org (dlcdn.apache.org)... 151.101.2.132, 2a04:4e42::644
Connecting to dlcdn.apache.org (dlcdn.apache.org)|151.101.2.132|:443... connected.
HTTP request sent, awaiting response... 200 OK
Length: 408587111 (390M) [application/x-gzip]
Saving to: ‘hadoop-2.10.1.tar.gz’

hadoop-2.10.1.tar.gz                   100%[============================================================================>] 389.66M   111MB/s    in 3.5s    

2022-03-09 17:00:04 (111 MB/s) - ‘hadoop-2.10.1.tar.gz’ saved [408587111/408587111]

[root@gpu-server]$ tar -zxvf ./hadoop-2.10.1.tar.gz

# 아래 환경변수 추가
[root@gpu-server]$ vi ~/.bashrc

  # JAVA
  export JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.312.b07-2.el8_5.x86_64

  # Hadoop ENV
  export HADOOP_HOME=/data/IBK_hive/hadoop/hadoop-2.10.1
  export HADOOP_PREFIX=${HADOOP_HOME}
  export HADOOP_MAPRED_HOME=${HADOOP_HOME}
  export HADOOP_COMMON_HOME=${HADOOP_HOME}
  export HADOOP_HDFS_HOME=${HADOOP_HOME}
  export YARN_HOME=${HADOOP_HOME}
  export HADOOP_CONF_DIR=${HADOOP_HOME}/etc/hadoop

  # Hive ENV
  export HIVE_HOME=/data/IBK_hive/hive/apache-hive-2.3.9-bin

  export PATH=$PATH:$JAVA_HOME/bin:$HADOOP_HOME/bin/:$HADOOP_HOME/sbin:$HIVE_HOME/bin

# 환경변수 적용
[root@gpu-server]$ source ~/.bashrc

# Hadoop  설정
[root@gpu-server]$ cd $HADOOP_HOME

## core-site.xml 
[root@gpu-server]$ vi ./etc/hadoop/core-site.xml 
  <configuration>
    <property>
      <name>fs.default.name</name>
      <value>hdfs://gpu-server:9000</value>
    </property>
    <property>
      <name>hadoop.tmp.dir</name>
      <value>/data/IBK_hive/hadoop/hadoop-2.10.1/DATA/</value>
    </property>
    <property>
      <name>hadoop.proxyuser.hue.hosts</name>
      <value>*</value>
    </property>
    <property>
      <name>hadoop.proxyuser.hive.groups</name>
      <value>*</value>
    </property>
    <property>
      <name>hadoop.proxyuser.hive.hosts</name>
      <value>*</value>
    </property>
    <property>
      <name>hadoop.proxyuser.root.hosts</name>
      <value>*</value>
    </property>
    <property>
      <name>hadoop.proxyuser.root.groups</name>
      <value>*</value>
    </property>
  </configuration>

## hdfs-site.xml
[root@gpu-server]$ vi ./etc/hadoop/hdfs-site.xml
  <configuration>
      <property>
            <name>dfs.replication</name>
            <value>1</value>
      </property>
      <property>
            <name>dfs.permissions.enabled</name>
            <value>false</value>
      </property>
  </configuration>

## yarn-site.xml
[root@gpu-server]$ vi ./etc/hadoop/yarn-site.xml
  <configuration>
  <property>
      <name>yarn.resourcemanager.hostname</name>
      <value>gpu-server</value>
    </property>
  <property>
      <name>yarn.nodemanager.aux-services</name>
      <value>mapreduce_shuffle</value>
  </property>
  <property>
      <name>yarn.nodemanager.aux-services.mapreduce_shuffle.class</name>
      <value>org.apache.hadoop.mapred.ShuffleHandler</value>
    </property>
  </configuration>

## mapred-site.xml
[root@gpu-server]$ vi ./etc/hadoop/mapred-site.xml
  <configuration>
      <property>
            <name>mapreduce.framework.name</name>
            <value>yarn</value>
      </property>
      <property>
            <name>mapreduce.map.memory.mb</name>
            <value>4096</value>
      </property>
      <property>
            <name>mapreduce.reduce.memory.mb</name>
            <value>8192</value>
      </property>
  </configuration>

## hadoop-env.sh (ssh 가 22 아닐 경우 아래 내용 추가 )
[root@gpu-server]$ vi ./etc/hadoop/hadoop-env.sh
  export HADOOP_SSH_OPTS="-p 4022"

## NameNode 포맷 / 최초 설치시 필수
[root@gpu-server]$ hadoop namenode -format

## 전부 기동
[root@gpu-server]$ start-all.sh

## 기동 확인
[root@gpu-server]$ jps |grep -v Jps
105251 NameNode
106546 NodeManager
105657 SecondaryNameNode
106925 Jps
106396 ResourceManager
105422 DataNode
```

## STEP3 : Hive 구성
### Hive 설치
```sh
# Binary 다운로드
# https://hadoop.apache.org/releases.html
[root@gpu-server]$ wget https://downloads.apache.org/hive/hive-2.3.9/apache-hive-2.3.9-bin.tar.gz
--2022-03-09 18:11:39--  https://downloads.apache.org/hive/hive-2.3.9/apache-hive-2.3.9-bin.tar.gz
Resolving downloads.apache.org (downloads.apache.org)... 135.181.214.104, 88.99.95.219, 2a01:4f8:10a:201a::2, ...
Connecting to downloads.apache.org (downloads.apache.org)|135.181.214.104|:443... connected.
HTTP request sent, awaiting response... 200 OK
Length: 286170958 (273M) [application/x-gzip]
Saving to: ‘apache-hive-2.3.9-bin.tar.gz’

apache-hive-2.3.9-bin.tar.gz           100%[============================================================================>] 272.91M  1009KB/s    in 6m 45s  

2022-03-09 18:18:25 (690 KB/s) - ‘apache-hive-2.3.9-bin.tar.gz’ saved [286170958/286170958]

# 압축 해제
[root@gpu-server]$ tar -zxvf ./apache-hive-2.3.9-bin.tar.gz

# 환경변수 지정
[root@gpu-server]$ vi ~/.bashrc 
# Hive ENV
export HIVE_HOME=/data/IBK_hive/hive/apache-hive-2.3.9-bin
export PATH=$PATH:$JAVA_HOME/bin:$HADOOP_HOME/bin/:$HADOOP_HOME/sbin:$HIVE_HOME/bin

# 환경변수 적용
[root@gpu-server]$ source ~/.bashrc 
```

### Metastore mysql 설치
```sh
# MySQL Binary 다운로드
# https://downloads.mysql.com/archives/community/
[root@gpu-server]$ wget https://downloads.mysql.com/archives/get/p/23/file/mysql-5.7.36-1.el7.x86_64.rpm-bundle.tar
--2022-03-09 19:06:29--  https://downloads.mysql.com/archives/get/p/23/file/mysql-5.7.36-1.el7.x86_64.rpm-bundle.tar
Resolving downloads.mysql.com (downloads.mysql.com)... 137.254.60.14
Connecting to downloads.mysql.com (downloads.mysql.com)|137.254.60.14|:443... connected.
HTTP request sent, awaiting response... 302 Found
Location: https://cdn.mysql.com/archives/mysql-5.7/mysql-5.7.36-1.el7.x86_64.rpm-bundle.tar [following]
--2022-03-09 19:06:31--  https://cdn.mysql.com/archives/mysql-5.7/mysql-5.7.36-1.el7.x86_64.rpm-bundle.tar
Resolving cdn.mysql.com (cdn.mysql.com)... 23.59.65.109
Connecting to cdn.mysql.com (cdn.mysql.com)|23.59.65.109|:443... connected.
HTTP request sent, awaiting response... 200 OK
Length: 545863680 (521M) [application/x-tar]
Saving to: ‘mysql-5.7.36-1.el7.x86_64.rpm-bundle.tar’

mysql-5.7.36-1.el7.x86_64.rpm-bundle.t 100%[============================================================================>] 520.58M  20.2MB/s    in 27s     

2022-03-09 19:06:58 (19.2 MB/s) - ‘mysql-5.7.36-1.el7.x86_64.rpm-bundle.tar’ saved [545863680/545863680]

# 폐쇄 고려 추가 Package Download
[root@gpu-server]$ yum localinstall -y --downloadonly --downloaddir=/data/IBK_hive/mysql ./mysql-community-*

# 설치
[root@gpu-server 02.mysql]# rpm -i ./*
경고: ./mysql-community-client-5.7.36-1.el7.x86_64.rpm: Header V3 DSA/SHA256 Signature, key ID 5072e1f5: NOKEY

[/usr/lib/tmpfiles.d/mysql.conf:23] Line references path below legacy directory /var/run/, updating /var/run/mysqld → /run/mysqld; please update the tmpfiles.d/ drop-in file accordingly.

# 설치 확인
[root@gpu-server IBK_]$ cat /etc/my.cnf
# For advice on how to change settings please see
# http://dev.mysql.com/doc/refman/5.7/en/server-configuration-defaults.html

[mysqld]
#
# Remove leading # and set to the amount of RAM for the most important data
# cache in MySQL. Start at 70% of total RAM for dedicated server, else 10%.
# innodb_buffer_pool_size = 128M
#
# Remove leading # to turn on a very important data integrity option: logging
# changes to the binary log between backups.
# log_bin
#
# Remove leading # to set options mainly useful for reporting servers.
# The server defaults are faster for transactions and fast SELECTs.
# Adjust sizes as needed, experiment to find the optimal values.
# join_buffer_size = 128M
# sort_buffer_size = 2M
# read_rnd_buffer_size = 2M
datadir=/var/lib/mysql
socket=/var/lib/mysql/mysql.sock

# Disabling symbolic-links is recommended to prevent assorted security risks
symbolic-links=0

log-error=/var/log/mysqld.log
pid-file=/var/run/mysqld/mysqld.pid

# Mysql 서비스 시작
[root@gpu-server IBK_]$ systemctl start mysqld
[root@gpu-server IBK_]$ systemctl status mysqld
● mysqld.service - MySQL Server
   Loaded: loaded (/usr/lib/systemd/system/mysqld.service; enabled; vendor preset: disabled)
   Active: active (running) since Wed 2022-03-09 19:21:06 KST; 14s ago
     Docs: man:mysqld(8)
           http://dev.mysql.com/doc/refman/en/using-systemd.html
  Process: 109930 ExecStart=/usr/sbin/mysqld --daemonize --pid-file=/var/run/mysqld/mysqld.pid $MYSQLD_OPTS (code=exited, status=0/SUCCESS)
  Process: 109876 ExecStartPre=/usr/bin/mysqld_pre_systemd (code=exited, status=0/SUCCESS)
 Main PID: 109932 (mysqld)
    Tasks: 27 (limit: 408682)
   Memory: 301.7M
   CGroup: /system.slice/mysqld.service
           └─109932 /usr/sbin/mysqld --daemonize --pid-file=/var/run/mysqld/mysqld.pid

 3월 09 19:21:02 gpu-server systemd[1]: Starting MySQL Server...
 3월 09 19:21:06 gpu-server systemd[1]: Started MySQL Server.

# 임시 PASSWD 확인
[root@gpu-server IBK_]$ grep 'password' /var/log/mysqld.log
2022-03-09T10:21:04.391221Z 1 [Note] A temporary password is generated for root@localhost: :c:PrWRk3q.U

# 무권한 모드로 설정 후 기동
[root@gpu-server IBK_]$ systemctl stop mysqld
[root@gpu-server IBK_]$ systemctl set-environment MYSQLD_OPTS="--skip-grant-tables"
[root@gpu-server IBK_]$ systemctl start mysqld

# 비밀번호 설정
[root@gpu-server IBK_]$ mysql -u root
Welcome to the MySQL monitor.  Commands end with ; or \g.
Your MySQL connection id is 2
Server version: 5.7.36 MySQL Community Server (GPL)

Copyright (c) 2000, 2021, Oracle and/or its affiliates.

Oracle is a registered trademark of Oracle Corporation and/or its
affiliates. Other names may be trademarks of their respective
owners.

Type 'help;' or '\h' for help. Type '\c' to clear the current input statement.

mysql> FLUSH PRIVILEGES;
Query OK, 0 rows affected (0.00 sec)

mysql> ALTER USER 'root'@'localhost' IDENTIFIED BY 'tmaxtmax';
mysql> quit
Bye

# 무권한 모드 해제 후 기동
[root@gpu-server IBK_]$ systemctl stop mysqld
[root@gpu-server IBK_]$ systemctl unset-environment MYSQLD_OPTS
[root@gpu-server IBK_]$ systemctl start mysqld.service 
[root@gpu-server IBK_]$ systemctl status mysqld.service 
● mysqld.service - MySQL Server
   Loaded: loaded (/usr/lib/systemd/system/mysqld.service; enabled; vendor preset: disabled)
   Active: active (running) since Wed 2022-03-09 19:26:55 KST; 7s ago
     Docs: man:mysqld(8)
           http://dev.mysql.com/doc/refman/en/using-systemd.html
  Process: 110084 ExecStart=/usr/sbin/mysqld --daemonize --pid-file=/var/run/mysqld/mysqld.pid $MYSQLD_OPTS (code=exited, status=0/SUCCESS)
  Process: 110062 ExecStartPre=/usr/bin/mysqld_pre_systemd (code=exited, status=0/SUCCESS)
 Main PID: 110086 (mysqld)
    Tasks: 27 (limit: 408682)
   Memory: 206.8M
   CGroup: /system.slice/mysqld.service
           └─110086 /usr/sbin/mysqld --daemonize --pid-file=/var/run/mysqld/mysqld.pid

 3월 09 19:26:54 gpu-server systemd[1]: Starting MySQL Server...
 3월 09 19:26:55 gpu-server systemd[1]: Started MySQL Server.

# Metastore database 생성 및 계정 생성
[root@gpu-server IBK_]$ mysql -u root -p
Enter password: 
Welcome to the MySQL monitor.  Commands end with ; or \g.
Your MySQL connection id is 3
Server version: 5.7.36 MySQL Community Server (GPL)

Copyright (c) 2000, 2021, Oracle and/or its affiliates.

Oracle is a registered trademark of Oracle Corporation and/or its
affiliates. Other names may be trademarks of their respective
owners.

Type 'help;' or '\h' for help. Type '\c' to clear the current input statement.

## 데이터 베이스 생성
mysql> create database metastore default character set utf8;
Query OK, 1 row affected (0.00 sec)

## 비밀번호 규칙 Low로 변경
mysql> set global validate_password_policy=LOW;
Query OK, 0 rows affected (0.00 sec)

mysql> show variables like 'validate_password%';
+--------------------------------------+-------+
| Variable_name                        | Value |
+--------------------------------------+-------+
| validate_password_check_user_name    | OFF   |
| validate_password_dictionary_file    |       |
| validate_password_length             | 8     |
| validate_password_mixed_case_count   | 1     |
| validate_password_number_count       | 1     |
| validate_password_policy             | LOW   |
| validate_password_special_char_count | 1     |
+--------------------------------------+-------+
7 rows in set (0.00 sec)

## 계정 생성
mysql> create user 'hive'@'%' identified by 'tmaxtmax';
Query OK, 0 rows affected (0.00 sec)

## 권한 부여
mysql> GRANT ALL PRIVILEGES ON metastore.* TO 'hive'@'%';
Query OK, 0 rows affected (0.00 sec)

mysql> FLUSH PRIVILEGES;
Query OK, 0 rows affected (0.00 sec)

## 계정 확인
mysql> SELECT Host,User FROM mysql.user;
+-----------+---------------+
| Host      | User          |
+-----------+---------------+
| %         | hive          |
| localhost | mysql.session |
| localhost | mysql.sys     |
| localhost | root          |
+-----------+---------------+
4 rows in set (0.00 sec)

mysql> SELECT SCHEMA_NAME, DEFAULT_CHARACTER_SET_NAME FROM INFORMATION_SCHEMA.SCHEMATA;
+--------------------+----------------------------+
| SCHEMA_NAME        | DEFAULT_CHARACTER_SET_NAME |
+--------------------+----------------------------+
| information_schema | utf8                       |
| metastore          | utf8                       |
| mysql              | latin1                     |
| performance_schema | utf8                       |
| sys                | utf8                       |
+--------------------+----------------------------+
5 rows in set (0.00 sec)

mysql> quit
```

### MySQL - Hive JDBC 연결 
```sh
# Mysql Java Connector Lib 다운로드
# https://downloads.mysql.com/archives/c-j/
[root@gpu-server]$ wget https://downloads.mysql.com/archives/get/p/3/file/mysql-connector-java-5.1.49.tar.gz
--2022-03-09 19:39:22--  https://downloads.mysql.com/archives/get/p/3/file/mysql-connector-java-5.1.49.tar.gz
Resolving downloads.mysql.com (downloads.mysql.com)... 137.254.60.14
Connecting to downloads.mysql.com (downloads.mysql.com)|137.254.60.14|:443... connected.
HTTP request sent, awaiting response... 302 Found
Location: https://cdn.mysql.com/archives/mysql-connector-java-5.1/mysql-connector-java-5.1.49.tar.gz [following]
--2022-03-09 19:39:23--  https://cdn.mysql.com/archives/mysql-connector-java-5.1/mysql-connector-java-5.1.49.tar.gz
Resolving cdn.mysql.com (cdn.mysql.com)... 23.201.36.233
Connecting to cdn.mysql.com (cdn.mysql.com)|23.201.36.233|:443... connected.
HTTP request sent, awaiting response... 200 OK
Length: 3362563 (3.2M) [application/x-tar-gz]
Saving to: ‘mysql-connector-java-5.1.49.tar.gz’

mysql-connector-java-5.1.49.tar.gz     100%[============================================================================>]   3.21M  3.60MB/s    in 0.9s    

2022-03-09 19:39:25 (3.60 MB/s) - ‘mysql-connector-java-5.1.49.tar.gz’ saved [3362563/3362563]

# 압축 해제 후 "mysql-connector-java-5.1.49-bin.jar" 파일만 Hive/lib로 옮김
# https://downloads.mysql.com/archives/c-j/
[root@gpu-server]$ cp mysql-connector-java-5.1.49-bin.jar ../../hive/apache-hive-2.3.9-bin/lib/

## Hive Site 설정
[root@gpu-server]$ cat ./conf/hive-site.xml 
<configuration> 
	<property> 
		<name>javax.jdo.option.ConnectionURL</name>
		<value>jdbc:mysql://localhost:3306/metastore?createDatabaseIfNotExist=true&amp;useSSL=false</value> 
	</property> 
	<property> 
		<name>javax.jdo.option.ConnectionDriverName</name> 
		<value>com.mysql.jdbc.Driver</value> 
	</property> 
	<property> 
		<name>javax.jdo.option.ConnectionUserName</name>
		<value>hive</value> 
	</property> 
	<property> 
		<name>javax.jdo.option.ConnectionPassword</name>
		<value>tmaxtmax</value> 
	</property> 
</configuration>

# Meta Store Init 스키마 설정
[root@gpu-server]$ bin/schematool -dbType mysql -initSchema
SLF4J: Class path contains multiple SLF4J bindings.
SLF4J: Found binding in [jar:file:/data/IBK_hive/hive/apache-hive-2.3.9-bin/lib/log4j-slf4j-impl-2.6.2.jar!/org/slf4j/impl/StaticLoggerBinder.class]
SLF4J: Found binding in [jar:file:/data/IBK_hive/hadoop/hadoop-2.10.1/share/hadoop/common/lib/slf4j-log4j12-1.7.25.jar!/org/slf4j/impl/StaticLoggerBinder.class]
SLF4J: See http://www.slf4j.org/codes.html#multiple_bindings for an explanation.
SLF4J: Actual binding is of type [org.apache.logging.slf4j.Log4jLoggerFactory]
Metastore connection URL:	 jdbc:mysql://localhost:3306/metastore?createDatabaseIfNotExist=true
Metastore Connection Driver :	 com.mysql.jdbc.Driver
Metastore connection User:	 hive
Starting metastore schema initialization to 2.3.0
Initialization script hive-schema-2.3.0.mysql.sql
Initialization script completed
schemaTool completed

# hive 접근
[root@gpu-server]$ hive
/usr/bin/which: no hbase in (/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.312.b07-2.el8_5.x86_64/bin:/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.312.b07-2.el8_5.x86_64/bin:/data/IBK_hive/hadoop/hadoop-2.10.1//bin/:/data/IBK_hive/hadoop/hadoop-2.10.1//sbin:/root/bin:/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.312.b07-2.el8_5.x86_64/bin:/data/IBK_hive/hadoop/hadoop-2.10.1//bin/:/data/IBK_hive/hadoop/hadoop-2.10.1//sbin:/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.312.b07-2.el8_5.x86_64/bin:/data/IBK_hive/hadoop/hadoop-2.10.1/bin/:/data/IBK_hive/hadoop/hadoop-2.10.1/sbin:/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.312.b07-2.el8_5.x86_64/bin:/data/IBK_hive/hadoop/hadoop-2.10.1/bin/:/data/IBK_hive/hadoop/hadoop-2.10.1/sbin:/data/IBK_hive/hive/apache-hive-2.3.9-bin/bin)
SLF4J: Class path contains multiple SLF4J bindings.
SLF4J: Found binding in [jar:file:/data/IBK_hive/hive/apache-hive-2.3.9-bin/lib/log4j-slf4j-impl-2.6.2.jar!/org/slf4j/impl/StaticLoggerBinder.class]
SLF4J: Found binding in [jar:file:/data/IBK_hive/hadoop/hadoop-2.10.1/share/hadoop/common/lib/slf4j-log4j12-1.7.25.jar!/org/slf4j/impl/StaticLoggerBinder.class]
SLF4J: See http://www.slf4j.org/codes.html#multiple_bindings for an explanation.
SLF4J: Actual binding is of type [org.apache.logging.slf4j.Log4jLoggerFactory]

Logging initialized using configuration in jar:file:/data/IBK_hive/hive/apache-hive-2.3.9-bin/lib/hive-common-2.3.9.jar!/hive-log4j2.properties Async: true
Hive-on-MR is deprecated in Hive 2 and may not be available in the future versions. Consider using a different execution engine (i.e. spark, tez) or using Hive 1.X releases.

## 데이터 베이스 조회
hive> show databases;
OK
default
test
Time taken: 2.143 seconds, Fetched: 2 row(s)

## 테이블 생성
hive> create table test.tab1( col1 integer, col2 string);
OK
Time taken: 0.458 seconds

## 테이블 값 입력
hive> insert into table test.tab1 select 1 as col1, 'ASBDF' as col2;
WARNING: Hive-on-MR is deprecated in Hive 2 and may not be available in the future versions. Consider using a different execution engine (i.e. spark, tez) or using Hive 1.X releases.
Query ID = root_20220309200122_7e9c856b-5ddc-46c7-a98e-16ea4afdf500
Total jobs = 3
Launching Job 1 out of 3
Number of reduce tasks is set to 0 since theres no reduce operator
Starting Job = job_1646816828015_0001, Tracking URL = http://gpu-server:8088/proxy/application_1646816828015_0001/
Kill Command = /data/IBK_hive/hadoop/hadoop-2.10.1/bin/hadoop job  -kill job_1646816828015_0001
Hadoop job information for Stage-1: number of mappers: 1; number of reducers: 0
2022-03-09 20:01:29,991 Stage-1 map = 0%,  reduce = 0%
2022-03-09 20:01:33,071 Stage-1 map = 100%,  reduce = 0%, Cumulative CPU 1.31 sec
MapReduce Total cumulative CPU time: 1 seconds 310 msec
Ended Job = job_1646816828015_0001
Stage-4 is selected by condition resolver.
Stage-3 is filtered out by condition resolver.
Stage-5 is filtered out by condition resolver.
Moving data to directory hdfs://gpu-server:9000/user/hive/warehouse/test.db/tab1/.hive-staging_hive_2022-03-09_20-01-22_936_5316437951572512567-1/-ext-10000
Loading data to table test.tab1
MapReduce Jobs Launched: 
Stage-Stage-1: Map: 1   Cumulative CPU: 1.31 sec   HDFS Read: 4281 HDFS Write: 73 SUCCESS
Total MapReduce CPU Time Spent: 1 seconds 310 msec
OK
Time taken: 11.481 seconds

## 테이블 조회
hive> select * from test.tab1;
OK
1	ASBDF
Time taken: 0.364 seconds, Fetched: 1 row(s)

## 나오기
hive> quit;

```
