# sb-jwt-v1

## == v1 sql==
table : user
user_pk
id
username
password
salt
roles
refresh_token
created_at
updated_at

## == v2 메커니즘==
기존의 v1에서는 refresh 토큰이 하나만 발급이 되어서 여러 아이피 혹은 여러 브라우저에서 같은 유저로 접근하게되면 기존에 로그인되어있던 유저는 액세스 토큰이 만료가되면 자동으로 로그아웃이 된것처럼 처리가 된다. 

그러나 v2에서는 refresh 토큰을 여러개 발급할수 있게하여서 N개의 아이피 혹은 브라우저로 로그인 접근이 가능하게 하는것이 목적이다.

#### DB TABLES
table: user | (default columns) => user_pk, id, username, password, salt, roles, allowed_access_count, updated_at, created_at

table: refresh_token | (default columns) => refresh_token_pk, id, user_id, refresh_token, created_at, updated_at
