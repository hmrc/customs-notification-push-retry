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

package unit

import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.customs.notificationpushretry.controllers.NotificationsController
import uk.gov.hmrc.play.test.UnitSpec

class NotificationsControllerSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach {

  private val controller = new NotificationsController()

  "NotificationsController" should {

    "respond with status 200 for a processed valid request" in {

      val validRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/blocked-count")

      val result = await(controller.get().apply(validRequest))

      status(result) shouldBe OK

    }
  }
}
