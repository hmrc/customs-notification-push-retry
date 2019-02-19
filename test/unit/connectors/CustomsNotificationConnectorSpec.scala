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

import component.CustomsNotificationExternalServicesConfig.{BlockedFlagEndpointWithContext, BlockedCountEndpointWithContext}
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.Eventually
import org.scalatest.mockito.MockitoSugar
import play.api.test.Helpers._
import play.mvc.Http.Status.NOT_FOUND
import uk.gov.hmrc.customs.api.common.config.{ServiceConfig, ServiceConfigProvider}
import uk.gov.hmrc.customs.api.common.logging.CdsLogger
import uk.gov.hmrc.customs.notificationpushretry.connectors.CustomsNotificationConnector
import uk.gov.hmrc.customs.notificationpushretry.model.ClientId
import uk.gov.hmrc.http._
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.play.test.UnitSpec
import util.MockitoPassByNameHelper.PassByNameVerifier

import scala.concurrent.{ExecutionContext, Future}

class CustomsNotificationConnectorSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach with Eventually {

  trait Setup {

    val X_CLIENT_ID_HEADER_NAME = "X-Client-ID"
    val clientId = "client-id"

    val mockServiceConfiguration: ServiceConfigProvider = mock[ServiceConfigProvider]
    val mockHttpClient: HttpClient = mock[HttpClient]
    val mockHttpResponse: HttpResponse = mock[HttpResponse]
    val mockLogger: CdsLogger = mock[CdsLogger]

    val customsNotificationConnector = new CustomsNotificationConnector(mockServiceConfiguration, mockHttpClient, mockLogger)

    when(mockServiceConfiguration.getConfig("customs-notification")).thenReturn(ServiceConfig("http://customs-notification.url/customs-notification", None, ""))

  }

  "CustomsNotificationConnector" should {

    "return a count of blocked notifications" in new Setup {

      when(mockHttpClient.GET[HttpResponse](meq(s"http://customs-notification.url$BlockedCountEndpointWithContext"))
        (any[HttpReads[HttpResponse]](), any[HeaderCarrier](), any[ExecutionContext])).thenReturn(Future.successful(mockHttpResponse))
      when(mockHttpResponse.body).thenReturn("<pushNotificationBlockedCount>1</pushNotificationBlockedCount>")

      val result: String = await(customsNotificationConnector.getNotifications(ClientId(clientId)))

      result shouldBe "<pushNotificationBlockedCount>1</pushNotificationBlockedCount>"
    }

    "return correct status when deleting blocked flags" in new Setup {

      when(mockHttpClient.DELETE[HttpResponse](meq(s"http://customs-notification.url$BlockedFlagEndpointWithContext"))
        (any[HttpReads[HttpResponse]](), any[HeaderCarrier](), any[ExecutionContext])).thenReturn(Future.successful(mockHttpResponse))
      when(mockHttpResponse.status).thenReturn(NO_CONTENT)

      val result: Int = await(customsNotificationConnector.deleteBlocked(ClientId(clientId)))

      result shouldBe NO_CONTENT
      logVerifier(mockLogger, "debug", s"calling http://customs-notification.url$BlockedFlagEndpointWithContext")
    }

    "return correct status when no blocked flags are deleted" in new Setup {

      when(mockHttpClient.DELETE[HttpResponse](meq(s"http://customs-notification.url$BlockedFlagEndpointWithContext"))
        (any[HttpReads[HttpResponse]](), any[HeaderCarrier](), any[ExecutionContext])).thenReturn(Future.failed(new NotFoundException("not found")))

      val result: Int = await(customsNotificationConnector.deleteBlocked(ClientId(clientId)))
      result shouldBe NOT_FOUND
    }

    "return failure when call to upstream service results in failure" in new Setup {

      when(mockHttpClient.DELETE[HttpResponse](meq(s"http://customs-notification.url$BlockedFlagEndpointWithContext"))
        (any[HttpReads[HttpResponse]](), any[HeaderCarrier](), any[ExecutionContext])).thenReturn(Future.failed(new RuntimeException("Fail")))

      val caught: RuntimeException = intercept[RuntimeException] {
        await(customsNotificationConnector.deleteBlocked(ClientId(clientId)))
      }

      caught.getMessage shouldBe "Fail"
    }
  }

  private def logVerifier(mockLogger: CdsLogger, logLevel: String, logText: String): Unit = {
    PassByNameVerifier(mockLogger, logLevel)
      .withByNameParam(logText)
      .verify()
  }

}
