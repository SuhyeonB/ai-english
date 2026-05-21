# AI English — AI 기반 영어 회화 학습 플랫폼

> Spring Boot 백엔드 포트폴리오 프로젝트  
> Google OAuth2 로그인 · OpenAI 실시간 스트리밍 · 자동 피드백 · ELK + Spring Batch 분석

---

## 목차

- [프로젝트 소개](#프로젝트-소개)
- [기술 스택](#기술-스택)
- [아키텍처 개요](#아키텍처-개요)
- [ERD](#erd)
- [API 명세](#api-명세)
- [핵심 구현 포인트](#핵심-구현-포인트)
- [브랜치 전략 및 Git 관리](#브랜치-전략-및-git-관리)
- [실행 방법](#실행-방법)

---

## 프로젝트 소개

**AI English**는 OpenAI GPT 모델과 실시간 대화를 나누고, 대화 종료 후 문법·어휘·유창성 등 세부 항목별 피드백을 자동으로 받을 수 있는 영어 회화 학습 백엔드입니다.

단순한 기능 구현보다 **설계 의도를 명확히 설명할 수 있는 코드**를 목표로 처음부터 다시 설계한 프로젝트입니다.

---

## 기술 스택

| 분류 | 기술 |
|---|---|
| Language / Framework | Java 17, Spring Boot 3.x |
| Auth | Spring Security, OAuth2 (Google), JWT |
| Database | MySQL 8, Spring Data JPA |
| Cache / Session | Redis (RefreshToken, 대화 히스토리) |
| AI | OpenAI Chat Completions API, WebClient, SSE |
| Batch | Spring Batch |
| Logging / Monitoring | ELK Stack (Elasticsearch, Logstash, Kibana) |
| Build | Gradle |

---

## 아키텍처 개요

```
Client
  │
  ├─ POST /api/v1/auth/google          → Google OAuth2 리다이렉트
  ├─ POST /api/v1/conversations        → 대화 세션 생성
  ├─ POST /api/v1/conversations/{id}/messages  → 메시지 전송 (SSE 스트리밍)
  └─ POST /api/v1/conversations/{id}/end      → 세션 종료 → 피드백 생성

[Auth Flow]
  Google OAuth2 → JWT 발급 (AccessToken + RefreshToken)
  RefreshToken → Redis 저장 (TTL 관리)
  AccessToken 만료 시 → /auth/reissue 로 재발급

[Conversation Flow]
  세션 생성 → Redis에 대화 히스토리 적재
  메시지 수신 → OpenAI API 호출 → SSE로 실시간 스트리밍
  세션 종료 → RDB 영구 저장 → 피드백 생성 요청

[Feedback Flow]
  대화 내역 전달 → OpenAI (chat, 동기) → 항목별 점수 파싱 → DB 저장

[Batch / ELK]
  Spring Batch → 매주 월요일 자정 WeeklyReportJob 실행
    - 지난 주 FeedbackReport 집계 (유저별 평균 점수, 세션 수, 출석일, 약점 영역)
    - JpaPagingItemReader → ItemProcessor → ItemWriter(로그) 구조
  ELK → 애플리케이션 로그 수집·시각화
    - Logback → Logstash(TCP 5000) → Elasticsearch → Kibana
    - 인덱스 패턴: ai-english-logs-{날짜}
```

---

## ERD

<img width="2000" height="776" alt="image" src="https://github.com/user-attachments/assets/fa727e69-276c-4552-ba7d-7242f1c71ad9" />


| 테이블 | 설명 |
|---|---|
| `users` | 회원 정보 (닉네임, 레벨, 연속 학습일 등) |
| `oauth_accounts` | OAuth 제공자별 계정 연결 정보 |
| `conversation_sessions` | 대화 세션 (상태, 시작/종료 시간, 메시지 수) |
| `conversation_messages` | 세션별 메시지 (USER / ASSISTANT 구분) |
| `feedback_reports` | 세션 종료 후 생성되는 피드백 (항목별 점수, 요약) |
| `feedback_errors` | 피드백 내 오류 표현 (오류 유형, 원문, 교정 표현, 설명) |
| `feedback_vocabularies` | 피드백 내 추천 어휘 (단어, 뜻, 예문) |
| `user_weakness_stats` | 누적 오류 유형 통계 |
| `user_goals` | 학습 목표 (일별 학습 시간, 월별 세션 수 등) |
| `user_badges` | 유저별 획득 뱃지 |
| `badges` | 뱃지 정의 (조건, 이름) |

---

## API 명세

### Auth

| 기능 | Method | URL | 인증 |
|---|---|---|---|
| Google 로그인 | GET | `/api/v1/auth/google` | - |
| Google 콜백 | GET | `/api/v1/auth/google/callback` | - |
| 토큰 재발급 | POST | `/api/v1/auth/reissue` | RefreshToken |
| 로그아웃 | POST | `/api/v1/auth/logout` | AccessToken |

### Conversation

| 기능 | Method | URL | 인증 |
|---|---|---|---|
| 대화 세션 시작 | POST | `/api/v1/conversations` | AccessToken |
| 메시지 전송 (SSE) | POST | `/api/v1/conversations/{sessionId}/messages` | AccessToken |
| 대화 세션 종료 | POST | `/api/v1/conversations/{sessionId}/end` | AccessToken |
| 세션 목록 조회 | GET | `/api/v1/conversations` | AccessToken |
| 세션 상세 조회 | GET | `/api/v1/conversations/{sessionId}` | AccessToken |

### Feedback

| 기능 | Method | URL | 인증 |
|---|---|---|---|
| 피드백 조회 | GET | `/api/v1/feedbacks/{feedbackId}` | AccessToken |

**피드백 응답 항목:** `overallScore`, `grammarScore`, `vocabularyScore`, `pronunciationScore`, `fluencyScore`, `summary`, `strengths`, `errors`, `vocabularies`

### MyPage

| 기능 | Method | URL | 인증 |
|---|---|---|---|
| 내 프로필 조회 | GET | `/api/v1/users/me` | AccessToken |
| 홈 통계 조회 | GET | `/api/v1/users/me/stats` | AccessToken |
| 누적 약점 조회 | GET | `/api/v1/users/me/weakness` | AccessToken |
| 주간 성적 조회 | GET | `/api/v1/users/me/progress` | AccessToken |
| 학습 목표 조회 | GET | `/api/v1/users/me/goals` | AccessToken |
| 학습 목표 설정 | POST | `/api/v1/users/me/goals` | AccessToken |
| 뱃지 목록 조회 | GET | `/api/v1/users/me/badges` | AccessToken |
|내 프로필 수정 | PATCH | /api/v1/users/me | AccessToken|

---

## 핵심 구현 포인트

### 1. OAuth2 + JWT + Redis 인증 구조

Google OAuth2로 로그인한 뒤 AccessToken(단기)과 RefreshToken(장기)을 발급합니다. RefreshToken은 Redis에 저장하여 만료·무효화를 관리하고, `/auth/reissue`로 AccessToken을 재발급합니다.

Spring Boot의 Redis 자동 설정(`spring.data.redis.*`)을 활용하여 별도 `RedisConfig`를 두지 않고 `StringRedisTemplate`을 바로 주입하는 방식을 택했습니다.

### 2. OpenAI SSE 스트리밍

클라이언트→서버는 일반 HTTP POST, 서버→클라이언트만 스트리밍이 필요하기 때문에 WebSocket 대신 **SSE(Server-Sent Events)**를 선택했습니다.

`WebClient`로 OpenAI Chat Completions API를 `stream: true`로 호출하고, `bodyToFlux(String.class)`로 청크를 수신하여 `text/event-stream`으로 클라이언트에 전달합니다.

`OpenAiService`는 `chat()` (동기, 피드백용)과 `stream()` (Flux, 대화용)으로 분리하여 두 용도를 명확히 구분했습니다.

### 3. Redis 대화 히스토리 관리

대화 중 메시지는 실시간으로 RDB에 저장하고, OpenAI API 호출에 필요한 대화 히스토리는 Redis에 JSON 직렬화 문자열로 별도 관리합니다. 세션 종료 시 Redis 히스토리를 삭제하고, RDB에 영구 보존된 메시지를 피드백 생성에 활용합니다.

### 4. Spring Batch 일별 집계

매주 월요일 자정 WeeklyReportJob이 실행됩니다. JpaPagingItemReader로 지난 주 FeedbackReport를 유저별로 집계하고, ItemProcessor에서 출석일 수 계산 및 약점 영역 판별 후 WeeklyReportDto로 변환합니다. GoalCheck와 Badge 지급은 즉각적인 반응이 필요한 기능이므로 Batch가 아닌 세션 종료 시점에 즉시 처리합니다.

### 5. ELK 로그 수집

logstash-logback-encoder를 통해 Spring Boot 로그를 JSON 형식으로 Logstash(TCP 5001)에 전송합니다. Logstash는 app 필드를 태깅하여 Elasticsearch에 ai-english-logs-{날짜} 인덱스로 저장하고, Kibana Discover에서 레벨·logger·메시지 기준으로 실시간 조회할 수 있습니다.

---

## 브랜치 전략 및 Git 관리

```
main          ← 배포용 (PR merge만 허용)
develop       ← 통합 브랜치
feat/*        ← 기능 단위 브랜치
  feat/auth
  feat/conversation
  feat/feedback
  feat/mypage
  feat/elk-batch
```

커밋 컨벤션: `feat:`, `fix:`, `refactor:`, `docs:`, `test:`, `chore:`

---

## 실행 방법

```bash
# 환경 변수 설정 (.env 또는 application-local.yml)
GOOGLE_CLIENT_ID=...
GOOGLE_CLIENT_SECRET=...
OPENAI_API_KEY=...
REDIS_HOST=localhost
REDIS_PORT=6379
DB_URL=jdbc:mysql://localhost:3306/ai_english

# 실행
./gradlew bootRun
```
---
## 트러블 슈팅
### 🔒JwtAuthentication에서 UserRepository를 호출하는 이유
https://coral-bonsai-6fd.notion.site/JwtAuthentication-UserRepository-341fe68bada980b1b16fed2e26fce6d1?source=copy_link
### 💬Spring Boot + Spring Security + SSE 스트리밍을 구현하면서 겪은 세 가지 문제
https://coral-bonsai-6fd.notion.site/Spring-Boot-Spring-Security-SSE-346fe68bada980f697d3ebfb5350b064?source=copy_link
