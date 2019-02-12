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

package unit.connectors

import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.Eventually
import org.scalatest.mockito.MockitoSugar
import uk.gov.hmrc.customs.notificationpushretry.config.ServiceConfiguration
import uk.gov.hmrc.customs.notificationpushretry.connectors.CustomsNotificationConnector
import uk.gov.hmrc.customs.notificationpushretry.model.ClientId
import uk.gov.hmrc.http._
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.{ExecutionContext, Future}

class CustomsNotificationConnectorSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach with Eventually {

  trait Setup {

    val X_CLIENT_ID_HEADER_NAME = "X-Client-ID"
    val clientId = "client-id"

    val mockServiceConfiguration: ServiceConfiguration = mock[ServiceConfiguration]
    val mockHttpClient: HttpClient = mock[HttpClient]
    val mockHttpResponse = mock[HttpResponse]

    val customsNotificationConnector = new CustomsNotificationConnector(mockServiceConfiguration, mockHttpClient)

    when(mockServiceConfiguration.baseUrl("customs-notification")).thenReturn("http://customs-notification.url")

  }

  "CustomsNotificationConnector" should {

    "return a count of blocked notifications" in new Setup {

      when(mockHttpClient.GET[HttpResponse](meq(s"http://customs-notification.url/blocked-count"))
        (any[HttpReads[HttpResponse]](), any[HeaderCarrier](), any[ExecutionContext])).thenReturn(Future.successful(mockHttpResponse))
      when(mockHttpResponse.body).thenReturn("<pushNotificationBlockedCount>1</pushNotificationBlockedCount>")

      val result: String = await(customsNotificationConnector.getNotifications(ClientId(clientId)))

      result shouldBe "<pushNotificationBlockedCount>1</pushNotificationBlockedCount>"
    }
  }
}
