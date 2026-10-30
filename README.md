# Kick Stream Monitor

[![Descargar APK](https://img.shields.io/badge/Descargar-APK-39FF14?style=for-the-badge&logo=android)](https://github.com/Bryanmgomez/control-stream/releases/latest)
[![Build](https://github.com/Bryanmgomez/control-stream/actions/workflows/android-build.yml/badge.svg)](https://github.com/Bryanmgomez/control-stream/actions/workflows/android-build.yml)

> Pulsa el botón verde de arriba para descargar el APK más reciente.

## Estructura
- `backend/` - Spring Boot 2.7 (Java 8). WebSocket crudo en `/stream`, STOMP en `/ws`, REST en `/api`.
- `android/` - App Android (minSdk 21).

## Backend
Requisitos: JDK 8+, Maven.
```
cd backend
mvn spring-boot:run
```
Servidor en `http://localhost:8080`. WebSocket: `ws://localhost:8080/stream`.

Configurar canal en `src/main/resources/application.yml` (`kick.channel`).

## Android
Abrir `android/` en Android Studio. Compilar. En el dispositivo/emulador, conectar a la URL del backend (emulador: `http://10.0.2.2:8080`).

## Funciones
- Chat en vivo
- Contador de espectadores + máximo
- Detección de nuevos seguidores
- Eventos de KICKs/donaciones
- Suscripciones y regalos

## Notas
Kick no expone todos los eventos de forma pública; el backend usa la API no oficial de Kick y Pusher. Los eventos pueden cambiar.
