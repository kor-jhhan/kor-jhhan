## mysql 사용
### # Join 관련 힌트
 - JOIN_FIXED_ORDER: FROM절에 지정된 순서대로(FIXED) 테이블을 조인하도록 지시 (STRAIGHT_JOIN의 힌트화)
 - JOIN_ORDER: 가능하다면 힌트에 지정된 순서대로 조인하도록 지시
 - JOIN_PREFIX: 가장 먼저 조인을 시작 할 테이블 지정
 - JOIN_SUFFIX: 가장 마지막으로 조인 할 테이블 지정

```sql
--TABLE_NAME에 별칭이 있는 경우 힌트는 테이블 이름이 아니라 별칭을 참조
--t2, t5@subq2, t4@subq1, t3, t1 순서대로 조인
SELECT
/*+ JOIN_PREFIX(t2, t5@subq2, t4@subq1)
    JOIN_ORDER(t4@subq1, t3)
    JOIN_SUFFIX(t1) */
COUNT(*) 
FROM t1 JOIN t2 JOIN t3
WHERE t1.f1 IN 
    (SELECT /*+ QB_NAME(subq1) */ f1 FROM t4) -- 쿼리 블록의 이름을 subq1로 지정
  AND t2.f1 IN 
    (SELECT /*+ QB_NAME(subq2) */ f1 FROM t5) 
;
```

### # Table 관련 힌트
 - USE 키워드 : 특정 인덱스를 사용하도록 권장
 - IGNORE 키워드 : 특정 인덱스를 사용하지 않도록 지정
 - FORCE 키워드 : USE 키워드와 동일한 기능을 하지만, 옵티마이저에게 보다 강하게 해당 인덱스를 사용하도록 권장
 - USE INDEX FOR JOIN : JOIN 키워드는 테이블간 조인뿐 아니라 레코드 검색하는 용도까지 포함
 - USE INDEX FOR ORDER BY : 명시된 인덱스를 ORDER BY 용도로만 사용하도록 제한
 - USE INDEX FOR GROUP BY : 명시된 인덱스를 GROUP BY 용도로만 사용하도록 제한

```sql
SELECT * 
FROM TABLE1 
  USE INDEX (COL1_INDEX, COL2_INDEX) -- where 조건시  col1_index, col2_index 사용 권장
WHERE COL1=1 AND COL2=2 AND COL3=3;

SELECT * 
FROM TABLE2 
  IGNORE INDEX (COL1_INDEX) -- where 조건시 col1_index 비권장
WHERE COL1=1 AND COL2=2 AND COL3=3;

SELECT * 
FROM TABLE3
  USE INDEX (COL1_INDEX) --where 조건시 col1_index 권장
  IGNORE INDEX (COL2_INDEX) FOR ORDER BY -- 정렬시 col2_index 비권장
  IGNORE INDEX (COL3_INDEX) FOR GROUP BY -- 그룹시 col3_index 비권장
WHERE COL1=1 AND COL2=2 AND COL3=3;
```

### # 실행계획 보는방법 ('EXPLAN')
MySQL에서는 실행할 쿼리문 앞에 'EXPLAN' 키워드를 이용해 실행계획
```sql
EXPLAIN SELECT * FROM short_url su LEFT OUTER JOIN short_url_stat sus ON su.hash = sus.hash WHERE deleted_date IS NULL;
```
![explan](./explan.png)

- id : 쿼리 내의 select 문의 실행 순서
- select_type : select 문의 유형입니다.
    - SIMPLE: 단순 select ( union이나 서브쿼리를 사용하지 않음 )
    - PRIMARY: 가장 외곽에 있는 select문
    - UNION: union에서의 두번째 혹은 나중에 따라오는 select문
    - DEPENDENT UNION: union에서의 두번째 혹은 나중에 따라오는 select문, 외곽 쿼리에 의존적이다.
    - UNION RESULT: union의 결과물
    - SUBQUERY: 서브쿼리의 첫번째 select
    - DEPENDENT SUBQUERY: 서브쿼리의 첫번째 select, 바깥 쪽 쿼리에 의존적이다.
    - DERIVED: from절의 서브쿼리
- table : 참조 테이블명
- type : 조인타입이며 쿼리 성능과 아주 밀접한 항목입니다. 아래 항목들 중에서 밑으로 갈 수록 안 좋은 쿼리형태입니다.
    1. system : 테이블에 단 하나의 행만 존재(=시스템 테이블). const 조인의 특별한 형태이다.
    2. const : 하나의 매치되는 행만 존재하는 경우. 하나의 행이기 때문에 상수로 간주되며, 한번만 읽어들이기 때문에 무척 빠르다.
    3. eq_ref : 조인수행을 위해 각 테이블에서 하나의 행만이 읽혀지는 형태. const 타입 외에 가장 훌륭한 조인타입이다.
    4. ref : ref조인에서 키의 가장 왼쪽 접두사 만 사용하거나 키가 a PRIMARY KEY또는 UNIQUE인덱스 가 아닌 경우 (즉, 조인이 키 값을 기반으로 단일 행을 선택할 수없는 경우) 사용된다. 사용되는 키가 몇 개의 행과 만 일치하는 경우 이는 좋은 조인 유형이다.
    5. fulltext : fulltext 색인을 사용하여 수행된다.
    6. ref_or_null : 이 조인 유형은 비슷 ref하지만 MySQL이 NULL값 을 포함하는 행을 추가로 검색한다는 점이 다르다. 이 조인 유형 최적화는 하위 쿼리를 해결하는 데 가장 자주 사용된다.
    7. index_merge : 인덱스 병합 최적화가 적용되는 조인타입. 이 경우, key컬럼은 사용된 인덱스의 리스트를 나타내며 key_len 컬럼은 사용된 인덱스중 가장 긴 key명을 나타낸다.
    8. range : 인덱스를 사용하여 주어진 범위 내의 행들만 추출된다. key 컬럼은 사용된 인덱스를 나타내고 key_len은 사용된 가장 긴 key부분을 나타낸다. ref 컬럼은 이 타입의 조인에서 NULL 이다. range 타입은 키 컬럼이 상수와 =, <>, >, >=, <, <=, IS NULL, <=>, BETWEEN 또는 IN 연산에 사용될때 적용된다.
    9. index : 이 타입은 인덱스가 스캔되는걸 제외하면 ALL과 같다. 보통 인덱스 파일이 데이터 파일보다 작기 때문에 ALL보다 빠르다.
    10. ALL : 이전 테이블과의 조인을 위해 풀스캔이 된다. 만약 조인에 쓰인 첫 테이블이 고정이 아니라면 비효율적이다. 그리고 대부분의 경우 아주 느리며, 보통 상수값이나 상수인 컬럼값으로 row를 추출하도록 인덱스를 추가하여 ALL 타입을 피할 수 있다.
- possible_keys : MySQL이 해당 테이블의 검색에 사용할 수 있는 인덱스들을 나타냅니다.
- key : MySQL이 실제 사용한 key나 인덱스를 나타냅니다.
- key_len : MySQL이 사용한 인덱스의 길이를 나타낸다. key 컬럼의 값이 NULL이면 이 컬럼의 값도 NULL입니다.
- ref : 행을 추출하는 데 키와 함께 사용 된 컬럼이나 상수값을 나타냅니다.
- rows : 이 값은 쿼리 수행에서 MySQL이 찾아야하는 데이터행 수의 예상값을 나타냅니다. 추정 수치이며 항상 정확하지 않다.
- filtered : filetered열에 나타난 조건에 의해 필터링 될 테이블 행의 예상 비율을 나타낸다. 즉 rows는 검사 된 행 수를 나타내고 rows * filtered / 100은 이전 테이블과 조인 될 행 수를 표시합니다.
- Extra : MySQL이 이 쿼리를 어떻게 해석하는 지에 대한 추가 정보가 들어있습니다. 