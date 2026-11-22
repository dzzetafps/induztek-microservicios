package cl.duocuc.demo.eurekaserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * ============================================================
 * EurekaserverApplication - Servidor de descubrimiento
 * ============================================================
 *
 * Eureka funciona como una "guia telefonica" de microservicios:
 *   - Cada microservicio se registra al arrancar (nombre + IP + puerto).
 *   - El Gateway consulta a Eureka para saber donde esta cada servicio.
 *
 * @EnableEurekaServer convierte esta aplicacion en el SERVIDOR Eureka.
 * (Los clientes no necesitan anotacion, solo la dependencia).
 *
 * Dashboard: http://localhost:8761 (muestra los servicios registrados)
 */
@SpringBootApplication
@EnableEurekaServer
public class EurekaserverApplication {

	public static void main(String[] args) {
		SpringApplication.run(EurekaserverApplication.class, args);
		// URL accesible desde TU PC gracias al mapeo 8761:8761 del compose
		System.out.println("**************************************************");
		System.out.println("Eureka Server corriendo en: http://localhost:8761");
		System.out.println("**************************************************");
	}

}
