# Projeto WEB — OficinaFácil (Spring Boot)

Sistema web de gestão de oficina mecânica desenvolvido com Java e Spring Boot | Auto repair shop management web application built with Java and Spring Boot.

---

## 🇧🇷 Sobre o projeto

O **OficinaFácil** é uma aplicação web completa para gestão de oficinas mecânicas, desenvolvida para a disciplina de Backend. Permite o cadastro e controlo de clientes, veículos, serviços e ordens de serviço, além de contar com um painel gerencial interativo.

O sistema possui layout responsivo (computador e celular), persistência com JPA/Hibernate, autenticação segura com perfis de acesso (gerente e atendente), validação em múltiplas camadas e tratamento de concorrência.

### ✨ Funcionalidades
* **Gestão de Clientes:** Cadastro, edição, exclusão e busca rápida por nome com validação de CPF.
* **Gestão de Veículos:** Registo de veículos por cliente com suporte a placas antigas e padrão Mercosul.
* **Catálogo de Serviços:** Definição de preços e situação (ativo/inativo) gerida exclusivamente por gerentes.
* **Ordens de Serviço (OS):** Abertura de ordens com problema relatado, adição de serviços com cálculo automático de totais, controlo de fluxo de status e restrições de negócio.
* **Painel Gerencial:** Visualização de ordens por status, faturamento exclusivo para gerentes e serviços mais solicitados.
* **Segurança Avançada:** Autenticação com perfis, proteção CSRF, bloqueio contra força bruta e tratamento de concorrência com `@Version`.

---

## 🛠️ Tecnologias utilizadas

* **Java 17+**
* **Spring Boot 3.5.16** (Spring MVC, Spring Data JPA, Spring Security, Bean Validation)
* **Thymeleaf** (renderização de páginas HTML no servidor)
* **H2 Database** (em arquivo, modo padrão) / **PostgreSQL** (opcional via Docker)
* **HTML5 & CSS3** (responsivo, sem frameworks externos de front-end)
* **JUnit 5** (testes automatizados de unidade, negócio e integração)
* **Maven / Maven Wrapper**

---

## 🇺🇸 About the project

**OficinaFácil** is a full-featured web application for auto repair shop management, built with Java and Spring Boot for a backend academic course. It allows registration and control of clients, vehicles, services, and work orders, featuring an interactive management dashboard.

The system includes a responsive layout (desktop and mobile), JPA/Hibernate persistence, secure role-based authentication (manager and attendant), multi-layer validation, and concurrency handling.

### ✨ Features
* **Client Management:** Registration, editing, deletion, and fast search by name with CPF validation.
* **Vehicle Management:** Vehicle registration per client with support for old plates and Mercosul standard.
* **Service Catalog:** Pricing and status management handled exclusively by managers.
* **Work Orders (OS):** Creation of orders with reported issues, adding services with automatic total calculations, status workflow control, and business rules.
* **Management Dashboard:** Orders by status, manager-exclusive revenue tracking, and top-requested services.
* **Advanced Security:** Role-based authentication, CSRF protection, brute-force blocking, and concurrency control using `@Version`.

---

## 🎯 Objetivo | Purpose

Desenvolver uma aplicação web robusta com Spring Boot aplicando padrões arquiteturais de mercado, segurança avançada, testes automatizados e regras de negócio complexas. / Develop a robust Spring Boot web application applying industry architectural patterns, advanced security, automated tests, and complex business rules.

---

## ⚙️ Como rodar o projeto / How to run

### Pré-requisitos / Prerequisites
* **JDK 17 ou superior** (`java -version`)
* **VS Code** com extensões de Java e Spring Boot (opcional)
* **Git**

### Passos para execução / Execution steps
1. Abra a pasta raiz do projeto no seu terminal ou editor.
2. Execute o comando do Maven Wrapper:
   ```bash
   # Linux / macOS
   ./mvnw spring-boot:run

   # Windows (PowerShell ou CMD)
   .\mvnw.cmd spring-boot:run
