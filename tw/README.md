# CUMe y Calla

Aplicacion web de pedidos de comida desarrollada con Spring Boot, Thymeleaf y SQLite para la asignatura de Tecnologias Web.

## Descripcion

El proyecto implementa una plataforma tipo marketplace donde los usuarios pueden:

- registrarse e iniciar sesion
- consultar restaurantes
- buscar y filtrar restaurantes
- gestionar favoritos
- realizar pedidos
- dejar valoraciones
- administrar su perfil y direcciones

Ademas, los propietarios pueden:

- crear y editar restaurantes
- gestionar platos
- activar o pausar pedidos
- consultar pedidos recibidos
- administrar las etiquetas del menu

## Stack tecnologico

- Java 17
- Spring Boot 3.2.4
- Spring MVC
- Spring Security
- Spring Data JPA
- Thymeleaf
- SQLite
- Lombok
- Maven

## Estructura del proyecto

```text
tw/
├─ src/
│  ├─ main/
│  │  ├─ java/com/tw/
│  │  │  ├─ controller/
│  │  │  ├─ model/
│  │  │  ├─ repository/
│  │  │  ├─ service/
│  │  │  ├─ config/
│  │  │  └─ TwApplication.java
│  │  └─ resources/
│  │     ├─ templates/
│  │     ├─ static/
│  │     └─ application.properties
├─ uploads/
├─ TANI.db
└─ pom.xml
```

## Modulos principales

- `controller`: controladores MVC para autenticacion, restaurantes, pedidos, platos, direcciones, categorias y valoraciones.
- `model`: entidades JPA del dominio.
- `repository`: acceso a datos con Spring Data JPA.
- `service`: logica de negocio.
- `templates`: vistas Thymeleaf.
- `static`: recursos CSS, JS e imagenes estaticas.

## Entidades principales

- `Usuario`
- `Restaurante`
- `Plato`
- `Pedido`
- `LineaPedido`
- `Valoracion`
- `Categoria`
- `Direccion`

## Configuracion

La aplicacion usa SQLite como base de datos local:

```properties
spring.datasource.url=jdbc:sqlite:TANI.db
spring.jpa.hibernate.ddl-auto=update
app.upload.dir=uploads
```

Notas:

- la base de datos se guarda en `TANI.db`
- las imagenes subidas se almacenan en la carpeta `uploads`
- Thymeleaf tiene cache desactivada para desarrollo
- el proyecto esta preparado para ejecutarse tambien como WAR en Tomcat externo

## Requisitos

- JDK 17
- Maven 3.9 o superior

## Ejecucion en desarrollo

Desde la raiz del proyecto:

```bash
mvn spring-boot:run
```

La aplicacion quedara disponible en:

```text
http://localhost:8080
```

## Compilacion

Compilar el proyecto:

```bash
mvn clean compile
```

Generar el WAR:

```bash
mvn clean package
```

El artefacto generado se crea con nombre:

```text
target/cume.war
```

## Funcionalidades implementadas

- autenticacion y gestion de usuarios
- perfil de usuario con edicion de datos e imagen
- gestion de direcciones
- listado de restaurantes con filtros
- busqueda avanzada
- detalle de restaurante
- sistema de favoritos
- gestion de platos
- realizacion y seguimiento de pedidos
- gestion de pedidos del propietario
- valoraciones de restaurantes
- categorias de restaurantes
- subida de imagenes

## Despliegue

El proyecto esta configurado como `war` en `pom.xml`, por lo que puede:

- ejecutarse en desarrollo con Spring Boot
- desplegarse en un Tomcat externo copiando `cume.war` a `webapps`

## Observaciones

- la aplicacion utiliza `SpringBootServletInitializer` en `TwApplication` para compatibilidad con Tomcat externo
- la base de datos SQLite se actualiza automaticamente con `ddl-auto=update`
- en desarrollo pueden verse trazas SQL activadas en `application.properties`

## Autor

Proyecto academico de Tecnologias Web.
