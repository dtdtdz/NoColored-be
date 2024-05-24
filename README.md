<h1 align="center">NoColored - Back-End</h1>
<img alt="게임 시작 화면" src="https://github.com/NoColored/.github/blob/main/profile/docs/images/main/view/landing_fullScreen.png" width="100%" height="100%"/>

-----
## 목차
- [프로젝트 소개](#-프로젝트-소개)
- [팀원 소개](#-팀원-소개)
- [역할](#-역할)
- [기술 스택](#-기술-스택)
- [ERD 다이어그램](#-erd-다이어그램)
- [폴더 구조 및 아키텍쳐 설계](#-폴더-구조-및-아키텍쳐-설계)


-----
## 🍀 프로젝트 소개
<br>

**숨바꼭질 대전 웹 게임 - NoColored**

#### main-service
- **숨바꼭질 대전 웹 게임**
- NoColored는 2~4인 경쟁 기반 2D 캐쥬얼 게임입니다.
- NPC 사이 숨은 플레이어를 찾아 점수를 획득하는 방식의 게임입니다.
- 경쟁전을 통해 유사한 성적의 플레이어들과 대전을 펼칠 수 있습니다.
- 비공개/공개방을 생성하여 친선전 플레이가 가능합니다.

#### sub-service

- 게임의 진행상황을 통해 스킨, 칭호, 업적 보상을 얻을 수 있습니다.
- 전체 플레이어의 랭킹을 제공하여 자신의 티어와 점수를 확인할 수 있습니다.

<p style="font-size: 0.8em; text-align: right">
  <a href="https://github.com/NoColored/.github/wiki/%F0%9F%8C%9E%EA%B8%B0%ED%9A%8D-%EB%B0%B0%EA%B2%BD"><b>기획 배경 바로 가기</b></a>
</p>


저희 NoColored의 Back-End 가 가장 중요시하게 생각했던 부분은 아래와 같습니다.
- 동시성 처리를 위한 정말 짧은 시간의 단일 스레드 스케쥴링
- 유저의 접속 상태를 효율적으로 관리하기 위한 유저 상태관리
- 많은 플레이어를 관리하기 위한 효율적인 부하점검
- 기본에 충실한 API 제작 및 관리

-----
## 🎯 팀원 소개
|손의성|                                             차우열                                             |
|:--:|:-------------------------------------------------------------------------------------------:|
|<img alt="gabalja" src="https://github.com/gabalja.png" width="230px" height="full" > | <img alt="dtdtdz" src="https://github.com/dtdtdz.png" width="230" height="100%"/> |

---

## 👨‍👩‍👧‍👦 역할

**차우열**
- Backend Leader
- phaser.js와 맞는 게임물리 구현
- 웹소켓을 사용한 매칭, 친선전, 게임 로직 설계
- Jmeter websocket 부하테스트
- 유저 접속 상태 추적 및 관리
- 웹소켓 binary message 설계

**손의성**
- Infra
- Dockerizing
- Jenkins를 사용한 CI/CD
- dns, ssl 설정
- AWS S3를 사용한 데이터 관리
- 랭킹 시스템 설계
- 칭호, 업적, 스킨 로직 구현

-----
## 👩‍💻 기술 스택
### language
- JAVA 17

### framework
- Spring Boot 3.2.3
- Spring JPA

### sub
- JWT

### database
- MariaDB 11.3
- MongoDB 7.0.7
- Redis 7.2

### ci/cd
- AWS EC2
- Jenkins
- Docker, Docker-compose

<p style="font-size: 0.8em; text-align: left">
  <a href="https://github.com/NoColored/NoColored-be/wiki/%E2%9C%A8-%EA%B8%B0%EC%88%A0-%EC%84%A0%ED%83%9D-%EC%9D%B4%EC%9C%A0"><b>* 기술 별 선택 이유 확인하기</b></a>
</p>



-----
## 💾 ERD 다이어그램
![ERD 다이어그램](https://github.com/NoColored/.github/blob/main/profile/docs/images/backend/nocolored_erd.png)




---
## 📂 폴더 구조 및 아키텍쳐 설계

```tree
├─assets
│  ├─dao
│  └─document
├─collection
│  ├─controller
│  ├─document
│  ├─dto
│  ├─repository
│  ├─service
│  └─util
├─config
├─game
│  ├─controller
│  ├─document
│  ├─domain
│  ├─dto
│  ├─service
│  ├─type
│  └─util
├─play
│  ├─controller
│  ├─domain
│  ├─dto
│  ├─service
│  └─util
├─rank
│  ├─controller
│  ├─document
│  ├─dto
│  ├─repository
│  ├─service
│  └─util
├─user
│  ├─controller
│  ├─dto
│  ├─entity
│  ├─repository
│  ├─service
│  └─util
└─websocket
    ├─domain
    ├─handler
    ├─service
    └─util
```
