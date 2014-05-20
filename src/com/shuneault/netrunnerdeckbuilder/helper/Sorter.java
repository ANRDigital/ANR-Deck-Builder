package com.shuneault.netrunnerdeckbuilder.helper;

import java.util.Collections;
import java.util.Comparator;

import com.shuneault.netrunnerdeckbuilder.game.Card;
import com.shuneault.netrunnerdeckbuilder.game.CardCount;
import com.shuneault.netrunnerdeckbuilder.game.Deck;

public final class Sorter {
	
	public static final class IdentitySorter implements Comparator<Card> {

		@Override
		public int compare(Card lhs, Card rhs) {
			if (lhs.getFaction().equals(rhs.getFaction())) {
				return lhs.getTitle().compareTo(rhs.getTitle());
			} else {
				return lhs.getFaction().compareTo(rhs.getFaction());
			}
		}
		
	}
	
	public static final class DeckSorter implements Comparator<Deck> {

		@Override
		public int compare(Deck lhs, Deck rhs) {
			if (lhs.getIdentity().getFaction().equals(rhs.getIdentity().getFaction())) {
				return lhs.getName().compareTo(rhs.getName());
			} else {
				return lhs.getIdentity().getFaction().compareTo(rhs.getIdentity().getFaction());
			}
		}
		
	}
	
	public static final class CardSorterByName implements Comparator<Card> {

		@Override
		public int compare(Card lhs, Card rhs) {
			return lhs.getTitle().toLowerCase().compareTo(rhs.getTitle().toLowerCase());
		}
		
	}
	
	public static final class CardCountSorterByName implements Comparator<CardCount> {

		@Override
		public int compare(CardCount lhs, CardCount rhs) {
			return lhs.getCard().getTitle().toLowerCase().compareTo(rhs.getCard().getTitle().toLowerCase());
		}
		
	}
	
	public static final class CardSorterByFaction implements Comparator<Card> {

		@Override
		public int compare(Card lhs, Card rhs) {
			if (lhs.getFaction().equals(rhs.getFaction())) {
				return lhs.getTitle().toLowerCase().compareTo(rhs.getTitle().toLowerCase());
			} else {
				return lhs.getFaction().compareTo(rhs.getFaction());
			}
		}
	}
	
	/**
	 * 
	 * @author sebas_000
	 *
	 * returns the cards in this order:
	 * 	1. My Faction (ordered by name)
	 *  2. Neutral faction (ordered by name)
	 *  3. Other faction (ordered by name)
	 *
	 */
	public static final class CardSorterByFactionWithMineFirst implements Comparator<Card> {
		private Card mIdentity;

		public CardSorterByFactionWithMineFirst(Card identity) {
			mIdentity = identity;
		}
		
		@Override
		public int compare(Card lhs, Card rhs) {
			// Faction is my faction
			if (lhs.getFaction().equals(mIdentity.getFaction()) && rhs.getFaction().equals(mIdentity.getFaction())) {
				return lhs.getTitle().toLowerCase().compareTo(rhs.getTitle().toLowerCase());
			} else {
				if (lhs.getFaction().equals(mIdentity.getFaction())) {
					return -1;
				} else if (rhs.getFaction().equals(mIdentity.getFaction())) {
					return 1;
				} else {
					
					
					
					// Faction is neutral
					if (lhs.getFactionCode().equals(Card.Faction.FACTION_NEUTRAL) && rhs.getFactionCode().equals(Card.Faction.FACTION_NEUTRAL)) {
						return lhs.getTitle().toLowerCase().compareTo(rhs.getTitle().toLowerCase());
					} else {
						if (lhs.getFactionCode().equals(Card.Faction.FACTION_NEUTRAL)) {
							return -1;
						} else if (rhs.getFactionCode().equals(Card.Faction.FACTION_NEUTRAL)) {
							return 1;
						} else {
			

							// NOT my faction and NOT neutral
							if (lhs.getFaction().equals(rhs.getFaction())) {
								return lhs.getTitle().toLowerCase().compareTo(rhs.getTitle().toLowerCase());
							} else {
								return lhs.getFaction().compareTo(rhs.getFaction());
							}
							
							
					
						}
				
					}
					
				}
			
			}
			
		}
	}
	
	public static final class CardSorterByCardType implements Comparator<Card> {

		@Override
		public int compare(Card lhs, Card rhs) {
			if (lhs.getType().equals(rhs.getType())) {
				return lhs.getTitle().toLowerCase().compareTo(rhs.getTitle().toLowerCase());
			} else {
				return lhs.getType().compareTo(rhs.getType());
			}
		}
	}
	
	public static final class CardSorterByCardNumber implements Comparator<Card> {

		@Override
		public int compare(Card lhs, Card rhs) {
			return Integer.valueOf(lhs.getNumber()).compareTo(rhs.getNumber());
		}
	}
}
