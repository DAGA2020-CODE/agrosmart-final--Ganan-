package ec.edu.espe.agrosmart;

import ec.edu.espe.agrosmart.service.AgroSmartAIService;
import ec.edu.espe.agrosmart.service.PublicidadService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.test.StepVerifier;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class PublicidadServiceTest {

    @Test
    void obtenerPublicidad_cuandoIAFalla_debeRetornarMensajeRespaldo() {
        // Arrange
        AgroSmartAIService aiMock = Mockito.mock(AgroSmartAIService.class);
        when(aiMock.generarPublicidad(anyString(), anyString()))
                .thenThrow(new RuntimeException("Error de API"));

        PublicidadService service = new PublicidadService(aiMock);

        // Act & Assert
        StepVerifier.create(service.obtenerPublicidadReactiva("Platano", "Mayoristas"))
                .expectNextMatches(msg -> msg.contains("calidad garantizada"))
                .verifyComplete();
    }
}