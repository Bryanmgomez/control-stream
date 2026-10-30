# Kick Stream Monitor

Aplicación Android (Java) + backend Spring Boot para monitorear transmisiones en vivo de Kick (canal bryanmg83).

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
