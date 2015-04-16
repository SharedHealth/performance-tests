import io.gatling.core
import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation
import io.gatling.http.Predef._

import scala.concurrent.duration._

class CatchmentFeed extends Simulation {
  val feeder = core.Predef.csv("catchments").circular

  val httpConf = http
    .baseURL("http://localhost:8081")
    .header("X-Auth-Token", "2d926ff2-a41a-4a21-aef1-b5424d984b2a")
    .header("client_id", "18552")
    .header("From", "rappasam@thoughtworks.com")
    .acceptHeader("application/atom+xml")
    .acceptEncodingHeader("gzip")

  val time = 60 seconds

  val catchmentFeed = scenario("catchment feed")
    .feed(feeder)
    .during(time) {
    
    exec(http("login")
      .post("http://172.18.46.56:8080/signin")
      .header("X-Auth-Token", "1c2a599423203f639dcdd8574ac5391dd67d21316ea30ee364c8a8787fb79dd3")
      .header("client_id", "18549")
      .formParam("email", "rappasam@thoughtworks.com")
      .formParam("password", "thoughtworks").check(jsonPath("$.access_token")
      .saveAs("token")))
    
    .exec(http("catchment feed")
      .get("/catchments/${CATCHMENT}/encounters?updatedSince=2015-01-01")
      .header("X-Auth-Token", "${token}")
      .check()
    )
  }
  setUp(
    catchmentFeed.inject(atOnceUsers(50)).protocols(httpConf)
  )
}

