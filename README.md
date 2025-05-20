Version.1 merge complete

DB 스키마 통일화 및 데이터 생성시 스키마에 맞게끔 rule추가

member생성시 (기존 : name, role) -> (신규 : email, nickname, role)

[to-do]
1. task detail에서 파일 이랑 설명 넣을 수 있도록 수정
2. task chatting기능
3. MVC패턴으로 refactoring?


[done]
1. task 삭제 기능 O
2. tasklist header에 프로젝트명에 맞게 출력 O
3. project setting 추가 (이름, member추가[DB에 있는 user만 가능하게?])
4. project 삭제랑 setting은 owner email만 가능하게끔 설정
5. taskitem -> 자세히 보기 버튼을 task setting 버튼으로 변경, 담당자 밑에 deadline표시
6. add_task_acitivity, setting_task_activity(멤버, 이름 수정, 삭제), task_detail로 세분화
