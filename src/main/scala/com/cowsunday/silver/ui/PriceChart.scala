package com.cowsunday.silver.ui

import com.cowsunday.silver.data.PriceBar
import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import javafx.scene.layout.Pane
import javafx.scene.layout.StackPane
import javafx.scene.canvas.Canvas
import javafx.scene.shape.Box
import javafx.scene.layout.VBox
import javafx.scene.control.ScrollBar
import javafx.geometry.Orientation
import javafx.scene.layout.BorderPane

class PriceChart(
  bars: Seq[PriceBar],
  features: Seq[Seq[Double]],
  barWidth: Int,
  barSpacing: Int,
) extends BorderPane {
  val fullBarWidth = barWidth + barSpacing
  val allBarsWidth = bars.size * fullBarWidth
  println(s"allBarsWidth $allBarsWidth")
  val canvasWrapper = new StackPane()
  val canvas = new Canvas()

  var barIndex = 0

  // Bind canvas width and height to the container's width and height
  canvas.widthProperty().bind(canvasWrapper.widthProperty())
  canvas.heightProperty().bind(canvasWrapper.heightProperty())

  // Ensure the canvas is redrawn when the size changes
  canvas.widthProperty().addListener((_, _, _) => draw(canvas.getGraphicsContext2D(), bars, features, barIndex))
  canvas.heightProperty().addListener((_, _, _) => draw(canvas.getGraphicsContext2D(), bars, features, barIndex))

  val scrollbar = new ScrollBar()
  scrollbar.setOrientation(Orientation.HORIZONTAL)
  scrollbar.setMin(0)
  scrollbar.setMax(math.max(allBarsWidth, canvas.getWidth()))
  scrollbar.setUnitIncrement(fullBarWidth)
  scrollbar.setBlockIncrement(fullBarWidth)
  scrollbar.visibleAmountProperty().bind(canvas.widthProperty())
  scrollbar.valueProperty().addListener((_, oldValue, newValue) => {
    barIndex = (newValue.intValue() / fullBarWidth)
    draw(canvas.getGraphicsContext2D(), bars, features, barIndex)
  })

  canvasWrapper.getChildren().add(canvas)
  setCenter(canvasWrapper)
  setBottom(scrollbar)

    // Initial draw
  draw(canvas.getGraphicsContext2D(), bars, features, barIndex)

  private def draw(gc: GraphicsContext, bars: Seq[PriceBar], features: Seq[Seq[Double]], barIndex: Int): Unit = {
    PriceChart.drawBars(gc, bars, features, barWidth, barSpacing, barIndex)
  }
}

object PriceChart {
  val upBarColor = Color.GREEN
  val downBarColor = Color.RED

  def drawBars(
    gc: GraphicsContext,
    bars: Seq[PriceBar],
    features: Seq[Seq[Double]],
    barWidth: Int,
    barSpacing: Int,
    barIndex: Int,
  ): Unit = {
    val canvasWidth = gc.getCanvas().getWidth()
    val canvasHeight = gc.getCanvas().getHeight()

    val numVisibleBars = canvasWidth.toInt / (barWidth + barSpacing)
    val nextVisibleBarIndex = barIndex + numVisibleBars

    val highLow = bars.slice(barIndex, nextVisibleBarIndex).foldLeft(HighLow(0, Double.MaxValue)){ (acc, el) =>
      acc.accHigh(el.high).accLow(el.low)
    }
    // println(s"highLow: $highLow")
    // println(s"canvasHeight: $canvasHeight")


    // Clear the canvas
    gc.clearRect(0, 0, canvasWidth, canvasHeight)

    // Background
    gc.setFill(Color.BLACK)
    gc.fillRect(0, 0, canvasWidth, canvasHeight)
    
    bars.slice(barIndex, nextVisibleBarIndex).map { bar =>
      Candlestick(bar, highLow, canvasHeight)
    }.zipWithIndex
    .foreach { case (bar, i) =>
      drawCandlestick(gc, bar, i, barWidth, barSpacing)
    }

    features.map { feature =>
      val values = feature.slice(barIndex, nextVisibleBarIndex).map { value =>
        val y = (1 - highLow.ratio(value)) * canvasHeight
        // println(s"highest(20): $value, ui: $y")
        y
      }
      for (i <- 0 until values.size - 1) {
        val thisY = values(i)
        val nextY = values(i + 1)

        val thisX = (i * (barWidth + barSpacing)) + (barWidth / 2)
        val nextX = ((i+1) * (barWidth + barSpacing)) + (barWidth / 2)

        gc.setStroke(Color.YELLOW)
        gc.strokeLine(thisX, thisY, nextX, nextY)
      }
    }
  }

  def drawCandlestick(gc: GraphicsContext, candlestick: Candlestick, i: Int, barWidth: Int, barSpacing: Int) {
    val x = i * (barWidth + barSpacing)
    val wickX = x + (barWidth / 2)

    gc.setFill(candlestick.color)

    // fill body
    gc.fillRect(x, candlestick.bodyTopY, barWidth, candlestick.bodyHeight())
    // fill wicks
    gc.fillRect(wickX, candlestick.wickTopY, 1, candlestick.upperWickHeight())
    gc.fillRect(wickX, candlestick.bodyBottomY, 1, candlestick.lowerWickHeight())
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

  def ratio(value: Double): Double = {
    (value - low) / range()
  }
}

object Candlestick {
  def apply(bar: PriceBar, highLow: HighLow, canvasHeight: Double): Candlestick = {
    val openRatio = highLow.ratio(bar.open)
    val highRatio = highLow.ratio(bar.high)
    val lowRatio = highLow.ratio(bar.low)
    val closeRatio = highLow.ratio(bar.close)

    // println(s"openRatio: $openRatio")

    val openY = (1 - openRatio) * canvasHeight
    val highY = (1 - highRatio) * canvasHeight
    val lowY = (1 - lowRatio) * canvasHeight
    val closeY = (1 - closeRatio) * canvasHeight

    // println(s"openY: $openY")

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