import io.gatling.core
import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation
import io.gatling.http.Predef._

import scala.concurrent.duration._

class GetEncounters extends Simulation {
  val patientId = core.Predef.csv("patients.txt").random

  val httpConf = http
    .baseURL("http://172.18.46.2:8081")
    .header("X-Auth-Token", "8dad0c07-caf8-48a9-ac2a-1815a9aa11a1")
    .acceptHeader("application/atom+xml")
    .acceptEncodingHeader("gzip")

  val time = 600 seconds

  val createEncounters = scenario("create encounters")
    .feed(patientId)
    .during(time) {
    exec(http("get encounters")
      .get("/patients/${HEALTHID}/encounters")
    )
  }
  setUp(
    createEncounters.inject(atOnceUsers(100)).protocols(httpConf)
  )
}

