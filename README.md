# 🚗 Sistema de Gestión de Vehículos – CarCare
---
# frontend
> **Estado:** En desarrollo  
> **Última actualización:** Octubre 2025  

---

## 📋 Índice

- [Descripción](#descripción)
- [Características](#características)
- [Arquitectura](#arquitectura)
- [Estructura del Proyecto](#estructura-del-proyecto)
- [Tecnologías](#tecnologías)
- [Requisitos](#requisitos)
- [Instalación y Configuración](#instalación-y-configuración)
- [Documentación de Código](#documentación-de-código)
  - [Models](#models)
  - [Network](#network)
  - [ViewModels](#viewmodels)
  - [Screens](#screens)
- [Tests](#tests)
- [Estado del Desarrollo](#estado-del-desarrollo)
- [Roadmap](#roadmap)
- [API Backend](#api-backend)
- [Contribución](#contribución)
- [Licencia](#licencia)
- [Contacto](#contacto)

---

## 📖 Descripción

CarCare es una aplicación desarrollada para la **EINA (Universidad de Zaragoza)** cuyo objetivo es la gestión integral de una flota de vehículos.  
Permite registrar, reservar y localizar vehículos, además de gestionar incidencias y usuarios de manera colaborativa.

---

## ✨ Características

### Implementadas ✅
- Autenticación (registro, login y persistencia de sesión)
- Gestión básica de vehículos
- Creación y visualización de reservas
- Navegación principal y barra inferior
- Visualización de mapas con **MapLibre**

### En desarrollo 🚧
- Sistema completo de incidencias
- Visualización de calendario de reservas
- Mapa interactivo con geolocalización en tiempo real
- Panel de administración y roles de usuario
- Sistema de notificaciones

---

## 🏗️ Arquitectura

El proyecto sigue el patrón **MVVM (Model-View-ViewModel)**.  
La comunicación entre capas se realiza con **Kotlin coroutines**, y el almacenamiento local con `SharedPreferences`.

### Frontend (Kotlin + Jetpack Compose)
```
frontend/
├── models/          # Modelos de datos y DTOs
├── network/         # Retrofit y endpoints API
├── viewmodels/      # Lógica de negocio y estados UI
└── screens/         # Composables (UI)
```

### Backend (Node.js + Express)
Estructura modular en carpetas `/routes`, `/controllers`, `/models`.  
*(Documentación en desarrollo)*

---

## 📁 Estructura del Proyecto

```
proyecto/
├── frontend/
│   └── src/main/java/eina/unizar/frontend/
│       ├── models/
│       ├── network/
│       ├── viewmodels/
│       └── screens/
└── backend/
    └── (Pendiente de documentar)
```

---

## 🛠️ Tecnologías

### Frontend
- Kotlin  
- Jetpack Compose  
- Retrofit  
- Coroutines & Flow  
- ViewModel / LiveData  
- Gson  
- MapLibre (para mapas interactivos)

### Backend
- Node.js  
- Express.js  
- MongoDB *(pendiente de integrar)*  

---

## 📋 Requisitos

- Android Studio Arctic Fox o superior  
- JDK 11+  
- Android SDK 26+  
- Gradle 7+  
- Node.js 18+ *(para el backend)*

---

## 🚀 Instalación y Configuración

### Frontend

1. **Clonar el repositorio**
```bash
git clone [URL_DEL_REPOSITORIO]
cd proyecto/frontend
```

2. **Configurar la URL del backend**
```kotlin
// En RetrofitClient.kt
private const val BASE_URL = "http://TU_IP:3000"
```

3. **Abrir y ejecutar**
- Abrir el proyecto en Android Studio  
- Sincronizar Gradle  
- Ejecutar en emulador o dispositivo físico  

---

## 📚 Documentación de Código

### 🧩 Models

Define las entidades principales de la app:

| Clase | Descripción |
|--------|-------------|
| `Usuario` | Datos del usuario (id, nombre, email, iniciales). |
| `Vehiculo` | Información de cada vehículo: tipo, modelo, matrícula, etc. |
| `TipoVehiculo` | Enum con tipos (Camión, Furgoneta, Coche, Moto, Otro) y sus colores/íconos personalizados. |
| `Reserva` | Representa una reserva con usuario, vehículo, fecha y tipo. |
| `TipoReserva` | Enum de tipos de reserva (`Trabajo`, `Personal`, `Urgente`, `Otro`). |
| `Incidencia` | Reporte de averías o problemas de un vehículo. |
| `TipoIncidencia` | Enum de tipos (`Avería`, `Accidente`, `Mantenimiento`, `Otro`). |
| `PrioridadIncidencia` | Enum de prioridad (`Alta`, `Media`, `Baja`) con colores. |
| `EstadoIncidencia` | Enum (`Activa`, `Resuelta`). |

---

### 🌐 Network

- **`ApiService.kt`**  
  Define endpoints REST para login, registro, vehículos, reservas, incidencias, etc.

- **`RetrofitClient.kt`**  
  Configura Retrofit con `GsonConverterFactory` y logging interceptor.

---

### 🧠 ViewModels

| ViewModel | Descripción |
|------------|-------------|
| `AuthViewModel` | Gestiona login, registro y persistencia de sesión. |
| `HomeViewModel` | Carga y gestiona los vehículos asociados al usuario. |
| `VehiculoViewModel` | Crea y registra nuevos vehículos en la base de datos. |

---

### 🖥️ Screens

Todas las pantallas están construidas con **Jetpack Compose**, siguiendo una estructura clara y reactiva.

| Pantalla | Descripción | Estado |
|-----------|--------------|--------|
| **PantallaPrincipal** | Pantalla inicial de bienvenida con el logo y acceso al flujo principal. | ✅ Completa |
| **AppNavigation** | Controla la navegación principal de la app. Gestiona `NavController` y barra inferior. | ✅ Completa |
| **HomeScreen / HomeScreenWrapper** | Pantalla principal tras iniciar sesión; muestra vehículos y accesos rápidos. | 🚧 En mejora |
| **DetalleVehiculoScreen** | Muestra detalles técnicos del vehículo y opciones para ver en mapa o añadir usuario. | 🚧 En desarrollo |
| **AddVehiculoScreen** | Formulario para añadir un vehículo (nombre, tipo, modelo, combustible, etc.). Incluye validaciones de campos. | 🚧 En desarrollo |
| **NuevaReservaScreen** | Crea nuevas reservas de vehículos con selección de fecha, hora y tipo. | 🚧 En desarrollo |
| **CalendarioScreen** | Vista mensual con reservas programadas y posibilidad de crear nuevas. | 🚧 En desarrollo |
| **IncidenciasScreen** | Gestiona incidencias activas y resueltas. Permite filtrar, crear y ver detalles. | 🚧 En desarrollo |
| **NuevaIncidenciaScreen** | Formulario para registrar una nueva incidencia asociada a un vehículo. | 🚧 En desarrollo |
| **UbicacionVehiculoScreen** | Muestra la ubicación del vehículo en un mapa (MapLibre) con acciones para centrar o abrir en Maps. | 🚧 En desarrollo |
| **PantallaEleccionInicio** | Selector entre iniciar sesión, registrarse o recuperar contraseña. | ✅ Funcional |
| **RegistroUsuarioScreen** | Formulario completo de registro con selector de fecha y validación de campos. | 🚧 En desarrollo |
| **BottomNavigationBar** | Barra inferior con pestañas (Inicio, Mapa, Reservas). | ✅ Completa |

---

## 🧪 Tests

Pendiente de implementación.

### Plan de pruebas

**Frontend**
- Unit tests para validación de modelos y lógica de ViewModels.  
- UI tests (Compose) para flujo de pantallas, navegación y manejo de errores.

**Backend**
- Pruebas de endpoints (Jest/Supertest).  
- Validación de autenticación, CRUD de vehículos y reservas.

---

## 🔄 Estado del Desarrollo

| Estado | Descripción |
|--------|--------------|
| ✅ Completo | Autenticación, modelos y capa de red |
| 🚧 En progreso | Screens y validaciones de formularios |
| ⏳ Pendiente | Backend, incidencias, notificaciones, tests |

---

## 🗺️ Roadmap

### Fase 1: MVP  
✔ Autenticación y gestión básica de vehículos  
✔ Creación de reservas  
✔ Navegación principal  

### Fase 2: Integración  
🚧 Incidencias  
🚧 Mapas y localización  

### Fase 3: Optimización  
⏳ Notificaciones, validaciones, y pruebas unitarias  

### Fase 4: Expansión  
⏳ Roles, administración, informes de uso  

---

## 🔌 API Backend

### Endpoints actuales

**Autenticación**
```
POST /usuario/sign-up
POST /usuario/sign-in
GET /usuario/obtenerNombreUsuario/{id}
```

**Vehículos**
```
POST /vehiculo/registrar
GET /vehiculo/obtenerVehiculos/{userId}
```

**Reservas**
```
POST /reserva
```

*(Documentación detallada en progreso)*

---

## 🤝 Contribución

Pendiente de definir flujo de contribución y estilo de commits.

---

## 📄 Licencia

Pendiente de definir licencia pública (MIT, GPL o institucional).

---

## 📞 Contacto

**Universidad de Zaragoza – EINA**  
Desarrollo por estudiantes de Ingeniería Informática  
📅 Octubre 2025  
🔖 Proyecto: *CarCare – Gestión de vehículos institucionales*
