package person.prashant.react.spring.demo.mongo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@SpringBootApplication
public class DemoMongoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoMongoApplication.class, args);
	}

}

@Component
@RequiredArgsConstructor
@Log4j2
class SampleDataInitializer {
	private final ReservationRepository reservationRepository;

	@EventListener(ApplicationReadyEvent.class)
	public void ready(){
		Flux<Reservation> reservations = Flux
				.just("Prashant", "Anu", "Dhanya", "Bhaargav", "Ashok", "Asha", "Shweta", "Divya")
				.map(name -> new Reservation(null, name))
				.flatMap(res -> this.reservationRepository.save(res));
		//.subscribe(log::info);
		this.reservationRepository
				.deleteAll()
				.thenMany(reservations)
				//.thenMany(this.reservationRepository.findAll())
				.subscribe(log::info);

	}

}

interface ReservationRepository extends ReactiveCrudRepository<Reservation, String>{
	Flux<Reservation> findByName(String name);
}

@Document
@Data
@AllArgsConstructor
@NoArgsConstructor
class Reservation {
	@Id
	private String id;
	private String name;
}
