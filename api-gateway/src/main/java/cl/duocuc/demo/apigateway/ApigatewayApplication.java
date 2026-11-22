package cl.duocuc.demo.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================
 * ApigatewayApplication - Punto de entrada unico del sistema
 * ============================================================
 *
 * Todas las peticiones de Postman/frontend llegan al puerto 8081
 * y el Gateway las reenvia al microservicio correspondiente:
 *   /api/productos/** -> ms-productos
 *   /api/clientes/**  -> ms-clientes
 *
 * No necesita ninguna anotacion extra:
 *   - Las rutas se definen en application.yaml.
 *   - El registro en Eureka es automatico al tener la dependencia
 *     spring-cloud-starter-netflix-eureka-client en el pom.xml.
 *
 * El Gateway usa WebFlux (reactivo, servidor Netty), no Tomcat.
 */
@SpringBootApplication
public class ApigatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApigatewayApplication.class, args);
	}

}
