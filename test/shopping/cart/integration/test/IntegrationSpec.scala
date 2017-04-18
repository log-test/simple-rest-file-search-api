
package shopping.cart.integration.test

import org.scalatestplus.play._
import play.api.libs.ws.WSClient
import play.api.test.Helpers._
import scala.concurrent.duration._
import scala.concurrent.Await

class IntegrationSpec extends PlaySpec with OneServerPerTest {

  "Simple Checkout Application" should {

    "be able to process a checkout request with list of scanned items" in {
      
      val wsClient = app.injector.instanceOf[WSClient]      
      val result = Await.result(wsClient.url(s"http://localhost:$port/checkout/orange").get(), 12.seconds)
      result.status mustBe OK
    }
  }
}

