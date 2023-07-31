## Trace 방법
@@ Win 10 환경
### TraceTCP 사용
1. WinPcap_4_1_3.exe 설치
2. win_trace 다운로드 받아 압축해제
3. [window Key] + R 키로 실행창을 열어 cmd 를 입력하여 터미널창을 연다.
4. 터미널에서 win_trace 압축 해제한 경로로 이동한다.
5. 다음과 같이 사용 (자세한건 여기서 : https://github.com/SimulatedSimian/tracetcp)
   $ C:\> tracetcp.exe 110.165.23.46:30030 -t 10000 -p 5

### Tracert 사용
1. 기본 Window 기능으로 사용 가능, 단 IP단위로만 추적되어 포트 제약이 있을시 사용 불가
2. 다음과 같이 사용 ( 자세힌건 여기서 : https://support.microsoft.com/ko-kr/topic/tracert%EB%A5%BC-%EC%82%AC%EC%9A%A9%ED%95%98%EC%97%AC-windows%EC%97%90%EC%84%9C-tcp-ip-%EB%AC%B8%EC%A0%9C%EB%A5%BC-%ED%95%B4%EA%B2%B0%ED%95%98%EB%8A%94-%EB%B0%A9%EB%B2%95-e643d72b-2f4f-cdd6-09a0-fd2989c7ca8e)
   $ C:\> tracert 11.1.0.1

@@ Linux 환경

### tcptraceroute 사용
1. yum install -y traceroute 설치
2. 아래와 같이 사용
   $ tcptraceroute 110.165.23.46 30030



tracetcp.exe 192.165.173.60:22 -t 10000 -p 5