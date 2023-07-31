참고 : https://sysops.tistory.com/115
참고 : https://www.lesstif.com/system-admin/systemd-journal-82215092.html

``` bash
vi /etc/systemd/journald.conf 

Storage=persistent
SystemMaxFileSize=500
MaxFileSec=1month
```