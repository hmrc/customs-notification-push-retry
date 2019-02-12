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

package unit.controllers

import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import play.api.http.HeaderNames.ACCEPT
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.customs.api.common.logging.CdsLogger
import uk.gov.hmrc.customs.notificationpushretry.controllers.NotificationsController
import uk.gov.hmrc.customs.notificationpushretry.model.ClientId
import uk.gov.hmrc.customs.notificationpushretry.services.CustomsNotificationService
import uk.gov.hmrc.customs.notificationpushretry.validators.HeaderValidator
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

class NotificationsControllerSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach {

  private val mockCustomsNotification: CustomsNotificationService = mock[CustomsNotificationService]

  private val controller = new NotificationsController(mockCustomsNotification, new HeaderValidator(), mock[CdsLogger])

  "NotificationsController" should {

    val clientId = ClientId("ABCD")

    "respond with status 200 for a processed valid request" in {

      val validRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/blocked-count").withHeaders(ACCEPT -> "application/vnd.hmrc.1.0+xml", "X-Client-ID" -> clientId.toString)
      when(mockCustomsNotification.getNotifications(clientId)).thenReturn(Future.successful("<pushNotificationBlockedCount>1</pushNotificationBlockedCount>"))

      val result = await(controller.get().apply(validRequest))

      status(result) shouldBe OK
    }

    "respond with status 500 for when an error is returned from downstream" in {

      val validRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/blocked-count").withHeaders(ACCEPT -> "application/vnd.hmrc.1.0+xml", "X-Client-ID" -> clientId.toString)
      when(mockCustomsNotification.getNotifications(clientId)).thenReturn(Future.failed(new Exception("ERROR")))

      val result = await(controller.get().apply(validRequest))

      status(result) shouldBe INTERNAL_SERVER_ERROR
    }
  }
}
