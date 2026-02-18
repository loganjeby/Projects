import java.util.*;

public class ModelCode_CardGame {

    public static final int POCKETSIZE = 25;
    public static int aiPocketSize = POCKETSIZE, myPocketSize = POCKETSIZE;
    public static Scanner myInputScanner;    
    public static HandsMaxHeap aiMaxHeap;
      
    
    public static void main(String args[]) throws Exception
    {        
        CardPool myCardPool;
        Card[] aiCards, myCards;        
        Hands AIHAND = null, PLAYERHAND = null;
        
        HandsRBT myRBT = null;
                

        int aiScore = 0, playerScore = 0; 
        
        myCardPool = new CardPool();

        // Lab 2 - Turn-base AI (Aggresive) vs Player

        // General Rules
        // AI and Player get 25 cards each, 5 turns.  Player and AI Score init to zero.
        // In each turn
        //      Print AI Cards, then Numbered My Cards
        //      Player makes a choice - proceed when valid
        //      AI makes a move, compare Player's hand vs. AI's hand
        //      Update score
        //      Players and AI cannot make INVALID moves until they are out of valid hands
        // At the end of the game, report winner with score


        // You can upgrade your Lab 1 algorithm to Lab 2 to complete this game
        // You can also redesign the entire game loop logic

        // Step 1 - Initialization
        //  - Given the CardPool instance, get 25 cards (POCKETSIZE) for both AI and Player.
        aiCards = new Card[aiPocketSize];
        myCards = new Card[myPocketSize];

        aiCards = myCardPool.getRandomCards(aiPocketSize);
        myCards = myCardPool.getRandomCards(myPocketSize);
        //  - Sort their cards using sortCards() from Lab 0/1. Assign a serial number to Player's cards
        sortCards(aiCards);
        sortCards(myCards);


        //  - Instantiate a HandsRBT for the player and invoke generateHandsIntoBST() 
        //    to populate the player RBT with all possible hands from the pocket card.
        myRBT = new HandsRBT();
        generateHandsIntoRBT(myCards, myRBT);
        aiMaxHeap = new HandsMaxHeap(aiPocketSize);  
        generateHandsAI(aiCards);
        //  - If you have successfully completed Lab 1, you can replace HandsBST with your own HandsMaxHeap
        //    to improve the program performance.

        // Step 2 - Game Loop Logic
        //  - Given POCKETSIZE = 25, and 5-card hand is consumed each round, our game loop should only 
        //    repeat 5 times.  You can optionally parameterize the iteration count for scalability.
        
            // Step 2-1 : Print Both AI and Player Pocket Cards for Strategy Analysis
            //            - Also check if RBT is empty.  If yes, notify player that he/she is out of moves.
            //            - When printing the Player pocket cards, you **MUST** print with serial number.

        for (int i = 0; i < 5; i++) {

            System.out.println("AI Pocket Cards:");
            for (int j = 0; j < aiPocketSize; j++) {
                aiCards[j].printCard();
            }
            System.out.println();

            if (myRBT.isEmpty()) {
                System.out.println("You are out of moves!");
            }
            System.out.println();

            System.out.println("My Pocket Cards: (Count: " + myPocketSize + ")");
            for (int j = 0; j < myPocketSize; j++) {
                System.out.printf("[%d]", j + 1);
                myCards[j].printCard();
            }


                // Step 2-2 : Use the provided getUserHand() method to allow player to pick the 5-card hand from
                //            the pocket cards.
                //              - After the hand is chosen, check if this hand can be found in the RBT
                //              -  If this hand is not in the RBT and the RBT is not empty
                //                 notify the player that there are still valid 5-card hands and cannot pass.
                //                 Wait for Player to input another hand
            
            PLAYERHAND = getUserHand(myCards);
            if(myRBT.isEmpty() == false)
            {
                while(!PLAYERHAND.isAValidHand())
                {
                    System.out.println("Invalid hand! There are still valid 5-card hands."); 
                    PLAYERHAND = getUserHand(myCards);
                }
            }

                // Step 2-3 : Save the chosen hand as "PLAYERHAND", and update pocket card and RBT
                //            - Delete the invalid hands from the RBT using deleteInvalidHands()
                //            - Remove the consumed 5 cards from the pocket cards. 
                //            - Remember to reduce the pocket size by 5.
     
            myRBT.deleteInvalidHands(PLAYERHAND);

            for (int j = 0; j < 5; j++) {
                for (int k = 0; k < myPocketSize; k++) {
                    if (myCards[k] != null && myCards[k].equals(PLAYERHAND.getCard(j))) {
                        myCards[k] = null;
                        break;
                    }
                }
            }
            
            // Shift remaining cards to fill null spots
            int newIndex = 0;
            for (int j = 0; j < myPocketSize; j++) {
                if (myCards[j] != null) {
                    myCards[newIndex++] = myCards[j];
                }
            }
            for (int j = newIndex; j < myPocketSize; j++) {
                myCards[j] = null;
            }
            myPocketSize -= 5;

                // Step 2-4 : Using the logic from Lab 1, construct the Aggressive AI Logic
                //            - If you have completed Lab 1, use HandsMaxHeap.  
                //              Otherwise, you can use the provided HandsBST. (apply knowledge from 2SI3)
                //            - For every 5-card move made, remove the consumed 5 cards from AI pocket cards, reduce the pocket size
                //              then regenerate the MaxHeap 
                //            - Save the chosen move as the "AIHAND"
                //            - Remember, once out of valid hands, AI can pick any cards to form a 5-card pass move.


            if(aiMaxHeap.isEmpty() == false) 
            {
                AIHAND = aiMaxHeap.removeMax();
            } 
            else 
            {
                Card C1 = myCards[0];
                Card C2 = myCards[1];
                Card C3 = myCards[2];
                Card C4 = myCards[3];
                Card C5 = myCards[4];

                AIHAND = new Hands(C1, C2, C3, C4, C5);
            }

            // Step 2:
            // - Remove the Cards used in the move from the pocket cards and update the Max Heap
            // - Print the remaining cards and the contents of the heap

            // Remove cards
            for (int j = 0; j < 5; j++) {
                for (int k = 0; k < aiPocketSize; k++) {
                    if (aiCards[k] != null && aiCards[k].equals(AIHAND.getCard(j))) {
                        aiCards[k] = null;
                        break;
                    }
                }
            }
            
            // Shift remaining cards to fill null spots
            newIndex = 0;
            for (int j = 0; j < aiPocketSize; j++) {
                if (aiCards[j] != null) {
                    aiCards[newIndex++] = aiCards[j];
                }
            }
            for (int j = newIndex; j < aiPocketSize; j++) {
                aiCards[j] = null;
            }
            aiPocketSize -= 5;

            
                // Step 2-5 : Determine the Win/Lose result for this round, and update the scores for AI or Player
                //            - Print both PLAYERHAND and AIHAND for visual confirmation
                //            - Compare hands, and increase the score for the respective party who wins the round
                //            - An unlikely Draw (no winner) condition will result in no score increase for either party
            System.out.println();
            System.out.print("Player's Hand: ");
            PLAYERHAND.printMyHand();
            System.out.println();
            System.out.print("AI's Hand: ");
            AIHAND.printMyHand();
            System.out.println();
            
            if (PLAYERHAND.isMyHandLarger(AIHAND)) {
                playerScore++;
                System.out.println(" [RESULT] Player wins hand!");
                System.out.println();
            } else if (AIHAND.isMyHandLarger(PLAYERHAND)) {
                aiScore++;
                System.out.println(" [RESULT] AI wins hand!");
                System.out.println();
            }
        }
            // Step 3 - Report the Results
            //  - This part is easy.  Refer to the provided sample executable for printout format
        System.out.println();
        System.out.println("=== GAME RESULTS ===");
        System.out.println("Player's Score: " + playerScore);
        System.out.println("AI's Score: " + aiScore);
        if (playerScore > aiScore) {
            System.out.println("Player wins game!");
        } else if (aiScore > playerScore) {
            System.out.println("AI wins game!");
        } else {
            System.out.println("It's a draw!");
        }
        System.out.println("====================");
        myInputScanner.close();
    }

    public static void generateHandsAI(Card[] thisPocket) {
        // If thisPocket has less than 5 cards, no hand can be generated, thus the heap will be empty
        if (thisPocket.length < 5) {
            System.out.println("Not enough cards!");
            return;
        }
    
        // Generate all possible valid hands from thisPocket and store them in aiMaxHeap
        for (int i = 0; i < thisPocket.length - 4; i++) {
            for (int j = i + 1; j < thisPocket.length - 3; j++) {
                for (int k = j + 1; k < thisPocket.length - 2; k++) {
                    if((thisPocket[i].rank - thisPocket[j].rank > 4 && thisPocket[i].rank - thisPocket[j].rank < -4) 
                    || (thisPocket[j].rank - thisPocket[k].rank > 4 && thisPocket[j].rank - thisPocket[k].rank < -4)
                    || (thisPocket[i].rank - thisPocket[k].rank > 4 && thisPocket[i].rank - thisPocket[k].rank < -4)) {
                        break;
                    }
                    for (int l = k + 1; l < thisPocket.length - 1; l++) {
                        for (int m = l + 1; m < thisPocket.length; m++) {
                            Hands newHand = new Hands(thisPocket[i], thisPocket[j], thisPocket[k], thisPocket[l], thisPocket[m]);
                            if (newHand.getHandType() != -1) {
                                aiMaxHeap.insert(newHand);
                            }
                        }
                    }
                }
            }
        }
    }

    public static void generateHandsIntoRBT(Card[] cards, HandsRBT thisRBT)
    {
        // Populate all valid hands into the RBT; generate efficiently the valid hands to get the full mark
        for (int i = 0; i < cards.length - 4; i++) {
            for (int j = i + 1; j < cards.length - 3; j++) {
                for (int k = j + 1; k < cards.length - 2; k++) {
                    if((cards[i].rank - cards[j].rank > 4 && cards[i].rank - cards[j].rank < -4) 
                    || (cards[j].rank - cards[k].rank > 4 && cards[j].rank - cards[k].rank < -4)
                    || (cards[i].rank - cards[k].rank > 4 && cards[i].rank - cards[k].rank < -4)) {
                        break;
                    }
                    for (int l = k + 1; l < cards.length - 1; l++) {
                        for (int m = l + 1; m < cards.length; m++) {
                            Hands newHand = new Hands(cards[i], cards[j], cards[k], cards[l], cards[m]);
                            if (newHand.isAValidHand()) {
                                thisRBT.insert(newHand);
                            }
                        }
                    }
                }
            }
        }
    }

    public static void sortCards(Card[] cards)
    {
        // Implement this method using the logic() from Lab 0 / 1.
        //  You may simply copy and paste the contents over. 
        int j;
        Card temp;        
        int size = cards.length;
        
        for(int i = 1; i < size; i++) 
        { 
            temp = cards[i];		
            for(j = i; j > 0 && cards[j-1].isMyCardLarger(temp); j--) 
                cards[j] = cards[j-1]; 
            cards[j] = temp;
        }  
    }


    // This method enables Player to use the numerical key entries to select
    // the 5 cards to form a hand as a tentative move.
    public static Hands getUserHand(Card[] cards)
    {
        int[] mySelection = new int[5];  
        myInputScanner = new Scanner(System.in);
        int size = cards.length;

        System.out.println();
        for(int i = 0; i < 5; i++)
        {            
            System.out.printf("Card Choice #%d: ", i + 1);
            mySelection[i] = myInputScanner.nextInt() - 1;
            if(mySelection[i] > size) mySelection[i] = size - 1;
            if(mySelection[i] < 0) mySelection[i] = 0;         
        }
        
        Hands newHand = new Hands(  cards[mySelection[0]], 
                                    cards[mySelection[1]], 
                                    cards[mySelection[2]], 
                                    cards[mySelection[3]], 
                                    cards[mySelection[4]]);

        return newHand;
    }

}
