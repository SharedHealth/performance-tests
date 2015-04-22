package org.sharedhealth.perf.mci

import io.gatling.core
import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation
import io.gatling.http.Predef._
import org.sharedhealth.perf.Login

import scala.concurrent.duration._

class CatchmentApprovals extends Simulation{
  val catchmentFeeder = core.Predef.csv("catchments").circular
  val hidFeeder = core.Predef.csv("patients.txt").circular

  val httpConf = http
    .baseURL("http://mciperf.twhosted.com")
    .header("client_id", "18574")
    .header("From", "facilityPerfm@test.com")
    .acceptEncodingHeader("gzip")

  val time = 100 seconds

  var getAuthToken = scenario("Login")
    .repeat(1) {
      exec(Login.login)
      .exec(Login.getTokenFromSession)
  }

  var getCatchmentsApprovals = http("Get Catchment Approvals")
    .get("/api/v1/catchments/${CATCHMENT}/approvals")
    .header("X-Auth-Token", "${token}")
    .check()

  var getCatchmentsApprovalsForPatient = http("Get Catchment Approvals For Patient")
    .get("/api/v1/catchments/${CATCHMENT}/approvals/${HEALTHID}")
    .header("X-Auth-Token", "${token}")
    .check()

  val catchmentApproval = scenario("Catchment Approvals")
    .feed(catchmentFeeder)
    .exec(Login.setTokenToSession)
    .during(time) {
      exec(getCatchmentsApprovals)
  }
  
  val catchmentApprovalForPatient = scenario("Catchment Approvals For Patient")
    .feed(catchmentFeeder)
    .feed(hidFeeder)
    .exec(Login.setTokenToSession)
    .during(time) {
      exec(getCatchmentsApprovalsForPatient)
  }

  setUp(
    getAuthToken.inject(atOnceUsers(1)),
    catchmentApproval.inject(
      nothingFor(5 seconds),
      atOnceUsers(50)
    ).protocols(httpConf),
    catchmentApprovalForPatient.inject(
      nothingFor(5 seconds),
      atOnceUsers(50)
    ).protocols(httpConf)
  )
}