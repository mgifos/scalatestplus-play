/*
 * Copyright 2001-2016 Artima, Inc.
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
package org.scalatestplus.play.examples.onebrowserpersuite

import play.api.test.Helpers
import org.scalatest.tags.FirefoxBrowser
import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.{Application, Play}
import play.api.inject.guice._
import play.api.routing._

@FirefoxBrowser
class ExampleSpec extends PlaySpec with GuiceOneServerPerSuite with OneBrowserPerSuite with FirefoxFactory {

  // Override fakeApplication or use GuiceOneServerPerSuite if you need a Application with other than non-default parameters.
  override def fakeApplication(): Application =
    new GuiceApplicationBuilder().configure("foo" -> "bar", "ehcacheplugin" -> "disabled").router(Router.from(TestRoute)).build()

  "The OneBrowserPerSuite trait" must {
    "provide an Application" in {
      app.configuration.getString("ehcacheplugin") mustBe Some("disabled")
    }
    "make the Application available implicitly" in {
      def getConfig(key: String)(implicit app: Application) = app.configuration.getString(key)
      getConfig("ehcacheplugin") mustBe Some("disabled")
    }
    "start the Application" in {
      Play.maybeApplication mustBe Some(app)
    }
    "provide the port number" in {
      port mustBe Helpers.testServerPort
    }
    "provide an actual running server" in {
      import java.net._
      val url = new URL("http://localhost:" + port + "/boum")
      val con = url.openConnection().asInstanceOf[HttpURLConnection]
      try con.getResponseCode mustBe 404
      finally con.disconnect()
    }
    "provide a web driver" in {
      go to ("http://localhost:" + port + "/testing")
      pageTitle mustBe "Test Page"
      click on find(name("b")).value
      eventually { pageTitle mustBe "scalatest" }
    }
  }
}
