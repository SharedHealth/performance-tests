package org.sharedhealth.perf.mci

import java.net.URLEncoder

import io.gatling.core
import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation
import io.gatling.http.Predef._
import org.sharedhealth.perf.Login

import scala.concurrent.duration._

class CreatePatients extends Simulation {
  val hidFeeder = core.Predef.csv("patients.txt").circular
  val random = new util.Random
  val nameFeeder = Iterator.continually(Map("lastname" -> Math.abs(random.nextInt()),
  "firstname" -> Math.abs(random.nextInt())))
  
  val httpConf = http
    .baseURL("http://mciperf.twhosted.com:8082")
    .header("client_id", "18574")
    .header("From", "facilityPerfm@test.com")
    .contentTypeHeader("application/json")
    .acceptEncodingHeader("gzip").check(status.is(201))

  val patient = http("Patient")
    .post("/api/v1/patients")
    .header("X-Auth-Token", "${token}")
    .body(ELFileBody("request-bodies/patients/patient.json"))

  val patientWithNid = http("Patient With NID")
    .post("/api/v1/patients")
    .header("X-Auth-Token", "${token}")
    .body(ELFileBody("request-bodies/patients/patient_with_nid.json"))
  
  val patientUpdate = http("Update Patient")
    .put("/api/v1/patients/${HEALTHID}")
    .header("X-Auth-Token", "${token}")
    .body(ELFileBody("request-bodies/patients/patient_update.json"))


  val time = 100 seconds

  var getAuthToken = scenario("Login")
    .repeat(1) {
      exec(Login.login)
      .exec(Login.getTokenFromSession)
  }

  val createPatients = scenario("create patients")
    .feed(nameFeeder)
    .exec(Login.setTokenToSession)
    .during(time) {
      exec(patient)
  }

  val createPatientsWithNid = scenario("create patients with NID")
    .exec(Login.setTokenToSession)
    .during(time) {
      exec(patientWithNid)
  }

  val updatePatient = scenario("Update Patient")
    .feed(hidFeeder)
    .exec(Login.setTokenToSession)
    .during(time) {
      exec(patientUpdate)
  }


  setUp(
    getAuthToken.inject(atOnceUsers(1)),
    createPatients.inject(
      nothingFor(5 seconds),
      atOnceUsers(2), rampUsers(2) over(5 seconds)).protocols(httpConf)
//    createPatientsWithNid.inject(
//      nothingFor(5 seconds),
//      atOnceUsers(50)).protocols(httpConf)
//    updatePatient.inject(
//      nothingFor(5 seconds),
//      atOnceUsers(50)).protocols(httpConf)
  )
}

