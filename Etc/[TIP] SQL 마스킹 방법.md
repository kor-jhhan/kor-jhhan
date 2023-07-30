## Oracle, Tibero를 이용한 마스킹 방법 (예시)
```SQL
-- 특정 위치 값 기준 *로 마스킹
WITH T AS (
	SELECT '서울시 강남구 논현동 110-1번지' AS test FROM DUAL UNION ALL
	SELECT '서울시 강서구 등촌동 312-1번지' AS test FROM DUAL UNION ALL
	SELECT '태양계 지구 대한민국동 1234-1번지' AS test FROM DUAL
)
SELECT 
	'**'||SUBSTR(TEST,3) AS CASE1
	, SUBSTR(TEST,1,REGEXP_INSTR(TEST,'[[:digit:]]')-1) ||'***' || SUBSTR(TEST,REGEXP_INSTR(TEST,'-')) AS CASE2
FROM T
;

-- 특정 값을 *로 마스킹
WITH T AS (
	SELECT '서울시 강남구 논현동 110-1번지' AS test FROM DUAL UNION ALL
	SELECT '서울시 강서구 등촌동 312-1번지' AS test FROM DUAL UNION ALL
	SELECT '태양계 지구 대한민국동 1234-1번지' AS test FROM DUAL
)
SELECT REPLACE(REPLACE(TEST,'서울','*'),'태양','*') AS CASE1
FROM T
;

-- 특정 위치의 값만을 *로 마스킹 예제
WITH T AS (
	SELECT '서울시 강남구 논현동 110-1번지' AS test FROM DUAL UNION ALL
	SELECT '서울시 강서구 등촌동 312-1번지' AS test FROM DUAL UNION ALL
	SELECT '태양계 지구 대한민국동 1234-1번지' AS test FROM DUAL
)
SELECT 
	REGEXP_REPLACE(test, '[^ ]+([구])', '***\1') AS CASE1
	, REGEXP_REPLACE(test, '[^ ]+([동])', '**\1') AS CASE2
	, REGEXP_REPLACE(test, '[^ ]+(번지)', '**\1') AS CASE3
FROM T
;

-- 특정 위치의 값만을 *로 마스킹 예제
WITH t AS(
	SELECT '서울시 강남구 논현동 110-1번지' AS test FROM DUAL UNION ALL
	SELECT '서울시 강서구 등촌동 312-1번지' AS test FROM DUAL UNION ALL
	SELECT '태양계 지구 대한민국동 1234-1번지' AS test FROM DUAL
)
SELECT
test,
REGEXP_SUBSTR(test,'[^ ]+',1,1)||' '||REGEXP_SUBSTR(test,'[^ ]+',1,2)||' '||LPAD(SUBSTR(REGEXP_SUBSTR(test,'[^ ]+',1,3),-1),LENGTH(REGEXP_SUBSTR(test,'[^ ]+',1,3))+1,'*')||' '||REGEXP_SUBSTR(test,REGEXP_SUBSTR(test,'[^ ]+',1,4)||'.*') as AAA
FROM t
;



-- IBK 마스킹 케이스 샘플
WITH T AS (
	SELECT 
		'721010-1234567' 		AS 주민번호
		, '801102-123456-1' 	AS 외국인번호
		, 'MJ1234567' 			AS 여권번호
		, '서울 12-123456-12' 		AS 운전면허번호 
		, '422-123456-01-234' 	AS 계좌번호
		, '1234-5678-9012-4567' AS 카드번호
		, '홍길동' 				AS 성명
		, '02-1234-5678' 		AS 연락처
		, 'abcd1234@daum.net' 	AS 이메일
		, '서울시 강남구 논현동 110-1번지' 	AS 주소
	FROM DUAL UNION ALL
	SELECT 
		'891110-1234567' 		AS 주민번호
		, '801102-133333-2' 	AS 외국인번호
		, 'MU2345678' 			AS 여권번호
		, '경기 34-345678-34' 		AS 운전면허번호 
		, '1234-123-1234-56' 	AS 계좌번호
		, '1234-5678-9012-4567' AS 카드번호
		, 'Hong Gil Dong'		AS 성명
		, '010-1234-5678' 		AS 연락처
		, 'abcd12_31@daum.net' 	AS 이메일
		, '중구 을지로20길 42-1' 	AS 주소
	FROM DUAL UNION ALL
	SELECT 
		'901110-1234567' 		AS 주민번호
		, '801102-345672-3' 	AS 외국인번호
		, 'MC3456789' 			AS 여권번호
		, '부산 23-234567-23' 		AS 운전면허번호 
		, '123456-12-123456' 	AS 계좌번호
		, '1234-5678-9012-4567' AS 카드번호
		, '문새마로' 				AS 성명
		, '017-123-5678' 		AS 연락처
		, 'abc2d12@daum.net' 	AS 이메일
		, '태양계 지구 대한민국동 14번지' 	AS 주소
	FROM DUAL 
)
SELECT 
	주민번호
	, SUBSTR(주민번호, 1,8) || '******' AS "후-주민번호"
	, 외국인번호
	, REGEXP_REPLACE(외국인번호, '(\d{6}\-)(\d{6})(\-\d)', '\1******\3' ) AS "후-외국인번호"
	, 여권번호
	, REGEXP_REPLACE(여권번호, '(\D)(\D)(\d{4})(\d{3})', '\1*\3***' ) AS "후-여권번호"
	, 운전면허번호
	, REGEXP_REPLACE(운전면허번호, '(\D{2}\s\d)(\d)(\-\d{4})(.{4})(\d)', '\1*\3**-*\5') AS "후-운전면허번호"
	, 계좌번호
	, CASE WHEN REGEXP_INSTR(계좌번호, '(\d{3}-\d{4})(\d{2})(-\d{2}-)(\d{3})') = 1 THEN REGEXP_REPLACE(계좌번호, '(\d{3}-\d{4})(\d{2})(-\d{2}-)(\d{3})', '\1**\3***') 
		WHEN INSTR(SUBSTR(계좌번호, -5), '-') > 0 THEN SUBSTR(계좌번호, 1, LENGTH(계좌번호)-6) || REGEXP_REPLACE(SUBSTR(계좌번호, -6), '\d', '*',1,0)
		ELSE REGEXP_REPLACE(계좌번호, '\d{5}$', '*****') 
	END AS "후-계좌번호"
	, 성명
	, CASE WHEN INSTR(성명, ' ') = 0 THEN REGEXP_REPLACE(성명, '.', '*', 2, 1)
		ELSE REGEXP_REPLACE(성명, '\s(.+)\s', ' * ', 1, 1)
	END AS "후-성명"
	, 연락처
	, REGEXP_REPLACE(연락처, '\-(\d{3,4})\-', '-****-') AS "후-연락처"
	, 이메일
	, SUBSTR(이메일, 1,2) || RPAD('*', INSTR(이메일, '@')-3 ,'*') || SUBSTR(이메일, INSTR(이메일, '@') ) AS "후-이메일"
	, 주소
	, REGEXP_REPLACE(주소, '(^.+\s)(.+)([동길])(\s[\d\-]+)(번?지?)$', '\1 ***\3 ***\5') AS "후-주소"
FROM T
;
```



