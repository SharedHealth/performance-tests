import io.gatling.core
import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation
import io.gatling.http.Predef._

import scala.concurrent.duration._

class CreateEncounters extends Simulation {
  val patientId = core.Predef.csv("patients.txt").random

  val httpConf = http
    .baseURL("http://localhost:8081")
    .header("client_id", "18549")
    .header("From", "dmishra@thoughtworks.com")
    .contentTypeHeader("application/xml;charset=UTF-8")
    .acceptEncodingHeader("gzip")


  val time = 240 seconds

  val createEncounters = scenario("create encounters")
    .feed(patientId)
    .during(time) {
    
      exec(http("login")
        .post("http://172.18.46.56:8080/signin")
        .header("X-Auth-Token", "1c2a599423203f639dcdd8574ac5391dd67d21316ea30ee364c8a8787fb79dd3")
        .header("client_id", "18549")
        .formParam("email", "dmishra@thoughtworks.com")
        .formParam("password", "thoughtworks").check(jsonPath("$.access_token")
        .saveAs("token")))
        
      .exec(http("registration")
        .post("/patients/${HEALTHID}/encounters")
        .header("X-Auth-Token", "${token}")
        .body(ELFileBody("request-bodies/reg.xml")))
        
      .exec(http("encounter")
        .post("/patients/${HEALTHID}/encounters")
        .header("X-Auth-Token", "${token}")
        .body(ELFileBody("request-bodies/enc.xml")))
        
      .exec(http("big encounter")
      .post("/patients/${HEALTHID}/encounters")
      .header("X-Auth-Token", "${token}")
      .body(ELFileBody("request-bodies/bigenc.xml")))
  }
  setUp(
    createEncounters.inject(atOnceUsers(100)).protocols(httpConf)
  )
}

