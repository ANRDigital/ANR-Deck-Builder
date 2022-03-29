package com.shuneault.netrunnerdeckbuilder.game

import com.shuneault.netrunnerdeckbuilder.helper.AppManager
import android.text.TextUtils
import com.shuneault.netrunnerdeckbuilder.db.DatabaseHelper
import com.shuneault.netrunnerdeckbuilder.db.CardRepository
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.Serializable
import java.lang.Exception
import java.util.*

class Deck(var identity: Card, var format: Format) : Serializable {
    var name = ""
    val fileSafeName: String
        get() = name.replace(ReservedChars.toRegex(), "_")

    var notes = ""
    var created = Date()
    var updated: Date? = null
    var isStarred = false
    var hasUnknownCards = false
    // database row id
    var rowId: Long? = null
    var nrdbId = 0
    // uuid not really used at present
    var uUID = UUID.randomUUID()
        private set

    // cards in the deck
    private val mCards = HashMap<Card, Int?>()

    // BUILD functionality
    // Cards to add and cards to remove for deck building
    private val mCardsToAdd = HashMap<Card, CardCount?>()
    private val mCardsToRemove = HashMap<Card, CardCount?>()
    private val mArrCardsToAdd = ArrayList<CardCount?>()
    private val mArrCardsToRemove = ArrayList<CardCount?>()

    var packFilter = ArrayList<String?>()
    var coreCount = 0 // 0 indicates no override

    val factionCode: String
        get() = identity.factionCode
    val minimumDeckSize: Int
        get() = identity.minimumDeckSize
    val influenceLimit: Int
        get() = identity.influenceLimit

    val side: String
        get() = try {
            identity.sideCode
        } catch (e: Exception) {
            ""
        }

    fun updateCardCount(card: Card, count: Int) {
        // Add or remove the card count
        val iCountToAdd = if (mCardsToAdd[card] == null) 0 else mCardsToAdd[card]!!.count
        val iCountToRemove = if (mCardsToRemove[card] == null) 0 else mCardsToRemove[card]!!
            .count
        val iCountOriginal =
            (if (mCards[card] == null) 0 else mCards[card])!! + -iCountToAdd - iCountToRemove
        mCardsToAdd.remove(card)
        mCardsToRemove.remove(card)
        if (iCountOriginal != count) {
            // Add or remove
            if (iCountOriginal > count) // We removed some cards
                mCardsToRemove[card] =
                    CardCount(card, count - iCountOriginal) else mCardsToAdd[card] =
                CardCount(card, count - iCountOriginal)
        }

        // Regenerate the arrays
        cardsToAdd
        cardsToRemove
        setCardCount(card, count)
    }

    fun setCardCount(card: Card, count: Int) {
        // Modify the deck
        mCards.remove(card)
        if (count > 0) mCards[card] = count
    }

    val cards: ArrayList<Card>
        get() {
            val cardList = ArrayList<Card>()
            for (card in mCards.keys) cardList.add(card)
            return cardList
        }

    fun getCardCount(card: Card): Int {
        val iCount = mCards[card]
        return iCount ?: 0
    }

    fun getCardCountByType(type: String?): Int {
        var iCount = 0
        for (card in mCards.keys) {
            if (card.typeCode.contains(type!!)) {
                iCount = iCount + getCardCount(card)
            }
        }
        return iCount
    }

    fun getCardCountByFaction(faction: String): Int {
        var iCount = 0
        for (card in mCards.keys) {
            if (card.factionCode == faction) {
                iCount = iCount + getCardCount(card)
            }
        }
        return iCount
    }

    fun getCardCountBySubTypeAndFaction(subtype: String?, faction: String): Int {
        var iCount = 0
        for (card in mCards.keys) {
            if (card.subtype.contains(subtype!!) && card.factionCode == faction) {
                iCount = iCount + getCardCount(card)
            }
        }
        return iCount
    }

    var cardsToAdd: ArrayList<CardCount?>
        get() {
            mArrCardsToAdd.clear()
            for (card in mCardsToAdd.keys) mArrCardsToAdd.add(mCardsToAdd[card])
            for (card in mCardsToRemove.keys) mArrCardsToAdd.add(mCardsToRemove[card])
            return mArrCardsToAdd
        }
        set(list) {
            mCardsToAdd.clear()
            for (cc in list) {
                mCardsToAdd[cc!!.card] = cc
            }
            mArrCardsToAdd.clear()
            mArrCardsToAdd.addAll(list)
        }
    var cardsToRemove: ArrayList<CardCount?>
        get() {
            mArrCardsToRemove.clear()
            for (card in mCardsToRemove.keys) mArrCardsToRemove.add(mCardsToRemove[card])
            return mArrCardsToRemove
        }
        set(list) {
            mCardsToRemove.clear()
            for (cc in list) {
                mCardsToRemove[cc!!.card] = cc
            }
            mArrCardsToRemove.clear()
            mArrCardsToRemove.addAll(list)
        }

    fun getCardsToAddCount(card: Card): Int {
        val iCount = mCardsToAdd[card]!!.count
        return iCount ?: 0
    }

    fun getCardsToRemoveCount(card: Card): Int {
        val iCount = mCardsToRemove[card]!!.count
        return iCount ?: 0
    }

    val deckSize: Int
        get() {
            var iDeckSize = 0
            for (card in cards) {
                iDeckSize = iDeckSize + getCardCount(card)
            }
            return iDeckSize
        }// Some cards have a different influence based on the text// First copy of each program does not count toward the influence value
    // removed mwl handling here

    // IDENTITY: The Professor (03029) does count influence differently
    val deckInfluence: Int
        get() {
            var iInfluence = 0
            // removed mwl handling here

            // IDENTITY: The Professor (03029) does count influence diffently
            if (identity.code == Card.SpecialCards.CARD_THE_PROCESSOR) {
                for (card in cards) {
                    if (identity.factionCode != card.factionCode) {
                        iInfluence = if (card.typeCode == Card.Type.PROGRAM) {
                            // First copy of each program does not count toward the influence value
                            iInfluence + card.factionCost * Math.max(getCardCount(card) - 1, 0)
                        } else {
                            iInfluence + card.factionCost * getCardCount(card)
                        }
                    }
                }
            } else {
                for (card in cards) {
                    if (identity.factionCode != card.factionCode) {

                        // Some cards have a different influence based on the text
                        when (card.code) {
                            "10018" -> if (getCardCountByType(Card.Type.ICE) <= 15) {
                                continue
                            }
                            "10019" -> if (deckSize >= 50) {
                                continue
                            }
                            "10029", "10067" -> if (getCardCountByFaction(Card.Faction.FACTION_HAAS_BIOROID) - getCardCountBySubTypeAndFaction(
                                    Card.SubTypeCode.ALLIANCE, Card.Faction.FACTION_HAAS_BIOROID
                                ) >= 6
                            ) {
                                continue
                            }
                            "10068" -> if (getCardCountByFaction(Card.Faction.FACTION_JINTEKI) - getCardCountBySubTypeAndFaction(
                                    Card.SubTypeCode.ALLIANCE, Card.Faction.FACTION_JINTEKI
                                ) >= 6
                            ) {
                                continue
                            }
                            "10109" -> if (getCardCountByFaction(Card.Faction.FACTION_NBN) - getCardCountBySubTypeAndFaction(
                                    Card.SubTypeCode.ALLIANCE, Card.Faction.FACTION_NBN
                                ) >= 6
                            ) {
                                continue
                            }
                            "10094", "10072" -> if (getCardCountByFaction(Card.Faction.FACTION_WEYLAND_CONSORTIUM) - getCardCountBySubTypeAndFaction(
                                    Card.SubTypeCode.ALLIANCE,
                                    Card.Faction.FACTION_WEYLAND_CONSORTIUM
                                ) >= 6
                            ) {
                                continue
                            }
                            "10013" -> if (getCardCountByFaction(Card.Faction.FACTION_JINTEKI) - getCardCountBySubTypeAndFaction(
                                    Card.SubTypeCode.ALLIANCE, Card.Faction.FACTION_JINTEKI
                                ) >= 6
                            ) {
                                continue
                            }
                            "10071" -> if (getCardCountByFaction(Card.Faction.FACTION_NBN) - getCardCountBySubTypeAndFaction(
                                    Card.SubTypeCode.ALLIANCE, Card.Faction.FACTION_NBN
                                ) >= 6
                            ) {
                                continue
                            }
                            "10076" -> if (getCardCountByType(Card.Type.ASSET) >= 7) {
                                continue
                            }
                            "10038" -> if (getCardCount(
                                    AppManager.getInstance().getCard("01109")
                                ) == 3
                            ) {
                                continue
                            }
                        }
                        iInfluence = iInfluence + card.factionCost * getCardCount(card)
                    }
                }
            }
            return iInfluence
        }
    val deckAgenda: Int
        get() {
            var iAgendaPoints = 0
            for (card in cards) {
                iAgendaPoints = iAgendaPoints + card.agendaPoints * getCardCount(card)
            }
            return iAgendaPoints
        }// Calculation: BASE_AGENDA + (floor(CardCount)/5*2)

    // Returns the maximum between the minimum agenda based on min deck size
    //	or the agenda requirement based on the current deck size
    val deckAgendaMinimum: Int
        get() =// Calculation: BASE_AGENDA + (floor(CardCount)/5*2)
            (BASE_AGENDA + Math.floor(
                (Math.max(
                    deckSize,
                    minimumDeckSize
                ) / 5).toDouble()
            ) * 2).toInt()

    fun clearCardsToAddAndRemove() {
        mCardsToAdd.clear()
        mCardsToRemove.clear()
        mArrCardsToAdd.clear()
        mArrCardsToRemove.clear()
    }

    fun clearCardsToAddAndRemove(onlyChecked: Boolean) {
        if (!onlyChecked) {
            clearCardsToAddAndRemove()
        } else {
            // Cards to add
            var it = mArrCardsToAdd.iterator()
            while (it.hasNext()) {
                val cc = it.next()
                if (cc!!.isDone) {
                    it.remove()
                    mCardsToAdd.remove(cc.card)
                }
            }
            // Cards to remove
            it = mArrCardsToRemove.iterator()
            while (it.hasNext()) {
                val cc = it.next()
                if (cc!!.isDone) {
                    it.remove()
                    mCardsToRemove.remove(cc.card)
                }
            }
        }
    }

    val isInfluenceOk: Boolean
        get() = deckInfluence <= influenceLimit
    val isCardCountOk: Boolean
        get() = deckSize >= minimumDeckSize
    val isAgendaOk: Boolean
        get() = side == Card.Side.SIDE_RUNNER || deckAgenda == deckAgendaMinimum || deckAgenda == deckAgendaMinimum + 1

    fun toJSON(): JSONObject {
        val json = JSONObject()
        val jsonCards = JSONArray()
        val jsonCardsToAdd = JSONArray()
        val jsonCardsToRemove = JSONArray()
        try {
            // Deck info
            json.putOpt(JSON_DECK_UUID, uUID.toString())
            json.putOpt(JSON_DECK_NAME, name)
            json.putOpt(JSON_DECK_NOTES, notes)
            json.putOpt(JSON_DECK_STARRED, isStarred)
            json.putOpt(JSON_DECK_IDENTITY_CODE, identity.code)
            json.putOpt(JSON_DECK_FORMAT, format.id)
            json.putOpt(
                JSON_DECK_PACK_FILTER,
                TextUtils.join(DatabaseHelper.PACK_FILTER_SEPARATOR, packFilter)
            )
            json.putOpt(JSON_DECK_CORE_COUNT, coreCount)

            // Cards
            val cardList = cards
            for (card in cardList) {
                val jsonCard = JSONObject()
                jsonCard.putOpt(JSON_DECK_CARD_CODE, card.code)
                jsonCard.putOpt(JSON_DECK_CARD_COUNT, getCardCount(card))
                jsonCards.put(jsonCard)
            }

            // Cards to add
            for (cardCount in cardsToAdd) {
                val jsonCardCount = JSONObject()
                jsonCardCount.putOpt(JSON_DECK_CARD_CODE, cardCount!!.card.code)
                jsonCardCount.putOpt(JSON_DECK_CARD_COUNT, cardCount.count)
                jsonCardCount.putOpt(JSON_DECK_CARDS_DONE, cardCount.isDone)
                jsonCardsToAdd.put(jsonCardCount)
            }

            // Cards to remove
            for (cardCount in cardsToRemove) {
                val jsonCardCount = JSONObject()
                jsonCardCount.putOpt(JSON_DECK_CARD_CODE, cardCount!!.card.code)
                jsonCardCount.putOpt(JSON_DECK_CARD_COUNT, cardCount.count)
                jsonCardCount.putOpt(JSON_DECK_CARDS_DONE, cardCount.isDone)
                jsonCardsToRemove.put(jsonCardCount)
            }
            json.putOpt(JSON_DECK_CARDS, jsonCards)
            json.putOpt(JSON_DECK_CARDS_TO_ADD, jsonCardsToAdd)
            json.putOpt(JSON_DECK_CARDS_TO_REMOVE, jsonCardsToRemove)
        } catch (e: JSONException) {
            //
            e.printStackTrace()
        }
        return json
    }

    val isApex: Boolean
        get() = identity.code == Card.SpecialCards.APEX

    val cardCounts: ArrayList<CardCount>
        get() {
            val cardCounts = ArrayList<CardCount>()
            for (card in mCards.keys) {
                val count = mCards[card]
                cardCounts.add(CardCount(card, count!!))
            }
            return cardCounts
        }

    fun isFaction(factionCode: String): Boolean {
        return factionCode == factionCode
    }

    fun ReduceCard(card: Card) {
        // not below 0
        val count = Math.max(0, getCardCount(card) - 1)
        updateCardCount(card, count)
    }

    fun AddCard(card: Card, maxAllowed: Int) {
        // not above the maximum allowed
        val count = Math.min(maxAllowed, getCardCount(card) + 1)
        updateCardCount(card, count)
    }

    fun replaceCard(card: Card, replacement: Card) {
        val count = mCards[card]!!
        updateCardCount(card, 0)
        updateCardCount(replacement, count)
    }

    val sideCode: String
        get() = identity.sideCode

    fun isSide(side: String): Boolean {
        return this.side == side
    }

    companion object {
        private const val ReservedChars = "[\\|\\?\\*\\<\\\"\\:\\>\\+\\[\\]\\/\\\\\\']"

        // JSON Values
        const val JSON_DECK_UUID = "deck_uuid"
        const val JSON_DECK_NAME = "deck_name"
        const val JSON_DECK_NOTES = "deck_notes"
        const val JSON_DECK_STARRED = "deck_starred"
        const val JSON_DECK_IDENTITY_CODE = "deck_identity_code"
        const val JSON_DECK_CARD_CODE = "card_code"
        const val JSON_DECK_CARD_COUNT = "card_count"
        const val JSON_DECK_CARDS = "cards"
        const val JSON_DECK_CARDS_TO_ADD = "cards_to_add"
        const val JSON_DECK_CARDS_TO_REMOVE = "cards_to_remove"
        const val JSON_DECK_CARDS_DONE = "is_done"
        const val JSON_DECK_FORMAT = "format"
        const val JSON_DECK_PACK_FILTER = "pack_filter"
        const val JSON_DECK_CORE_COUNT = "core_count"

        // Rules values
        const val BASE_AGENDA = 2
        private const val serialVersionUID = 2114649051205735605L
        @JvmStatic
        fun fromJSON(json: JSONObject, repo: CardRepository): Deck {
            val idCardCode = json.optString(JSON_DECK_IDENTITY_CODE)
            val identityCard = repo.getCard(idCardCode)
            val format = repo.getFormat(json.optInt(JSON_DECK_FORMAT))
            val deck = Deck(identityCard, format)
            val packFilterValue = json.optString(JSON_DECK_PACK_FILTER)
            if (!packFilterValue.isEmpty()) {
                val pf = ArrayList(
                    Arrays.asList(
                        *packFilterValue.split(DatabaseHelper.PACK_FILTER_SEPARATOR).toTypedArray()
                    )
                )
                deck.packFilter = pf
            }
            deck.coreCount = json.optInt(JSON_DECK_CORE_COUNT, 0) // specifying 0 to ensure meaning
            deck.uUID =
                UUID.fromString(json.optString(JSON_DECK_UUID, UUID.randomUUID().toString()))
            deck.name = json.optString(JSON_DECK_NAME)
            deck.notes = json.optString(JSON_DECK_NOTES)
            deck.isStarred = json.optBoolean(JSON_DECK_STARRED)

            // Get the cards
            try {
                val jsonCards = json.getJSONArray(JSON_DECK_CARDS)
                for (i in 0 until jsonCards.length()) {
                    val jsonCard = jsonCards.getJSONObject(i)
                    deck.setCardCount(
                        repo.getCard(jsonCard.optString(JSON_DECK_CARD_CODE)), jsonCard.optInt(
                            JSON_DECK_CARD_COUNT
                        )
                    )
                }

                // By default, when a new card is added to a deck, it is added to the ADD list
                deck.mCardsToAdd.clear()
                deck.mCardsToRemove.clear()
            } catch (e: JSONException) {
            }

            // Get the cards to add
            try {
                val jsonCards = json.getJSONArray(JSON_DECK_CARDS_TO_ADD)
                for (i in 0 until jsonCards.length()) {
                    val jsonCard = jsonCards.getJSONObject(i)
                    val card = repo.getCard(jsonCard.optString(JSON_DECK_CARD_CODE))
                    deck.mCardsToAdd[card] = CardCount(
                        card, jsonCard.optInt(JSON_DECK_CARD_COUNT), jsonCard.optBoolean(
                            JSON_DECK_CARDS_DONE
                        )
                    )
                }
            } catch (e: JSONException) {
            }

            // Get the cards to remove
            try {
                val jsonCards = json.getJSONArray(JSON_DECK_CARDS_TO_REMOVE)
                for (i in 0 until jsonCards.length()) {
                    val jsonCard = jsonCards.getJSONObject(i)
                    val card = repo.getCard(jsonCard.optString(JSON_DECK_CARD_CODE))
                    deck.mCardsToRemove[card] = CardCount(
                        card, jsonCard.optInt(JSON_DECK_CARD_COUNT), jsonCard.optBoolean(
                            JSON_DECK_CARDS_DONE
                        )
                    )
                }
            } catch (e: JSONException) {
            }
            return deck
        }
    }
}