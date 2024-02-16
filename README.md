# 오늘도 커뮤

## [개요]
![logo](https://github.com/jsungl/spring-community/assets/79460509/1691744b-58be-4e18-9590-5984eeb765ef)

### 프로젝트 소개 및 목적
**‘오늘도 커뮤’** 는 스프링부트 프레임워크 기반의 JPA를 활용한 커뮤니티 게시판 프로젝트로, 사용자들에게 다양한 정보 공유를 위한 효율적인 플랫폼을 제공한다. 이번 프로젝트에서 JPA를 사용한 이유는 이전에 만들었던 스프링부트 기반의 간단한 게시판 프로젝트에서의 복잡한 CRUD 작업의 반복적인 코드를 최소화하고, 보다 간결한 방식으로 데이터베이스 데이터를 조작하도록 하기 때문이다. 그리고 JPA를 활용하면 객체지향적인 코드 작성을 통해 데이터베이스 유지보수가 쉬워지고 내부적으로 쿼리를 작성해주므로 성능 향상의 여러 기능을 제공해준다.

**‘오늘도 커뮤’** 를 통해 사용자들은 다양한 주제에 대해 의견을 나누고 정보를 공유할 수 있으며, 커뮤니티 활동을 통해 상호작용하고 지식을 공유할 수 있는 환경을 제공한다. 게시판은 사용자들이 손쉽게 글을 작성하고 읽을 수 있는 기능을 제공하여, 다양한 관심사를 가진 사람들 간의 소통과 지식 공유를 촉진한다. 그래서 게시판을 통해 사람들 간의 연결과 정보 교류를 원활히 할 수 있는 온라인 공간을 만들어 나가는 것이 **‘오늘도 커뮤’** 의 목적이다.

## [주요기능]
- 스프링 시큐리티를 활용한 폼 로그인, OAuth 2.0 소셜 로그인(네이버, 구글)

- 회원가입 및 회원탈퇴, 회원정보 수정

- 이메일 인증을 통한 아이디,비밀번호 찾기

- 게시물 등록,수정,삭제

- 게시물 추천 및 댓글,대댓글 작성

- 게시물 제목,내용,작성자에 따른 검색

- 게시물 추천순, 조회순 정렬

- 댓글 실시간 알림

- 관리자의 회원정보 관리

- 관리자의 공지 등록,수정,삭제

- 관리자의 게시판 통계

## [개발환경]
### 백엔드 개발 환경
#### 개발언어
- Java 11
#### 시스템 구성 및 라이브러리
- Spring Boot 2.7.12
- Gradle
- IntelliJ
- Spring Data JPA
- Spring Security
- OAuth 2.0
- Java Mail
- WebFlux
- SSE

### 프론트엔드 개발 환경
- HTML5
- CSS3
- jQuery
- JavaScript
- BootStrap5
- Thymeleaf

### 데이터베이스
- MySQL 8.0이상

### 서버
- AWS EC2
- AWS RDS
- AWS S3

## [DB 설계]
![오늘도 커뮤 erd](https://github.com/jsungl/spring-community/assets/79460509/99f28b8c-da73-4e95-bad2-dfae306f7f5e)

## [실행화면]
<details>
<summary>메인 페이지</summary>

#### 홈
<img width="960" alt="메인 페이지" src="https://github.com/jsungl/spring-community/assets/79460509/48625c3d-a2b9-4efb-ab15-f5b3103cf49e">

#### 로그인
<img width="960" alt="로그인 페이지" src="https://github.com/jsungl/spring-community/assets/79460509/74baf5a6-5bd2-4d63-88ee-b8803529fff0">

#### 회원가입
<img width="960" alt="회원가입 페이지" src="https://github.com/jsungl/spring-community/assets/79460509/59a76825-443c-4df2-b78d-7cd7703a2ff3">

</details>
<details>
<summary>회원 관련</summary>

#### 회원 정보
<img width="960" alt="회원정보" src="https://github.com/jsungl/spring-community/assets/79460509/da923299-ad7c-47da-9ada-adb1c02a7887">

#### 내 게시물
<img width="960" alt="내 게시물" src="https://github.com/jsungl/spring-community/assets/79460509/076c131e-c54d-467a-9538-e3fd85d9795b">

#### 내 댓글
<img width="960" alt="내 댓글" src="https://github.com/jsungl/spring-community/assets/79460509/f669aa6b-59a6-4dd3-8f47-986fc6c86cef">

#### 아이디 찾기
<img width="960" alt="아이디 찾기" src="https://github.com/jsungl/spring-community/assets/79460509/589f140f-6c4f-4cff-999a-493d441f3684">

#### 비밀번호 찾기
<img width="960" alt="비밀번호 찾기" src="https://github.com/jsungl/spring-community/assets/79460509/593ef0c8-5a30-4dec-bbdb-a11c3b4ed4b1">

#### 실시간 알림
<img width="960" alt="알림" src="https://github.com/jsungl/spring-community/assets/79460509/e7e1303c-6aa0-47fd-94a9-3b19bc44b2ce">


</details>
<details>
<summary>게시물 관련</summary>

#### 게시물 검색
<img width="960" alt="게시물 검색" src="https://github.com/jsungl/spring-community/assets/79460509/4d7f22e2-4326-4538-b480-1e61a679ed8e">

#### 게시물 상세
<img width="960" alt="게시물 상세" src="https://github.com/jsungl/spring-community/assets/79460509/7d8e908d-7641-404a-a122-3b1f060fc21c">

#### 게시물 등록
<img width="960" alt="게시물 등록" src="https://github.com/jsungl/spring-community/assets/79460509/580e8b64-5cea-4c83-97f7-7506848274e9">

#### 게시물 수정
<img width="960" alt="게시물 수정" src="https://github.com/jsungl/spring-community/assets/79460509/87a72fc8-0769-4116-a646-d4fc10a76ba7">


</details>
<details>
<summary>댓글 관련</summary>

#### 댓글 등록
<img width="960" alt="댓글 등록" src="https://github.com/jsungl/spring-community/assets/79460509/53cb5a02-4ce6-4517-89a9-96b51962e2eb">
<img width="960" alt="댓글 등록2" src="https://github.com/jsungl/spring-community/assets/79460509/118be10a-f466-4f88-b292-3f820aa456fd">

#### 댓글 수정/삭제
<img width="960" alt="댓글 수정" src="https://github.com/jsungl/spring-community/assets/79460509/c9d74d4d-52bf-4deb-9d78-d7dbfea8bc65">


</details>
<details>
<summary>관리자 관련</summary>

#### 회원 목록
<img width="960" alt="회원 목록" src="https://github.com/jsungl/spring-community/assets/79460509/023ad742-e8c2-4624-b203-5b1d13d705e5">

#### 게시판 통계
![게시판 통계](https://github.com/jsungl/spring-community/assets/79460509/39bf6c8c-f4d8-40a9-8f21-a8728f4a0dca)


</details>

## [개발내용]
- [회원가입](https://morefromjs.notion.site/74344fedaef64eb58d630a0cacae623f)
- [로그인](https://morefromjs.notion.site/Spring-Security-Form-Login-5f80c85bd955419eb8139807eb4f79b9)
- [소셜(네이버/구글) 로그인](https://morefromjs.notion.site/Spring-Security-Spring-Security-OAuth2-SSR-97c89219c2dc421a8a06e6d8cae4581a)
- [아이디/비밀번호 찾기](https://morefromjs.notion.site/888d1232c5ed442a83e0bc5bc5b423d9)
- [게시물 등록](https://morefromjs.notion.site/6ad3e8a1783245269a9611ede26bac8a)
- [S3를 이용한 이미지 업로드](https://morefromjs.notion.site/AWS-S3-3ca156b35b584b82b8e038e74312796c)
- [게시물 수정/삭제](https://morefromjs.notion.site/3029afbf72664caf83e283baa72bfb51)
- [게시물 추천](https://morefromjs.notion.site/3e6a9eba454949b2abc50d074646279e)
- [게시물 검색](https://morefromjs.notion.site/64e06670c1564cc98c91518b0a5a8c26)
- [회원정보 수정](https://morefromjs.notion.site/c41bfe21248946d79561eccc8404194f)
- [회원 탈퇴](https://morefromjs.notion.site/ed59f6a596e54cdeba5dd2c78e4d611d)
- [댓글 등록](https://morefromjs.notion.site/d41c5f75d97348be8ff2dd0e3178d180)
- [댓글 수정/삭제](https://morefromjs.notion.site/37f56aeac4074a8cb1f8e1944c9b8267)
- [댓글 실시간 알림](https://morefromjs.notion.site/727b0d62bdbf4834a8c672c6d91fee05)
- [[관리자] 회원 관리](https://morefromjs.notion.site/4ed3c671413e4493b10a904da14dfac9)
- [[관리자] 게시판 통계](https://morefromjs.notion.site/16e81f4928d44b2cb59aff4db0a69af5)

그밖에 추가적인 기능이나 자세한 내용은 [블로그](https://morefromjs.notion.site/JPA-SpringBoot-733786ef555e42fabcccd349d89cc6f3)
에 기술

