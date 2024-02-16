package PruebasUnitarias;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


import Rest.App;
import Token.OAuthAccessTokenGenerator;


public class AppTest {

	private static final String REAL_ACCESS_TOKEN = "Bearer ya29.a0AfB_byDwLbnqC3WSw0ZU79QaLFwsmufw4Dvy3O9uIGWnmK-vixxdraJxMm3pCGbKngGfhcDxorTjHjWCTW2U8pLs9UfIadC_-10fAvmKlzPzYb4C8MAwI_3JhU1ddgoRjZG4ww-2GW7Ar51v0aLKtwmVnnMCcINACQggaCgYKATISARESFQHGX2MiiLbzSxvF1ILvb1vsi4V5cw0171"
			+ "";
    private App app;
    
    OAuthAccessTokenGenerator token;
        
    @BeforeEach
    public void setUp() {
        app = new App();
    }

    @Test
    public void getEventsTest() throws Exception {
    	
        // Ejecución del método getEvents con un token de acceso real
        ResponseEntity<List<Map<String, String>>> response = app.getEvents(REAL_ACCESS_TOKEN);

        // Verificaciones y aserciones
        // Aquí debes ajustar tus aserciones en función de lo que esperas de la respuesta real
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        // Otras aserciones basadas en la respuesta real del calendario...
    }
    
    @Test
    public void getEventsProximosTest() throws Exception {
        ResponseEntity<List<Map<String, String>>> response = app.getEventsProximos(REAL_ACCESS_TOKEN);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        // Verificar que la lista de eventos no está vacía
        assertFalse(response.getBody().isEmpty());

        boolean foundSpecificEvent = false;
        // Recorrer cada evento y verificar su estructura y contenido
        for (Map<String, String> event : response.getBody()) {
            assertTrue(event.containsKey("summary"));
            assertTrue(event.containsKey("start"));

            // Verifica que los valores de cada evento son los esperados
            String eventSummary = event.get("summary");
            String eventStart = event.get("start");

            assertNotNull(eventSummary);
            assertNotNull(eventStart);
            
            if ("hola".equals(eventSummary) && eventStart.contains("2025-01-02T12:00:00")) {
                foundSpecificEvent = true;
                break;
            }

            
        }
        assertTrue("El evento específico no fue encontrado.", foundSpecificEvent);
   
        int expectedNumberOfEvents = 1; // Ajusta este número según los eventos del calendario

        assertEquals("Número de eventos no coincide con lo esperado", expectedNumberOfEvents, response.getBody().size());
        
        
    }
    
    @Test
    public void getStatsTest() throws Exception {
        ResponseEntity<Map<String, Object>> response = app.getStats(REAL_ACCESS_TOKEN);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<String, Object> stats = response.getBody();

        // Suponiendo que sabes los valores esperados para el porcentaje de tiempo ocupado y libre
        double expectedOccupiedPercentage = 0.1436;
        double expectedFreePercentage = 99.85;

        // Obtiene los valores del cuerpo de la respuesta
        double occupiedPercentage = (double) stats.get("occupancyPercentage");
        double freePercentage = (double) stats.get("freePercentage");

        // Comprueba que los porcentajes coinciden con lo esperado
        assertEquals("El porcentaje de tiempo ocupado no coincide", expectedOccupiedPercentage, occupiedPercentage, 0.01);
        assertEquals("El porcentaje de tiempo libre no coincide", expectedFreePercentage, freePercentage, 0.01);
        
        freePercentage = (double) stats.get("freePercentage");

        assertTrue("El porcentaje de tiempo libre debería ser muy alto", freePercentage > 80.0);
        
        assertTrue(stats.containsKey("occupancyPercentage"));
        assertTrue(stats.containsKey("freePercentage"));
    }
    
    @Test
    public void getEventsThisMonthTest() throws Exception {
        ResponseEntity<Map<String, Object>> response = app.getEventsThisMonth(REAL_ACCESS_TOKEN);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<String, Object> responseBody = response.getBody();

        // Asegúrate de que la clave del número de eventos sea correcta
        Object numberOfEventsObject = responseBody.get("numberOfEventsThisMonth");
        assertNotNull("El campo numberOfEventsThisMonth está ausente", numberOfEventsObject);

        // Comprueba si el objeto es un número entero
        assertTrue("numberOfEventsThisMonth no es un número entero", numberOfEventsObject instanceof Integer);

        int numberOfEventsThisMonth = (Integer) numberOfEventsObject;
        assertEquals("La cantidad de eventos no es la esperada", 1, numberOfEventsThisMonth);
    }
    
    @Test
    public void getAverageEventDurationTest() throws Exception {
        ResponseEntity<Map<String, Object>> response = app.getAverageEventDuration(REAL_ACCESS_TOKEN);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<String, Object> responseBody = response.getBody();
        String averageDurationString = (String) responseBody.get("averageDuration");
        assertNotNull("El campo averageDuration está ausente", averageDurationString);

        // Comprueba que el string tiene el formato esperado
        assertTrue("El formato de la duración promedio no es correcto", averageDurationString.matches("\\d+ horas y \\d+ minutos") || averageDurationString.matches("\\d+ minutos"));

     // Comprueba que la duración promedio coincide con el valor esperado
        String expectedAverageDuration = "1 horas y 41 minutos";
        assertEquals("La duración promedio no coincide con lo esperado", expectedAverageDuration, averageDurationString);
        
        //String averageDurationStringEmpty = (String) response.getBody().get("averageDuration");
        //assertEquals("0 minutos", averageDurationStringEmpty);
    }
    
    @Test
    public void getStatsCalendarTest() throws Exception {
    	
    	//SIN SOLAPAMIENTO
        
        String startDate = "2024-01-21T00:00:00Z"; // Fecha de inicio en formato YYYY-MM-DD
        String endDate = "2024-01-26T00:00:00Z"; // Fecha de fin en formato YYYY-MM-DD
        String startTime = "08:00"; // Hora de inicio en formato HH:MM
        String endTime = "22:00"; // Hora de fin en formato HH:MM

        // Lista de días seleccionados, por ejemplo, Lunes y Miércoles
        List<String> selectedDays = new ArrayList<>();
        selectedDays.add("Lunes");
        selectedDays.add("Miercoles");

        ResponseEntity<Map<String, Object>> response = app.getStatsCalendar(REAL_ACCESS_TOKEN, startDate, endDate, startTime, endTime, selectedDays);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        // Verifica que los porcentajes de ocupación y tiempo libre coincidan con los valores esperados
        Map<String, Object> stats = response.getBody();
        assertEquals("El porcentaje de tiempo ocupado no coincide", 2.38, (Double) stats.get("occupancyPercentage"), 0.01);
        assertEquals("El porcentaje de tiempo libre no coincide",97.62, (Double) stats.get("freePercentage"), 0.01);
        
        //SOLAPAMIENTO DIAS LABORABLES
        
        // Configura las fechas, horas y días seleccionados
        String startDateSolapa = "2024-01-01T00:00:00Z";
        String endDateSolapa = "2024-01-04T00:00:00Z";
        String startTimeSolapa = "08:00";
        String endTimeSolapa = "18:00";
        
 
        List<String> selectedDaysSolapa = new ArrayList<>();

        // Ejecutar la solicitud y obtener la respuesta
        ResponseEntity<Map<String, Object>> responseSolapa = app.getStatsCalendar(REAL_ACCESS_TOKEN, startDateSolapa, endDateSolapa, startTimeSolapa, endTimeSolapa, selectedDaysSolapa);

        // Asegúrate de que la respuesta no sea nula y de que el estado sea OK
        assertNotNull(responseSolapa);
        assertEquals(HttpStatus.OK, responseSolapa.getStatusCode());
        assertNotNull(responseSolapa.getBody());

        // Verifica que los porcentajes de ocupación y tiempo libre coincidan con los valores esperados
        Map<String, Object> statsSolapa = responseSolapa.getBody();
        assertEquals("El porcentaje de tiempo ocupado no coincide", 15.00, (Double)statsSolapa.get("occupancyPercentage"), 0.01);
        assertEquals("El porcentaje de tiempo libre no coincide", 85.00, (Double) statsSolapa.get("freePercentage"), 0.01);
        
        //SOLAPAMIENTO CON DIAS NO LABORABLES
        
        List<String> selectedDaysSolapaSelectDay = new ArrayList<>();
       selectedDaysSolapaSelectDay.add("Jueves");

        // Ejecutar la solicitud y obtener la respuesta
        ResponseEntity<Map<String, Object>> responseSolapaSelectDay = app.getStatsCalendar(REAL_ACCESS_TOKEN, startDateSolapa, endDateSolapa, startTimeSolapa, endTimeSolapa, selectedDaysSolapaSelectDay);

        // Asegúrate de que la respuesta no sea nula y de que el estado sea OK
        assertNotNull(responseSolapaSelectDay);
        assertEquals(HttpStatus.OK, responseSolapaSelectDay.getStatusCode());
        assertNotNull(responseSolapaSelectDay.getBody());

        // Verifica que los porcentajes de ocupación y tiempo libre coincidan con los valores esperados
        Map<String, Object> statsSolapaSelectDay = responseSolapaSelectDay.getBody();
        assertEquals(5.00, statsSolapaSelectDay.get("occupancyPercentage"));
        assertEquals(95.00, statsSolapaSelectDay.get("freePercentage"));
        
        
        //SOLAPAMIENTO CON EL HORARIO LABORAL PRINCIPIO JORNADA
        
        String startDateHlaboral1 = "2024-01-08T00:00:00Z";
        String endDateHlaboral1 = "2024-01-10T00:00:00Z";
        String startTimeHlaboral1 = "10:00";
        String endTimeHlaboral1 = "15:00";
        
        // Aquí podrías añadir los días seleccionados si tu lógica los requiere
        List<String> selectedDaysHLaboral1 = new ArrayList<>();

        // Ejecutar la solicitud y obtener la respuesta
        ResponseEntity<Map<String, Object>> responseHLaboral1 = app.getStatsCalendar(REAL_ACCESS_TOKEN, startDateHlaboral1, endDateHlaboral1, startTimeHlaboral1, endTimeHlaboral1, selectedDaysHLaboral1);

        // Verificar que la respuesta no sea nula y que el estado sea OK
        assertNotNull("La respuesta no debe ser nula", responseHLaboral1);
        assertEquals("El estado de la respuesta debe ser OK", HttpStatus.OK, responseHLaboral1.getStatusCode());
        assertNotNull("El cuerpo de la respuesta no debe ser nulo", responseHLaboral1.getBody());

        // Verificar que los porcentajes coincidan con los valores esperados
        Map<String, Object> statsHLaboral = responseHLaboral1.getBody();
        assertEquals("El porcentaje de ocupación debe coincidir", 20.00, (Double) statsHLaboral.get("occupancyPercentage"), 0.01);
        assertEquals("El porcentaje de tiempo libre debe coincidir", 80.00, (Double) statsHLaboral.get("freePercentage"), 0.01);
        
      //SOLAPAMIENTO CON EL HORARIO LABORAL FIN JORNADA
       
        // Configura las fechas y horas según los datos proporcionados
        String startDateHlaboral2 = "2024-01-10T00:00:00Z";
        String endDateHlaboral2 = "2024-01-17T00:00:00Z";
        String startTimeHlaboral2 = "10:00";
        String endTimeHlaboral2 = "16:00";
        
        // Aquí, también puedes configurar los días seleccionados si es necesario
        // Por ejemplo, si solo se consideran días laborales para el cálculo
        List<String> selectedDaysHLaboral2 = new ArrayList<>();

       // Ejecutar la solicitud y obtener la respuesta
        ResponseEntity<Map<String, Object>> responseHLaboral2 = app.getStatsCalendar(REAL_ACCESS_TOKEN, startDateHlaboral2, endDateHlaboral2, startTimeHlaboral2, endTimeHlaboral2, selectedDaysHLaboral2);

        // Verifica que la respuesta no sea nula y que el estado sea OK
        assertNotNull(responseHLaboral2);
        assertEquals(HttpStatus.OK, responseHLaboral2.getStatusCode());
        assertNotNull(responseHLaboral2.getBody());

        // Verifica que los porcentajes de ocupación y tiempo libre coincidan con los valores esperados
        Map<String, Object> statsHlaboral2 = responseHLaboral2.getBody();
        assertEquals("El porcentaje de tiempo ocupado no coincide", 1.04, (Double)statsHlaboral2.get("occupancyPercentage"), 0.01);
        assertEquals("El porcentaje de tiempo libre no coincide", 98.96, (Double) statsHlaboral2.get("freePercentage"), 0.01);
        
      //SIN SOLAPAMIENTO TODOS LOS DIAS NO LABORABLES
        
        String startDateNoLaboral = "2024-01-01T00:00:00Z"; // Fecha de inicio en formato YYYY-MM-DD
        String endDateNoLaboral = "2024-05-07T00:00:00Z"; // Fecha de fin en formato YYYY-MM-DD
        String startTimeNoLaboral = "08:00"; // Hora de inicio en formato HH:MM
        String endTimeNoLaboral = "17:00"; // Hora de fin en formato HH:MM

        // Lista de días seleccionados, por ejemplo, Lunes y Miércoles
        List<String> selectedDaysNoLaboral = new ArrayList<>();
        selectedDaysNoLaboral.add("Lunes");
        selectedDaysNoLaboral.add("Martes");
        selectedDaysNoLaboral.add("Miercoles");
        selectedDaysNoLaboral.add("Jueves");
        selectedDaysNoLaboral.add("Viernes");
        selectedDaysNoLaboral.add("Sabado");
        selectedDaysNoLaboral.add("Domingo");

        ResponseEntity<Map<String, Object>> response3 = app.getStatsCalendar(REAL_ACCESS_TOKEN, startDateNoLaboral, endDateNoLaboral, startTimeNoLaboral, endTimeNoLaboral, selectedDaysNoLaboral);

        assertNotNull(response3);
        assertEquals(HttpStatus.OK, response3.getStatusCode());
        assertNotNull(response3.getBody());
        
        // Verifica que los porcentajes de ocupación y tiempo libre coincidan con los valores esperados
        Map<String, Object> stats3 = response3.getBody();
        assertEquals(0.00, stats3.get("occupancyPercentage"));
        assertEquals(100.00, stats3.get("freePercentage"));
        
        //SIN SOLAPAMIENTO FULL TIME TRABJANDO (Ocuapdo al 100%)
        
        String startDateOcupado = "2024-01-22T00:00:00Z"; // Corregido: Fecha de inicio en formato YYYY-MM-DD
        String endDateOcupado = "2024-01-23T00:00:00Z"; // Corregido: Fecha de fin en formato YYYY-MM-DD
        String startTimeOcupado = "18:00"; // Hora de inicio en formato HH:MM
        String endTimeOcupado = "19:00"; // Hora de fin en formato HH:MM

        // Lista de días seleccionados
        List<String> selectedDaysOcupado = new ArrayList<>();

        ResponseEntity<Map<String, Object>> response4 = app.getStatsCalendar(REAL_ACCESS_TOKEN, startDateOcupado, endDateOcupado, startTimeOcupado, endTimeOcupado, selectedDaysOcupado);

        assertNotNull(response4);
        assertEquals(HttpStatus.OK, response4.getStatusCode());
        assertNotNull(response4.getBody());
        
        // Verifica que los porcentajes de ocupación y tiempo libre coincidan con los valores esperados
        Map<String, Object> stats4 = response4.getBody();
        assertEquals(0.00, stats4.get("freePercentage"));
        assertEquals(100.00, stats4.get("occupancyPercentage"));
        
              
    }
    
    @Test
    public void getMatchingEventsTest() throws Exception {
        
        String startDate  = "2024-01-01T00:00:00Z"; // Fecha de inicio en formato YYYY-MM-DD
        String endDate = "2024-01-07T00:00:00Z"; // Fecha de fin en formato YYYY-MM-DD
        String keyword ="hola";

        ResponseEntity<List<Map<String, String>>> response = app.getMatchingEvents(REAL_ACCESS_TOKEN, startDate, endDate, keyword);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        //Búsqueda Básica por Palabra Clave
        
     // Preparar datos esperados
        List<Map<String, String>> expectedEvents = new ArrayList<>();
        Map<String, String> event1 = new HashMap<>();
        event1.put("title", "hola");
        event1.put("startTime", "2024-01-04T11:00:00.000+01:00");
        expectedEvents.add(event1);

        Map<String, String> event2 = new HashMap<>();
        event2.put("title", "hola");
        event2.put("startTime", "2024-01-02T11:00:00.000+01:00");
        expectedEvents.add(event2);

        // Realizar la búsqueda
        String startDate1 = "2024-01-01T00:00:00Z";
        String endDate1 = "2024-01-07T00:00:00Z";
        String keyword1 = "hola";

        ResponseEntity<List<Map<String, String>>> response1 = app.getMatchingEvents(REAL_ACCESS_TOKEN, startDate1, endDate1, keyword1);

        assertNotNull("La respuesta no debe ser nula", response1);
        assertEquals("El estado de la respuesta debe ser OK", HttpStatus.OK, response1.getStatusCode());
        assertNotNull("El cuerpo de la respuesta no debe ser nulo", response1.getBody());

        // Comparar los resultados obtenidos con los esperados
        List<Map<String, String>> actualEvents = response1.getBody();
        assertEquals("Los eventos obtenidos deben coincidir con los esperados", expectedEvents, actualEvents);
    
        //Búsqueda con Rango de Fechas Específico
        
        String startDate2 = "2024-02-01T00:00:00Z";
        String endDate2 = "2024-02-10T00:00:00Z";
        String keyword2 = ""; // Sin palabra clave

        ResponseEntity<List<Map<String, String>>> response2 = app.getMatchingEvents(REAL_ACCESS_TOKEN, startDate2, endDate2, keyword2);

        assertNotNull("La respuesta no debe ser nula", response2);
        assertEquals("El estado de la respuesta debe ser OK", HttpStatus.OK, response2.getStatusCode());
        assertNotNull("El cuerpo de la respuesta no debe ser nulo", response2.getBody());
        
        //Palabra que no se encuentra
        String startDate3 = "2024-03-01T00:00:00Z";
        String endDate3 = "2024-03-05T00:00:00Z";
        String keyword3 = "unlikelykeyword";

        // Realizar la búsqueda y obtener la respuesta
        ResponseEntity<List<Map<String, String>>> response3 = app.getMatchingEvents(REAL_ACCESS_TOKEN, startDate3, endDate3, keyword3);

        // Verificar que la respuesta no sea nula y que el estado sea OK
        assertNotNull("La respuesta no debe ser nula", response3);
        assertEquals("El estado de la respuesta debe ser OK", HttpStatus.OK, response3.getStatusCode());

        // Verificar que la lista de eventos esté vacía
        assertTrue("La lista de eventos debe estar vacía", response3.getBody().isEmpty());
        
        
        //Búsqueda con Fechas Invertidas

        String startDate4 = "2024-04-10T00:00:00Z";
        String endDate4 ="2024-04-01T00:00:00Z";
        String keyword4 = "music";

        ResponseEntity<List<Map<String, String>>> response4 = app.getMatchingEvents(REAL_ACCESS_TOKEN, startDate4, endDate4, keyword4);

        // Aquí, asumimos que el sistema devuelve un error o una lista vacía
        assertNotNull("La respuesta no debe ser nula", response4);
        assertTrue("La lista de eventos debe estar vacía o el estado no debe ser OK", 
                   response4.getBody().isEmpty() || response4.getStatusCode() != HttpStatus.OK);
             
    }

}