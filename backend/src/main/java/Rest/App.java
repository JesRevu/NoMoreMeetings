package Rest;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SuppressWarnings("deprecation")
@RestController
@RequestMapping("/app")
@CrossOrigin(origins = "http://localhost:5173") 
public class App implements WebMvcConfigurer{

	private static final String APPLICATION_NAME = "TuAppName";
	private static final String CLIENT_ID = "549283482423-gkidn4sjbo7p8h606asqtfgeidok4u0r.apps.googleusercontent.com";
	private static final String CLIENT_SECRET = "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";


	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**")
		.allowedOrigins("http://localhost:5173") // Reemplaza con la URL de tu frontend
		.allowedMethods("*")
		.allowedHeaders("*");
	}
	

	@GetMapping("/events")
	public ResponseEntity<List<Map<String, String>>> getEvents(@RequestHeader("Authorization") String authorizationHeader) {
		try {
			HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
			JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

			GoogleCredential credential = new GoogleCredential.Builder()
					.setJsonFactory(jsonFactory)
					.setTransport(httpTransport)
					.setClientSecrets(CLIENT_ID, CLIENT_SECRET)
					.build()
					.setAccessToken(getAccessTokenFromHeader(authorizationHeader))
					.createScoped(CalendarScopes.all());

			Calendar client = new Calendar.Builder(httpTransport, jsonFactory, credential)
					.setApplicationName(APPLICATION_NAME)
					.build();

			String calendarId = "primary"; // Cambiar 'primary' por el ID del calendario que desees
			Events eventsResponse = client.events().list(calendarId)
					.setMaxResults(10)
					.setOrderBy("startTime")
					.setSingleEvents(true)
					.execute();

			List<Event> items = eventsResponse.getItems();

			List<Map<String, String>> events = new ArrayList<>();
			for (Event event : items) {
				Map<String, String> eventData = new HashMap<>();
				eventData.put("summary", event.getSummary());
				eventData.put("start", event.getStart().getDateTime().toString());

				// Agregar más propiedades si es necesario
				events.add(eventData);
			}

			return ResponseEntity.ok(events);

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}
	@GetMapping("/eventsProximos")
	public ResponseEntity<List<Map<String, String>>> getEventsProximos(@RequestHeader("Authorization") String authorizationHeader) {
		try {
			HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
			JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

			GoogleCredential credential = new GoogleCredential.Builder()
					.setJsonFactory(jsonFactory)
					.setTransport(httpTransport)
					.setClientSecrets(CLIENT_ID, CLIENT_SECRET)
					.build()
					.setAccessToken(getAccessTokenFromHeader(authorizationHeader))
					.createScoped(CalendarScopes.all());

			Calendar client = new Calendar.Builder(httpTransport, jsonFactory, credential)
					.setApplicationName(APPLICATION_NAME)
					.build();

			String calendarId = "primary"; // Cambiar 'primary' por el ID del calendario que desees
			DateTime now = new DateTime(System.currentTimeMillis());

			Events eventsResponse = client.events().list(calendarId)
					.setMaxResults(10)
					.setOrderBy("startTime")
					.setSingleEvents(true)
					.setTimeMin(now)
					.execute();

			List<Event> items = eventsResponse.getItems();

			List<Map<String, String>> events = new ArrayList<>();
			for (Event event : items) {
				Map<String, String> eventData = new HashMap<>();
				eventData.put("summary", event.getSummary());
				eventData.put("start", event.getStart().getDateTime().toString());
				eventData.put("end", event.getEnd().getDateTime().toString());

				// Agregar más propiedades si es necesario
				events.add(eventData);
			}

			return ResponseEntity.ok(events);

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}



	@GetMapping("/stats")
	public ResponseEntity<Map<String, Object>> getStats(@RequestHeader("Authorization") String authorizationHeader) {

		try {
			HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
			JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

			GoogleCredential credential = new GoogleCredential.Builder()
					.setJsonFactory(jsonFactory)
					.setTransport(httpTransport)
					.setClientSecrets(CLIENT_ID, CLIENT_SECRET)
					.build()
					.setAccessToken(getAccessTokenFromHeader(authorizationHeader))
					.createScoped(CalendarScopes.all());

			Calendar calendar = new Calendar.Builder(httpTransport, jsonFactory, credential)
					.setApplicationName(APPLICATION_NAME)
					.build();


			String calendarId = "primary"; // Change 'primary' to the desired calendar ID

			// Get the current month and year using Joda-Time
			LocalDate now = LocalDate.now();
			int currentYear = now.getYear();
			int currentMonth = now.getMonthOfYear();

			// Set the start and end dates for the current month
			LocalDate startOfMonth = new LocalDate(currentYear, currentMonth, 1);
			LocalDate endOfMonth = startOfMonth.plusMonths(1);

			Events eventsResponse = calendar.events().list(calendarId)
					.setOrderBy("startTime")
					.setSingleEvents(true)
					.setTimeMin(new DateTime(startOfMonth.toDate()))
					.setTimeMax(new DateTime(endOfMonth.toDate()))
					.execute();

			List<Event> items = eventsResponse.getItems();

			long totalOccupiedDurationInMilliseconds = 0;
			long totalFreeDurationInMilliseconds = Days.daysBetween(startOfMonth, endOfMonth).getDays() * 24 * 60 * 60 * 1000L;

			// Calculate occupied duration using Joda-Time

			for (Event event : items) {

				if (event.getStart() == null || event.getEnd() == null) {
					continue;
				}
				long eventStartMillis = event.getStart().getDateTime().getValue();
				long eventEndMillis = event.getEnd().getDateTime().getValue();

				totalOccupiedDurationInMilliseconds += eventEndMillis - eventStartMillis;
			}

			double occupancyPercentage = (double) totalOccupiedDurationInMilliseconds / totalFreeDurationInMilliseconds * 100;
			double freePercentage = 100 - occupancyPercentage;

			Map<String, Object> stats = new HashMap<>();
			stats.put("occupancyPercentage", occupancyPercentage);
			stats.put("freePercentage", freePercentage);

			return ResponseEntity.ok(stats);

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GetMapping("/eventsThisMonth")
	public ResponseEntity<Map<String, Object>> getEventsThisMonth(@RequestHeader("Authorization") String authorizationHeader) {
		try {
			HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
			JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

			GoogleCredential credential = new GoogleCredential.Builder()
					.setJsonFactory(jsonFactory)
					.setTransport(httpTransport)
					.setClientSecrets(CLIENT_ID, CLIENT_SECRET)
					.build()
					.setAccessToken(getAccessTokenFromHeader(authorizationHeader))
					.createScoped(CalendarScopes.all());

			Calendar calendar = new Calendar.Builder(httpTransport, jsonFactory, credential)
					.setApplicationName(APPLICATION_NAME)
					.build();

			String calendarId = "primary"; 

			LocalDate now = new LocalDate();
			int currentYear = now.getYear();
			int currentMonth = now.getMonthOfYear();

			LocalDate startOfMonth = new LocalDate(currentYear, currentMonth, 1);
			LocalDate endOfMonth = startOfMonth.plusMonths(1);



			Events eventsResponse = calendar.events().list(calendarId)
					.setTimeMin(new DateTime(startOfMonth.toDate()))
					.setTimeMax(new DateTime(endOfMonth.toDate()))
					.execute();

			List<Event> events = eventsResponse.getItems();

			int numberOfEventsThisMonth = events.size();

			Map<String, Object> numeroTotal = new HashMap<>();
			numeroTotal.put("numberOfEventsThisMonth", numberOfEventsThisMonth);

			return ResponseEntity.ok(numeroTotal);

		} catch (IOException | GeneralSecurityException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GetMapping("/averageEventDuration")
	public ResponseEntity<Map<String, Object>> getAverageEventDuration(@RequestHeader("Authorization") String authorizationHeader) {
		try {
			HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
			JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

			GoogleCredential credential = new GoogleCredential.Builder()
					.setJsonFactory(jsonFactory)
					.setTransport(httpTransport)
					.setClientSecrets(CLIENT_ID, CLIENT_SECRET)
					.build()
					.setAccessToken(getAccessTokenFromHeader(authorizationHeader))
					.createScoped(CalendarScopes.all());

			Calendar calendar = new Calendar.Builder(httpTransport, jsonFactory, credential)
					.setApplicationName(APPLICATION_NAME)
					.build();

			String calendarId = "primary"; 

			Events eventsResponse = calendar.events().list(calendarId)
					.execute();

			List<Event> events = eventsResponse.getItems();
			ArrayList <Event> goodEvents = new ArrayList<Event>();
			for (Event event: events) {
				if (event.getStart() == null || event.getEnd() == null) {
					continue;
				}
				goodEvents.add(event);
			}
			Map<String, Object> averageDuration = new HashMap<>();

			int totalEvents = goodEvents.size();

			String averageDurationString = null;

			if (totalEvents < 1) {
				averageDurationString = "0 minutos";
				averageDuration.put("averageDuration", averageDurationString);
			}
			else {
				long totalDuration = 0;

				for (Event event : goodEvents) {

					long startTime = event.getStart().getDateTime().getValue();
					long endTime = event.getEnd().getDateTime().getValue();
					totalDuration += (endTime - startTime);
				}

				long averageDurationMillis = totalDuration / totalEvents;

				long hours = averageDurationMillis / (60 * 60 * 1000);
				long minutes = (averageDurationMillis % (60 * 60 * 1000)) / (60 * 1000);

				if (hours > 0) {
					averageDurationString = String.format("%d horas y %d minutos", hours, minutes);
				}
				else {
					averageDurationString = String.format("%d minutos", minutes);
				}

				averageDuration.put("averageDuration", averageDurationString);
			}



			return ResponseEntity.ok(averageDuration);

		} catch (IOException | GeneralSecurityException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}


	@GetMapping("/calendar")
	public ResponseEntity<Map<String, Object>> getStatsCalendar(
			@RequestHeader("Authorization") String authorizationHeader,
			@RequestParam String startDate,
	        @RequestParam String endDate,
	        @RequestParam String startTime,
	        @RequestParam String endTime,
	        @RequestParam List<String> selectedDays) {

		try {
			HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
			JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

			GoogleCredential credential = new GoogleCredential.Builder()
					.setJsonFactory(jsonFactory)
					.setTransport(httpTransport)
					.setClientSecrets(CLIENT_ID, CLIENT_SECRET)
					.build()
					.setAccessToken(getAccessTokenFromHeader(authorizationHeader))
					.createScoped(CalendarScopes.all());

			Calendar calendar = new Calendar.Builder(httpTransport, jsonFactory, credential)
					.setApplicationName(APPLICATION_NAME)
					.build();

			String calendarId = "primary"; // Change 'primary' to the desired calendar ID

			DateTime startDateTime = DateTime.parseRfc3339(startDate);
			DateTime endDateTime = DateTime.parseRfc3339(endDate);
			LocalTime time1 = LocalTime.parse(startTime);
			LocalTime time2 = LocalTime.parse(endTime);

			// Calcula la diferencia en milisegundos entre las fechas
			long diferenciaEnMillis = endDateTime.getValue() - startDateTime.getValue();

			// Convierte la diferencia de milisegundos a días
			long diferenciaEnDias = TimeUnit.MILLISECONDS.toDays(diferenciaEnMillis) + 1;
			
			// Resta un día (24 horas) en milisegundos
			long oneDayInMillis = 24 * 60 * 60 * 1000;
			DateTime adjustedEndDateTime = new DateTime(endDateTime.getValue() + oneDayInMillis);

			Events eventsResponse = calendar.events().list(calendarId)
					.setTimeMin(startDateTime)
					.setTimeMax(adjustedEndDateTime)
					.execute();

			List<Event> items = eventsResponse.getItems();

			long sumaMinsOcupado = 0;         
			int minutosInicioJornada = (time1.getHour() * 60) + time1.getMinute();
			int minutosFinJornada = (time2.getHour() * 60) + time2.getMinute();

			int longitud = minutosFinJornada - minutosInicioJornada;
			List<Integer> listaSlots = new ArrayList<>(Collections.nCopies(longitud*(int)diferenciaEnDias, 0));

			for (Event event : items) {
				
				//List<Integer> listaSlots = new ArrayList<>(Collections.nCopies(longitud, 0));
				DateTime eventStartDate = event.getStart().getDateTime();
				DateTime eventEndDate = event.getEnd().getDateTime();

				if (eventStartDate == null || eventEndDate == null) {
					continue;
				}

				java.util.Date date = new java.util.Date(eventStartDate.getValue());
				java.util.Calendar calendarDay = java.util.Calendar.getInstance();
				calendarDay.setTime(date);

				//Obtener dia de la semana (Domingo = 1, Lunes = 2 ...)
				int dayOfWeek = calendarDay.get(java.util.Calendar.DAY_OF_WEEK);
				String dayOfWeekString = convertDayOfWeekToString(dayOfWeek);

				boolean skipEvent = false;

				for(String day : selectedDays) {
					if(day.equals(dayOfWeekString)) {
						skipEvent = true;
						break;
					}
				}

				if(skipEvent) {
					continue;
				}

				LocalTime timeEventStart = LocalTime.parse(eventStartDate.toString().split("T")[1].substring(0, 5));
				LocalTime timeEventEnd = LocalTime.parse(eventEndDate.toString().split("T")[1].substring(0, 5));

				int minutosInicioEvento = (60 * timeEventStart.getHour()) + timeEventStart.getMinute();
				int minutosFinEvento = (60 * timeEventEnd.getHour()) + timeEventEnd.getMinute();

				minutosInicioEvento = Math.max(minutosInicioEvento, minutosInicioJornada) - minutosInicioJornada;
				minutosFinEvento = Math.min(minutosFinEvento, minutosFinJornada) - minutosInicioJornada;

				
				// Calcula el índice del inicio y fin del evento en listaSlots
			    long diffDaysInicio = TimeUnit.MILLISECONDS.toDays(eventStartDate.getValue() - startDateTime.getValue());
			    long diffDaysFin = TimeUnit.MILLISECONDS.toDays(eventEndDate.getValue() - startDateTime.getValue());

			    int indiceInicio = (int) diffDaysInicio * longitud + minutosInicioEvento;
			    int indiceFin = (int) diffDaysFin * longitud + minutosFinEvento;

			    for (int i = indiceInicio; i < indiceFin && i < listaSlots.size(); i++) {
			        listaSlots.set(i, 1);
			    }
				
				
			}	
			for (int j : listaSlots) {
				sumaMinsOcupado += j;
			}

			double occupancyPercentage = (double) sumaMinsOcupado / (longitud * diferenciaEnDias) * 100;
			double freePercentage = 100 - occupancyPercentage;

			Map<String, Object> stats = new HashMap<>();
			stats.put("occupancyPercentage", occupancyPercentage);
			stats.put("freePercentage", freePercentage);



			return ResponseEntity.ok(stats);

		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Error occurred: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GetMapping("/calendarString")
	public ResponseEntity<List<Map<String, String>>> getMatchingEvents(
			@RequestHeader("Authorization") String authorizationHeader,
			@RequestParam String startDate,
	        @RequestParam String endDate,
	        @RequestParam String keyword) {
		try {
			System.out.println(authorizationHeader);

			HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
			JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
			
			GoogleCredential credential = new GoogleCredential.Builder()
					.setJsonFactory(jsonFactory)
					.setTransport(httpTransport)
					.setClientSecrets(CLIENT_ID, CLIENT_SECRET)
					.build()
					.setAccessToken(getAccessTokenFromHeader(authorizationHeader))
					.createScoped(CalendarScopes.all());
			Calendar calendar = new Calendar.Builder(httpTransport, jsonFactory, credential)
					.setApplicationName(APPLICATION_NAME)
					.build();
			String calendarId = "primary"; // Cambiar 'primary' por el ID de calendario deseado
			DateTime startDateTime = DateTime.parseRfc3339(startDate);
			DateTime endDateTime = DateTime.parseRfc3339(endDate);


			long oneDayInMillis = 24 * 60 * 60 * 1000;
			DateTime adjustedEndDateTime = new DateTime(endDateTime.getValue() + oneDayInMillis);
			
			Events eventsResponse = calendar.events().list(calendarId)
					.setTimeMin(startDateTime)
					.setTimeMax(adjustedEndDateTime)
					.execute();
			List<Event> items = eventsResponse.getItems();
			List<Map<String, String>> matchingEvents = new ArrayList<>();

			// Convertir la palabra clave de búsqueda a minúsculas
			String normalizedSearchKeyword = keyword.toLowerCase();

			for (Event event : items) {
				String eventTitle = event.getSummary();
				String eventDescription = event.getDescription();

				// Verificar si 'start' es null antes de llamar a getDateTime()
				EventDateTime start = event.getStart();
				if (start != null && start.getDateTime() != null) {
					String eventStartTime = start.getDateTime().toString();

					// Convertir título y descripción a minúsculas para la comparación
					boolean titleMatches = eventTitle != null && eventTitle.toLowerCase().contains(normalizedSearchKeyword);
					boolean descriptionMatches = eventDescription != null && eventDescription.toLowerCase().contains(normalizedSearchKeyword);

					if (titleMatches || descriptionMatches) {
						Map<String, String> eventInfo = new HashMap<>();
						eventInfo.put("title", eventTitle);
						eventInfo.put("startTime", eventStartTime);
						matchingEvents.add(eventInfo);
					}
				}
			}
			return ResponseEntity.ok(matchingEvents);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Error occurred: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}




	private static String convertDayOfWeekToString(int dayOfWeekNumber) {
		String[] daysOfWeek = {
				"", "Domingo", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado"
		};

		if (dayOfWeekNumber >= 1 && dayOfWeekNumber <= 7) {
			return daysOfWeek[dayOfWeekNumber];
		} else {
			return "Invalid day of the week";
		}
	}


	private String getAccessTokenFromHeader(String authorizationHeader) {
		if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
			return authorizationHeader.substring(7);
		}
		return null;
	}

}

