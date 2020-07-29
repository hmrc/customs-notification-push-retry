/*
 * Copyright 2020 HM Revenue & Customs
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
import component.CustomsNotificationExternalServicesConfig.{BlockedCountEndpoint, BlockedFlagEndpointWithContext, BlockedFlagEndpoint, BlockedCountEndpointWithContext}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.util.control.NonFatal
import scala.xml.Utility.trim
import scala.xml.{Node, NodeSeq, Utility, XML}

class CustomsNotificationPushRetrySpec extends ComponentSpec with ExternalServices {

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

  private val MissingAcceptHeaderError =
    <errorResponse>
      <code>ACCEPT_HEADER_INVALID</code>
      <message>The accept header is missing or invalid</message>
    </errorResponse>

  private val InternalServerError =
    """<?xml version="1.0" encoding="UTF-8"?>
      |<errorResponse>
      |  <code>INTERNAL_SERVER_ERROR</code>
      |  <message>Internal server error</message>
      |</errorResponse>
    """.stripMargin

  val validHeaders = Seq(ACCEPT -> "application/vnd.hmrc.1.0+xml", xClientIdHeader -> clientId)

  private val validCountRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", BlockedCountEndpoint).
    withHeaders(validHeaders: _*)

  private val validDeleteRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("DELETE", BlockedFlagEndpoint).
    withHeaders(validHeaders: _*)

  feature("count blocked notifications via the customs notification service") {

    scenario("Successful GET and 3rd party receives the blocked notification count") {
      info("As a 3rd Party")
      info("I want to successfully count notification blocked flags by client id")

      stubForGetBlockedFlagCount(OK, <pushNotificationBlockedCount>100</pushNotificationBlockedCount>)

      Given("There are blocked notifications in customs notification")

      When("You call the 'GET' action on the customs-notification-push-retry service")
      val result = route(app, validCountRequest).get

      Then("You will receive all notifications for your client id")
      status(result) shouldBe OK

      And("The body is as expected")
      val expectedBody = trim(<pushNotificationBlockedCount>100</pushNotificationBlockedCount>)
      contentAsString(result) shouldBe expectedBody.toString()

      And("The count of blocked notifications has been called")
      verify(getRequestedFor(urlMatching(BlockedCountEndpointWithContext)))
    }

    scenario("Failed GET and 3rd party receives an error message") {
      stubForGetBlockedFlagCount(NOT_ACCEPTABLE, MissingAcceptHeaderError)

      Given("There are blocked notifications in customs notification")

      When("You call the 'GET' action on the customs-notification-push-retry service")
      val result = route(app, validCountRequest).get

      Then("You will receive an error message")
      status(result) shouldBe INTERNAL_SERVER_ERROR

      And("The body is as expected")
      string2xml(contentAsString(result)) shouldBe string2xml(InternalServerError)

      And("The count of blocked notifications has been called")
      verify(getRequestedFor(urlMatching(BlockedCountEndpointWithContext)))
    }

  }

  feature("delete blocked notification flags via the customs notification service") {

    scenario("Successful DELETE and 3rd party receives confirmation") {
      Given("There are notifications with blocked flags in customs notification")
      stubForDeleteBlockedFlagCount(NO_CONTENT, NodeSeq.Empty)

      When("The 'DELETE' action is called on the customs-notification-push-retry service")
      val result = route(app, validDeleteRequest).get

      Then("Blocked flags will be deleted and confirmation response is returned")
      status(result) shouldBe NO_CONTENT

      And("The body is as expected")
      contentAsString(result) shouldBe empty

      And("The delete blocked notification flags has been called")
      verify(deleteRequestedFor(urlMatching(BlockedFlagEndpointWithContext)))
    }

    scenario("Failed DELETE and 3rd party receives error") {
      Given("There are notifications with blocked flags in customs notification")
      stubForDeleteBlockedFlagCount(INTERNAL_SERVER_ERROR, NodeSeq.Empty)

      When("The 'DELETE' action is called on the customs-notification-push-retry service")
      val result = route(app, validDeleteRequest).get

      Then("No blocked flags are deleted and an error response is returned")
      status(result) shouldBe INTERNAL_SERVER_ERROR

      And("The body is as expected")
      string2xml(contentAsString(result)) shouldBe string2xml(InternalServerError)

      And("The delete blocked notification flags has been called")
      verify(deleteRequestedFor(urlMatching(BlockedFlagEndpointWithContext)))
    }

  }


    protected def string2xml(s: String): Node = {
    val xml = try {
      XML.loadString(s)
    } catch {
      case NonFatal(thr) => fail("Not an xml: " + s, thr)
    }
    Utility.trim(xml)
  }

}
