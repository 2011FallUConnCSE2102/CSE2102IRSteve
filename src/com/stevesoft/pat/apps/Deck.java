//
// This software is now distributed according to
// the Lesser Gnu Public License.  Please see
// http://www.gnu.org/copyleft/lesser.txt for
// the details.
//    -- Happy Computing!
//
package com.stevesoft.pat.apps;
import java.util.*;

/** An interface to java.util.Random which simulates a deck
of "cards."  Actually, it just supplies random numbers from
0 to ncards()-1 without ever repeating a number. Thus, you
can think of each integer in this range as a card. */
public class Deck {
    /** The random number generator this class uses. */
    public Random r = new Random();
    private int[] cards;
    private int ncards;
    /** The number of cards remaining in this object. */
    public int ncards() { return ncards; }
    /** Initialize the size of the deck.  The deck has
	a number of cards equal to "ncards" in it. */
    public Deck(int ncards) {
        cards = new int[ncards];
        this.ncards = ncards;
        for(int i=0;i<ncards;i++)
            cards[i]=i;
    }
    /* Discard n cards from the deck. */
    public void discard(int n) {
        for(int i=0;i<n;i++)
            draw();
    }
    /* Draw one card, and return its value. */
    public int draw() {
        int roll = r.nextInt();
        if(roll < 0) roll = -roll;
        roll = roll % ncards;
        ncards--;
        int sav = cards[ncards];
        cards[ncards]=cards[roll];
        cards[roll] = sav;
        return cards[ncards];
    }
}
