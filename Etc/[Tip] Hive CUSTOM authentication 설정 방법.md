# Hive CUSTOM authentication 설정 방법
## 자바 파일 생성
```sh
$ cat > SampleAuthenticator.java
```
- SampleAuthenticator.java 내용
```java
import java.util.Hashtable;
import javax.security.sasl.AuthenticationException;
import org.apache.hive.service.auth.PasswdAuthenticationProvider;

public class SampleAuthenticator implements PasswdAuthenticationProvider {

  Hashtable<String, String> store = null;

  public SampleAuthenticator () {
    store = new Hashtable<String, String>();
    store.put("hive", "hive");
    store.put("user2", "passwd2");
  }

  @Override
  public void Authenticate(String user, String  password)
      throws AuthenticationException {

    String storedPasswd = store.get(user);

    if (storedPasswd != null && storedPasswd.equals(password))
      return;
     
    throw new AuthenticationException("SampleAuthenticator: Error validating user");
  }

}
```

## Manifest 파일생성
```sh
$ cat > manifest.txt 
Main-class: SampleAuthenticator
```

## 자바 빌드
 - hive-service-2.3.9.jar 파일 필요! Hive/lib 안에 기본으로 제공됨.
```sh
$ ls -al
  합계 540
  -rw-r--r--.  1 root root    920  3월  8 20:57 SampleAuthenticator.java
  -rw-r--r--.  1 root root 527783  3월  8 21:15 hive-service-2.3.9.jar
  -rw-r--r--.  1 root root     32  3월  8 21:00 manifest.txt

## java 컴파일
$ javac -cp ".:./hive-service-2.3.9.jar" SampleAuthenticator.java

## JAR 빌드
$ jar -cvmf manifest.txt tmax-auth.jar SampleAuthenticator.class
  Manifest를 추가함
  추가하는 중: SampleAuthenticator.class(입력 = 1069) (출력 = 599)(43%를 감소함)

## 확인
$ jar tf tmax-auth.jar 
  META-INF/
  META-INF/MANIFEST.MF
  SampleAuthenticator.class

## lib jar 적용
$ cp ./tmax-auth.jar $HIVE_HOME/lib/
```

## Hive site 파일 수정
```sh
$ vi $HIVE_HOME/hive-site.xml
## 아래 처럼 내용 추가
  <property>
    <name>hive.server2.authentication</name>
    <value>CUSTOM</value>
  </property>
  <property>
    <name>hive.server2.custom.authentication.class</name>
    <value>SampleAuthenticator</value>
  </property>
```
