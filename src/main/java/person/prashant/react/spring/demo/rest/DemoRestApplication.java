package person.prashant.react.spring.demo.rest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.Instant;
import java.util.stream.Stream;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@SpringBootApplication
public class DemoRestApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoRestApplication.class, args);
    }


    @Bean
    RouterFunction<ServerResponse> routes(GreetingService gs){
        return route()
                .GET("/functional/greeting/{name}", serverRequest -> ServerResponse.ok().body(gs.greetOnce(new GreetingRequest(serverRequest.pathVariable("name"))), GreetingResponse.class))
                .GET("/functional/greetings/{name}", serverRequest -> ServerResponse.ok().contentType(MediaType.TEXT_EVENT_STREAM).body(gs.keepGreeting(new GreetingRequest(serverRequest.pathVariable("name"))), GreetingResponse.class))
                .build();
    }

}

@RestController
@RequiredArgsConstructor
class GreetingRestController {
    private final GreetingService gs;

    @GetMapping("/mvc/greeting/{name}")
    Mono<GreetingResponse> greet(@PathVariable String name){
        return this.gs.greetOnce(new GreetingRequest(name));
    }
}

@Component
class GreetingService {
    private GreetingResponse greet(String name){
        return new GreetingResponse("Hello " + name + " @" + Instant.now());
    }

    public Mono<GreetingResponse> greetOnce(GreetingRequest request){
        return Mono.just(greet(request.getName()));
    }

    public Flux<GreetingResponse> keepGreeting(GreetingRequest request){
        return Flux
                .fromStream(Stream.generate(() -> greet(request.getName())))
                .delayElements(Duration.ofSeconds(1));
                //.subscribeOn(Schedulers.elastic());
    }
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class GreetingRequest {
    private String name;
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class GreetingResponse {
    private String message;
}
