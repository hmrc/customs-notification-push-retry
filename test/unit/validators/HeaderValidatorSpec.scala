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

package unit.validators

import play.api.http.HeaderNames.ACCEPT
import play.api.mvc.Results.Ok
import play.api.mvc.{Action, AnyContent}
import play.api.test.{FakeRequest, Helpers}
import uk.gov.hmrc.customs.api.common.controllers.ErrorResponse.{ErrorAcceptHeaderInvalid, ErrorInternalServerError}
import uk.gov.hmrc.customs.notificationpushretry.validators.HeaderValidator
import util.UnitSpec

class HeaderValidatorSpec extends UnitSpec {
  private val validator = new HeaderValidator(Helpers.stubControllerComponents())
  private val expectedResult = Ok

  private val validateAccept: Action[AnyContent] = validator.validateAcceptHeader {
    expectedResult
  }

  private val validateXClientId: Action[AnyContent] = validator.validateXClientIdHeader {
    expectedResult
  }

  "validateAcceptHeader" should {
    "return 406 NOT_ACCEPTABLE xml response if Accept header is not present" in {
      await(validateAccept.apply(FakeRequest())) shouldBe ErrorAcceptHeaderInvalid.XmlResult
    }

    "return 406 NOT_ACCEPTABLE if Accept header is invalid" in {
      await(validateAccept.apply(FakeRequest().withHeaders(ACCEPT -> "invalid"))) shouldBe ErrorAcceptHeaderInvalid.XmlResult
    }

    "return result from block if Accept header is application/vnd.hmrc.1.0+xml" in {
      await(validateAccept.apply(FakeRequest().withHeaders(ACCEPT -> "application/vnd.hmrc.1.0+xml"))) shouldBe expectedResult
    }
  }

  "validateXClientIdHeader" should {
    "return 500 INTERNAL_SERVER_ERROR if X-Client-ID header is not present" in {
      await(validateXClientId.apply(FakeRequest())) shouldBe ErrorInternalServerError.XmlResult
    }

    "return result from block if X-Client-ID header is present" in {
      await(validateXClientId.apply(FakeRequest().withHeaders("X-Client-ID" -> "client-id"))) shouldBe expectedResult
    }
  }
}
