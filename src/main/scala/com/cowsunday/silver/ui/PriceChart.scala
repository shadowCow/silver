package com.cowsunday.silver.ui

import com.cowsunday.silver.data.PriceBar
import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color

object PriceChart {
  val upBarColor = Color.GREEN
  val downBarColor = Color.RED

  def drawBars(
    gc: GraphicsContext,
    bars: Seq[PriceBar],
    barWidth: Int,
    barSpacing: Int,
  ): Unit = {
    val canvasWidth = gc.getCanvas().getWidth()
    val canvasHeight = gc.getCanvas().getHeight()

    val highLow = bars.foldLeft(HighLow(0, Double.MaxValue)){ (acc, el) =>
      acc.accHigh(el.high).accLow(el.low)
    }
    println(s"highLow: $highLow")
    println(s"canvasHeight: $canvasHeight")


    // Clear the canvas
    gc.clearRect(0, 0, canvasWidth, canvasHeight)

    // Background
    gc.setFill(Color.BLACK)
    gc.fillRect(0, 0, canvasWidth, canvasHeight)
    
    bars.map { bar =>
      Candlestick(bar, highLow, canvasHeight)
    }.zipWithIndex
    .foreach { case (bar, i) =>
      println(bar)
      val x = i * (barWidth + barSpacing)
      val wickX = x + (barWidth / 2)

      gc.setFill(bar.color)

      // fill body
      gc.fillRect(x, bar.bodyTopY, barWidth, bar.bodyHeight())
      // fill wicks
      gc.fillRect(wickX, bar.wickTopY, 1, bar.upperWickHeight())
      gc.fillRect(wickX, bar.bodyBottomY, 1, bar.lowerWickHeight())
    }
  }
}

case class HighLow(
    high: Double,
    low: Double,
) {
  def accHigh(h: Double): HighLow = {
    if (h > this.high) {
      copy(high = h)
    } else {
      this
    }
  }
  def accLow(l: Double): HighLow = {
    if (l < this.low) {
      copy(low = l)
    } else {
      this
    }
  }
  def range(): Double = high - low
}

object Candlestick {
  def apply(bar: PriceBar, highLow: HighLow, canvasHeight: Double): Candlestick = {
    val openRatio = ratio(bar.open, highLow)
    val highRatio = ratio(bar.high, highLow)
    val lowRatio = ratio(bar.low, highLow)
    val closeRatio = ratio(bar.close, highLow)

    println(s"openRatio: $openRatio")

    val openY = (1 - openRatio) * canvasHeight
    val highY = (1 - highRatio) * canvasHeight
    val lowY = (1 - lowRatio) * canvasHeight
    val closeY = (1 - closeRatio) * canvasHeight

    println(s"openY: $openY")

    if (bar.isUp()) {
      Candlestick(
        closeY,
        openY,
        highY,
        lowY,
        Color.GREEN,
      )
    } else if (bar.isDown()) {
      Candlestick(
        openY,
        closeY,
        highY,
        lowY,
        Color.RED,
      )
    } else {
      Candlestick(
        closeY,
        openY,
        highY,
        lowY,
        Color.GREY,
      )
    }
  }

  def ratio(value: Double, highLow: HighLow): Double = {
    (value - highLow.low) / highLow.range()
  }
}

case class Candlestick(
  bodyTopY: Double,
  bodyBottomY: Double,
  wickTopY: Double,
  wickBottomY: Double,
  color: Color,
) {
  def bodyHeight(): Double = math.max(bodyBottomY - bodyTopY, 1.0)
  def lowerWickHeight(): Double = wickBottomY - bodyBottomY
  def upperWickHeight(): Double = bodyTopY - wickTopY
}