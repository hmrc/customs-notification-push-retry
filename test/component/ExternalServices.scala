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

package component

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import util.WireMockRunner

import scala.xml.NodeSeq

trait ExternalServices extends WireMockRunner {

  val clientId: String = "client-id"
  val xClientIdHeader: String = "X-Client-ID"

  def stubForGetBlockedFlagCount(status: Int, body: NodeSeq): StubMapping = {
    stubFor(get(urlMatching("/customs-notification/blocked-count"))
      .withHeader(xClientIdHeader, equalTo(clientId))
      .withHeader(ACCEPT, equalTo("application/vnd.hmrc.1.0+xml"))
      .willReturn(
        aResponse()
          .withStatus(status)
          .withBody(body.toString())))
  }

  def stubForDeleteBlockedFlagCount(status: Int, body: NodeSeq): StubMapping = {
    stubFor(delete(urlMatching("/customs-notification/blocked-flag"))
      .withHeader(xClientIdHeader, equalTo(clientId))
      .withHeader(ACCEPT, equalTo("application/vnd.hmrc.1.0+xml"))
      .willReturn(
        aResponse()
          .withStatus(status)
          .withBody(body.toString())))
  }
}
