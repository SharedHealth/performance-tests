import io.gatling.core
import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation
import io.gatling.http.Predef._

import scala.concurrent.duration._

class Encounters extends Simulation {
  val patientId = core.Predef.csv("patients.txt").queue

  val httpConf = http
    .baseURL("http://172.18.46.2:8081")
    .header("X-Auth-Token", "8dad0c07-caf8-48a9-ac2a-1815a9aa11a1")
    .contentTypeHeader("application/xml;charset=UTF-8")

  val time = 1800 seconds

  val createEncounters = scenario("create encounters")
    .feed(patientId)
    .during(time) {
//      exec(http("registration")
//        .post("/patients/${HEALTHID}/encounters").body(ELFileBody("request-bodies/reg.xml"))
//      )
      exec(http("encounter")
      .post("/patients/${HEALTHID}/encounters").body(ELFileBody("request-bodies/enc.xml"))
      )
//      .exec(http("big encounter")
//      .post("/patients/${HEALTHID}/encounters").body(ELFileBody("request-bodies/bigenc.xml"))
//      )
  }
  setUp(
    createEncounters.inject(atOnceUsers(50)).protocols(httpConf)
  )
}

