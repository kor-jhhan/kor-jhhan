https://jinmay.github.io/2019/01/29/linux/linux-command-sed/


## [case1] 특정 라인에 문자열 추가
``` bash
perl -p -i -e '$.==358 and print "\t\"/opt/cni/bin\",\n",' /etc/crio/crio.conf
```

## [case2] 치환 후 저장
``` bash
sed -i 's/"docker.io"/"docker.io", "192.168.56.105:5000"/' /etc/containers/registries.conf
```

## [case3] CRIO Container 찌꺼기가 많아 장애나는 현상 복구 방안. shell
 -  1 줄 데이터를 구분자로 라인으로 나누고
 -  나뉜 라인에서 특정 값(jupyter)을 포함한 라인만 도출
 -  \\"pod\\":true 라는 값을 가진 라인 제거
 -  id 값만 뽑기 쉽게 앞뒤로 Tab 값을 넣어 구분지어 준다.
 -  그럼 Line 별로 2번째 인자가 원하는 id 값을 불러 올수 있게 된다.
 -  id 값만 뽑아낸다.
```bash
# 도출 방법 (사용시 jupyter라는 필터 값을 꼭 변경해야 한다!!!!!)
[CMD]$ sed 's/},{/}\n{/g' ./containers.json |sed '/jupyter/!d' |sed '/\\"pod\\":true/d'| sed 's/"id":"/"id":"\t/g' | sed 's/","names"/\t","names"/g' |awk '{print "crictl rm " $2}'

# Container 삭제 shell 만들기
[CMD]$ sed 's/},{/}\n{/g' ./containers.json |sed '/jupyter/!d' |sed '/\\"pod\\":true/d'| sed 's/"id":"/"id":"\t/g' | sed 's/","names"/\t","names"/g' |awk '{print "crictl rm " $2}' > delete_crio_container.sh

# pod 삭제 shell 만들기
[CMD]$ sed 's/},{/}\n{/g' ./containers.json |sed '/jupyter/!d' |sed '/\\"pod\\":true/!d'| sed 's/"id":"/"id":"\t/g' | sed 's/","names"/\t","names"/g' |awk '{print "crictl rmp " $2}' > delete_crio_pod.sh
```

- 2번째 과정까지만 실행한 결과
```bash
## 보이는것 POD id 와 Container id 값이 섞여 있따.
## 이를 구분하기 위해 \"pod\":true 이걸 필터 해야 한다
[CMD]$ sed 's/},{/}\n{/g' ./containers.json |sed '/jupyter/!d'
{"id":"7f8e68b0361f10dabfce07f7bc11f7ccc8f0d5e6ca29b1b873da4cfcab5d2852","names":["k8s_POD_jupyter-3.7-gpu-deployment-9679459ff-tf4k9_nps_db8524d6-dc30-4916-9e99-cf866ef5312d_0","k8s_jupyter-3.7-gpu-deployment-9679459ff-tf4k9_nps_db8524d6-dc30-4916-9e99-cf866ef5312d_0"],"image":"da86e6ba6ca197bf6bc5e9d900febd906b133eaa4750e6bed647b0fbe50ed43e","layer":"425d7d6bda3798d453db42d2ff1af7147fcc4bad1729c6f0160f0eb0dbdc261a","metadata":"{\"pod-name\":\"k8s_jupyter-3.7-gpu-deployment-9679459ff-tf4k9_nps_db8524d6-dc30-4916-9e99-cf866ef5312d_0\",\"pod-id\":\"7f8e68b0361f10dabfce07f7bc11f7ccc8f0d5e6ca29b1b873da4cfcab5d2852\",\"image-name\":\"192.168.210.242:5000/k8s.gcr.io/pause:3.1\",\"image-id\":\"da86e6ba6ca197bf6bc5e9d900febd906b133eaa4750e6bed647b0fbe50ed43e\",\"name\":\"k8s_POD_jupyter-3.7-gpu-deployment-9679459ff-tf4k9_nps_db8524d6-dc30-4916-9e99-cf866ef5312d_0\",\"metadata-name\":\"jupyter-3.7-gpu-deployment-9679459ff-tf4k9\",\"uid\":\"db8524d6-dc30-4916-9e99-cf866ef5312d\",\"namespace\":\"nps\",\"created-at\":1636461257,\"pod\":true}","created":"2021-11-09T12:34:17.378448893Z","flags":{"MountLabel":"","ProcessLabel":""}}
{"id":"89daba6aced3a6ac731ca2934af759eb1b230b62e6c8d9742dbf0453a1435325","names":["k8s_con-jupyter_jupyter-3.7-gpu-deployment-9679459ff-tf4k9_nps_db8524d6-dc30-4916-9e99-cf866ef5312d_0"],"image":"8cf613e5a9a8066f2d14ff2b11d83e6f1c3d30794eb6a28fe6874d2bf875148b","layer":"4e09c4b3a8c89d31412f2270d6e721d9163a3f8f8152013a9f844d4f82fa9166","metadata":"{\"pod-name\":\"k8s_jupyter-3.7-gpu-deployment-9679459ff-tf4k9_nps_db8524d6-dc30-4916-9e99-cf866ef5312d_0\",\"pod-id\":\"7f8e68b0361f10dabfce07f7bc11f7ccc8f0d5e6ca29b1b873da4cfcab5d2852\",\"image-name\":\"192.168.210.242:5000/nps-jupyter-gpu@sha256:a3b277cc8e505790055b58d90e343868137e1a294d34845887932dec1e9746b0\",\"image-id\":\"8cf613e5a9a8066f2d14ff2b11d83e6f1c3d30794eb6a28fe6874d2bf875148b\",\"name\":\"k8s_con-jupyter_jupyter-3.7-gpu-deployment-9679459ff-tf4k9_nps_db8524d6-dc30-4916-9e99-cf866ef5312d_0\",\"metadata-name\":\"con-jupyter\",\"created-at\":1636461334}","created":"2021-11-09T12:35:34.056808892Z","flags":{"MountLabel":"","ProcessLabel":""}}
{"id":"310859b50c1ef928c4f996357e611780f1ea4a12fdca84528efd0365985ab86b","names":["k8s_POD_jupyter-3.8-gpu-deployment-564559d7d5-x6spd_nps_2baef0b3-42cb-42ce-9583-6724912f5094_0","k8s_jupyter-3.8-gpu-deployment-564559d7d5-x6spd_nps_2baef0b3-42cb-42ce-9583-6724912f5094_0"],"image":"da86e6ba6ca197bf6bc5e9d900febd906b133eaa4750e6bed647b0fbe50ed43e","layer":"4e028fa4ec42657ab909beb03572559e3cf3397895c87b6ea4e289a1e75eec6c","metadata":"{\"pod-name\":\"k8s_jupyter-3.8-gpu-deployment-564559d7d5-x6spd_nps_2baef0b3-42cb-42ce-9583-6724912f5094_0\",\"pod-id\":\"310859b50c1ef928c4f996357e611780f1ea4a12fdca84528efd0365985ab86b\",\"image-name\":\"192.168.210.242:5000/k8s.gcr.io/pause:3.1\",\"image-id\":\"da86e6ba6ca197bf6bc5e9d900febd906b133eaa4750e6bed647b0fbe50ed43e\",\"name\":\"k8s_POD_jupyter-3.8-gpu-deployment-564559d7d5-x6spd_nps_2baef0b3-42cb-42ce-9583-6724912f5094_0\",\"metadata-name\":\"jupyter-3.8-gpu-deployment-564559d7d5-x6spd\",\"uid\":\"2baef0b3-42cb-42ce-9583-6724912f5094\",\"namespace\":\"nps\",\"created-at\":1636964900,\"pod\":true}","created":"2021-11-15T08:28:20.805046537Z","flags":{"MountLabel":"","ProcessLabel":""}}
{"id":"9c229313def4c4496f8e3dcefdff5118fa03af8f2df5c6818b3c7b973c097972","names":["k8s_con-jupyter_jupyter-3.8-gpu-deployment-564559d7d5-x6spd_nps_2baef0b3-42cb-42ce-9583-6724912f5094_0"],"image":"e24353c15f4a4651a8b472f910f190943c35725a072d1c8e9a8996363de3d7f7","layer":"39fa6fe419dbad283a3a80bed2a584c59ce42c3f980d5ef71e3419b6277cc0ba","metadata":"{\"pod-name\":\"k8s_jupyter-3.8-gpu-deployment-564559d7d5-x6spd_nps_2baef0b3-42cb-42ce-9583-6724912f5094_0\",\"pod-id\":\"310859b50c1ef928c4f996357e611780f1ea4a12fdca84528efd0365985ab86b\",\"image-name\":\"192.168.210.242:5000/nps-jupyter-gpu@sha256:f4d5ed702ec2f321c34a4b8af4828ee2ac2d5bb3ff4b6c2310ce2423058925c7\",\"image-id\":\"e24353c15f4a4651a8b472f910f190943c35725a072d1c8e9a8996363de3d7f7\",\"name\":\"k8s_con-jupyter_jupyter-3.8-gpu-deployment-564559d7d5-x6spd_nps_2baef0b3-42cb-42ce-9583-6724912f5094_0\",\"metadata-name\":\"con-jupyter\",\"created-at\":1636964994}","created":"2021-11-15T08:29:54.543796034Z","flags":{"MountLabel":"","ProcessLabel":""}}

## 필터 한 결과 컨데이너 아이디만 추릴수 있다.
[CMD]$ sed 's/},{/}\n{/g' ./containers.json |sed '/jupyter/!d' |sed '/\\"pod\\":true/d'
{"id":"89daba6aced3a6ac731ca2934af759eb1b230b62e6c8d9742dbf0453a1435325","names":["k8s_con-jupyter_jupyter-3.7-gpu-deployment-9679459ff-tf4k9_nps_db8524d6-dc30-4916-9e99-cf866ef5312d_0"],"image":"8cf613e5a9a8066f2d14ff2b11d83e6f1c3d30794eb6a28fe6874d2bf875148b","layer":"4e09c4b3a8c89d31412f2270d6e721d9163a3f8f8152013a9f844d4f82fa9166","metadata":"{\"pod-name\":\"k8s_jupyter-3.7-gpu-deployment-9679459ff-tf4k9_nps_db8524d6-dc30-4916-9e99-cf866ef5312d_0\",\"pod-id\":\"7f8e68b0361f10dabfce07f7bc11f7ccc8f0d5e6ca29b1b873da4cfcab5d2852\",\"image-name\":\"192.168.210.242:5000/nps-jupyter-gpu@sha256:a3b277cc8e505790055b58d90e343868137e1a294d34845887932dec1e9746b0\",\"image-id\":\"8cf613e5a9a8066f2d14ff2b11d83e6f1c3d30794eb6a28fe6874d2bf875148b\",\"name\":\"k8s_con-jupyter_jupyter-3.7-gpu-deployment-9679459ff-tf4k9_nps_db8524d6-dc30-4916-9e99-cf866ef5312d_0\",\"metadata-name\":\"con-jupyter\",\"created-at\":1636461334}","created":"2021-11-09T12:35:34.056808892Z","flags":{"MountLabel":"","ProcessLabel":""}}
{"id":"9c229313def4c4496f8e3dcefdff5118fa03af8f2df5c6818b3c7b973c097972","names":["k8s_con-jupyter_jupyter-3.8-gpu-deployment-564559d7d5-x6spd_nps_2baef0b3-42cb-42ce-9583-6724912f5094_0"],"image":"e24353c15f4a4651a8b472f910f190943c35725a072d1c8e9a8996363de3d7f7","layer":"39fa6fe419dbad283a3a80bed2a584c59ce42c3f980d5ef71e3419b6277cc0ba","metadata":"{\"pod-name\":\"k8s_jupyter-3.8-gpu-deployment-564559d7d5-x6spd_nps_2baef0b3-42cb-42ce-9583-6724912f5094_0\",\"pod-id\":\"310859b50c1ef928c4f996357e611780f1ea4a12fdca84528efd0365985ab86b\",\"image-name\":\"192.168.210.242:5000/nps-jupyter-gpu@sha256:f4d5ed702ec2f321c34a4b8af4828ee2ac2d5bb3ff4b6c2310ce2423058925c7\",\"image-id\":\"e24353c15f4a4651a8b472f910f190943c35725a072d1c8e9a8996363de3d7f7\",\"name\":\"k8s_con-jupyter_jupyter-3.8-gpu-deployment-564559d7d5-x6spd_nps_2baef0b3-42cb-42ce-9583-6724912f5094_0\",\"metadata-name\":\"con-jupyter\",\"created-at\":1636964994}","created":"2021-11-15T08:29:54.543796034Z","flags":{"MountLabel":"","ProcessLabel":""}}

```



```SQL

INSERT INTO HYPERDATA.DEIDENTIFICATION_PREDEFINED_RULE
(ID, COLUMN_NAME, METHOD, SQL)
VALUES(14, 'cdn', 'CREDIT_CARD_NUMBER', 'CASE WHEN REGEXP_LIKE(:PHYSICAL_COLUMN_NAME, ''^[:digit:]{15,16}$'')
   THEN RPAD(SUBSTR(:PHYSICAL_COLUMN_NAME, 1, 6), 12, ''*'') || SUBSTR(:PHYSICAL_COLUMN_NAME, 13)
WHEN :PHYSICAL_COLUMN_NAME IS NULL THEN NULL
ELSE LPAD(''*'', LENGTH(:PHYSICAL_COLUMN_NAME), ''*'') END :LOGICAL_COLUMN_NAME');


INSERT INTO HYPERDATA.DEIDENTIFICATION_PREDEFINED_RULE (ID, COLUMN_NAME, METHOD, SQL) 
VALUES(12, 'acn', 'ACCOUNT_NUMBER', 'CASE WHEN REGEXP_LIKE(:PHYSICAL_COLUMN_NAME, ''^[:digit:]{14}$'') THEN RPAD(SUBSTR(:PHYSICAL_COLUMN_NAME,1,7),9,''*'') || RPAD(SUBSTR(:PHYSICAL_COLUMN_NAME,10,2),5,''*'') WHEN REGEXP_LIKE(:PHYSICAL_COLUMN_NAME, ''^[:digit:]{11,13}$'') THEN RPAD(SUBSTR(:PHYSICAL_COLUMN_NAME,1,LENGTH(:PHYSICAL_COLUMN_NAME)-5), LENGTH(:PHYSICAL_COLUMN_NAME), ''*'') WHEN :PHYSICAL_COLUMN_NAME IS NULL THEN NULL ELSE LPAD(''*'', LENGTH(:PHYSICAL_COLUMN_NAME), ''*'') END :LOGICAL_COLUMN_NAME');

INSERT INTO HYPERDATA.DEIDENTIFICATION_PREDEFINED_RULE
(ID, COLUMN_NAME, METHOD, SQL)
VALUES(16, 'srnm', 'NAME', 'CASE WHEN REGEXP_LIKE(:PHYSICAL_COLUMN_NAME, ''^[A-Za-z\s]+$'')
   THEN SUBSTR(:PHYSICAL_COLUMN_NAME, 1, REGEXP_INSTR(:PHYSICAL_COLUMN_NAME, ''^(([[:alpha:]](\s*)){4})'', 1, 1, 1)-1) ||
      REGEXP_REPLACE(SUBSTR(:PHYSICAL_COLUMN_NAME, REGEXP_INSTR(:PHYSICAL_COLUMN_NAME, ''^(([[:alpha:]](\s*)){4})'', 1, 1, 1)), ''[[:alpha:]]'', ''*'')
WHEN REGEXP_LIKE(:PHYSICAL_COLUMN_NAME, ''^[\uac00-\ud7a3\s]+$'')
	THEN SUBSTR(:PHYSICAL_COLUMN_NAME, 1, 1) || ''*'' || SUBSTR(:PHYSICAL_COLUMN_NAME, 3)
WHEN :PHYSICAL_COLUMN_NAME IS NULL THEN NULL
ELSE LPAD(''*'', LENGTH(:PHYSICAL_COLUMN_NAME), ''*'') END :LOGICAL_COLUMN_NAME');



INSERT INTO HYPERDATA.DEIDENTIFICATION_PREDEFINED_RULE
(ID, COLUMN_NAME, METHOD, SQL)
VALUES(8, 'tpn', 'PHONE_NUMBER', 'CASE 
     WHEN REGEXP_LIKE(:PHYSICAL_COLUMN_NAME, ''^(02|031|032|033|041|042|043|044|051|052|053|054|061|062|063|064|010|011|012|016|017|018|019)-[0-9]{3,4}-[0-9]{4}$'')
     THEN REGEXP_REPLACE(:PHYSICAL_COLUMN_NAME, ''(02|031|032|033|041|042|043|044|051|052|053|054|061|062|063|064|010|011|012|016|017|018|019)-([0-9]{3,4})-([0-9]{4})'', ''\1-'' || 
     LPAD(''*'',LENGTH(REGEXP_REPLACE(:PHYSICAL_COLUMN_NAME, ''(02|031|032|033|041|042|043|044|051|052|053|054|061|062|063|064|010|011|012|016|017|018|019)-([0-9]{3,4})-([0-9]{4})'', ''\2'')),''*'') || ''-\3'')
     WHEN REGEXP_LIKE(:PHYSICAL_COLUMN_NAME, ''(02|031|032|033|041|042|043|044|051|052|053|054|061|062|063|064|010|011|012|016|017|018|019)[0-9]{3,4}[0-9]{4}'') 
     THEN REGEXP_REPLACE(:PHYSICAL_COLUMN_NAME, ''(02|031|032|033|041|042|043|044|051|052|053|054|061|062|063|064|010|011|012|016|017|018|019)([0-9]{3,4})([0-9]{4})'', ''\1'' || 
     LPAD(''*'',LENGTH(REGEXP_REPLACE(:PHYSICAL_COLUMN_NAME, ''(02|031|032|033|041|042|043|044|051|052|053|054|061|062|063|064|010|011|012|016|017|018|019)([0-9]{3,4})([0-9]{4})'', ''\2'')),''*'') || ''\3'')
     WHEN :PHYSICAL_COLUMN_NAME IS NULL
     THEN NULL
     ELSE LPAD(''*'', LENGTH(:PHYSICAL_COLUMN_NAME), ''*'')
     END :LOGICAL_COLUMN_NAME');


INSERT INTO HYPERDATA.DEIDENTIFICATION_PREDEFINED_RULE
(ID, COLUMN_NAME, METHOD, SQL)
VALUES(6, 'adr', 'ADDRESS', 'CASE WHEN REGEXP_LIKE(:PHYSICAL_COLUMN_NAME, ''[\uac00-\ud7a30-9-]+[\ub3c4\uc2dc] [\uac00-\ud7a30-9-]+[\uc2dc\uad70\uad6c][\uac00-\ud7a30-9-\s]*'') 
     THEN REGEXP_REPLACE(:PHYSICAL_COLUMN_NAME, ''([\uac00-\ud7a30-9-]+[\ub3c4\uc2dc]) ([\uac00-\ud7a30-9-]+[\uc2dc\uad70\uad6c])([\uac00-\ud7a30-9-\s]*)'', ''\1 \2 '' ||
     REGEXP_REPLACE(REGEXP_REPLACE(:PHYSICAL_COLUMN_NAME, ''([\uac00-\ud7a30-9-]+[\ub3c4\uc2dc]) ([\uac00-\ud7a30-9-]+[\uc2dc\uad70\uad6c])([\uac00-\ud7a30-9-\s]*)'', ''\3''), ''[\uac00-\ud7a30-9-]'', ''*''))
     WHEN :PHYSICAL_COLUMN_NAME IS NULL
     THEN NULL
     ELSE LPAD(''*'', LENGTH(:PHYSICAL_COLUMN_NAME), ''*'')
     END :LOGICAL_COLUMN_NAME');


INSERT INTO HYPERDATA.DEIDENTIFICATION_PREDEFINED_RULE
(ID, COLUMN_NAME, METHOD, SQL)
VALUES(4, 'ead', 'EMAIL', 'CASE WHEN REGEXP_LIKE(:PHYSICAL_COLUMN_NAME, ''[\w!-_\.]+@[\w\.]+'') 
     THEN REGEXP_REPLACE(:PHYSICAL_COLUMN_NAME, ''([\w!-_\.]{2})([\w!-_\.]*)@([\w\.]+)'', ''\1''|| LPAD(''*'',LENGTH(REGEXP_REPLACE(:PHYSICAL_COLUMN_NAME, ''([\w!-_\.]{2})([\w!-_\.]*)@([\w\.]+)'', ''\2'')),''*'') || ''@\3'') 
     WHEN :PHYSICAL_COLUMN_NAME IS NULL
     THEN NULL
     ELSE LPAD(''*'', LENGTH(:PHYSICAL_COLUMN_NAME), ''*'')
     END :LOGICAL_COLUMN_NAME');


```


