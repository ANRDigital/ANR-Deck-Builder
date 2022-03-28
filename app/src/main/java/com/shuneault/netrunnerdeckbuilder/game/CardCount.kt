package com.shuneault.netrunnerdeckbuilder.game

class CardCount(var card: Card, var count: Int) {
    var isDone = false

    constructor(card: Card, count: Int, done: Boolean) : this(card, count) {
        isDone = done
    }

    fun add(quantity: Int) {
        count += quantity
    }
}