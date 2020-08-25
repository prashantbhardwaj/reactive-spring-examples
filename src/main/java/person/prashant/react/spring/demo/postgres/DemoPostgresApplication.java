package person.prashant.react.spring.demo.postgres;

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
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import static org.springframework.data.domain.Sort.Order.desc;

@SpringBootApplication
public class DemoPostgresApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoPostgresApplication.class, args);
    }

}

@Component
@RequiredArgsConstructor
@Log4j2
class SampleDataInitializer {
    private final DatabaseClient databaseClient;

    @EventListener(ApplicationReadyEvent.class)
    public void ready(){
        log.info("start data initialization...");
        this.databaseClient.delete().from("reservation")
                .then().
                and(

                        this.databaseClient.insert()
                                .into("reservation")
                                //.nullValue("id", Integer.class)
                                .value("name", "Prashant")
                                .map((r, m) -> r.get("id", Integer.class)).all()
                                .log()
                )
                .thenMany(
                        this.databaseClient.select()
                                .from("reservation")
                                .orderBy(Sort.by(desc("id")))
                                .as(Reservation.class)
                                .fetch()
                                .all()
                                .log()
                )
                .subscribe(null, null, () -> log.info("initialization is done..."));
    }

}

interface ReservationRepository extends ReactiveCrudRepository<Reservation, Integer>{
    @Query("select * from reservation where name = $1 ")
    Flux<Reservation> findByName(String name);
}

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table
class Reservation {
    @Id
    private Integer id;
    private String name;
}
