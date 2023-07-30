# GPU 패치를 위한 확인
## 1. GPU NODE 접근
## 2. OS 정보확인
``` BASH
$ cat /etc/*rele*

    CentOS Linux release 7.9.2009 (Core)
    Derived from Red Hat Enterprise Linux 7.9 (Source)
    NAME="CentOS Linux"
    VERSION="7 (Core)"
    ID="centos"
    ID_LIKE="rhel fedora"
    VERSION_ID="7"
    PRETTY_NAME="CentOS Linux 7 (Core)"
    ANSI_COLOR="0;31"
    CPE_NAME="cpe:/o:centos:centos:7"
    HOME_URL="https://www.centos.org/"
    BUG_REPORT_URL="https://bugs.centos.org/"

    CENTOS_MANTISBT_PROJECT="CentOS-7"
    CENTOS_MANTISBT_PROJECT_VERSION="7"
    REDHAT_SUPPORT_PRODUCT="centos"
    REDHAT_SUPPORT_PRODUCT_VERSION="7"

    CentOS Linux release 7.9.2009 (Core)
    CentOS Linux release 7.9.2009 (Core)
    cpe:/o:centos:centos:7
```
## 3. gcc Version 확인
``` BASH
$ yum list installed |grep gcc

    gcc.x86_64                            4.8.5-44.el7                    @base     
    libgcc.x86_64                         4.8.5-44.el7                    @base
```

## 4. GPU Drive Version 확인
``` BASH
$ nvidia-smi

Every 2.0s: nvidia-smi   

Tue Aug  3 11:18:18 2021
+---------------------------------------------------------------------------+
|NVIDIA-SMI 460.67	  Driver Version: 460.67      CUDA Version: 11.2     |
|------------------------------+----------------------+---------------------+
|GPU  Name        Persistence-M| Bus-Id        Disp.A |Volatile Uncorr. ECC |
|Fan  Temp  Perf  Pwr:Usage/Cap|         Memory-Usage |GPU-Util  Compute M. |
|                              |                      |              MIG M. |
|==============================+======================+=====================|
|  0  GeForce GTX 108...  Off  | 00000000:17:00.0 Off |                 N/A |
| 0%   27C    P8     8W / 250W |    137MiB / 11178MiB |     0%      Default |
|                              |                      |                 N/A |
+------------------------------+----------------------+---------------------+
|  1  GeForce GTX 108...  Off  | 00000000:65:00.0 Off |                 N/A |
| 0%   28C    P8    16W / 250W |      0MiB / 11176MiB |     0%      Default |
|                              |                      |                 N/A |
+------------------------------+----------------------+---------------------+
|  2  GeForce GTX 108...  Off  | 00000000:B3:00.0 Off |                 N/A |
| 0%   26C    P8     8W / 250W |      0MiB / 11178MiB |     0%      Default |
|                              |                      |                 N/A |
+------------------------------+----------------------+---------------------+
+---------------------------------------------------------------------------+
|Processes:                                                                 |
| GPU   GI   CI        PID   Type   Process name                 GPU Memory |
|       ID   ID                                                  Usage      |
|===========================================================================|
|   0   N/A  N/A     27939	 C   /opt/conda/bin/python            135MiB |
+---------------------------------------------------------------------------+
```
## 5. Cuda Version 확인
``` BASH
$ nvcc -V

    nvcc: NVIDIA (R) Cuda compiler driver
    Copyright (c) 2005-2020 NVIDIA Corporation
    Built on Mon_Nov_30_19:08:53_PST_2020
    Cuda compilation tools, release 11.2, V11.2.67
    Build cuda_11.2.r11.2/compiler.29373293_0

## 위 같이 정보가 안나오면 cuda 설치여부 확인
$ ls -al /usr/local/ |grep cuda

    lrwxrwxrwx.  1 root root   21  7월 30 15:05 cuda -> /usr/local/cuda-11.2/
    drwxr-xr-x. 17 root root 4096  7월 30 15:06 cuda-11.2
```

``` bash 
$ cat /etc/*rele*
NAME="ProLinux"
VERSION="7"
ID="prolinux"
VERSION_ID="7"
PRETTY_NAME="ProLinux 7"
ANSI_COLOR="0;31"
CPE_NAME="cpe:/o:prolinux:prolinux:7"
HOME_URL="https://tmaxos.com/"
BUG_REPORT_URL="https://tmaxos.com/"

PROLINUX_MANTISBT_PROJECT="ProLinux-7"
PROLINUX_MANTISBT_PROJECT_VERSION="7"
PROLINUX_SUPPORT_PRODUCT="prolinux"
PROLINUX_SUPPORT_PRODUCT_VERSION="7"

ProLinux release 7.5
Derived from Red Hat Enterprise Linux 7.5 (Source)
ProLinux release 7.5
ProLinux release 7.5
cpe:/o:prolinux:prolinux:7


===============================================================================


$ yum list installed |grep gcc
Repodata is over 2 weeks old. Install yum-cron? Or run: yum makecache fast
gcc.x86_64                          4.8.5-39.el7                   @local-repo
gcc-c++.x86_64                      4.8.5-39.el7                   @local-repo
gcc-gfortran.x86_64                 4.8.5-39.el7                   @local-repo
libgcc.x86_64                       4.8.5-39.el7                   @local-repo


===============================================================================


$ nvidia-smi
Thu Aug  5 10:38:16 2021
+-----------------------------------------------------------------------------+
| NVIDIA-SMI 450.66       Driver Version: 450.66       CUDA Version: 11.0     |
|-------------------------------+----------------------+----------------------+
| GPU  Name        Persistence-M| Bus-Id        Disp.A | Volatile Uncorr. ECC |
| Fan  Temp  Perf  Pwr:Usage/Cap|         Memory-Usage | GPU-Util  Compute M. |
|                               |                      |               MIG M. |
|===============================+======================+======================|
|   0  Tesla T4            Off  | 00000000:3B:00.0 Off |                    0 |
| N/A   27C    P8     9W /  70W |      0MiB / 15109MiB |      0%      Default |
|                               |                      |                  N/A |
+-------------------------------+----------------------+----------------------+

+-----------------------------------------------------------------------------+
| Processes:                                                                  |
|  GPU   GI   CI        PID   Type   Process name                  GPU Memory |
|        ID   ID                                                   Usage      |
|=============================================================================|
|  No running processes found                                                 |
+-----------------------------------------------------------------------------+


===============================================================================


$ ls -al /usr/local/ |grep cuda
$ ls -al /usr/local/
total 0
drwxr-xr-x. 14 root root 155 Feb 18 15:36 .
drwxr-xr-x. 13 root root 155 Oct  7  2020 ..
drwxr-xr-x.  2 root root   6 Apr 11  2019 bin
drwxr-xr-x.  2 root root   6 Apr 11  2019 etc
drwxr-xr-x.  2 root root   6 Apr 11  2019 games
drwxr-xr-x.  2 root root   6 Apr 11  2019 include
drwxr-xr-x.  2 root root   6 Apr 11  2019 lib
drwxr-xr-x.  2 root root   6 Apr 11  2019 lib64
drwxr-xr-x.  2 root root   6 Apr 11  2019 libexec
drwxr-xr-x.  2 root root   6 Nov 17  2020 NPKI
drwxr-xr-x.  2 root root   6 Apr 11  2019 sbin
drwxr-xr-x.  5 root root  49 Oct  7  2020 share
drwx------.  5 root root  46 Feb 18 15:37 snet
drwxr-xr-x.  2 root root   6 Apr 11  2019 src

```

## Chenck Cuda tookit Version
Table 1. CUDA Toolkit and Compatible Driver Versions   
CUDA Toolkit	Linux x86_64 Driver Version	Windows x86_64 Driver Version  

CUDA 10.1 (10.1.105 general release, and updates)	>= 418.39	>= 418.96   
CUDA 10.0.130	>= 410.48	>= 411.31    

## Check - 2 : Cuda Version and CuDNN version and tensorflow version
tensorflow-2.3.0	3.5-3.8	GCC 7.3.1	Bazel 3.1.0	7.6	10.1
tensorflow-2.2.0	3.5-3.8	GCC 7.3.1	Bazel 2.0.0	7.6	10.1
tensorflow-2.1.0	2.7, 3.5-3.7	GCC 7.3.1	Bazel 0.27.1	7.6	10.1
tensorflow-2.0.0	2.7, 3.3-3.7	GCC 7.3.1	Bazel 0.26.1	7.4	10.0
tensorflow_gpu-1.15.0	2.7, 3.3-3.7	GCC 7.3.1	Bazel 0.26.1	7.4	10.0
tensorflow_gpu-1.14.0	2.7, 3.3-3.7	GCC 4.8	Bazel 0.24.1	7.4	10.0
tensorflow_gpu-1.13.1	2.7, 3.3-3.7	GCC 4.8	Bazel 0.19.2	7.4	10.0

## 셋팅 버전 
| GCC | GPU Drive | CUDA | cnDNN | Python | Tensorflow |
| ----- | ----- | ----- | ----- | ----- | ----- |
| GCC 4.8, | GPU Drive 450.66 | CUDA 10.0 | cuDNN 7.4 | Python 3.7 | Tensorflow_gpu 1.13.1 | 
| GCC 4.8, | GPU Drive 450.66 | CUDA 10.0 | cuDNN 7.4 | Python 3.7 | Tensorflow_gpu 1.14.0 |
| GCC 7.3.1 | GPU Drive 450.66 | CUDA 10.0 | cuDNN 7.4 | Python 3.7 | Tensorflow_gpu 1.15.0 |
| GCC 7.3.1 | GPU Drive 450.66 | CUDA 10.0 | cuDNN 7.4 | Python 3.7 | Tensorflow_2.0.0 |
| GCC 7.3.1 | GPU Drive 450.66 | CUDA 10.1 | cuDNN 7.6 | Python 3.7 | Tensorflow_2.1.0 |
| GCC 7.3.1 | GPU Drive 450.66 | CUDA 10.1 | cuDNN 7.6 | Python 3.7-8 | Tensorflow_2.2.0 |
| GCC 7.3.1 | GPU Drive 450.66 | CUDA 10.1 | cuDNN 7.6 | Python 3.7-8 | Tensorflow_2.3.0 |


## cuDNN 버전확인
cuDNN 8.x 이전은  cat /usr/local/cuda/include/cudnn.h 파일에서 확인을 해야 합니다.   
> cat /usr/local/cuda/include/cudnn.h | grep CUDNN_MAJOR -A 2   

cuDNN 8.x 이후 버전에서는 위의 명령어로 설치 확인이 되지 않습니다.   
cuDNN 8.x 이상 부터는 cudnn_version.h 파일에서 버전을 확인해야 합니다.   
> cat /usr/local/cuda/include/cudnn_version.h | grep CUDNN_MAJOR -A 2   