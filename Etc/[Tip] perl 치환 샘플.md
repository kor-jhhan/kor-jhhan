perl -pe 's |("\|"\S+_)aaaa("\|_\S+")\s("\|"\S+_)aaaa("\|_[^,]+)|"change" "change"|g' ./test.txt

[정규표현시 문법 설명]
("\|"\S+_)aaaa("\|_\S+")\s("\|"\S+_)aaaa("\|_\S+) 
 > 이부분이 정규표현식

1. ("\|"\S+_)
 => aaaa 문자열 앞에는 " 또는{\|} 공백이 아닌{\S} 문자열이 1번이상{+}에 _ 문자가 붙는다.
 => "aaaa 인경우 또는 dddddd_ 인경우 를 한그룹으로 본다 ()
 
2. aaaa 
 => aaaa 문자열이 온다.

3. ("\|_\S+")
 => 3번째 그룹
 => aaaa 문자열 뒤에 " 또는 _ 문자 다음 공백이 아닌{\S} 문자열이 1번이상{+} 나오는것.
 => aaaa" 인경우 또는 _dddddd 인경우 를 한그룹으로 본다 ()
 
4. 1-2-3 이 반복되는 구조 사이에 공백이 있다. {\s}

5. ("\|_[^,]+)
 => 뒤에 Alias로 반복되는 컬럼명은 타켓 명칭 뒷부분의 표현식이 다르다 
 => 뒷 부분에선 공백이 아닌 ,를 기준으로 값이 끝이 난다.
 => 그래서 3번과 다르게 " 또는 _ 문자 다음 ,가 아닌{[^,]} 문자열이 1번이상{+} 나온다.



-----------------
[정규식 설명
CASE((?!CASE).)*END "abnm_jtm" 

1. CASE 로 시작된 문장
2. CASE 뒤에 CASE가 들어가거나 존재하지 않는 경우 