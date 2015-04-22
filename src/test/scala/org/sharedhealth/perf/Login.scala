package org.sharedhealth.perf

import io.gatling.core.Predef._
import io.gatling.core.validation.Validation
import io.gatling.http.Predef._

object Login {
  @volatile var auth_token = ""

  val login = http("login")
    .post("http://hrmtest.dghs.gov.bd/api/1.0/sso/signin")
    .header("X-Auth-Token", "6b83bf41083c7f37373bc12fb0dac856b95e95e5dccbf71361127fb9efd3a411")
    .header("client_id", "18574")
    .formParam("email", "facilityPerfm@test.com")
    .formParam("password", "thoughtworks").check(jsonPath("$.access_token")
    .saveAs("token")
    )

  val getTokenFromSession: (Session) => Validation[Session] = session => {
    auth_token = session("token").as[String]
    session
  }

  val setTokenToSession: (Session) => Validation[Session] = _.set("token", auth_token)

}