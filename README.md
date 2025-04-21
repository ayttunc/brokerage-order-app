# Brokerage Order Management Service

A Spring Boot microservice that allows brokerage firm employees to create, list, and cancel stock orders on behalf of their customers. Orders are persisted in an H2 database and published/consumed asynchronously via Kafka for eventual processing and matching.

## ✨ Features
- **Order Management** – create, list by customer/date range, and cancel pending orders.
- **Asset Tracking** – real‑time asset balances and usable size per customer.
- **Role‑Based Security** – JWT authentication with ADMIN and CUSTOMER roles.
- **Asynchronous Processing** – orders are produced to *order-topic* and consumed for persistence.
- **Swagger / OpenAPI 3** – interactive API documentation out‑of‑the‑box.
- **In‑Memory H2 Database** – easy local development with console at `/h2-console`.
- **Test Coverage** – unit tests with JUnit 5 & Mockito.

## 🛠️ Tech Stack
| Layer            | Technology             |
|------------------|------------------------|
| Language         | Java 21               |
| Framework        | Spring Boot 3.x        |
| Data             | Spring Data JPA + H2   |
| Messaging        | Apache Kafka          |
| AuthN/AuthZ      | Spring Security + JWT |
| Docs             | springdoc‑openapi      |
| Build            | Maven 3.9.x           |

## 🚀 Getting Started

### Prerequisites
* JDK 21+
* Maven 3.9+
* Docker & Docker Compose (for Kafka)

### Clone & Build
```bash
git clone https://github.com/<your‑org>/brokerage-order-app.git
cd brokerage-order-app
mvn clean verify
```

### Run locally
```bash
# start Kafka & Zookeeper
docker compose -f infra/docker-compose-kafka.yml up -d

# run Spring Boot with dev profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

| URL                                  | Description                  |
|--------------------------------------|------------------------------|
| `http://localhost:8080/swagger-ui.html` | Swagger UI & API Explorer    |
| `http://localhost:8080/h2-console`      | H2 database console          |

The default **dev** profile disables security (see `DevTestSecurityConfig`) for easier exploration. Switch to **prod** profile to enable JWT & role checks.

### Configuration (prod profile)

| Property            | Description                           | Example           |
|---------------------|---------------------------------------|-------------------|
| `jwt.secret`        | HMAC signing key                      | `mySecretKey123`  |
| `jwt.expiration`    | Token TTL in ms                       | `3600000`         |
| `spring.kafka.*`    | Kafka bootstrap servers etc.          | see `application.yaml` |

## 🧬 Project Structure
```
├── config          # Spring @Configuration classes
├── controller      # REST controllers
├── dto             # DTOs for API & Kafka
├── entity          # JPA entities & enums
├── repository      # Spring Data repositories
├── service         # Business services
├── security        # JWT utils & filters
└── kafka           # Producer & consumer
```

## 📚 API Reference

After starting the application, navigate to **Swagger UI**. Endpoints include

* `POST /api/auth/login` – obtain JWT
* `POST /api/orders` – place an order
* `GET /api/orders` – list orders
* `DELETE /api/orders/{id}` – cancel pending order
* `GET /api/assets` – list assets

Full schema is generated from source annotations.

## 🗄️ Database Model
| Table  | Columns                                                              |
|--------|----------------------------------------------------------------------|
| assets | id, customer_id, asset_name, size, usable_size                       |
| orders | id, customer_id, asset_name, order_side, size, price, status, create_date |
| users  | id, username, password, roles                                        |

## 🧪 Tests
```bash
mvn test
```

## 🤝 Contributing
Pull requests are welcome! Please open an issue first to discuss changes.

## 📄 License
Distributed under the MIT License.  See `LICENSE` for more information.
