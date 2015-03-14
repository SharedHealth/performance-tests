import io.gatling.core
import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation
import io.gatling.http.Predef._

import scala.concurrent.duration._

class CatchmentFeed extends Simulation {
  val feeder = core.Predef.csv("catchments").circular

  val httpConf = http
    .baseURL("http://172.18.46.2")
    .header("X-Auth-Token", "8dad0c07-caf8-48a9-ac2a-1815a9aa11a1")
    .acceptHeader("application/atom+xml")
    .acceptEncodingHeader("gzip")

  val time = 60 seconds

  val catchmentFeed = scenario("catchment feed")
    .feed(feeder)
    .during(time) {
    exec(http("catchment feed")
      .get("/catchments/${CATCHMENT}/encounters?updatedSince=2015-01-01").header("facilityId","${FACILITY}")
    .check()
    )
  }
  setUp(
    catchmentFeed.inject(atOnceUsers(50)).protocols(httpConf)
  )
}

