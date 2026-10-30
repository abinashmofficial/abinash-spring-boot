# Abinash Platform

> High-Performance Universal Application supporting all Java versions (Java 8, 11, 17, 21+).

## Overview
- **Name**: `abinash`
- **Version**: `1.0.0`
- **Group ID**: `com.abinash`
- **License**: Apache 2.0 (Published by Abinash)
- **Compatibility**: Java 8, Java 11, Java 17, Java 21, Java 25+

## Quick Start

### Build Universal Package
```bash
./mvnw clean package
```

### Run Application
```bash
./mvnw spring-boot:run
```

### Endpoints
- `GET /` -> Returns platform status and welcome payload
- `GET /api/greet?name=Abinash` -> Personalized greeting
