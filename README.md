# Gym System - Backend (MVP)

This is a **modular monolith** starting point for our Gym SaaS backend.

## Stack
- **Java 21 (LTS)** – broadly supported in Spring Boot 3.5.x
- **Spring Boot 3.5.6**
- **Maven**
- **PostgreSQL + Flyway**
- **Spring Security + JWT**

## Modules (planned)
- Identity & Auth (this module)
- Booking (classes & schedules)
- Check-in integrations (Gympass, TotalPass, Direct)
- Documents (PDF uploads - S3/MinIO)
- Payments & Subscriptions
- Admin Console APIs

## Running locally
1. Provision PostgreSQL and create database `gymdb` with user/password `gym/gym` (or change in `application.yml`).
2. `cd gym-api`
3. `./mvnw spring-boot:run`

## First Endpoints
- `POST /api/v1/auth/signup` – body `{ email, password, signupToken }`
- `POST /api/v1/auth/login` – body `{ email, password }`

> Use an initial row in `signup_tokens` to allow the first user to sign up.
