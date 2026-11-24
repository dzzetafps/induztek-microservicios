# Induztek — Backend de microservicios

Backend de **Induztek Terreno (EQ073)**, desarrollado con Java 21, Spring Boot y arquitectura de microservicios. Expone servicios de usuarios, visitas técnicas, equipos, instrumentos y calibraciones para la aplicación Android.

> **Alcance:** documentación preparada para `feature/sebastian-e3-integracion-backend`, contrastada con archivos de configuración y código de esa rama y con verificaciones locales del 23 de noviembre de 2026. La instalación en otro computador aún debe comprobarse allí. No contiene contraseñas reales ni datos de cuentas existentes.

## Arquitectura

```text
Aplicación Android (Retrofit)
           |
           v
API Gateway (8081) <----> Eureka Server (8761)
           |
     +-----+------+----------------+
     |            |                |
     v            v                v
ms-usuarios   ms-visitas       ms-equipos
  (8082)        (8083)           (8084)
     |            |                |
     v            v                v
  MySQL         MySQL            MySQL
induztek_     induztek_        induztek_
usuarios       visitas          equipos
```

Eureka permite registrar y descubrir los microservicios. El Gateway distribuye las solicitudes hacia cada servicio mediante sus nombres registrados en Eureka.

## Módulos y rutas

| Módulo | Puerto | Responsabilidad | Rutas del Gateway |
|---|---:|---|---|
| `eureka-server` | 8761 | Registro y descubrimiento | Panel de Eureka en `http://localhost:8761/` |
| `api-gateway` | 8081 | Entrada HTTP y enrutamiento | `http://localhost:8081/` |
| `ms-usuarios` | 8082 | Autenticación, usuarios y perfil | `/api/auth/**`, `/api/usuarios`, `/api/usuarios/**` |
| `ms-visitas` | 8083 | Visitas técnicas | `/api/visitas`, `/api/visitas/**` |
| `ms-equipos` | 8084 | Equipos, instrumentos y calibraciones | `/api/equipos`, `/api/equipos/**`, `/api/instrumentos`, `/api/instrumentos/**`, `/api/calibraciones`, `/api/calibraciones/**` |

Las rutas indican **prefijos configurados en el Gateway**; no implican que todas las combinaciones de métodos HTTP y subrutas existan. Consultar los controladores de cada servicio para conocer las operaciones específicas.

## Requisitos

- **Java JDK 21**, disponible en `PATH` o mediante `JAVA_HOME`.
- **MySQL** accesible en `localhost:3306`.
- Terminal de Windows PowerShell (los módulos incluyen `mvnw.cmd`, por lo que no es obligatorio instalar Maven globalmente).
- Puertos **8761, 8081, 8082, 8083 y 8084** disponibles.
- Para probar la aplicación Android: Android Studio y un emulador o dispositivo compatible.

## Preparar MySQL

Crear las bases de datos necesarias en MySQL Workbench o en la consola de MySQL:

```sql
CREATE DATABASE IF NOT EXISTS induztek_usuarios;
CREATE DATABASE IF NOT EXISTS induztek_visitas;
CREATE DATABASE IF NOT EXISTS induztek_equipos;
```

Configuración observada:

| Servicio | Base de datos | Configuración JPA |
|---|---|---|
| `ms-usuarios` | `induztek_usuarios` | `spring.jpa.hibernate.ddl-auto=update` |
| `ms-visitas` | `induztek_visitas` | `spring.jpa.hibernate.ddl-auto=update` |
| `ms-equipos` | `induztek_equipos` | `spring.jpa.hibernate.ddl-auto=update` |

`ms-usuarios` utiliza `createDatabaseIfNotExist=true` en su URL JDBC; los otros dos servicios no lo especifican. Se recomienda crear previamente las tres bases para reproducir la instalación.

### Credenciales MySQL (especialmente `ms-usuarios`)

**Importante:** las credenciales **MySQL** permiten conectar Spring Boot a las bases de datos; el **correo y contraseña de la app Android** pertenecen a una cuenta de la tabla `usuarios` y son diferentes.

En el repositorio se comprobó:

- `ms-usuarios/src/main/resources/application.properties` usa por defecto `spring.datasource.username=induztek_app` y `spring.datasource.password=` **vacío**. Además importa de manera opcional `classpath:application-local.properties`; ese archivo **no figura publicado** en la rama revisada. Por eso una instalación nueva puede fallar si no se configuran credenciales explícitas.
- `ms-visitas` y `ms-equipos` leen `${DB_USERNAME:root}` y `${DB_PASSWORD:}`. Sin variables, pueden intentar conectar como `root` **sin contraseña**; no es una configuración segura ni reproducible.
- Para no depender de un archivo local que tal vez no exista en otro computador, el procedimiento de abajo utiliza variables de entorno **en cada terminal**. Spring Boot reconoce `SPRING_DATASOURCE_USERNAME` y `SPRING_DATASOURCE_PASSWORD` para los tres servicios, y tienen prioridad sobre los valores ordinarios del archivo de propiedades.

**Opción recomendada para un computador de evaluación:** crear una cuenta MySQL exclusiva para estas tres bases. En MySQL Workbench, conectado como administrador, ejecutar el siguiente SQL **reemplazando primero** `ELEGIR_CLAVE_MYSQL_FUERTE` por una clave propia; no dejar ese texto como contraseña definitiva:

```sql
CREATE DATABASE IF NOT EXISTS induztek_usuarios;
CREATE DATABASE IF NOT EXISTS induztek_visitas;
CREATE DATABASE IF NOT EXISTS induztek_equipos;

CREATE USER IF NOT EXISTS 'induztek_app'@'localhost'
  IDENTIFIED BY 'ELEGIR_CLAVE_MYSQL_FUERTE';

GRANT ALL PRIVILEGES ON induztek_usuarios.* TO 'induztek_app'@'localhost';
GRANT ALL PRIVILEGES ON induztek_visitas.* TO 'induztek_app'@'localhost';
GRANT ALL PRIVILEGES ON induztek_equipos.* TO 'induztek_app'@'localhost';
```

Si `induztek_app` **ya existe**, `CREATE USER IF NOT EXISTS` no cambia su contraseña: utilizar la contraseña vigente o, si se administra esa cuenta y se desea cambiarla, ejecutar `ALTER USER 'induztek_app'@'localhost' IDENTIFIED BY 'NUEVA_CLAVE_SEGURA';` de forma consciente. Evitar usar `root` para la app en una instalación de evaluación. Los permisos anteriores se limitan a las tres bases para que Hibernate pueda crear/actualizar tablas durante la demostración (`ddl-auto=update`); para producción habría que reducirlos.

**En cada terminal nueva de `ms-usuarios`, `ms-visitas` y `ms-equipos`, antes de iniciar Spring Boot:**

```powershell
$env:SPRING_DATASOURCE_USERNAME = 'induztek_app'
$claveSegura = Read-Host 'Contraseña MySQL de induztek_app' -AsSecureString
$env:SPRING_DATASOURCE_PASSWORD = [System.Net.NetworkCredential]::new('', $claveSegura).Password
Remove-Variable claveSegura
```

La clave no se muestra mientras se escribe, aunque JDBC debe recibirla como texto en una variable de entorno del proceso. No imprimir esas variables, grabar la sesión ni incluirlas en commits/capturas. Cerrar la terminal tras detener el servicio para descartarlas. Si se decide utilizar `DB_USERNAME`/`DB_PASSWORD` en lugar de `SPRING_DATASOURCE_*`, **solo sirven directamente a `ms-visitas` y `ms-equipos`** según sus propiedades publicadas: no son la opción más portable para `ms-usuarios`.

**Diagnóstico de conexión:** si aparece `Access denied`, comprobar que MySQL está encendido, que la cuenta y la contraseña coinciden, que el usuario tiene acceso a la base correspondiente y que las variables están definidas en **esa misma terminal**. El archivo opcional `application-local.properties` no es requisito si se utilizan las variables anteriores; no copiarlo a Git cuando incluya secretos.

### Primera cuenta de prueba para instalar en otro computador

La aplicación Android **no debe iniciar sesión con el usuario MySQL**. Necesita un usuario real en la base `induztek_usuarios`, tabla `usuarios`.

Según el código publicado de `ms-usuarios`:

- `POST /api/usuarios/login` permite verificar correo y contraseña.
- `POST /api/usuarios` **exige previamente un jefe técnico autenticado** y solo permite registrar cuentas nuevas con rol `TECNICO`.
- Los roles están definidos en código: `1=TECNICO`, `2=INGENIERO`, `3=JEFE_TECNICO`, `4=CLIENTE`.
- Las contraseñas se verifican mediante `PasswordEncoder` (BCrypt); **no insertar contraseñas sin cifrar** en la tabla.
- En la rama revisada **no se verificó una cuenta inicial creada automáticamente**. Por eso, en una base vacía hay que preparar una cuenta de demostración antes de probar el inicio de sesión.

**Paso A — Generar el hash BCrypt sin subir ni revelar la clave.** En el computador de pruebas, si está disponible Python 3, instalar una sola vez la biblioteca en el entorno local:

```powershell
python -m pip install bcrypt
```

Luego ejecutar este comando en PowerShell (pedirá una **contraseña nueva de demostración**, sin mostrarla; imprimirá únicamente su hash):

```powershell
python -c "import bcrypt, getpass; clave=getpass.getpass('Nueva clave de prueba: '); print(bcrypt.hashpw(clave.encode('utf-8'), bcrypt.gensalt()).decode('utf-8'))"
```

Guardar la contraseña temporal de manera privada. El hash generado empieza normalmente por `$2b$` y puede utilizarse con BCrypt. **No usar servicios web de terceros para generar hashes con contraseñas reales**.

**Paso B — Crear el primer jefe técnico de prueba.** Tras iniciar `ms-usuarios` una vez para que Hibernate cree la tabla `usuarios` (si todavía no existe), detenerlo o mantenerlo y ejecutar en MySQL Workbench, conectado a `induztek_usuarios`:

```sql
-- Sustituir el hash de ejemplo por el hash BCrypt real generado en el paso A.
-- Sustituir también el correo por uno ficticio dedicado a la demostración.
INSERT INTO induztek_usuarios.usuarios
    (nombre, correo, contrasena, ciudad, rol_id)
VALUES
    ('Jefe Tecnico Demo', 'jefe.demo@example.test', 'PEGAR_HASH_BCRYPT_GENERADO', 'Santiago', 3);
```

Este SQL es un procedimiento de **inicialización de laboratorio**, no un usuario creado por defecto ni una migración que deba agregarse al repositorio. La cuenta de ejemplo tiene privilegios de jefe técnico: usar una clave robusta, no reutilizar credenciales personales, limitar su uso al entorno local y retirarla al finalizar la evaluación. Si ya existe una cuenta de prueba conocida y autorizada, no crear otra innecesariamente.

**Paso C — Comprobar el login.** Con Eureka, Gateway y `ms-usuarios` activos, ejecutar desde PowerShell, reemplazando el correo por el de la cuenta creada:

```powershell
$correo = 'jefe.demo@example.test'
$segura = Read-Host 'Clave de la cuenta Android de prueba' -AsSecureString
$clave = [System.Net.NetworkCredential]::new('', $segura).Password
try {
    $respuesta = Invoke-RestMethod -Uri 'http://localhost:8081/api/usuarios/login' `
        -Method Post -ContentType 'application/json' `
        -Body (@{ correo = $correo; contrasena = $clave } | ConvertTo-Json)
    $respuesta | Select-Object id, nombre, correo, rolId, rol
} finally {
    Remove-Variable clave
    Remove-Variable segura
}
```

Un usuario válido debe obtener una respuesta de perfil; un `401` indica que el login no se autorizó. **No guardar la respuesta completa si contiene información sensible**. La aplicación Android debe autenticarse contra el Gateway `http://10.0.2.2:8081/` al ejecutarse en el emulador. La cuenta creada por SQL funciona solo si el backend apunta a **esa misma instancia MySQL**. Las cuentas adicionales `TECNICO` se pueden crear posteriormente por la API con autenticación de `JEFE_TECNICO`.

## Ejecutar el backend en Windows

Abrir **cinco terminales separadas** en la carpeta raíz del repositorio. Es conveniente iniciar Eureka primero, luego Gateway y los microservicios.

**Terminal 1 — Eureka Server**

```powershell
cd .\eureka-server
.\mvnw.cmd spring-boot:run
```

**Terminal 2 — API Gateway**

```powershell
cd .\api-gateway
.\mvnw.cmd spring-boot:run
```

**Terminal 3 — Usuarios** (establecer primero `SPRING_DATASOURCE_USERNAME` y `SPRING_DATASOURCE_PASSWORD` siguiendo el apartado de credenciales)

```powershell
cd .\ms-usuarios
.\mvnw.cmd spring-boot:run
```

Si el equipo tiene un archivo `application-local.properties` propio y necesita activar explícitamente un perfil `local`, puede iniciar en su lugar con el comando siguiente, **verificando antes** qué propiedades activa. El archivo opcional no se distribuye en la rama revisada:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```

**Terminal 4 — Visitas** (configurar `SPRING_DATASOURCE_USERNAME` y `SPRING_DATASOURCE_PASSWORD` en esta terminal)

```powershell
cd .\ms-visitas
.\mvnw.cmd spring-boot:run
```

**Terminal 5 — Equipos** (configurar `SPRING_DATASOURCE_USERNAME` y `SPRING_DATASOURCE_PASSWORD` en esta otra terminal)

```powershell
cd .\ms-equipos
.\mvnw.cmd spring-boot:run
```

> Cada bloque `cd` parte desde la carpeta raíz del repositorio, en una **terminal independiente**. Mantener las cinco terminales abiertas durante la demostración. Si el puerto ya está en uso, comprobar primero si el servicio existente funciona; no iniciar una segunda instancia ni detener procesos sin identificarlos. Si falla MySQL con `Access denied ... (using password: NO)`, revisar `SPRING_DATASOURCE_PASSWORD` en **la misma terminal** del módulo.

## Verificar servicios

1. Abrir `http://localhost:8761/` y comprobar el registro de los servicios de usuarios, visitas y equipos (sus nombres visibles dependen de `spring.application.name` y la configuración de descubrimiento).
2. Comprobar en las terminales que Eureka, Gateway y los servicios arrancaron correctamente.
3. Realizar una operación autorizada desde Android y verificar que el Gateway la redirige al microservicio correspondiente.
4. Confirmar la persistencia de la operación en la base de datos adecuada.

**Resultado observado en el computador de prueba (23-11-2026):** Eureka devolvió HTTP 200, y el panel mostró Gateway, `MS-USUARIOS`, `MS-VISITAS` y `MS-EQUIPOS` registrados como activos. `ms-visitas` y `ms-equipos` iniciaron, abrieron conexiones MySQL y se registraron en Eureka.

Consulta básica de lectura a través del Gateway, **sin credenciales**:

```powershell
foreach ($ruta in @('/api/usuarios', '/api/visitas', '/api/equipos', '/api/instrumentos', '/api/calibraciones')) {
    try {
        $r = Invoke-WebRequest "http://localhost:8081$ruta" -UseBasicParsing
        Write-Host "$ruta -> HTTP $($r.StatusCode)"
    } catch {
        if ($_.Exception.Response) {
            Write-Host "$ruta -> HTTP $([int]$_.Exception.Response.StatusCode)"
        } else {
            Write-Host "$ruta -> error de conexión: $($_.Exception.Message)"
        }
    }
}
```

Respuestas observadas: `/api/usuarios` → **401** (sin autenticación); `/api/visitas`, `/api/equipos`, `/api/instrumentos` y `/api/calibraciones` → **200**. Un 200 confirma una respuesta HTTP, pero **no prueba por sí solo los permisos, la creación de datos ni todas las rutas**.

La documentación OpenAPI se visualizó en:

- Visitas: `http://localhost:8083/swagger-ui.html`
- Equipos, instrumentos y calibraciones: `http://localhost:8084/swagger-ui.html`

En Swagger se confirmaron visualmente las operaciones GET/POST de visitas, equipos e instrumentos y GET por identificador. La existencia del controlador de calibraciones es visible, pero sus rutas exactas deben comprobarse antes de detallarlas.

**Nota:** algunos endpoints requieren autenticación; recibir `401` o `403` no significa necesariamente que el servicio esté caído. El aviso de renovaciones (self-preservation) de Eureka puede mostrarse en un laboratorio local; revisar el estado real de las instancias.

## Integración Android

La aplicación Android consume los servicios mediante Retrofit y la dirección del Gateway. En un emulador Android, la dirección de red necesaria depende de la configuración del entorno.

En el emulador estándar de Android, el equipo integró la URL base de Retrofit **`http://10.0.2.2:8081/`** para acceder al Gateway que se ejecuta en el PC anfitrión. Sebastián informó que la integración Android, el inicio de sesión y el APK release firmado fueron probados satisfactoriamente. Esta dirección se utiliza **dentro del emulador** y no equivale a una URL pública válida para otros dispositivos físicos o redes. Confirmar la configuración final del repositorio Android antes de la defensa.

La consulta meteorológica con **Open-Meteo** se realiza desde Android como integración con una API externa; no es uno de estos microservicios.

## Pruebas automatizadas

Se identificaron pruebas JUnit en los tres microservicios:

- **`ms-usuarios`**: `PasswordConfigTest`, `SecurityConfigTest`, `UsuarioControllerTest`, `AlmacenamientoServiceTest`, `UsuarioServiceTest`.
- **`ms-visitas`**: `VisitaControllerTest`, `VisitaTecnicoIdTest`, `VisitaServiceTest`.
- **`ms-equipos`**: `CalibracionServiceTest`, `EquipoServiceTest`, `InstrumentoServiceTest`.

Para ejecutar las pruebas de cada servicio, desde su carpeta:

```powershell
.\mvnw.cmd test
```

Para comprobar la compilación de un módulo sin omitir pruebas:

```powershell
.\mvnw.cmd verify
```

Los tres módulos se probaron en Windows el **23 de noviembre de 2026**, con **115 pruebas aprobadas, 0 fallos, 0 errores y 0 omitidas**:

| Microservicio | Pruebas aprobadas | Cobertura de instrucciones JaCoCo | Cobertura de ramas JaCoCo |
|---|---:|---:|---:|
| `ms-usuarios` | 54 | 80 % | 81 % |
| `ms-visitas` | 35 | 97 % | 97 % |
| `ms-equipos` | 26 | 84 % | 64 % |
| **Total de pruebas** | **115** | **No calculado como agregado** | **No calculado como agregado** |

Los porcentajes corresponden a los informes independientes de cada módulo. Para **regenerar el informe JaCoCo**, ejecutar desde la carpeta de cada microservicio:

```powershell
.\mvnw.cmd test
.\mvnw.cmd jacoco:report
Start-Process "target\site\jacoco\index.html"
```

El último comando abre el reporte HTML en el navegador. Si un módulo no genera el reporte, revisar su configuración JaCoCo en `pom.xml` y la salida de Maven antes de reportar cobertura.

Las pruebas de Android se ejecutan desde el repositorio Android, que es independiente:

```powershell
.\gradlew.bat :app:testDebugUnitTest :app:jacocoTestReport
```

No debe confundirse la cobertura Android con la cobertura Java de los microservicios: son informes diferentes.

## Evidencias obtenidas en la verificación local

En una carpeta de evidencias **fuera del repositorio** se guardaron:

- `07-eureka-microservicios.png`: instancias registradas en Eureka (confirmar que el archivo esté efectivamente guardado antes de entregar).
- `08-gateway-rutas.png`: respuestas de lectura HTTP por Gateway.
- `09-swagger-equipos.png`: documentación de equipos e instrumentos.
- `10-swagger-visitas.png`: documentación de visitas.
- `11-jacoco-backend-equipos.png`: cobertura de `ms-equipos` (84 % instrucciones).
- `12-jacoco-backend-visitas.png`: cobertura de `ms-visitas` (97 % instrucciones).
- `13-jacoco-backend-usuarios.png`: cobertura de `ms-usuarios` (80 % instrucciones).

Las capturas 08 a 13 fueron confirmadas como guardadas por el equipo. No subir a un repositorio público evidencias con credenciales, tokens o datos sensibles.

## Evidencias recomendadas para la entrega

- Compilación y ejecución de los cinco módulos.
- Dashboard de Eureka con los tres microservicios registrados.
- Acceso a través del API Gateway.
- Registro y consulta reales de usuarios, visitas, equipos, instrumentos y calibraciones.
- Persistencia correspondiente en MySQL.
- Ejecución satisfactoria de pruebas automatizadas e informes de cobertura.
- Pruebas desde Android y evidencia de controles de acceso.

Incluir únicamente capturas y cifras verificadas en la versión integrada definitiva. Evitar mostrar contraseñas, tokens o datos personales reales.

## Repositorios y coordinación

- Backend: https://github.com/dzzetafps/induztek-microservicios
- Android: https://github.com/Sebastian-Montupil/Induztek
- Rama de integración del backend consultada: `feature/sebastian-e3-integracion-backend`

Esta versión incorpora los resultados de pruebas backend verificados el 23-11-2026. Esta edición amplía el procedimiento para credenciales MySQL y cuenta inicial de prueba por solicitud de Sebastián. Incorporar el archivo como `README.md` **solo** en `feature/sebastian-e3-integracion-backend` o mediante una rama/PR basada en ella, después de confirmar la versión vigente. No modificar `main`.

