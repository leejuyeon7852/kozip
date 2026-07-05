package simulations;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import java.util.List;
import java.util.Map;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

public class AddressSearchSimulation extends Simulation {

    HttpProtocolBuilder httpProtocol = http
            .baseUrl("http://localhost:8080")
            .acceptHeader("application/json");

    FeederBuilder<Object> feeder = listFeeder(
            List.of(
                    Map.of("query", "06236"),
                    Map.of("query", "경기도"),
                    Map.of("query", "전북"),
                    Map.of("query", "자하문로 94"),
                    Map.of("query", "판교역로 166"),
                    Map.of("query", "테헤란로"),
                    Map.of("query", "강남대로"),
                    Map.of("query", "고려대"),
                    Map.of("query", "강남구 역삼동"),
                    Map.of("query", "동덕여자대학교")
            )
    ).random();

    ScenarioBuilder scn = scenario("주소 검색 부하테스트")
            .feed(feeder)
            .exec(
                    http("주소 검색")
                            .get("/api/address")
                            .queryParam("query", "#{query}")
                            .check(status().is(200))
            );

    {
        setUp(
                scn.injectOpen(
                        atOnceUsers(1),
                        nothingFor(5),
                        rampUsers(10).during(10),
                        nothingFor(5),
                        rampUsers(50).during(10),
                        nothingFor(5),
                        rampUsers(100).during(10)
                )
        ).protocols(httpProtocol);
    }
}
