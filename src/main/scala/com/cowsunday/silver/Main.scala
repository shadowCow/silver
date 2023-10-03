package com.cowsunday.silver

import com.cowsunday.silver.data.{PriceBar, Timestamp}

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.stage.Stage
import scala.util.Random
import com.cowsunday.silver.ui.PriceChart
import com.cowsunday.silver.data.HighestHigh
import com.cowsunday.silver.data.LowestLow

object Main extends App {
  Application.launch(classOf[MainApp], args: _*)
}

class MainApp extends Application {
  override def start(stage: Stage): Unit = {
    val bars = getFakeData()
    val highestHigh = HighestHigh(20).calculate(bars)
    val lowestLow = LowestLow(20).calculate(bars)
    val features = Seq(highestHigh, lowestLow)
    // bars.foreach(println)
    // highestHigh.foreach(println)
    // bars.zip(highestHigh)
    //   .foreach { case (bar, value) =>
    //     println(s"high: ${bar.high}, highest(20): $value")  
    //   }
    stage.setTitle("ScalaFX Canvas Fill Container Example")

    val barWidth = 5
    val barSpacing = 3
    val root = new PriceChart(bars, features, barWidth, barSpacing)

    val scene = new Scene(root, 800, 600)

    stage.setScene(scene)
    stage.show()
  }

  def getFakeData(): Seq[PriceBar] = {
    var lastOpen = 100.0
    (1 to 800).map { i =>
      val timestamp = Timestamp(2001, 1, i)
      val move = randomMove(5)
      val nextOpen = lastOpen + move
      lastOpen = nextOpen
      val (ocRange, upperWick, lowerWick) = randomRanges(5)

      if (move > 0) {
        val nextClose = nextOpen + ocRange
        PriceBar(
          timestamp,
          nextOpen,
          nextClose + upperWick,
          nextOpen - lowerWick,
          nextClose,
        )
      } else if (move < 0) {
        val nextClose = nextOpen - ocRange
        PriceBar(
          timestamp,
          nextOpen,
          nextOpen + upperWick,
          nextClose - lowerWick,
          nextClose,
        )
      } else {
        val nextClose = nextOpen + ocRange
        PriceBar(
          timestamp,
          nextOpen,
          nextClose + upperWick,
          nextOpen - lowerWick,
          nextClose,
        )
      }
    }
  }

  def randomMove(maxChange: Double): Double = {
    val random = new Random()
    val moveSize = random.nextDouble() * maxChange
    val isUp = random.nextBoolean()

    if (isUp) {
      moveSize
    } else {
      moveSize * -1
    }
  }
  def randomRanges(max: Double): (Double, Double, Double) = {
    val random = new Random()
    val r1 = random.nextDouble() * max
    val r2 = random.nextDouble() * max
    val r3 = random.nextDouble() * max

    (r1, r2, r3)
  }
}
