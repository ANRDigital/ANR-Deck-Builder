package com.shuneault.netrunnerdeckbuilder.ViewModel

import androidx.lifecycle.ViewModel
import com.shuneault.netrunnerdeckbuilder.db.IDeckRepository
import com.shuneault.netrunnerdeckbuilder.db.CardRepository
import com.shuneault.netrunnerdeckbuilder.game.*
import com.shuneault.netrunnerdeckbuilder.helper.ISettingsProvider
import com.shuneault.netrunnerdeckbuilder.helper.DeckValidator
import com.shuneault.netrunnerdeckbuilder.helper.Sorter.CardSorterByFactionWithMineFirst
import java.util.*

class DeckActivityViewModel(
    private val mDeckRepo: IDeckRepository,
    private val mCardRepo: CardRepository,
    private val settingsProvider: ISettingsProvider
) : ViewModel() {
    var deck: Deck? = null
        private set
    var isValid = false
        private set
    private var mCardPool: CardPool? = null
    fun setDeckId(deckId: Long) {
        if (deck == null) {
            reloadDeck(deckId)
        }
    }

    private fun reloadDeck(deckId: Long) {
        deck = mDeckRepo.getDeck(deckId)
        refreshCardPool()
        validateDeck()
    }

    fun changeDeckIdentity(mDeck: Deck?, identityCode: String?) {
        mDeckRepo.changeIdentity(mDeck!!, identityCode!!)
        reloadDeck(deck!!.rowId!!)
    }

    fun deleteDeck(deck: Deck?) {
        mDeckRepo.deleteDeck(deck!!)
    }

    fun cloneDeck(deck: Deck?): Long {
        return mDeckRepo.cloneDeck(deck!!)
    }

    fun changeDeckFormat(format: Format): Boolean {
        // only change format if different to the existing one.
        if (format.name != deck!!.format.name) {
            mDeckRepo.setDeckFormat(deck!!, format)
            deck!!.packFilter.clear()
            refreshCardPool()
            autoReplaceCards()
            validateDeck()
            mDeckRepo.saveDeck(deck!!)
            return true // format was changed
        }
        return false // format was not changed
    }

    private fun refreshCardPool() {
        mCardPool = mCardRepo.getCardPool(deck!!.format, deck!!.packFilter, deck!!.coreCount)
    }

    //todo: this probably belongs somewhere else
    private fun autoReplaceCards() {
        val identity = deck!!.identity
        val id = mCardPool!!.findCardByTitle(identity.title)
        if (id != null) {
            deck!!.identity = id
        }

        // loop the cards in the deck and replace with
        for (card in deck!!.cards) {
            if (mCardPool!!.getMaxCardCount(card) == 0) {
                val replacement = mCardPool!!.findCardByTitle(card.title)
                if (replacement != null) {
                    deck!!.replaceCard(card, replacement)
                }
            }
        }
    }

    fun validateDeck() {
        val format = deck!!.format
        val mostWantedList = mCardRepo.getMostWantedList(format.mwlId)
        val packs = mCardRepo.getPacks(format, deck!!.packFilter)
        val validator = DeckValidator(mostWantedList)
        isValid = validator.validate(deck, packs)
    }

    fun setDeckName(name: String?) {
        deck!!.name = name!!
    }

    fun setDeckDescription(description: String?) {
        deck!!.notes = description!!
    }

    fun setPackFilter(packFilter: ArrayList<String>?) {
        deck!!.packFilter = packFilter!!
        mDeckRepo.updateDeck(deck!!)
        refreshCardPool()
        validateDeck()
    }

    fun setCoreCount(count: Int) {
        deck!!.coreCount = count
        mDeckRepo.updateDeck(deck!!)
        refreshCardPool()
        validateDeck()
    }

    val cardHeaders: ArrayList<String>
        get() {
            val sideCode = deck!!.sideCode
            val headers = mCardRepo.getCardTypes(sideCode, false)
            Collections.sort(headers)
            return headers
        }

    fun getGroupedCards(deck: Deck, headers: ArrayList<String>): HashMap<String, ArrayList<Card>?> {
        val mListCards = HashMap<String, ArrayList<Card>?>()
        val sideCode = deck.sideCode
        val cardCollection = mCardPool!!.cards
        cardCollection.addExtras(deck.cards)
        for (theCard in cardCollection) {
            // Only add the cards that are on my side
            val isSameSide = theCard.sideCode == sideCode

            // Do not add the identities
            val isIdentity = theCard.isIdentity

            // Only display agendas that belong to neutral or my faction
            val deckFaction: String = deck.factionCode
            val isGoodAgenda = (!theCard.isAgenda
                    || theCard.factionCode == deckFaction || theCard.isNeutral
                    || deck.identity.isNeutral)

            // Cannot add Jinteki card for "Custom Biotics: Engineered for Success" Identity
            val isJintekiOK =
                !theCard.isJinteki || deck.identity.code != Card.SpecialCards.CARD_CUSTOM_BIOTICS_ENGINEERED_FOR_SUCCESS

            // Ignore non-virtual resources if runner is Apex and setting is set
            var isNonVirtualOK = true
            if (theCard.isResource && !theCard.isVirtual) {
                if (deck.isApex && settingsProvider.hideNonVirtualApex) {
                    isNonVirtualOK = false
                }
            }
            if (isSameSide && !isIdentity && isGoodAgenda && isJintekiOK && isNonVirtualOK) {
                // add the type grouping if it doesn't exist
                if (mListCards[theCard.typeCode] == null) mListCards[theCard.typeCode] = ArrayList()

                // add the card to the type group
                mListCards[theCard.typeCode]!!.add(theCard)
            }
        }

        // Sort the cards
        sortListCards(headers, mListCards, deck.factionCode)
        return mListCards
    }

    fun getMyGroupedCards(
        headers: ArrayList<String>,
        deck: Deck
    ): HashMap<String, ArrayList<Card>?> {
        // Generate a new card list to display and notify the adapter
        val cardList = HashMap<String, ArrayList<Card>?>()
        for (theHeader in headers) {
            cardList[theHeader] = ArrayList()
        }
        for (theCard in deck.cards) {
            // Only add the cards that are on my side
            // Do not add the identities
            val typeCode = theCard.typeCode
            val sideCode = theCard.sideCode
            if (typeCode != Card.Type.IDENTITY && sideCode == deck.sideCode) {
                if (cardList[typeCode] == null) cardList[typeCode] = ArrayList()
                cardList[typeCode]!!.add(theCard)
            }
        }
        sortListCards(headers, cardList, deck.factionCode)
        return cardList
    }

    private fun sortListCards(
        headers: ArrayList<String>,
        listCards: HashMap<String, ArrayList<Card>?>,
        factionCode: String
    ) {
        // Sort by faction,
        for (strCat in headers) {
            val list = listCards[strCat]
            if (list != null) {
                Collections.sort(list, CardSorterByFactionWithMineFirst(factionCode))
            }
        }
    }

    fun addCard(card: Card?) {
        val max = mCardPool!!.getMaxCardCount(card)
        deck!!.AddCard(card!!, max)
    }

    fun reduceCard(card: Card?) {
        deck!!.ReduceCard(card!!)
    }

    fun save() {
        mDeckRepo.saveDeck(deck!!)
    }
}