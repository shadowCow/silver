package com.cowsunday.silver.data

final case class PriceBar(
    timestamp: Timestamp,
    open: Double,
    high: Double,
    low: Double,
    close: Double,
) {
    def isUp(): Boolean = close > open
    def isDown(): Boolean = close < open
    def isUnchanged(): Boolean = close == open
    def hiLoRange(): Double = high - low
    def openCloseRange(): Double = close - open
}

final case class Timestamp(
    year: Int,
    month: Int,
    day: Int,
)