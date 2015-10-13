package org.sharedhealth.perf.shr

import io.gatling.core
import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation
import io.gatling.http.Predef._
import org.sharedhealth.perf.Login

import scala.concurrent.duration._

class CreateEncounters extends Simulation {
  val patientId = core.Predef.csv("patients.txt").circular

  val httpConf = http
    .baseURL("http://shrperf.twhosted.com")
    .header("client_id", "18574")
    .header("From", "facilityPerfm@test.com")
    .contentTypeHeader("application/xml;charset=UTF-8")
    .acceptEncodingHeader("gzip")

  val registration = http("registration")
    .post("/patients/${HEALTHID}/encounters")
    .header("X-Auth-Token", "${token}")
    .body(ELFileBody("request-bodies/reg.xml"))

  val encounter = http("encounter")
    .post("/patients/${HEALTHID}/encounters")
    .header("X-Auth-Token", "${token}")
    .body(ELFileBody("request-bodies/enc.xml"))

  val bigEncounter = http("big encounter")
    .post("/patients/${HEALTHID}/encounters")
    .header("X-Auth-Token", "${token}")
    .body(ELFileBody("request-bodies/bigenc.xml"))


  val time = 900 seconds

  var getAuthToken = scenario("Login")
    .repeat(1) {
    exec(Login.login)
      .exec(Login.getTokenFromSession)
  }

  val register = scenario("register")
    .feed(patientId)
    .exec(Login.setTokenToSession)
    .during(time) {
      exec(registration)
  }
  
  val createEncounter = scenario("create encounters")
    .feed(patientId)
    .exec(Login.setTokenToSession)
    .during(time) {
      exec(encounter)
  }
  
  val createBigEncounter = scenario("create big encounters")
    .feed(patientId)
    .exec(Login.setTokenToSession)
    .during(time) {
      exec(bigEncounter)
  }


  setUp(
    getAuthToken.inject(atOnceUsers(1)),
    register.inject(
      nothingFor(5 seconds),
      atOnceUsers(10)).protocols(httpConf),
    createEncounter.inject(
      nothingFor(5 seconds),
      atOnceUsers(10)).protocols(httpConf),
    createBigEncounter.inject(
      nothingFor(5 seconds),
      atOnceUsers(10)).protocols(httpConf)
  )
}

