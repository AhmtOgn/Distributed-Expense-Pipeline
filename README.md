# Distributed Employee Expense Pipeline

A full-stack distributed big data processing and serving pipeline designed to ingest real-time employee POS expense events, store high-throughput transactional logs in NoSQL, aggregate analytics via Apache Spark, and serve unified employee profiles through a Spring Boot web interface.

---

## System Architecture

The system is decoupled into two primary architectural layers: a **Streaming & Analytics Data Path** and a **Relational & Distributed Serving Layer**.

```text
                  [STREAMING & ANALYTICS DATA PATH]
  POS Simulator ──► Apache Kafka (4 Topics) ──► Apache Spark ──► Apache Cassandra
                    (accounting, research,                         (test.expenses)
                      sales, operations)                                  │
                                                                          ▼
  Web Browser ◄──► Spring Boot Application ◄─────────────────────── Spark SQL Engine
                            │         │                          (Total Expense Calc)
                            │         │
                            ▼         ▼
                    MySQL (`scott`)  Hadoop HDFS
                    (emp & dept)     (/user/images/*.jpg)
                  [RELATIONAL & DISTRIBUTED SERVING LAYER]
```

### Architectural Highlights
* **Topic-Based Partitioning (Kafka):** Expense streams are routed dynamically to department-specific Kafka topics (`accounting`, `research`, `sales`, `operations`).
* **Wide-Column Storage Optimization (Cassandra):** The `test.expenses` table is partitioned by `user_id` and clustered by `date_time ASC`, enabling $O(1)$ partition lookups when querying historical expenses for a specific employee.
* **Distributed Aggregation (Spark):** Instead of querying Cassandra directly from the web layer, Spring Boot delegates `SUM(payment)` aggregations to Apache Spark via the DataStax Spark-Cassandra Connector.
* **Hybrid Storage Serving (MySQL + HDFS):** Structured employee hierarchies (`SELF JOIN` for manager resolution) and department metadata reside in MySQL, while employee profile images are streamed directly from Hadoop HDFS.

---

## Tech Stack

| Layer | Technology | Version | Role |
| :--- | :--- | :--- | :--- |
| **Backend Framework** | Spring Boot (Java) | `2.7.18` (Java 11) | REST/MVC Serving, JPA, Service Orchestration |
| **Stream Processing** | Apache Spark (Core & SQL) | `3.4.1` (Scala 2.12) | Distributed Data Aggregation & Connector Runtime |
| **Message Broker** | Apache Kafka & ZooKeeper | `3.x` | Real-Time Event Ingestion (4 Department Topics) |
| **NoSQL Database** | Apache Cassandra | `4.x` (Java 11) | High-Throughput Time-Series Expense Storage |
| **Relational Database** | MySQL | `8.x` | Master Employee (`emp`) & Department (`dept`) Data |
| **Distributed Storage** | Apache Hadoop (HDFS/YARN) | `3.3.4` | Distributed Storage for Employee Avatars (`/user/images`) |

---

## Data Schemas

### 1. Apache Cassandra (`test.expenses`)
Designed for fast partition-level reads per employee without overwriting concurrent POS transactions:

```sql
CREATE KEYSPACE IF NOT EXISTS test 
WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'};

CREATE TABLE IF NOT EXISTS test.expenses (
    user_id int,
    date_time text,
    description text,
    type text,
    count int,
    payment float,
    PRIMARY KEY (user_id, date_time)
) WITH CLUSTERING ORDER BY (date_time ASC);
```

### 2. MySQL (`scott` Database)
Extended the classic `emp` and `dept` relational schema with an `img` column mapped to HDFS filenames (`smith.jpg`, `scott.jpg`, etc.):
* **`dept`:** `deptno (PK)`, `dname`, `loc`
* **`emp`:** `empno (PK)`, `ename`, `job`, `mgr (Self-FK)`, `hiredate`, `sal`, `comm`, `deptno (FK)`, `img`

---

## Getting Started

### Prerequisites
* Ubuntu / WSL2 environment with **OpenJDK 11** configured
* Running instances of **HDFS/YARN**, **ZooKeeper**, **Kafka**, **Apache Spark**, **MySQL**, and **Cassandra**

### 1. Start Infrastructure Services
```bash
# Hadoop HDFS & YARN
/usr/local/hadoop/sbin/start-dfs.sh
/usr/local/hadoop/sbin/start-yarn.sh

# Kafka & ZooKeeper
/usr/local/kafka/bin/zookeeper-server-start.sh -daemon /usr/local/kafka/config/zookeeper.properties
/usr/local/kafka/bin/kafka-server-start.sh -daemon /usr/local/kafka/config/server.properties

# Spark, MySQL & Cassandra
/usr/local/spark/sbin/start-all.sh
sudo com.example.pro3.service mysql start
sudo com.example.pro3.service cassandra start
```

### 2. Configure Application Properties
Update `src/main/resources/application.properties` with your local credentials:

```properties
server.port=8080
spring.datasource.url=jdbc:mysql://localhost:3306/scott?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD
spring.jpa.hibernate.ddl-auto=none

hdfs.uri=hdfs://localhost:9000
spark.cassandra.connection.host=127.0.0.1
```

### 3. Build & Run
```bash
mvn clean spring-boot:run
```

---

## Project Roadmap
- [x] Infrastructure provisioning (HDFS, Kafka 4 topics, Spark, Cassandra, MySQL)
- [x] Cassandra keyspace & wide-column schema setup via Spark SQL Catalog
- [x] MySQL `scott` schema migration (`img` mapping) & HDFS avatar verification
- [x] Spring Boot 2.7.18 + Spark-Cassandra Maven dependency isolation
- [ ] JPA Entities (`Emp`, `Dept`) with manager self-join & DTO layer
- [ ] HDFS image streaming com.example.pro3.service & Spark-Cassandra total expense com.example.pro3.service
- [ ] POS Simulator Kafka producer & Spark Structured Streaming consumer
- [ ] Web dashboard UI (`Image, Name, Manager, Department, Salary, Commission, Total Expense`)