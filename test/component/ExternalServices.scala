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

package component

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status.BAD_REQUEST
import util.WireMockRunner

trait ExternalServices extends WireMockRunner {

  val clientId = "client-id"
  val xClientIdHeader = "X-Client-ID"

  def stubForPushNotificationBlockedCount(): StubMapping = {
    stubFor(get(urlMatching("/blocked-count"))
      .withHeader(xClientIdHeader, equalTo(clientId))
      .withHeader(ACCEPT, equalTo("application/vnd.hmrc.1.0+xml"))
      .willReturn(okXml("<pushNotificationBlockedCount>100</pushNotificationBlockedCount>")))
  }

  def stubFor400ErrorPushNotificationBlockedCount(): StubMapping = {
    stubFor(get(urlMatching("/blocked-count"))
      .withHeader(xClientIdHeader, equalTo(clientId))
      .withHeader(ACCEPT, equalTo("application/vnd.hmrc.1.0+xml"))
      .willReturn(aResponse().withStatus(BAD_REQUEST)))
  }
}
