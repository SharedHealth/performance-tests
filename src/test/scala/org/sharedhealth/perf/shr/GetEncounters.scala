package org.sharedhealth.perf.shr

import io.gatling.core
import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation
import io.gatling.http.Predef._
import org.sharedhealth.perf.Login

import scala.concurrent.duration._

class GetEncounters extends Simulation {
  val patientId = core.Predef.csv("patients.txt").circular

  val httpConf = http
    .baseURL("http://shrperf.twhosted.com")
    .header("client_id", "18574")
    .header("From", "facilityPerfm@test.com")
    .acceptHeader("application/atom+xml")
    .acceptEncodingHeader("gzip")

  val encounter = http("get encounters")
    .get("/patients/${HEALTHID}/encounters")
    .header("X-Auth-Token", "${token}")
  
  val time = 100 seconds

  var getAuthToken = scenario("Login")
    .repeat(1) {
      exec(Login.login)
      .exec(Login.getTokenFromSession)
  }


  val getEncounters = scenario("get encounters")
    .feed(patientId)
    .exec(Login.setTokenToSession)
    .during(time) {
      exec(encounter)
  }
  setUp(
    getAuthToken.inject(atOnceUsers(1)),
    getEncounters.inject(
      nothingFor(5 seconds),
      atOnceUsers(100)
    ).protocols(httpConf)
  )
}

