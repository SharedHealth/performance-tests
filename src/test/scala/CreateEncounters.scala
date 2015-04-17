import io.gatling.core
import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation
import io.gatling.http.Predef._

import scala.concurrent.duration._

class CreateEncounters extends Simulation {
  val patientId = core.Predef.csv("patients.txt").circular
  @volatile var auth_token = "tmvpjorMeF8rabzpYW6z4Yzt7VCUU6ucuoA1sVCmg"

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
    .contentTypeHeader("application/xml;charset=UTF-8")
    .acceptEncodingHeader("gzip")

  val register = http("registration")
    .post("/patients/${HEALTHID}/encounters")
    .header("X-Auth-Token", "${token}")
    .body(ELFileBody("request-bodies/reg.xml"))

  val smallEncounter = http("encounter")
    .post("/patients/${HEALTHID}/encounters")
    .header("X-Auth-Token", "${token}")
    .body(ELFileBody("request-bodies/enc.xml"))

  val bigEncounter = http("big encounter")
    .post("/patients/${HEALTHID}/encounters")
    .header("X-Auth-Token", "${token}")
    .body(ELFileBody("request-bodies/bigenc.xml"))


  val time = 1200 seconds

  var getAuthToken = scenario("Login")
    .repeat(1) {
    exec(login)
      .exec(session => {
      auth_token = session("token").as[String]
      session
    })
  }

  val createEncounters = scenario("create encounters")
    .feed(patientId)
    .exec(_.set("token", auth_token))
    .during(time) {
    exec(register)
    .exec(smallEncounter)
    .exec(bigEncounter)
  }


  setUp(
    getAuthToken.inject(atOnceUsers(1)),
    createEncounters.inject(
      nothingFor(5 seconds),
      atOnceUsers(50)).protocols(httpConf)
  )
}

