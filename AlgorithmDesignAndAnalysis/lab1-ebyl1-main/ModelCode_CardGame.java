public class ModelCode_CardGame {

    public static final int POCKETSIZE = 25;

    public static CardPool myCardPool;
    public static HandsMaxHeap myMaxHeap;

    public static Card[] myCards, tempCards;
    public static int pocketSize = POCKETSIZE;

    // [Problem 2] Generate All Possible Valid Hands from the Pocket Cards and store them in myMaxHeap
    public static void generateHands(Card[] thisPocket)
    {
        // If thisPocket has less than 5 cards, no hand can be generated, thus the heap will be empty
        
        // Otherwise, generate all possible valid hands from thisPocket and store them in myMaxHeap

        // Pay attention that memory needs to be allocated for the heap!
        
        if(thisPocket.length < 5) 
        {
            System.out.println("Not enough cards!");
            return;
        }

        Hands newHand;
        // if pocket length is 25, generate 5 hands. if 20, generate four hands, etc.
        for(int i = 0; i < thisPocket.length / 5; i++) 
        {
            // create a new hand with the 5 cards, multiplying by 5 ensures that the correct cards are used
            newHand = new Hands(thisPocket[i * 5], thisPocket[i * 5 + 1],
            thisPocket[i * 5 + 2], thisPocket[i * 5 + 3], thisPocket[i * 5 + 4]);
            myMaxHeap.insert(newHand);
        }
    }

    // Sorts the array of Cards in ascending order: ascending order of ranks; cards of equal ranks are sorted in ascending order of suits
    public static void sortCards(Card[] cards)
    {
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

    public static void main(String args[]) throws Exception
    {
        Hands myMove;        
        
        myCardPool = new CardPool();
        
        // print the pool
        System.out.println("\nCard Pool:");
        myCardPool.printPool();
        System.out.println();

        myCards = new Card[pocketSize];
        myCards = myCardPool.getRandomCards(POCKETSIZE);  
        sortCards(myCards);

        // print cards
        System.out.println("My Pocket Cards:");
        for(int j = 0; j < pocketSize; j++)
        {            
            myCards[j].printCard();
        }
        System.out.println("\n");

        myMaxHeap = new HandsMaxHeap(pocketSize / 5);
        
        generateHands(myCards); // generates all valid hands from myCards and stores them in myMaxHeap
    
        // Print the contents of myMaxHeap
        System.out.println("Heap Cards:");
        myMaxHeap.printHeap();
        System.out.println();
        
        // [Problem 3] Implementing Game Logic Part 1 - Aggresive AI: Always Picks the Strongest Hand
        for(int i = 0; pocketSize > 4; i++)
        {            
            // Step 1:
            // - Check if the Max Heap contains any valid hands 
            //   - if yes, remove the Largest Hand from the heap as the current Move
            //   - otherwise, you are out of valid hands.  Just pick any 5 cards from the pocket as a "Pass Move"
            // - Once a move is chosen, print the Hand for confirmation. MUST PRINT FOR VALIDATION!!

            if(myMaxHeap.isEmpty() == false) 
            {
                myMove = myMaxHeap.removeMax();
            } 
            else 
            {
                Card C1 = myCards[0];
                Card C2 = myCards[1];
                Card C3 = myCards[2];
                Card C4 = myCards[3];
                Card C5 = myCards[4];

                myMove = new Hands(C1, C2, C3, C4, C5);
            }

            System.out.println("New Turn: \nMy Move:");
            myMove.printMyHand();
            System.out.println();

            // Step 2:
            // - Remove the Cards used in the move from the pocket cards and update the Max Heap
            // - Print the remaining cards and the contents of the heap
            
            // Print Heap
            System.out.println("\nRemaining Heap Cards");
            myMaxHeap.printHeap();
            System.out.println();

            // Remove cards
            for (i = 5; i < myCards.length; i++) 
            {
                myCards[i - 5] = myCards[i];
            }
            for (i = myCards.length - 5; i < myCards.length; i++) 
            {
                myCards[i] = null;
            }

            pocketSize -= 5;

            System.out.println("Remaining Pocket Cards:");
            for(int j = 0; j < pocketSize; j++)
            {            
                myCards[j].printCard();
            }
            System.out.println();
            System.out.println();

        }
        
    }

}