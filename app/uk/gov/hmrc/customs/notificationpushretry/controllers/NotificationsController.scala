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

package uk.gov.hmrc.customs.notificationpushretry.controllers

import akka.util.ByteString
import javax.inject.{Inject, Singleton}
import play.api.http.{ContentTypes, HttpEntity}
import play.api.mvc.{Action, AnyContent, ResponseHeader, Result}
import play.api.mvc.Results.Ok
import uk.gov.hmrc.customs.api.common.controllers.ErrorResponse.ErrorInternalServerError
import uk.gov.hmrc.customs.api.common.logging.CdsLogger
import uk.gov.hmrc.customs.notificationpushretry.model.ClientId
import uk.gov.hmrc.customs.notificationpushretry.services.CustomsNotificationService
import uk.gov.hmrc.customs.notificationpushretry.validators.HeaderValidator
import uk.gov.hmrc.play.bootstrap.controller.BaseController

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.control.NonFatal

@Singleton
class NotificationsController @Inject()(customsNotificationService: CustomsNotificationService,
                                        headerValidator: HeaderValidator,
                                        logger: CdsLogger) extends BaseController {

  def get(): Action[AnyContent] =
    (headerValidator.validateAcceptHeader andThen headerValidator.validateXClientIdHeader).async { implicit request =>

      val clientId = ClientId(request.headers.get("X-Client-ID").get) // Guaranteed to be populated.

      logger.info(s"Getting blocked count for client id: ${clientId.toString}")

      customsNotificationService.getNotifications(clientId).map(f => Ok(f).as(XML))
        .recover {
          case NonFatal(e) =>
            logger.error(s"Error obtaining blocked count: ${e.getMessage}", e)
            ErrorInternalServerError.XmlResult
        }
  }

  def deleteBlocked():Action[AnyContent] =
    (headerValidator.validateAcceptHeader andThen headerValidator.validateXClientIdHeader).async { implicit request =>

      val clientId = ClientId(request.headers.get("X-Client-ID").get) // Guaranteed to be populated.

      logger.info(s"called delete blocked-flag for client id: ${clientId.toString}")

      customsNotificationService.deleteBlocked(clientId).map { status =>
        logger.debug(s"delete response status was $status")
        status match {
          case NO_CONTENT => NoContent
          case NOT_FOUND => NotFound
        }
      }.recover {
        case NonFatal(e) =>
          logger.error(s"unable to delete blocked flags due to ${e.getMessage}", e)
          ErrorInternalServerError.XmlResult
        }
    }

}
