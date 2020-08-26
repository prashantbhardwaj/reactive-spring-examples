package person.prashant.react.spring.demo.websocket;

import lombok.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Stream;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@SpringBootApplication
public class DemoWebsocketApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoWebsocketApplication.class, args);
    }

}

@Configuration
@Log4j2
class GreetingWebSocketConfiguration {
    @Bean
    SimpleUrlHandlerMapping simpleUrlHandlerMapping(WebSocketHandler wsh){
        return new SimpleUrlHandlerMapping(Map.of("/ws/greetings", wsh), 10);
    }

    @Bean
    WebSocketHandler webSocketHandler(GreetingService gs){
        return webSocketSession -> {
            var receive = webSocketSession
                    .receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .map(GreetingRequest::new)
                    .flatMap(gs::keepGreeting)
                    .map(GreetingResponse::getMessage)
                    .map(webSocketSession::textMessage)
                    .doOnEach(signal -> log.info(signal.getType()))
                    .doFinally(signal -> log.info("finally: " + signal.toString()));
            return webSocketSession.send(receive);
        };
    }

    @Bean
    WebSocketHandlerAdapter webSocketHandlerAdapter(){
        return new WebSocketHandlerAdapter();
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
