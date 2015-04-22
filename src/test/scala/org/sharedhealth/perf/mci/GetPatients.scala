package org.sharedhealth.perf.mci

import io.gatling.core
import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation
import io.gatling.http.Predef._
import org.sharedhealth.perf.Login

import scala.concurrent.duration._

class GetPatients extends Simulation {
  val hidFeeder = core.Predef.csv("patients.txt").circular
  val nidFeeder = core.Predef.csv("nids.txt").circular

  val httpConf = http
    .baseURL("http://mciperf.twhosted.com")
    .header("client_id", "18574")
    .header("From", "facilityPerfm@test.com")
    .acceptEncodingHeader("gzip")

  val patientByHID = http("Patient by HID")
    .get("/api/v1/patients/${HEALTHID}")
    .header("X-Auth-Token", "${token}")

  val patientByNID = http("Patient by NID")
    .get("/api/v1/patients/?nid=${NID}")
    .header("X-Auth-Token", "${token}")

  val time = 100 seconds

  var getAuthToken = scenario("Login")
    .repeat(1) {
      exec(Login.login)
      .exec(Login.getTokenFromSession)
  }

  val getPatientsByHID = scenario("Get Patient By HID")
    .feed(hidFeeder)
    .exec(Login.setTokenToSession)
    .during(time) {
      exec(patientByHID)
  }
  val getPatientsByNID = scenario("Get Patient By NID")
    .feed(nidFeeder)
    .exec(Login.setTokenToSession)
    .during(time) {
      exec(patientByNID)
  }

  setUp(
    getAuthToken.inject(atOnceUsers(1)),
    getPatientsByHID.inject(
      nothingFor(5 seconds),
      atOnceUsers(50)).protocols(httpConf),
    getPatientsByNID.inject(
      nothingFor(5 seconds),
      atOnceUsers(50)).protocols(httpConf)
  )

}