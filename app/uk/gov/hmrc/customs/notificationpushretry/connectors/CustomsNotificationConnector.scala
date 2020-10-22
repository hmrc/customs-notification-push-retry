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

package uk.gov.hmrc.customs.notificationpushretry.connectors

import javax.inject.Inject
import play.api.http.HeaderNames.ACCEPT
import play.mvc.Http.Status.NOT_FOUND
import uk.gov.hmrc.customs.api.common.config.ServiceConfigProvider
import uk.gov.hmrc.customs.api.common.logging.CdsLogger
import uk.gov.hmrc.customs.notificationpushretry.model.ClientId
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.HttpClient
import uk.gov.hmrc.http.HttpReads.Implicits._

import scala.concurrent.{ExecutionContext, Future}

class CustomsNotificationConnector @Inject()(config: ServiceConfigProvider,
                                             http: HttpClient,
                                             logger: CdsLogger)
                                            (implicit ec: ExecutionContext) extends HttpErrorFunctions {

  private lazy val serviceBaseUrl: String = config.getConfig("customs-notification").url

  def getNotifications(clientId: ClientId): Future[String] = {

    implicit val hc: HeaderCarrier = headerCarrier(clientId)

    http.GET[HttpResponse](s"$serviceBaseUrl/blocked-count").map { response =>
      response.status match {
        case status if is2xx(status) =>
          response.body
        case status => //1xx, 3xx, 4xx, 5xx
          throw new Non2xxResponseException(status)
      }
    }
  }

  def deleteBlocked(clientId: ClientId): Future[Int] = {

    implicit val hc: HeaderCarrier = headerCarrier(clientId)
    val url = s"$serviceBaseUrl/blocked-flag"

    logger.debug(s"calling $url")
    http.DELETE[HttpResponse](url).map { response =>
      response.status match {
        case status if is2xx(status) =>
          response.status
        case status =>
          throw new Non2xxResponseException(status)
      }
    }.recoverWith {
      case _: NotFoundException => Future.successful(NOT_FOUND)
      case e: Throwable =>
        logger.error(s"call to notification service failed due to ${e.getMessage}. DELETE url=$url")
        Future.failed(e)
    }
  }

  private def headerCarrier(clientId: ClientId):HeaderCarrier = {
    HeaderCarrier(extraHeaders = Seq("X-Client-ID" ->  clientId.toString, ACCEPT -> "application/vnd.hmrc.1.0+xml"))
  }
}
