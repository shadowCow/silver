package com.cowsunday.silver.data

trait Feature {
  def calculate(bars: Seq[PriceBar]): Seq[Double]
}

case class HighestHigh(
  length: Int,
) extends Feature {
  def calculate(bars: Seq[PriceBar]): Seq[Double] = {
    require(bars.size > 0)
    val output = Array.ofDim[Double](bars.size)
    output(0) = Double.MinValue

    for (i <- 1 until bars.size) {
      val startIndex = math.max(0, i - length)
      output(i) = bars.slice(startIndex, i)
        .map(_.high)
        .maxBy(d => d)
    }

    output
  }
}

case class LowestLow(
  length: Int,
) extends Feature {
  def calculate(bars: Seq[PriceBar]): Seq[Double] = {
    require(bars.size > 0)
    val output = Array.ofDim[Double](bars.size)
    output(0) = Double.MaxValue

    for (i <- 1 until bars.size) {
      val startIndex = math.max(0, i - length)
      output(i) = bars.slice(startIndex, i)
        .map(_.low)
        .minBy(d => d)
    }

    output
  }
}
