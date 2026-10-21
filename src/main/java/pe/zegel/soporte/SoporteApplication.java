package pe.zegel.soporte;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Punto de entrada: inicia el servidor HTTP integrado de Spring Boot. */
@SpringBootApplication
public class SoporteApplication {
    public static void main(String[] args) {
        SpringApplication.run(SoporteApplication.class, args);
    }
}
