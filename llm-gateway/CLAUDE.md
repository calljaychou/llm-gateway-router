# CLAUDE.md

本文件用于指导agent处理本代码仓库中的代码。

## 技术栈

后端： Java 8 + Kotlin 1.7.0、Spring Boot、Spring Security、Spring Webflux
数据库： MySQL + MyBatis 3.5.11
缓存： Redis + Redisson 3.16.8
前端： React 18 + Vite 5 + TypeScript（管理后台）、Ant Design UI 5.29.2（管理后台）

## 架构概览

### 后端结构

后端为多语言 Spring Boot 应用，同时使用 Java 与 Kotlin：

```
llm-gateway/src/main/
├── java/              # Java entry point & Youzan extension wrappers
│   └── com/llm/gateway/
implementations
└── kotlin/            # Kotlin source code (primary business logic)
    └── com/llm/gateway/
        ├── controller/                        # REST endpoints
        │   ├── admin/                        # Admin-only endpoints
        │   ├── client/                       # Client-facing endpoints
        │   ├── open/                         # Open API endpoints
        │   ├── script/                       # Script controllers
        │   └── task/                         # Scheduled task endpoints
        ├── service/                          # Business logic layer
        ├── dal/                              # Data Access Layer
        │   ├── mapper/                       # MyBatis mappers
        │   └── model/                        # Database entities
        └── common/                           # Utilities and configuration
```

## 关键配置文件

- `/pom.xml` - Root Maven POM with dependency management
- `/llm-gateway/pom.xml` - Web module dependencies
- `/llm-gateway/src/main/resources/generatorConfig.xml` - MyBatis generator configuration

