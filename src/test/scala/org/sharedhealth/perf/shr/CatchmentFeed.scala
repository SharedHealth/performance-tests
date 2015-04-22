package org.sharedhealth.perf.shr

import io.gatling.core
import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation
import io.gatling.http.Predef._
import org.sharedhealth.perf.Login

import scala.concurrent.duration._

class CatchmentFeed extends Simulation {
  val feeder = core.Predef.csv("catchments").circular

  val httpConf = http
    .baseURL("http://shrperf.twhosted.com")
    .header("client_id", "18574")
    .header("From", "facilityPerfm@test.com")
    .acceptHeader("application/atom+xml")
    .acceptEncodingHeader("gzip")

  val time = 100 seconds

  var getAuthToken = scenario("Login")
    .repeat(1) {
      exec(Login.login)
      .exec(Login.getTokenFromSession)
  }

  var getCatchmentsFeed = http("catchment feed")
    .get("/catchments/${CATCHMENT}/encounters?updatedSince=2015-01-01")
    .header("X-Auth-Token", "${token}")
    .check()


  val catchmentFeed = scenario("catchment feed")
    .feed(feeder)
    .exec(Login.setTokenToSession)
    .during(time) {
      exec(getCatchmentsFeed)
  }
  setUp(
    getAuthToken.inject(atOnceUsers(1)),
    catchmentFeed.inject(
      nothingFor(5 seconds),
      atOnceUsers(100)
    ).protocols(httpConf)
  )
}

