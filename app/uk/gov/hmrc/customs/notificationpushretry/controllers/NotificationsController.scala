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

import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.customs.api.common.controllers.ErrorResponse
import uk.gov.hmrc.customs.api.common.logging.CdsLogger
import uk.gov.hmrc.customs.notificationpushretry.services.CustomsNotificationService
import uk.gov.hmrc.customs.notificationpushretry.validators.HeaderValidator
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.BaseController

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.control.NonFatal

@Singleton
class NotificationsController @Inject()(customsNotificationService: CustomsNotificationService,
                                        headerValidator: HeaderValidator,
                                        logger: CdsLogger) extends BaseController {

  def get(): Action[AnyContent] =
    (headerValidator.validateAcceptHeader andThen headerValidator.validateXClientIdHeader).async { implicit request =>

      def xmlResult(pushNotificationBlockedCount: Int) = <pushNotificationBlockedCount>{pushNotificationBlockedCount}</pushNotificationBlockedCount>

      implicit val hc: HeaderCarrier = HeaderCarrier(extraHeaders = request.headers.headers)

      customsNotificationService.getNotifications().map(f => Ok(xmlResult(f.pushNotificationBlockedCount)).as(XML))
        .recover {
          case NonFatal(e) =>
            logger.error(s"Error obtaining blocked count: ${e.getMessage}", e)
            ErrorResponse.ErrorInternalServerError.XmlResult
        }
  }
}
