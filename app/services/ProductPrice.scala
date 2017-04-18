package services

object ProductPrice {
  
    
  def checkoutTotalPrice(items : String) : BigDecimal =  checkoutTotalPrice(items.toLowerCase.split(",").collect {
      case Apple.name => Apple
      case Orange.name => Orange
    }.toSeq)

  // def checkoutTotalPrice(items : Seq[Item]) : BigDecimal = items.map(_.cost).sum
  
   def checkoutTotalPrice(items : Seq[Item]) : BigDecimal = {
    val uniqueItems = items.toSet
    val countedItems = uniqueItems.map( i => (i, items.count( _ == i))).toSeq
    val prices = countedItems.map( t => t._1.price(t._2))
    prices.sum
  }

}

sealed trait Item {
  val cost : BigDecimal
  val name : String
  def price(totalItem : Int) : BigDecimal
  def nProductsFormCost(n : Int, m : Int)(totalItem : Int) : BigDecimal = cost * (totalItem / n * m + totalItem % n)
  def buy1Get1FreeOffer = nProductsFormCost(2, 1) _
  def threeFor2Offer = nProductsFormCost(3, 2) _
}

case object Apple extends Item {
  val cost : BigDecimal = 0.60
  val name = "apple"  
   def price(totalItem : Int) : BigDecimal =  buy1Get1FreeOffer(totalItem)
}

case object Orange extends Item {
  val cost : BigDecimal = 0.25
  val name = "orange"  
  def price(totalItem : Int) : BigDecimal = threeFor2Offer(totalItem)
}