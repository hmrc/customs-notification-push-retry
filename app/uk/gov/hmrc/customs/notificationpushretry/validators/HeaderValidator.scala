/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.customs.notificationpushretry.validators

import javax.inject.Inject
import play.api.http.HeaderNames._
import play.api.mvc.{ActionBuilder, AnyContent, BodyParser, ControllerComponents, Request, Result, Results}
import uk.gov.hmrc.customs.api.common.controllers.ErrorResponse.{ErrorAcceptHeaderInvalid, ErrorInternalServerError}

import scala.concurrent.{ExecutionContext, Future}

class HeaderValidator @Inject()(cc: ControllerComponents) extends Results {

  private def validateHeader(rules: Option[String] => Boolean, headerName: String, error: Result): ActionBuilder[Request, AnyContent] =
    new ActionBuilder[Request, AnyContent] {
      override protected def executionContext: ExecutionContext = cc.executionContext
      override def parser: BodyParser[AnyContent] = cc.parsers.defaultBodyParser
      override def invokeBlock[A](request: Request[A], block: Request[A] => Future[Result]): Future[Result] = {
        if (rules(request.headers.get(headerName))) {
          block(request)
        } else {
          Future.successful(error)
        }
      }
  }

  private val acceptHeaderRules: Option[String] => Boolean = _ contains "application/vnd.hmrc.1.0+xml"
  private val xClientIdHeaderRules: Option[String] => Boolean = _ exists (_ => true)

  def validateAcceptHeader: ActionBuilder[Request, AnyContent] = validateHeader(acceptHeaderRules, ACCEPT, ErrorAcceptHeaderInvalid.XmlResult)
  def validateXClientIdHeader: ActionBuilder[Request, AnyContent] = validateHeader(xClientIdHeaderRules, "X-Client-ID", ErrorInternalServerError.XmlResult)
}
