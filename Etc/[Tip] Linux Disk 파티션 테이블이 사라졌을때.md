## 현상
 - 서버 재부팅 후 서버 시작이 비정상적으로 booting 됨
 - 로그 확인 결과 기본 mount 설정된 Partition을 찾지 못하여 안전모드로 접근됨.
 - "A Start job is running for dev-disk-by .........." 과 같은 booting 로그를 확인 할 수 있음.
 - 이는 /etc/fstab 설정된 영구 mount 설정이 무언가 잘못됫다라는 판단을 할 수 있음.
 - 안전 모드상태에서 fdisk -l 명령어로 확인 결과 특정 Disk가 Partition Table 정보를 잃음
``` bash

root@k8s-master02:/data# fdisk -l

Disk /dev/loop0: 97.9 MiB, 102612992 bytes, 200416 sectors
Units: sectors of 1 * 512 = 512 bytes
Sector size (logical/physical): 512 bytes / 512 bytes
I/O size (minimum/optimal): 512 bytes / 512 bytes

Disk /dev/loop1: 97.9 MiB, 102637568 bytes, 200464 sectors
Units: sectors of 1 * 512 = 512 bytes
Sector size (logical/physical): 512 bytes / 512 bytes
I/O size (minimum/optimal): 512 bytes / 512 bytes

Disk /dev/sdb: 931.5 GiB, 1000204886016 bytes, 1953525168 sectors
Units: sectors of 1 * 512 = 512 bytes
Sector size (logical/physical): 512 bytes / 4096 bytes
I/O size (minimum/optimal): 4096 bytes / 4096 bytes
Disklabel type: dos
Disk identifier: 0x00000000

## 주석처리된 부분이 보이지 않았음.
## Device     Boot Start        End    Sectors   Size Id Type
## /dev/sdb1        2048 1953525759 1953523712 931.5G 8e Linux LVM


Disk /dev/sda: 119.2 GiB, 128035676160 bytes, 250069680 sectors
Units: sectors of 1 * 512 = 512 bytes
Sector size (logical/physical): 512 bytes / 512 bytes
I/O size (minimum/optimal): 512 bytes / 512 bytes
Disklabel type: gpt
Disk identifier: 4521EAC5-4E24-4C6E-9ACC-5C02DEC62D33

Device       Start       End   Sectors   Size Type
/dev/sda1     2048   1050623   1048576   512M EFI System
/dev/sda2  1050624   3147775   2097152     1G Linux filesystem
/dev/sda3  3147776 250066943 246919168 117.8G Linux filesystem


Disk /dev/mapper/ubuntu--vg-root: 90 GiB, 96636764160 bytes, 188743680 sectors
Units: sectors of 1 * 512 = 512 bytes
Sector size (logical/physical): 512 bytes / 512 bytes
I/O size (minimum/optimal): 512 bytes / 512 bytes


Disk /dev/mapper/data-lv1: 931.5 GiB, 1000198897664 bytes, 1953513472 sectors
Units: sectors of 1 * 512 = 512 bytes
Sector size (logical/physical): 512 bytes / 4096 bytes
I/O size (minimum/optimal): 4096 bytes / 4096 bytes


Disk /dev/mapper/ubuntu--vg-home: 20 GiB, 21474836480 bytes, 41943040 sectors
Units: sectors of 1 * 512 = 512 bytes
Sector size (logical/physical): 512 bytes / 512 bytes
I/O size (minimum/optimal): 512 bytes / 512 bytes


Disk /dev/mapper/ubuntu--vg-swap: 7.8 GiB, 8308916224 bytes, 16228352 sectors
Units: sectors of 1 * 512 = 512 bytes
Sector size (logical/physical): 512 bytes / 512 bytes
I/O size (minimum/optimal): 512 bytes / 512 bytes

```

- 위처럼 확인 결과 /dev/sdb/ Disk의 Partition 정보가 사라짐을 확인 복구해 보았다.

## 복구 방법
참고 : http://www.iorchard.net/2019/02/12/wisekb_%ED%8C%8C%ED%8B%B0%EC%85%98_%ED%85%8C%EC%9D%B4%EB%B8%94_%EB%B3%B5%EA%B5%AC.html
``` bash
## disk 복구 Utile 사용을 위한 설치 (testdisk 라는 유틸)
$ apt-get install testdisk

## testdisk 실행
$ testdisk

    ### [Step1] 아래와 같이 출력되면서 로그를 생성할지 물어봄. [Create] 선택
    TestDisk 6.13, Data Recovery Utility, November 2011
    Christophe GRENIER <grenier@cgsecurity.org>
    http://www.cgsecurity.org


    TestDisk is free data recovery software designed to help recover lost
    partitions and/or make non-booting disks bootable again when these symptoms
    are caused by faulty software, certain types of viruses or human error.
    It can also be used to repair some filesystem errors.

    Information gathered during TestDisk use can be recorded for later
    review. If you choose to create the text file, testdisk.log , it
    will contain TestDisk options, technical information and various
    outputs; including any folder/file names TestDisk was used to find and
    list onscreen.

    Use arrow keys to select, then press Enter key:
    >[ Create ] Create a new log file
    [ Append ] Append information to log file
    [ No Log ] Dont record anything



    ### [Step2] 복구가 필요한 Disk 선택. [Disk /dev/sdb] 선택
    TestDisk 7.0, Data Recovery Utility, April 2015
    Christophe GRENIER <grenier@cgsecurity.org>
    http://www.cgsecurity.org

    TestDisk is free software, and
    comes with ABSOLUTELY NO WARRANTY.

    Select a media (use Arrow keys, then press Enter):
    Disk /dev/sda - 128 GB / 119 GiB - TOSHIBA THNSNJ128GCSU
    >Disk /dev/sdb - 1000 GB / 931 GiB - WDC WD10EZEX-00WN4A0
    Disk /dev/mapper/data-lv1 - 1000 GB / 931 GiB - WDC WD10EZEX-00WN4A0
    Disk /dev/mapper/ubuntu--vg-home - 21 GB / 20 GiB - TOSHIBA THNSNJ128GCSU
    Disk /dev/mapper/ubuntu--vg-root - 96 GB / 90 GiB - TOSHIBA THNSNJ128GCSU
    Disk /dev/mapper/ubuntu--vg-swap - 8308 MB / 7924 MiB - TOSHIBA THNSNJ128GCSU
    Disk /dev/dm-0 - 96 GB / 90 GiB - TOSHIBA THNSNJ128GCSU
    Disk /dev/dm-1 - 1000 GB / 931 GiB - WDC WD10EZEX-00WN4A0
    Disk /dev/dm-2 - 21 GB / 20 GiB - TOSHIBA THNSNJ128GCSU
    Disk /dev/dm-3 - 8308 MB / 7924 MiB - TOSHIBA THNSNJ128GCSU


    ### [Step3] 환경에 맞는 Type 선택 (자동으로 지정되는듯 하다, 그냥 Enter)
    TestDisk 7.0, Data Recovery Utility, April 2015
    Christophe GRENIER <grenier@cgsecurity.org>
    http://www.cgsecurity.org


    Disk /dev/sdb - 1000 GB / 931 GiB - WDC WD10EZEX-00WN4A0

    Please select the partition table type, press Enter when done.
    >[Intel  ] Intel/PC partition
    [EFI GPT] EFI GPT partition map (Mac i386, some x86_64...)
    [Humax  ] Humax partition table
    [Mac    ] Apple partition map
    [None   ] Non partitioned media
    [Sun    ] Sun Solaris partition
    [XBox   ] XBox partition
    [Return ] Return to disk selection



    Hint: Intel partition table type has been detected.
    Note: Do NOT select 'None' for media with only a single partition. Its very
    rare for a disk to be 'Non-partitioned'.

    ### [Step4] 파티션 복구를 위해서 [Analyse] 선택
    TestDisk 7.0, Data Recovery Utility, April 2015
    Christophe GRENIER <grenier@cgsecurity.org>
    http://www.cgsecurity.org


    Disk /dev/sdb - 1000 GB / 931 GiB - WDC WD10EZEX-00WN4A0
        CHS 121601 255 63 - sector size=512

    >[ Analyse  ] Analyse current partition structure and search for lost partitions
    [ Advanced ] Filesystem Utils
    [ Geometry ] Change disk geometry
    [ Options  ] Modify options
    [ MBR Code ] Write TestDisk MBR code to first sector
    [ Delete   ] Delete all data in the partition table
    [ Quit     ] Return to disk selection





    Note: Correct disk geometry is required for a successful recovery. 'Analyse'
    process may give some warnings if it thinks the logical geometry is mismatched.


    ### [Step5] 복구할 파티션을 선택해야하나 처음에는 아무것도 안보임. [Quick Search] 선택을 파티션을 찾는다.
    TestDisk 7.0, Data Recovery Utility, April 2015
    Christophe GRENIER <grenier@cgsecurity.org>
    http://www.cgsecurity.org

    Disk /dev/sdb - 1000 GB / 931 GiB - CHS 121601 255 63
    Current partition structure:
        Partition                  Start        End    Size in sectors

    1 P Linux LVM                0  32 33 121601  90 25 1953523712
    No partition is bootable


    *=Primary bootable  P=Primary  L=Logical  E=Extended  D=Deleted
    >[Quick Search]  [ Backup ]

    ### [Step6] 복구할 파티션을 찾으면 Enter를 누르면 다음과 같이 해당 파티션 정보를 복구할지 나온다.
    ###         여기서 [Write] 를 선택하여 복구 한다.

    TestDisk 6.13, Data Recovery Utility, November 2011
    Christophe GRENIER <grenier@cgsecurity.org>
    http://www.cgsecurity.org

    Disk /dev/mapper/pengrix_vg-vm--60--b - 8796 GB / 8192 GiB - CHS 17179869184 1 1

        Partition                  Start        End    Size in sectors

    1 P MS Data                     2048 17179867135 17179865088

    [  Quit  ] >[Deeper Search]  [ Write  ]
                            Try to find more partitions

    ### [Step 7] 최종 복구 의사에 Y를 입력하면 끝이 난다.
    TestDisk 6.13, Data Recovery Utility, November 2011
    Christophe GRENIER <grenier@cgsecurity.org>
    http://www.cgsecurity.org

    Write partition table, confirm ? (Y/N)

    ### [Step 8] 유틸에서 빠져나와 서버를 재기동 해주면 Partition이 다시 보인다. 다시 Mount 하여 사용하면 된다.
```