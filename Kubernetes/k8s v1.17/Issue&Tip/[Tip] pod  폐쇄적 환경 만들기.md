## Pod 폐쇄적 TEST 환경 만들기
폐쇄 환경을 만들기위해 NetworkPolicy 정책 Object를 사용한다. 
수신은 192.123.123.231 IP와 8888 포트 접근만 허용, 송신은 모두 막음.
 - Pod의 "app: jupyter-3.7" 메타 라벨을 가진 Pod에만 적용.
 - Pod Web 서비스 접근은 가능.
 - 동작 중 외부 패키지 다운로드와 같은 것은 막힘.

<br> 

주의 : Pod 외부로 나가는 네트웍을 모두 막는것으로 인한 정상 수행이 되야 할것이 안될 수도 있음. 이를 고려하여 Egress 도 별도 설정이 필요 할 수 있음.

``` yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: nps-external-enable
  namespace: nps
spec:
  podSelector:
    matchLabels:
      app: jupyter-3.7
  policyTypes:
  - Ingress
  - Egress
  ingress: 
  - from:
    - ipBlock:
        cidr: 192.123.123.231/32
    ports:
    - protocol: TCP
      port: 8888
```