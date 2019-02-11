/*
 * Copyright 2019 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package component

import java.util.UUID

import com.github.tomakehurst.wiremock.client.WireMock.{status => _, _}
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Helpers}

class GetBlockedNotificationCountSpec extends ComponentSpec with ExternalServices {


  override val clientId: String = UUID.randomUUID().toString

  override def beforeAll(): Unit = {
    super.beforeAll()
    startMockServer()
  }

  override protected def beforeEach() {
    resetMockServer()
  }

  override def afterAll(): Unit = {
    super.afterAll()
    stopMockServer()
  }


  feature("GET count of blocked notifications from the customs notification service") {

    info("As a 3rd Party")
    info("I want to successfully a count of blocked notification locations by client id")

    val validRequest = FakeRequest("GET", "/blocked-count").
      withHeaders(ACCEPT -> "application/vnd.hmrc.1.0+xml", xClientIdHeader -> clientId)

    scenario("Successful GET and 3rd party receives the blocked notification count") {

      stubForPushNotificationBlockedCount()

      Given("There are blocked notifications in customs notification")

      When("You call making the 'GET' action to the customs-notification-push-retry service")
      val result = route(app, validRequest).get

      Then("You will receive all notifications for your client id")
      Helpers.status(result) shouldBe OK

      val expectedBody = scala.xml.Utility.trim(<pushNotificationBlockedCount>100</pushNotificationBlockedCount>)

      Helpers.contentAsString(result).stripMargin shouldBe expectedBody.toString()

      And("The count of blocked notifications will be retrieved")
      verify(getRequestedFor(urlMatching("/blocked-count")))
    }

    scenario("Failed GET and 3rd party receives an error message") {

      stubFor400ErrorPushNotificationBlockedCount()

      Given("There are blocked notifications in customs notification")

      When("You call making the 'GET' action to the customs-notification-push-retry service")
      val result = route(app, validRequest).get

      Then("You will receive all notifications for your client id")
      Helpers.status(result) shouldBe INTERNAL_SERVER_ERROR

    }

    scenario("Missing Accept Header") {
      Given("You do not provide the Accept Header")
      val request = validRequest.copyFakeRequest(headers = validRequest.headers.remove(ACCEPT))

      When("You call make the 'GET' call to the customs-notification-push-retry service")
      val result = route(app, request).get

      Then("You will be returned a 406 error response")
      status(result) shouldBe NOT_ACCEPTABLE
      contentAsString(result) shouldBe ""
    }

    scenario("Missing X-Client-Id Header") {

      Given("The platform does not inject a X-Client-Id Header")
      val request = validRequest.copyFakeRequest(headers = validRequest.headers.remove(xClientIdHeader))

      When("You call make the 'GET' call to the customs-notification-push-retry service ")
      val result = route(app, request).get

      Then("You will be returned a 500 error response")
      status(result) shouldBe INTERNAL_SERVER_ERROR
      contentAsString(result) shouldBe ""
    }
  }

}
