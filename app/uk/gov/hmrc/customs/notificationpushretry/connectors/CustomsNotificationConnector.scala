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

package uk.gov.hmrc.customs.notificationpushretry.connectors

import javax.inject.Inject
import play.api.http.HeaderNames.ACCEPT
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import uk.gov.hmrc.customs.notificationpushretry.config.ServiceConfiguration
import uk.gov.hmrc.customs.notificationpushretry.model.ClientId
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.Future
import scala.xml.{NodeSeq, XML}

class CustomsNotificationConnector @Inject()(config: ServiceConfiguration, http: HttpClient) {

  private lazy val serviceBaseUrl: String = config.baseUrl("customs-notification")

  def getNotifications(clientId: ClientId): Future[NodeSeq] = {

    implicit val hc: HeaderCarrier = HeaderCarrier(extraHeaders = Seq("X-Client-ID" ->  clientId.toString, ACCEPT -> "application/vnd.hmrc.1.0+xml"))

    http.GET[HttpResponse](s"$serviceBaseUrl/blocked-count").map(
      response => {
          XML.loadString(response.body)
      }
    )
  }
}
