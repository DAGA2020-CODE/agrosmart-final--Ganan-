package ec.edu.espe.agrosmart.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import java.time.Duration;

@Service
public class PublicidadService {

    private final AgroSmartAIService aiService;

    public PublicidadService(ec.edu.espe.agrosmart.service.AgroSmartAIService aiService) {
        this.aiService = aiService;
    }

    public Mono<String> obtenerPublicidadReactiva(String producto, String audiencia) {
        return Mono.fromCallable(() -> aiService.generarPublicidad(producto, audiencia))
                // Aislar el bloqueo de la llamada HTTP a la IA
                .subscribeOn(Schedulers.boundedElastic())
                // Tiempo máximo de espera
                .timeout(Duration.ofSeconds(30))
                // Manejo de errores: si falla la IA, devolvemos este texto
                .onErrorResume(e -> Mono.just(
                        "¡El mejor plátano del Ecuador, calidad garantizada para su negocio!"));
    }
}