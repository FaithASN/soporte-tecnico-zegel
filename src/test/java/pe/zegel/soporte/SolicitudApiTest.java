package pe.zegel.soporte;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Pruebas de integracion HTTP con controlador, validaciones y servicio reales. */
@SpringBootTest
@AutoConfigureMockMvc
class SolicitudApiTest {
    private static final String API = "/api/v1/solicitudes";
    private static final String VALIDA = """
        {"asunto":"Computadora sin encender", "descripcion":"No responde al boton de encendido",
         "cliente":{"nombre":"Ana Torres", "email":"ana@example.com"},
         "tecnico":null, "estado":"PENDIENTE", "prioridad":"ALTA"}
        """;
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    private JsonNode crear() throws Exception {
        String body = mvc.perform(post(API).contentType(MediaType.APPLICATION_JSON).content(VALIDA))
            .andExpect(status().isCreated()).andExpect(header().exists("Location"))
            .andReturn().getResponse().getContentAsString();
        return mapper.readTree(body);
    }

    @Test void cicloCrudConservaIdentidadYFechaCreacion() throws Exception {
        JsonNode creada = crear(); long id = creada.get("id").asLong();
        mvc.perform(get(API + "/" + id)).andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id));
        String cambio = VALIDA.replace("PENDIENTE", "EN_PROCESO").replace("\"tecnico\":null",
            "\"tecnico\":{\"nombre\":\"Luis Perez\",\"email\":\"luis@example.com\"}");
        mvc.perform(put(API + "/" + id).contentType(MediaType.APPLICATION_JSON).content(cambio))
            .andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("EN_PROCESO"))
            .andExpect(jsonPath("$.id").value(id))
            .andExpect(jsonPath("$.fechaCreacion").value(creada.get("fechaCreacion").asText()));
        mvc.perform(delete(API + "/" + id)).andExpect(status().isNoContent())
            .andExpect(content().string(""));
        mvc.perform(get(API + "/" + id)).andExpect(status().isNotFound());
    }

    @Test void listaYFiltraPorEstado() throws Exception {
        JsonNode creada = crear();
        mvc.perform(get(API)).andExpect(status().isOk())
            .andExpect(jsonPath("$[*].id", hasItem(creada.get("id").asInt())));
        mvc.perform(get(API).param("estado", "PENDIENTE")).andExpect(status().isOk())
            .andExpect(jsonPath("$[*].estado", everyItem(is("PENDIENTE"))));
    }

    @Test void rechazaCamposObligatoriosYCorreoIncorrecto() throws Exception {
        mvc.perform(post(API).contentType(MediaType.APPLICATION_JSON)
                .content(VALIDA.replace("Computadora sin encender", " ").replace("ana@example.com", "incorrecto")))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.detalles[*].campo", hasItems("asunto", "cliente.email")));
    }

    @Test void rechazaClienteNulo() throws Exception {
        mvc.perform(post(API).contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.detalles[*].campo", hasItem("cliente")));
    }

    @Test void validaTambienElCuerpoDePutSinAlterarLaSolicitud() throws Exception {
        long id = crear().get("id").asLong();
        mvc.perform(put(API + "/" + id).contentType(MediaType.APPLICATION_JSON)
                .content(VALIDA.replace("Computadora sin encender", " ")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detalles[*].campo", hasItem("asunto")));
        mvc.perform(get(API + "/" + id))
            .andExpect(jsonPath("$.asunto").value("Computadora sin encender"));
    }

    @Test void rechazaLongitudExcesiva() throws Exception {
        mvc.perform(post(API).contentType(MediaType.APPLICATION_JSON)
                .content(VALIDA.replace("Computadora sin encender", "a".repeat(121))))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.detalles[*].campo", hasItem("asunto")));
    }

    @Test void exigeTecnicoYNoModificaAnteActualizacionInvalida() throws Exception {
        JsonNode creada = crear(); long id = creada.get("id").asLong();
        mvc.perform(put(API + "/" + id).contentType(MediaType.APPLICATION_JSON)
                .content(VALIDA.replace("PENDIENTE", "RESUELTA")))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.mensaje", containsString("tecnico")));
        mvc.perform(get(API + "/" + id)).andExpect(jsonPath("$.estado").value("PENDIENTE"));
    }

    @Test void validaDatosDelTecnico() throws Exception {
        mvc.perform(post(API).contentType(MediaType.APPLICATION_JSON)
                .content(VALIDA.replace("\"tecnico\":null", "\"tecnico\":{\"nombre\":\"\",\"email\":\"mal\"}")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detalles[*].campo", hasItems("tecnico.nombre", "tecnico.email")));
    }

    @Test void rechazaJsonMalFormadoEnumsYCamposDesconocidos() throws Exception {
        for (String body : new String[]{"{", VALIDA.replace("PENDIENTE", "DESCONOCIDO"),
                VALIDA.replace("\"ALTA\"", "1"), VALIDA.replace("\"asunto\"", "\"asuntto\"")}) {
            mvc.perform(post(API).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400));
        }
    }

    @Test void validaIdentificadoresYFiltros() throws Exception {
        for (String id : new String[]{"0", "-1", "abc"}) {
            mvc.perform(get(API + "/" + id)).andExpect(status().isBadRequest());
        }
        mvc.perform(get(API).param("estado", "OTRO")).andExpect(status().isBadRequest());
    }

    @Test void inexistentesDevuelven404() throws Exception {
        String ruta = API + "/9223372036854775807";
        mvc.perform(get(ruta)).andExpect(status().isNotFound()).andExpect(jsonPath("$.ruta").value(ruta));
        mvc.perform(put(ruta).contentType(MediaType.APPLICATION_JSON).content(VALIDA)).andExpect(status().isNotFound());
        mvc.perform(delete(ruta)).andExpect(status().isNotFound());
    }

    @Test void idsNoSeReutilizanTrasEliminar() throws Exception {
        long primero = crear().get("id").asLong();
        mvc.perform(delete(API + "/" + primero)).andExpect(status().isNoContent());
        long segundo = crear().get("id").asLong();
        assertThat(segundo).isGreaterThan(primero);
    }

    @Test void erroresDeProtocoloMantienenEstructura() throws Exception {
        mvc.perform(patch(API)).andExpect(status().isMethodNotAllowed())
            .andExpect(header().exists("Allow")).andExpect(jsonPath("$.status").value(405));
        mvc.perform(post(API).contentType(MediaType.TEXT_PLAIN).content("hola"))
            .andExpect(status().isUnsupportedMediaType()).andExpect(jsonPath("$.status").value(415));
        mvc.perform(get("/ruta-inexistente")).andExpect(status().isNotFound())
            .andExpect(jsonPath("$.mensaje").exists());
    }
}
