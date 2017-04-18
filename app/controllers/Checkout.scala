package controllers

import javax.inject.{Inject, Singleton}

import play.api.mvc.{Action, Controller}
import services.ProductPrice._

@Singleton
class Checkout @Inject() extends Controller {

  def cartValue(items: String) = Action {
        
    Ok("")
    
  }

}