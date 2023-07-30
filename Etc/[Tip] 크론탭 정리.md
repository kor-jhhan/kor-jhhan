## 크론탭 동작하도록 셋팅 정리

1. crontab 설정
    ``` bash
    $ crontab -e
    ```
2. SHELL 지정
3. PATH 지정
    ```bash
    SHELL=/bin/bash
    PATH=/usr/local/sbin:/usr/local/bin:/sbin:/bin:/usr/sbin:/usr/bin:/root/bin
    ## TEST 수행
    * * * * * /root/cron_test/test.sh
    ```
4. 수행 Shell 안에서 참조하는 환경변수 or 상대경로는 cron이 알수 없음.
    - 필요한 환경 변수는 모두 수행 shell 안에서 지역변수 처리하여 수행
    - 경로의 경우 ./ 와 같은 상대 경로는 알수 없음. 즉 절대경로 /root/test/test.log 이런식으로 작성되어야함.

5. 확인방법
    ```bash
    $ journalctl |grep cron
    ```
6. crontab 설정 후 적용 방법 (서비스재기동)
    ``` bash
    $ service crond restart
    # 또는
    $ service cron restart
    ```
7. crontab 이 2번 이상 중복실행 시 확인
    ```bash
    ## 프로세스 확인 (가끔 중복되어 실행되어 있음.)
    $ ps -ef |grep cron
    ## 과거 프로세스 강제 Kill 
    $ kill -9 xxxxx
    ```