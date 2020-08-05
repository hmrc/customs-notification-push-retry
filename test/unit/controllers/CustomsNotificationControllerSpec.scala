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

package unit.controllers

import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.HeaderNames.ACCEPT
import play.api.mvc._
import play.api.test.{FakeRequest, Helpers}
import play.api.test.Helpers._
import uk.gov.hmrc.customs.api.common.logging.CdsLogger
import uk.gov.hmrc.customs.notificationpushretry.controllers.CustomsNotificationController
import uk.gov.hmrc.customs.notificationpushretry.model.ClientId
import uk.gov.hmrc.customs.notificationpushretry.services.CustomsNotificationService
import uk.gov.hmrc.customs.notificationpushretry.validators.HeaderValidator
import util.UnitSpec
import util.MockitoPassByNameHelper.PassByNameVerifier

import scala.concurrent.Future

class CustomsNotificationControllerSpec extends UnitSpec
  with MockitoSugar
  with BeforeAndAfterEach {

  private val mockService: CustomsNotificationService = mock[CustomsNotificationService]
  private val mockLogger: CdsLogger = mock[CdsLogger]
  private val controllerComponents   = Helpers.stubControllerComponents()
  private val controller: CustomsNotificationController = new CustomsNotificationController(mockService, new HeaderValidator(controllerComponents), controllerComponents, mockLogger)
  private val clientId: ClientId = ClientId("ABCD")
  private val getRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/blocked-count").withHeaders(ACCEPT -> "application/vnd.hmrc.1.0+xml", "X-Client-ID" -> clientId.toString)
  private val deleteRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("DELETE", "/blocked-flag").withHeaders(ACCEPT -> "application/vnd.hmrc.1.0+xml", "X-Client-ID" -> clientId.toString)

  override def beforeEach(): Unit = {
    reset(mockService, mockLogger)
  }

  "NotificationsController" should {

    "for get endpoint" should {
      "respond with status 200 for a processed valid request" in {
        when(mockService.getNotifications(clientId)).thenReturn(Future.successful("<pushNotificationBlockedCount>1</pushNotificationBlockedCount>"))

        val result = await(controller.get().apply(getRequest))

        status(result) shouldBe OK
        logVerifier("info", "Getting blocked count for client id: ABCD")
      }

      "respond with status 500 for when an error is returned from upstream service" in {
        when(mockService.getNotifications(clientId)).thenReturn(Future.failed(new Exception("ERROR")))

        val result = await(controller.get().apply(getRequest))

        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }

    "for delete endpoint" should {
      "respond with status 204 for a valid request that deletes blocked flags" in {
        when(mockService.deleteBlocked(clientId)).thenReturn(Future.successful(NO_CONTENT))

        val result = await(controller.deleteBlocked().apply(deleteRequest))

        status(result) shouldBe NO_CONTENT
        logVerifier("info", "calling delete blocked-flag for client id: ABCD")
        logVerifier("debug", "delete response status was 204")
      }

      "respond with status 404 for a valid request that doesn't delete blocked flags" in {
        when(mockService.deleteBlocked(clientId)).thenReturn(Future.successful(NOT_FOUND))

        val result = await(controller.deleteBlocked().apply(deleteRequest))

        status(result) shouldBe NOT_FOUND
      }

      "respond with status 500 for when an error is returned from upstream service" in {
        when(mockService.deleteBlocked(clientId)).thenReturn(Future.failed(new Exception("ERROR")))

        val result = await(controller.deleteBlocked().apply(deleteRequest))

        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }
  }

  private def logVerifier(logLevel: String, logText: String) = {
    PassByNameVerifier(mockLogger, logLevel)
      .withByNameParam(logText)
      .verify()
  }

}
