package services

object ProductPrice {
  
    
  def checkoutTotalPrice(items : String) : BigDecimal =  checkoutTotalPrice(items.toLowerCase.split(",").collect {
      case Apple.name => Apple
      case Orange.name => Orange
    }.toSeq)

  def checkoutTotalPrice(items : Seq[Item]) : BigDecimal = items.map(_.cost).sum
}

sealed trait Item {
  val cost : BigDecimal
  val name : String  
}

case object Apple extends Item {
  val cost : BigDecimal = 0.60
  val name = "apple"  
}

case object Orange extends Item {
  val cost : BigDecimal = 0.25
  val name = "orange"  
}