# Colorgram
> 자바 웹 개발자 국비지원 과정 팀 프로젝트
> 팀 **자벤져스**의 협업으로 완성된 컬러 기반 이미지 갤러리 웹 애플리케이션
---
## 팀 소개 - 자벤져스
|  이름  |          역할            |          GitHub 링크           |
|--------|--------------------------|--------------------------------|
| 고강찬 | 팀장, 음악 플레이어 개발 | https://github.com/bushwick97  |
| 이승원 |       게시판 개발        | https://github.com/tinytinalee |
| 엄지원 |    회원관리/댓글 개발    | https://github.com/umg1        |
| 최산하 |        채팅 개발         | https://github.com/sanaxa      |
---
## 프로젝트 기간
- **2025.03.01 ~ 2025.04.05** (총 5주)
---
## 프로젝트 개요
### **Colorgram**은 사용자가 이미지를 업로드하면 주요 색상을 추출하고,
### 해당 색상 기반으로 이미지를 분류하여 갤러리 형태로 볼 수 있는 웹 서비스입니다.
---
## 사용 기술 스택
### Backend
- Java 11
- Spring Boot
- Spring Security
- Spring Data JPA
- MySQL
### Frontend
- HTML/CSS/JavaScript
- Thymeleaf
- Bootstrap
### 협업 & 배포
- Git & GitHub
- Notion, Figma
- AWS EC2
- Docker
---
## 주요 기능
- [x] 회원가입 / 로그인 / 로그아웃
- [x] 이미지 업로드 및 색상 추출
- [x] 갤러리 뷰 및 색상 필터링
- [x] 관리자 페이지
- [x] 반응형 웹 UI
---
## 서비스 화면
> 여기에 이미지 캡처 추가
> 예:
> ![메인화면](images/main_page.png)
> ![컬러추출](images/color_extraction.png)
---
## 폴더 구조
```bash
:파일_폴더: colorgram
├── src
│   ├── main
│   │   ├── java/com/colorgram
│   │   │   ├── controller
│   │   │   ├── service
│   │   │   ├── repository
│   │   │   └── domain
│   │   └── resources
│   │       ├── templates
│   │       ├── static
│   │       └── application.yml





