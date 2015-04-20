import io.gatling.core
import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation
import io.gatling.http.Predef._
import io.gatling.http.request.builder.HttpRequestBuilder

import scala.concurrent.duration._

class CatchmentFeed extends Simulation {
  val feeder = core.Predef.csv("catchments").circular
  @volatile var auth_token = ""

  val login = http("login")
      .post("http://hrmtest.dghs.gov.bd/api/1.0/sso/signin")
      .header("X-Auth-Token", "6b83bf41083c7f37373bc12fb0dac856b95e95e5dccbf71361127fb9efd3a411")
      .header("client_id", "18574")
      .formParam("email", "facilityPerfm@test.com")
      .formParam("password", "thoughtworks").check(jsonPath("$.access_token")
      .saveAs("token")
    )
  
  val httpConf = http
    .baseURL("http://172.18.46.2:8082")
    .header("client_id", "18574")
    .header("From", "facilityPerfm@test.com")
    .acceptHeader("application/atom+xml")
    .acceptEncodingHeader("gzip")

  val time = 100 seconds

  var getAuthToken = scenario("Login")
    .repeat(1) {
    exec(login)
      .exec(session => {
      auth_token = session("token").as[String]
      session
    })
  }

  var getCatchmentsFeed = http("catchment feed")
    .get("/catchments/${CATCHMENT}/encounters?updatedSince=2015-01-01")
    .header("X-Auth-Token", "${token}")
    .check()


  val catchmentFeed = scenario("catchment feed")
    .feed(feeder)
    .exec(_.set("token", auth_token))
    .during(time) {
    exec(getCatchmentsFeed)
  }
  setUp(
    getAuthToken.inject(atOnceUsers(1)),
    catchmentFeed.inject(
      nothingFor(5 seconds),
      atOnceUsers(50)
    ).protocols(httpConf)
  )
}

