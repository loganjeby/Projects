package stage1;

import java.util.LinkedList;

enum Direction {STOP, UP, DOWN, LEFT, RIGHT}

public class TreasureHunter {
    public static final int MAXWEIGHT = 25; // kg
    public static final char symbol = 'P';
    
    private int xPos;
    private int yPos;
    private int accScore;
    private int accWeight;
    private int instScore;
    private int instWeight;
    private TreasureMap myMap; 
    private boolean completed;   

    private LinkedList<TreasureTile> myLoad;
    private Direction myDir;
    private int myNodeCount;

    public TreasureHunter(int x, int y, TreasureMap map)
    {
        xPos = x;
        yPos = y;
        accScore = 0;
        accWeight = 0;
        instScore = 0;
        instWeight = 0;
        myMap = map;
        myMap.setHunterRef(this);    
        myDir = Direction.STOP;  
        completed = false;  

        generatePath();
    }

    public int getX() {return xPos;}
    public int getY() {return yPos;}
    public int getScore() {return accScore;}
    public int getWeight() {return accWeight;}
    public int getInstScore() {return instScore;}
    public int getInstWeight() {return instWeight;}
    public boolean isCompleted() {return completed;}
    public LinkedList<TreasureTile> getLoad() {return myLoad;}    

    private void generatePath()
    {
        LinkedList<TreasureTile> treasureList = myMap.getTreasureList();

        // binary or fractional
        if(myMap.isSplittable()) // fractional
        {
            myLoad = planFractionalKnapsack(treasureList);
        }
        else  // binary
        {
            myLoad = planBinaryKnapsackDP(treasureList);
        }

        // Make sure destination is on the path
        myLoad.addFirst(new TreasureTile(TreasureMap.BOARDSIZEX - 2, TreasureMap.BOARDSIZEY - 2, 'D', false));

        myNodeCount = myLoad.size() - 1;
    }

    private LinkedList<TreasureTile> planBinaryKnapsackGreedy(LinkedList<TreasureTile> treasureList)
    {
        LinkedList<TreasureTile> plannedLoad = new LinkedList<>();
        accScore = 0;
        accWeight = 0;
    
        // Sort treasures by profit ratio
        treasureList = sortProfitRatio(treasureList);
    
        // Select treasures greedily
        for (TreasureTile tile : treasureList) {
            if (accWeight + tile.getWeight() < MAXWEIGHT) {
                plannedLoad.add(tile);
                accScore += tile.getScore();
                accWeight += tile.getWeight();
            }
        }
    
        return plannedLoad;
    }

    private LinkedList<TreasureTile> sortProfitRatio(LinkedList<TreasureTile> treasureList)
    {
        LinkedList<TreasureTile> sortedList = new LinkedList<>();
        // Sort treasures by profit ratio in descending order
        treasureList.sort((tile1, tile2) -> {
            double ratio1 = (double) tile1.getScore() / tile1.getWeight();
            double ratio2 = (double) tile2.getScore() / tile2.getWeight();
            return Double.compare(ratio2, ratio1); 
        });
        sortedList.addAll(treasureList);
        return sortedList;
    }

    private LinkedList<TreasureTile> planBinaryKnapsackDP(LinkedList<TreasureTile> treasureList)
    {
        LinkedList<TreasureTile> plannedLoad = new LinkedList<>();
        treasureList = sortProfitRatio(treasureList);

        int n = treasureList.size();
        int[][] dp = new int[n + 1][MAXWEIGHT + 1];
    
        // Build DP table
        for (int item = 1; item <= n; item++) {                                             // Iterate through all items in treasure list
            TreasureTile tile = treasureList.get(item - 1);                                 // Get the current tile 
            for (int capacity = 1; capacity <= MAXWEIGHT; capacity++) {                     // Iterate through all capacities from 1 to MAXWEIGHT
                int maxValWithCurr = 0;                                                     // Initialize max value with current item to 0      

                if (tile.getWeight() <= capacity) {                                         // Check if the current tile can fit in the knapsack
                    maxValWithCurr = tile.getScore();                                       // Start with the score of the current tile         
                    int remainingCapacity = capacity - tile.getWeight();                    // Calculate the remaining capacity after including the current tile    
                    maxValWithCurr += dp[item - 1][remainingCapacity];                      // Add the value of the remaining capacity from the previous item
                }

                // Tie-breaking: prefer lower weight when scores are equal  
                if (maxValWithCurr == dp[item - 1][capacity]) {                             // Check if the current tile's value is equal to the previous item's value
                    dp[item][capacity] = Math.min(dp[item - 1][capacity], maxValWithCurr);  // Choose the one with lower weight
                } else {
                    dp[item][capacity] = Math.max(dp[item - 1][capacity], maxValWithCurr);  // Choose the maximum value between the current and previous items
                }
            }
        }
        // Backtrack to find the items included in the optimal solution
        int capacity = MAXWEIGHT;
        accScore = 0; // Reset accumulated score
        accWeight = 0; // Reset accumulated weight
    
        for (int item = n; item > 0 && capacity > 0; item--) {                              // Iterate through the items in reverse order
            TreasureTile tile = treasureList.get(item - 1);                                 // Get the current tile      
    
            // Check if the current tile is part of the optimal solution
            if (dp[item][capacity] != dp[item - 1][capacity]) {                             // If the value is different, it means the current tile is included     
                plannedLoad.addFirst(tile);                                                 // Add the tile to the planned load
                accScore += tile.getScore();                                                // Update the accumulated score
                accWeight += tile.getWeight();                                              // Update the accumulated weight        
                capacity -= tile.getWeight();                                               // Decrease the capacity by the weight of the current tile      
            }
        }
    
        return plannedLoad;
    }

    private LinkedList<TreasureTile> planFractionalKnapsack(LinkedList<TreasureTile> treasureList)
    {        
        LinkedList<TreasureTile> plannedLoad = new LinkedList<>();
        accScore = 0;
        accWeight = 0;
    
        // Sort treasures by profit ratio
        treasureList = sortProfitRatio(treasureList);
    
        // Select treasures greedily, allowing fractional selection
        for (TreasureTile tile : treasureList) {
            if (accWeight + tile.getWeight() <= MAXWEIGHT) {
                plannedLoad.add(tile);
                accScore += tile.getScore();
                accWeight += tile.getWeight();
            } else {
                int remainingWeight = MAXWEIGHT - accWeight;
                if (remainingWeight > 0) {
                    accScore += (tile.getScore() * remainingWeight) / tile.getWeight();
                    accWeight = MAXWEIGHT;
                    plannedLoad.add(tile);
                }
    
                break;
            }
        }
    
        return plannedLoad;
    }

    

    public void makeAMove()
    {
        // making moves to collect all the needed treasures and reach the destination

        if(myLoad == null) return;

        char[][] mapRef = myMap.getMapRef();
        // 1) Check the landed Tile property
        //    - Speed UP / DOWN: update the speed param
        //    - Speed Cam / Police: update the risk score
        //    - At Intersection, follow path and change the direction
        //    - if at Destination, set completed to true

        TreasureTile nextTile = myLoad.get(myNodeCount);
        ScoreWeightPair consumedAmount;
               
        if(mapRef[yPos][xPos] == '+' || mapRef[yPos][xPos] == '$' || mapRef[yPos][xPos] == '?')
        {            
            if(nextTile.getX() == xPos && nextTile.getY() == yPos)
            {
                if(myMap.isSplittable() && (instWeight + nextTile.getWeight()) > MAXWEIGHT)
                {
                    // splitting
                    consumedAmount = nextTile.consumeTreasure(MAXWEIGHT - instWeight); // consume the remaining
                }
                else
                {
                    consumedAmount = nextTile.consumeTreasure(nextTile.getWeight());                    
                }

                instScore += consumedAmount.getScore();
                instWeight += consumedAmount.getWeight(); 
                myMap.updateTreasureStatus(nextTile);   

                nextTile = myLoad.get(--myNodeCount);  // move on to the next tile
            }
        }
        else if(mapRef[yPos][xPos] == 'D')
        {            
            completed = true;
            return;
        }         

        if      (xPos > nextTile.getX())   myDir = Direction.LEFT;
        else if (xPos < nextTile.getX())   myDir = Direction.RIGHT;
        else if (yPos > nextTile.getY())   myDir = Direction.UP;
        else if (yPos < nextTile.getY())   myDir = Direction.DOWN;

        // 2) Check direction and update x / y Pos
        //    - and increment Tile count
        if      (myDir == Direction.LEFT)  xPos--;
        else if (myDir == Direction.RIGHT) xPos++;
        else if (myDir == Direction.UP)    yPos--;
        else if (myDir == Direction.DOWN)  yPos++;

    }








    // Test Bench Below
    // Test Bench Below
    // Test Bench Below

    private static boolean totalPassed = true;
    private static int totalTestCount = 0;
    private static int totalPassCount = 0;

    public static void main(String args[])
    {        
        // add test here

        // 3 test cases for profit ratio sorting test
        testSortProfitRatioCase1();
        testSortProfitRatioCase2();
        testSortProfitRatioCaseCustom();

        // 2 test cases for Greedy Approach of Binary Knapsack
        //  ** and its shortcomings compared to DP counterpart Case1 and Case2
        testBinaryKnapsackGreedyCase1();
        testBinaryKnapsackGreedyCase2();
        
        // 5 test cases for binary knapsack
        testBinaryKnapsackCase1();
        testBinaryKnapsackCase2();
        testBinaryKnapsackCase3();
        testBinaryKnapsackCase4();
        testBinaryKnapsackCaseCustom();

        // 5 test cases for fractional knapsack
        testFractionalKnapsackCase1();
        testFractionalKnapsackCase2();
        testFractionalKnapsackCase3();
        testFractionalKnapsackCase4();
        testFractionalKnapsackCaseCustom();

        System.out.println("================================");
        System.out.printf("Test Score: %d / %d\n", 
                          totalPassCount, 
                          totalTestCount);
        if(totalPassed)  
        {
            System.out.println("All Tests Passed.");
            System.exit(0);
        }
        else
        {   
            System.out.println("Tests Failed.");
            System.exit(-1);
        }        
    }



    // 3 test cases for profit ratio sorting test
    private static void testSortProfitRatioCase1()
    {
        // Setup
        System.out.println("============testSortProfitRatioCase1=============");
        boolean passed = true;
        totalTestCount++;

        TreasureMap myMap = new TreasureMap(false);
        TreasureHunter myHunter = new TreasureHunter(1, 1, myMap);

        TreasureTile[] testTiles = new TreasureTile[10];
        testTiles[0] = new TreasureTile(0, 0, '+', false);
        testTiles[0].getDataPair().setScore(10);
        testTiles[0].getDataPair().setWeight(2);

        testTiles[1] = new TreasureTile(1, 1, '+', false);
        testTiles[1].getDataPair().setScore(12);
        testTiles[1].getDataPair().setWeight(4);

        testTiles[2] = new TreasureTile(2, 2, '+', false);
        testTiles[2].getDataPair().setScore(21);
        testTiles[2].getDataPair().setWeight(3);

        testTiles[3] = new TreasureTile(3, 3, '+', false);
        testTiles[3].getDataPair().setScore(13);
        testTiles[3].getDataPair().setWeight(3);

        testTiles[4] = new TreasureTile(4, 4, '+', false);
        testTiles[4].getDataPair().setScore(18);
        testTiles[4].getDataPair().setWeight(1);

        testTiles[5] = new TreasureTile(5, 5, '+', false);
        testTiles[5].getDataPair().setScore(9);
        testTiles[5].getDataPair().setWeight(2);

        testTiles[6] = new TreasureTile(6, 6, '+', false);
        testTiles[6].getDataPair().setScore(22);
        testTiles[6].getDataPair().setWeight(5);

        testTiles[7] = new TreasureTile(7, 7, '+', false);
        testTiles[7].getDataPair().setScore(15);
        testTiles[7].getDataPair().setWeight(6);

        testTiles[8] = new TreasureTile(8, 8, '+', false);
        testTiles[8].getDataPair().setScore(16);
        testTiles[8].getDataPair().setWeight(4);

        testTiles[9] = new TreasureTile(9, 9, '+', false);
        testTiles[9].getDataPair().setScore(20);
        testTiles[9].getDataPair().setWeight(3);

        LinkedList<TreasureTile> treasureList = new LinkedList<>();
        for(int i = 0; i < 10; i++)
            treasureList.add(testTiles[i]);

        // Action
        LinkedList<TreasureTile> targetList = myHunter.sortProfitRatio(treasureList);        
        
        System.out.println("\tTest Array Sorted in Descending Order of Profit Ratio");
        for(int i = 0; i < 9; i++)
        {
            TreasureTile currTile = targetList.get(i);
            TreasureTile nextTile = targetList.get(i + 1);
            float currRatio = (float)currTile.getScore() / currTile.getWeight();
            float nextRatio = (float)nextTile.getScore() / nextTile.getWeight();

            System.out.printf("\t\tTest if Element #%d >= Element %d\n", i, i + 1);
            passed &= assertEquals(true, currRatio >= nextRatio);
        }

        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }
    
    private static void testSortProfitRatioCase2()
    {
        // Setup
        System.out.println("============testSortProfitRatioCase2=============");
        boolean passed = true;
        totalTestCount++;

        TreasureMap myMap = new TreasureMap(false);
        TreasureHunter myHunter = new TreasureHunter(1, 1, myMap);

        TreasureTile[] testTiles = new TreasureTile[15];
        testTiles[0] = new TreasureTile(0, 0, '+', false);
        testTiles[0].getDataPair().setScore(12);
        testTiles[0].getDataPair().setWeight(3);

        testTiles[1] = new TreasureTile(1, 1, '+', false);
        testTiles[1].getDataPair().setScore(7);
        testTiles[1].getDataPair().setWeight(2);

        testTiles[2] = new TreasureTile(2, 2, '+', false);
        testTiles[2].getDataPair().setScore(18);
        testTiles[2].getDataPair().setWeight(4);

        testTiles[3] = new TreasureTile(3, 3, '+', false);
        testTiles[3].getDataPair().setScore(9);
        testTiles[3].getDataPair().setWeight(1);

        testTiles[4] = new TreasureTile(4, 4, '+', false);
        testTiles[4].getDataPair().setScore(15);
        testTiles[4].getDataPair().setWeight(2);

        testTiles[5] = new TreasureTile(5, 5, '+', false);
        testTiles[5].getDataPair().setScore(11);
        testTiles[5].getDataPair().setWeight(3);

        testTiles[6] = new TreasureTile(6, 6, '+', false);
        testTiles[6].getDataPair().setScore(18);
        testTiles[6].getDataPair().setWeight(4);

        testTiles[7] = new TreasureTile(7, 7, '+', false);
        testTiles[7].getDataPair().setScore(22);
        testTiles[7].getDataPair().setWeight(4);

        testTiles[8] = new TreasureTile(8, 8, '+', false);
        testTiles[8].getDataPair().setScore(14);
        testTiles[8].getDataPair().setWeight(3);

        testTiles[9] = new TreasureTile(9, 9, '+', false);
        testTiles[9].getDataPair().setScore(8);
        testTiles[9].getDataPair().setWeight(2);

        testTiles[10] = new TreasureTile(10, 10, '+', false);
        testTiles[10].getDataPair().setScore(14);
        testTiles[10].getDataPair().setWeight(2);

        testTiles[11] = new TreasureTile(11, 11, '+', false);
        testTiles[11].getDataPair().setScore(7);
        testTiles[11].getDataPair().setWeight(1);

        testTiles[12] = new TreasureTile(12, 12, '+', false);
        testTiles[12].getDataPair().setScore(18);
        testTiles[12].getDataPair().setWeight(4);

        testTiles[13] = new TreasureTile(13, 13, '+', false);
        testTiles[13].getDataPair().setScore(20);
        testTiles[13].getDataPair().setWeight(4);

        testTiles[14] = new TreasureTile(14, 14, '+', false);
        testTiles[14].getDataPair().setScore(17);
        testTiles[14].getDataPair().setWeight(3);

        LinkedList<TreasureTile> treasureList = new LinkedList<>();
        for(int i = 0; i < 15; i++)
            treasureList.add(testTiles[i]);

        // Action
        LinkedList<TreasureTile> targetList = myHunter.sortProfitRatio(treasureList);        
        
        System.out.println("\tTest Array Sorted in Descending Order of Profit Ratio");
        for(int i = 0; i < 14; i++)
        {
            TreasureTile currTile = targetList.get(i);
            TreasureTile nextTile = targetList.get(i + 1);
            float currRatio = (float)currTile.getScore() / currTile.getWeight();
            float nextRatio = (float)nextTile.getScore() / nextTile.getWeight();

            System.out.printf("\t\tTest if Element #%d >= Element %d\n", i, i + 1);
            passed &= assertEquals(true, currRatio >= nextRatio);
        }

        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }
    
    private static void testSortProfitRatioCaseCustom()
    {
        // Setup
        System.out.println("============testSortProfitRatioCaseCustom=============");
        boolean passed = true;
        totalTestCount++;

        // Add your own custom test here
        // Design another case with minimally 8 Treasure Tiles

        TreasureMap myMap = new TreasureMap(false);
        TreasureHunter myHunter = new TreasureHunter(1, 1, myMap);

        TreasureTile[] testTiles = new TreasureTile[15];
        testTiles[0] = new TreasureTile(0, 0, '+', false);
        testTiles[0].getDataPair().setScore(12);
        testTiles[0].getDataPair().setWeight(3);

        testTiles[1] = new TreasureTile(1, 1, '+', false);
        testTiles[1].getDataPair().setScore(8);
        testTiles[1].getDataPair().setWeight(3);

        testTiles[2] = new TreasureTile(2, 2, '+', false);
        testTiles[2].getDataPair().setScore(17);
        testTiles[2].getDataPair().setWeight(5);

        testTiles[3] = new TreasureTile(3, 3, '+', false);
        testTiles[3].getDataPair().setScore(9);
        testTiles[3].getDataPair().setWeight(1);

        testTiles[4] = new TreasureTile(4, 4, '+', false);
        testTiles[4].getDataPair().setScore(13);
        testTiles[4].getDataPair().setWeight(2);

        testTiles[5] = new TreasureTile(5, 5, '+', false);
        testTiles[5].getDataPair().setScore(15);
        testTiles[5].getDataPair().setWeight(3);

        testTiles[6] = new TreasureTile(6, 6, '+', false);
        testTiles[6].getDataPair().setScore(18);
        testTiles[6].getDataPair().setWeight(4);

        testTiles[7] = new TreasureTile(7, 7, '+', false);
        testTiles[7].getDataPair().setScore(25);
        testTiles[7].getDataPair().setWeight(4);

        testTiles[8] = new TreasureTile(8, 8, '+', false);
        testTiles[8].getDataPair().setScore(14);
        testTiles[8].getDataPair().setWeight(3);

        testTiles[9] = new TreasureTile(9, 9, '+', false);
        testTiles[9].getDataPair().setScore(8);
        testTiles[9].getDataPair().setWeight(2);

        testTiles[10] = new TreasureTile(10, 10, '+', false);
        testTiles[10].getDataPair().setScore(17);
        testTiles[10].getDataPair().setWeight(2);

        testTiles[11] = new TreasureTile(11, 11, '+', false);
        testTiles[11].getDataPair().setScore(27);
        testTiles[11].getDataPair().setWeight(1);

        testTiles[12] = new TreasureTile(12, 12, '+', false);
        testTiles[12].getDataPair().setScore(18);
        testTiles[12].getDataPair().setWeight(4);

        testTiles[13] = new TreasureTile(13, 13, '+', false);
        testTiles[13].getDataPair().setScore(20);
        testTiles[13].getDataPair().setWeight(4);

        testTiles[14] = new TreasureTile(14, 14, '+', false);
        testTiles[14].getDataPair().setScore(17);
        testTiles[14].getDataPair().setWeight(3);

        LinkedList<TreasureTile> treasureList = new LinkedList<>();
        for(int i = 0; i < 15; i++)
            treasureList.add(testTiles[i]);

        // Action
        LinkedList<TreasureTile> targetList = myHunter.sortProfitRatio(treasureList);        
        
        System.out.println("\tTest Array Sorted in Descending Order of Profit Ratio");
        for(int i = 0; i < 14; i++)
        {
            TreasureTile currTile = targetList.get(i);
            TreasureTile nextTile = targetList.get(i + 1);
            float currRatio = (float)currTile.getScore() / currTile.getWeight();
            float nextRatio = (float)nextTile.getScore() / nextTile.getWeight();

            System.out.printf("\t\tTest if Element #%d >= Element %d\n", i, i + 1);
            passed &= assertEquals(true, currRatio >= nextRatio);
        }

        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }


    // 2 test cases for binary knapsack using Greedy approach (and bad)
    private static void testBinaryKnapsackGreedyCase1()
    {
        // Setup
        System.out.println("============testBinaryKnapsackGreedyCase1=============");
        boolean passed = true;
        totalTestCount++;

        TreasureMap myMap = new TreasureMap(false);
        TreasureHunter myHunter = new TreasureHunter(1, 1, myMap);    
        // MUST RESET accScore and accWeight because TreasureHunter constructor
        // triggers automatic knapsack algorithm for the newly generated map!!    
        myHunter.accScore = 0;
        myHunter.accWeight = 0;

        TreasureTile[] testTiles = new TreasureTile[6];
        testTiles[0] = new TreasureTile(0, 0, '+', false);
        testTiles[0].getDataPair().setScore(10);
        testTiles[0].getDataPair().setWeight(15);

        testTiles[1] = new TreasureTile(1, 1, '+', false);
        testTiles[1].getDataPair().setScore(40);
        testTiles[1].getDataPair().setWeight(8);

        testTiles[2] = new TreasureTile(2, 2, '+', false);
        testTiles[2].getDataPair().setScore(30);
        testTiles[2].getDataPair().setWeight(12);

        testTiles[3] = new TreasureTile(3, 3, '+', false);
        testTiles[3].getDataPair().setScore(50);
        testTiles[3].getDataPair().setWeight(13);

        testTiles[4] = new TreasureTile(4, 4, '+', false);
        testTiles[4].getDataPair().setScore(15);
        testTiles[4].getDataPair().setWeight(7);

        testTiles[5] = new TreasureTile(5, 5, '+', false);
        testTiles[5].getDataPair().setScore(60);
        testTiles[5].getDataPair().setWeight(10);

        int expectedAccScore = 100;
        int expectedAccWeight = 18; 

        LinkedList<TreasureTile> treasureList = new LinkedList<>();
        for(int i = 0; i < 6; i++)
            treasureList.add(testTiles[i]);

        // Action
        LinkedList<TreasureTile> targetList = myHunter.planBinaryKnapsackGreedy(treasureList);
        int accScore = myHunter.getScore();        
        int accWeight = myHunter.getWeight();               

        System.out.println("\tTest Accumulated Score");
        passed &= assertEquals(expectedAccScore, accScore);

        System.out.println("\tTest Accumulated Weight");
        passed &= assertEquals(expectedAccWeight, accWeight);

        System.out.println("\tTest Treasures in Knapsack");
        boolean expectedExistence, actualExistence;

        System.out.println("\t\tTest Tiles #0");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[0]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #1");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[1]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #2");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[2]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #3");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[3]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #4");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[4]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #5");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[5]);
        passed &= assertEquals(expectedExistence, actualExistence);        

        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }
    
    private static void testBinaryKnapsackGreedyCase2()
    {
        // Setup
        System.out.println("============testBinaryKnapsackGreedyCase3=============");
        boolean passed = true;
        totalTestCount++;

        TreasureMap myMap = new TreasureMap(false);
        TreasureHunter myHunter = new TreasureHunter(1, 1, myMap);
        // MUST RESET accScore and accWeight because TreasureHunter constructor
        // triggers automatic knapsack algorithm for the newly generated map!!
        myHunter.accScore = 0;
        myHunter.accWeight = 0;

        TreasureTile[] testTiles = new TreasureTile[10];
        testTiles[0] = new TreasureTile(0, 0, '+', false);
        testTiles[0].getDataPair().setScore(10);
        testTiles[0].getDataPair().setWeight(2);

        testTiles[1] = new TreasureTile(1, 1, '+', false);
        testTiles[1].getDataPair().setScore(12);
        testTiles[1].getDataPair().setWeight(4);

        testTiles[2] = new TreasureTile(2, 2, '+', false);
        testTiles[2].getDataPair().setScore(21);
        testTiles[2].getDataPair().setWeight(3);

        testTiles[3] = new TreasureTile(3, 3, '+', false);
        testTiles[3].getDataPair().setScore(13);
        testTiles[3].getDataPair().setWeight(3);

        testTiles[4] = new TreasureTile(4, 4, '+', false);
        testTiles[4].getDataPair().setScore(18);
        testTiles[4].getDataPair().setWeight(1);

        testTiles[5] = new TreasureTile(5, 5, '+', false);
        testTiles[5].getDataPair().setScore(9);
        testTiles[5].getDataPair().setWeight(2);

        testTiles[6] = new TreasureTile(6, 6, '+', false);
        testTiles[6].getDataPair().setScore(22);
        testTiles[6].getDataPair().setWeight(5);

        testTiles[7] = new TreasureTile(7, 7, '+', false);
        testTiles[7].getDataPair().setScore(15);
        testTiles[7].getDataPair().setWeight(6);

        testTiles[8] = new TreasureTile(8, 8, '+', false);
        testTiles[8].getDataPair().setScore(16);
        testTiles[8].getDataPair().setWeight(4);

        testTiles[9] = new TreasureTile(9, 9, '+', false);
        testTiles[9].getDataPair().setScore(20);
        testTiles[9].getDataPair().setWeight(3);

        int expectedAccScore = 129;
        int expectedAccWeight = 23;

        LinkedList<TreasureTile> treasureList = new LinkedList<>();
        for(int i = 0; i < 10; i++)
            treasureList.add(testTiles[i]);

        // Action
        LinkedList<TreasureTile> targetList = myHunter.planBinaryKnapsackGreedy(treasureList);
        int accScore = myHunter.getScore();        
        int accWeight = myHunter.getWeight();        
        
        System.out.println("\tTest Accumulated Score");
        passed &= assertEquals(expectedAccScore, accScore);

        System.out.println("\tTest Accumulated Weight");
        passed &= assertEquals(expectedAccWeight, accWeight);

        System.out.println("\tTest Treasures in Knapsack");
        boolean expectedExistence, actualExistence;

        System.out.println("\t\tTest Tiles #0");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[0]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #1");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[1]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #2");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[2]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #3");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[3]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #4");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[4]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #5");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[5]);
        passed &= assertEquals(expectedExistence, actualExistence);        

        System.out.println("\t\tTest Tiles #6");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[6]);
        passed &= assertEquals(expectedExistence, actualExistence);   

        System.out.println("\t\tTest Tiles #7");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[7]);
        passed &= assertEquals(expectedExistence, actualExistence);   

        System.out.println("\t\tTest Tiles #8");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[8]);
        passed &= assertEquals(expectedExistence, actualExistence); 

        System.out.println("\t\tTest Tiles #9");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[9]);
        passed &= assertEquals(expectedExistence, actualExistence); 

        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }
    
    


    // 5 test cases for binary knapsack
    private static void testBinaryKnapsackCase1()
    {
        // Setup
        System.out.println("============testBinaryKnapsackCase1=============");
        boolean passed = true;
        totalTestCount++;

        TreasureMap myMap = new TreasureMap(false);
        TreasureHunter myHunter = new TreasureHunter(1, 1, myMap);    
        // MUST RESET accScore and accWeight because TreasureHunter constructor
        // triggers automatic knapsack algorithm for the newly generated map!!    
        myHunter.accScore = 0;
        myHunter.accWeight = 0;

        TreasureTile[] testTiles = new TreasureTile[6];
        testTiles[0] = new TreasureTile(0, 0, '+', false);
        testTiles[0].getDataPair().setScore(10);
        testTiles[0].getDataPair().setWeight(15);

        testTiles[1] = new TreasureTile(1, 1, '+', false);
        testTiles[1].getDataPair().setScore(40);
        testTiles[1].getDataPair().setWeight(8);

        testTiles[2] = new TreasureTile(2, 2, '+', false);
        testTiles[2].getDataPair().setScore(30);
        testTiles[2].getDataPair().setWeight(12);

        testTiles[3] = new TreasureTile(3, 3, '+', false);
        testTiles[3].getDataPair().setScore(50);
        testTiles[3].getDataPair().setWeight(13);

        testTiles[4] = new TreasureTile(4, 4, '+', false);
        testTiles[4].getDataPair().setScore(15);
        testTiles[4].getDataPair().setWeight(7);

        testTiles[5] = new TreasureTile(5, 5, '+', false);
        testTiles[5].getDataPair().setScore(60);
        testTiles[5].getDataPair().setWeight(10);

        int expectedAccScore = 115;
        int expectedAccWeight = 25; 

        LinkedList<TreasureTile> treasureList = new LinkedList<>();
        for(int i = 0; i < 6; i++)
            treasureList.add(testTiles[i]);

        // Action
        LinkedList<TreasureTile> targetList = myHunter.planBinaryKnapsackDP(treasureList);
        int accScore = myHunter.getScore();        
        int accWeight = myHunter.getWeight();               

        System.out.println("\tTest Accumulated Score");
        passed &= assertEquals(expectedAccScore, accScore);

        System.out.println("\tTest Accumulated Weight");
        passed &= assertEquals(expectedAccWeight, accWeight);

        System.out.println("\tTest Treasures in Knapsack");
        boolean expectedExistence, actualExistence;

        System.out.println("\t\tTest Tiles #0");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[0]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #1");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[1]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #2");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[2]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #3");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[3]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #4");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[4]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #5");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[5]);
        passed &= assertEquals(expectedExistence, actualExistence);        

        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }
    
    private static void testBinaryKnapsackCase2()
    {
        // Setup
        System.out.println("============testBinaryKnapsackCase2=============");
        boolean passed = true;
        totalTestCount++;

        TreasureMap myMap = new TreasureMap(false);
        TreasureHunter myHunter = new TreasureHunter(1, 1, myMap);
        // MUST RESET accScore and accWeight because TreasureHunter constructor
        // triggers automatic knapsack algorithm for the newly generated map!!
        myHunter.accScore = 0;
        myHunter.accWeight = 0;

        TreasureTile[] testTiles = new TreasureTile[8];
        testTiles[0] = new TreasureTile(0, 0, '+', false);
        testTiles[0].getDataPair().setScore(12);
        testTiles[0].getDataPair().setWeight(6);

        testTiles[1] = new TreasureTile(1, 1, '+', false);
        testTiles[1].getDataPair().setScore(21);
        testTiles[1].getDataPair().setWeight(5);

        testTiles[2] = new TreasureTile(2, 2, '+', false);
        testTiles[2].getDataPair().setScore(15);
        testTiles[2].getDataPair().setWeight(8);

        testTiles[3] = new TreasureTile(3, 3, '+', false);
        testTiles[3].getDataPair().setScore(25);
        testTiles[3].getDataPair().setWeight(7);

        testTiles[4] = new TreasureTile(4, 4, '+', false);
        testTiles[4].getDataPair().setScore(16);
        testTiles[4].getDataPair().setWeight(9);

        testTiles[5] = new TreasureTile(5, 5, '+', false);
        testTiles[5].getDataPair().setScore(12);
        testTiles[5].getDataPair().setWeight(5);

        testTiles[6] = new TreasureTile(6, 6, '+', false);
        testTiles[6].getDataPair().setScore(17);
        testTiles[6].getDataPair().setWeight(7);

        testTiles[7] = new TreasureTile(7, 7, '+', false);
        testTiles[7].getDataPair().setScore(15);
        testTiles[7].getDataPair().setWeight(8);

        int expectedAccScore = 75;
        int expectedAccWeight = 24;

        LinkedList<TreasureTile> treasureList = new LinkedList<>();
        for(int i = 0; i < 8; i++)
            treasureList.add(testTiles[i]);

        // Action
        LinkedList<TreasureTile> targetList = myHunter.planBinaryKnapsackDP(treasureList);
        int accScore = myHunter.getScore();        
        int accWeight = myHunter.getWeight();        

        System.out.println("\tTest Accumulated Score");
        passed &= assertEquals(expectedAccScore, accScore);

        System.out.println("\tTest Accumulated Weight");
        passed &= assertEquals(expectedAccWeight, accWeight);

        System.out.println("\tTest Treasures in Knapsack");
        boolean expectedExistence, actualExistence;

        System.out.println("\t\tTest Tiles #0");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[0]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #1");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[1]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #2");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[2]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #3");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[3]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #4");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[4]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #5");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[5]);
        passed &= assertEquals(expectedExistence, actualExistence);        

        System.out.println("\t\tTest Tiles #6");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[6]);
        passed &= assertEquals(expectedExistence, actualExistence);   

        System.out.println("\t\tTest Tiles #7");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[7]);
        passed &= assertEquals(expectedExistence, actualExistence);   

        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }
    
    private static void testBinaryKnapsackCase3()
    {
        // Setup
        System.out.println("============testBinaryKnapsackCase3=============");
        boolean passed = true;
        totalTestCount++;

        TreasureMap myMap = new TreasureMap(false);
        TreasureHunter myHunter = new TreasureHunter(1, 1, myMap);
        // MUST RESET accScore and accWeight because TreasureHunter constructor
        // triggers automatic knapsack algorithm for the newly generated map!!
        myHunter.accScore = 0;
        myHunter.accWeight = 0;

        TreasureTile[] testTiles = new TreasureTile[10];
        testTiles[0] = new TreasureTile(0, 0, '+', false);
        testTiles[0].getDataPair().setScore(10);
        testTiles[0].getDataPair().setWeight(2);

        testTiles[1] = new TreasureTile(1, 1, '+', false);
        testTiles[1].getDataPair().setScore(12);
        testTiles[1].getDataPair().setWeight(4);

        testTiles[2] = new TreasureTile(2, 2, '+', false);
        testTiles[2].getDataPair().setScore(21);
        testTiles[2].getDataPair().setWeight(3);

        testTiles[3] = new TreasureTile(3, 3, '+', false);
        testTiles[3].getDataPair().setScore(13);
        testTiles[3].getDataPair().setWeight(3);

        testTiles[4] = new TreasureTile(4, 4, '+', false);
        testTiles[4].getDataPair().setScore(18);
        testTiles[4].getDataPair().setWeight(1);

        testTiles[5] = new TreasureTile(5, 5, '+', false);
        testTiles[5].getDataPair().setScore(9);
        testTiles[5].getDataPair().setWeight(2);

        testTiles[6] = new TreasureTile(6, 6, '+', false);
        testTiles[6].getDataPair().setScore(22);
        testTiles[6].getDataPair().setWeight(5);

        testTiles[7] = new TreasureTile(7, 7, '+', false);
        testTiles[7].getDataPair().setScore(15);
        testTiles[7].getDataPair().setWeight(6);

        testTiles[8] = new TreasureTile(8, 8, '+', false);
        testTiles[8].getDataPair().setScore(16);
        testTiles[8].getDataPair().setWeight(4);

        testTiles[9] = new TreasureTile(9, 9, '+', false);
        testTiles[9].getDataPair().setScore(20);
        testTiles[9].getDataPair().setWeight(3);

        int expectedAccScore = 132;
        int expectedAccWeight = 25;

        LinkedList<TreasureTile> treasureList = new LinkedList<>();
        for(int i = 0; i < 10; i++)
            treasureList.add(testTiles[i]);

        // Action
        LinkedList<TreasureTile> targetList = myHunter.planBinaryKnapsackDP(treasureList);
        int accScore = myHunter.getScore();        
        int accWeight = myHunter.getWeight();        
        
        System.out.println("\tTest Accumulated Score");
        passed &= assertEquals(expectedAccScore, accScore);

        System.out.println("\tTest Accumulated Weight");
        passed &= assertEquals(expectedAccWeight, accWeight);

        System.out.println("\tTest Treasures in Knapsack");
        boolean expectedExistence, actualExistence;

        System.out.println("\t\tTest Tiles #0");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[0]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #1");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[1]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #2");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[2]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #3");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[3]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #4");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[4]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #5");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[5]);
        passed &= assertEquals(expectedExistence, actualExistence);        

        System.out.println("\t\tTest Tiles #6");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[6]);
        passed &= assertEquals(expectedExistence, actualExistence);   

        System.out.println("\t\tTest Tiles #7");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[7]);
        passed &= assertEquals(expectedExistence, actualExistence);   

        System.out.println("\t\tTest Tiles #8");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[8]);
        passed &= assertEquals(expectedExistence, actualExistence); 

        System.out.println("\t\tTest Tiles #9");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[9]);
        passed &= assertEquals(expectedExistence, actualExistence); 

        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }
    
    private static void testBinaryKnapsackCase4()
    {
        // Setup
        System.out.println("============testBinaryKnapsackCase4=============");
        boolean passed = true;
        totalTestCount++;

        TreasureMap myMap = new TreasureMap(false);
        TreasureHunter myHunter = new TreasureHunter(1, 1, myMap);
        // MUST RESET accScore and accWeight because TreasureHunter constructor
        // triggers automatic knapsack algorithm for the newly generated map!!
        myHunter.accScore = 0;
        myHunter.accWeight = 0;

        TreasureTile[] testTiles = new TreasureTile[15];
        testTiles[0] = new TreasureTile(0, 0, '+', false);
        testTiles[0].getDataPair().setScore(12);
        testTiles[0].getDataPair().setWeight(3);

        testTiles[1] = new TreasureTile(1, 1, '+', false);
        testTiles[1].getDataPair().setScore(7);
        testTiles[1].getDataPair().setWeight(2);

        testTiles[2] = new TreasureTile(2, 2, '+', false);
        testTiles[2].getDataPair().setScore(18);
        testTiles[2].getDataPair().setWeight(4);

        testTiles[3] = new TreasureTile(3, 3, '+', false);
        testTiles[3].getDataPair().setScore(9);
        testTiles[3].getDataPair().setWeight(1);

        testTiles[4] = new TreasureTile(4, 4, '+', false);
        testTiles[4].getDataPair().setScore(15);
        testTiles[4].getDataPair().setWeight(2);

        testTiles[5] = new TreasureTile(5, 5, '+', false);
        testTiles[5].getDataPair().setScore(11);
        testTiles[5].getDataPair().setWeight(3);

        testTiles[6] = new TreasureTile(6, 6, '+', false);
        testTiles[6].getDataPair().setScore(18);
        testTiles[6].getDataPair().setWeight(4);

        testTiles[7] = new TreasureTile(7, 7, '+', false);
        testTiles[7].getDataPair().setScore(22);
        testTiles[7].getDataPair().setWeight(4);

        testTiles[8] = new TreasureTile(8, 8, '+', false);
        testTiles[8].getDataPair().setScore(14);
        testTiles[8].getDataPair().setWeight(3);

        testTiles[9] = new TreasureTile(9, 9, '+', false);
        testTiles[9].getDataPair().setScore(8);
        testTiles[9].getDataPair().setWeight(2);

        testTiles[10] = new TreasureTile(10, 10, '+', false);
        testTiles[10].getDataPair().setScore(14);
        testTiles[10].getDataPair().setWeight(2);

        testTiles[11] = new TreasureTile(11, 11, '+', false);
        testTiles[11].getDataPair().setScore(7);
        testTiles[11].getDataPair().setWeight(1);

        testTiles[12] = new TreasureTile(12, 12, '+', false);
        testTiles[12].getDataPair().setScore(18);
        testTiles[12].getDataPair().setWeight(4);

        testTiles[13] = new TreasureTile(13, 13, '+', false);
        testTiles[13].getDataPair().setScore(20);
        testTiles[13].getDataPair().setWeight(4);

        testTiles[14] = new TreasureTile(14, 14, '+', false);
        testTiles[14].getDataPair().setScore(17);
        testTiles[14].getDataPair().setWeight(3);

        int expectedAccScore = 140;
        int expectedAccWeight = 25;

        LinkedList<TreasureTile> treasureList = new LinkedList<>();
        for(int i = 0; i < 15; i++)
            treasureList.add(testTiles[i]);

        // Action
        LinkedList<TreasureTile> targetList = myHunter.planBinaryKnapsackDP(treasureList);
        int accScore = myHunter.getScore();        
        int accWeight = myHunter.getWeight();        
        
        System.out.println("\tTest Accumulated Score");
        passed &= assertEquals(expectedAccScore, accScore);

        System.out.println("\tTest Accumulated Weight");
        passed &= assertEquals(expectedAccWeight, accWeight);

        System.out.println("\tTest Treasures in Knapsack");
        boolean expectedExistence, actualExistence;

        System.out.println("\t\tTest Tiles #0");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[0]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #1");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[1]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #2");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[2]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #3");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[3]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #4");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[4]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #5");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[5]);
        passed &= assertEquals(expectedExistence, actualExistence);        

        System.out.println("\t\tTest Tiles #6");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[6]);
        passed &= assertEquals(expectedExistence, actualExistence);   

        System.out.println("\t\tTest Tiles #7");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[7]);
        passed &= assertEquals(expectedExistence, actualExistence);   

        System.out.println("\t\tTest Tiles #8");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[8]);
        passed &= assertEquals(expectedExistence, actualExistence); 

        System.out.println("\t\tTest Tiles #9");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[9]);
        passed &= assertEquals(expectedExistence, actualExistence); 

        System.out.println("\t\tTest Tiles #10");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[10]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #11");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[11]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #12");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[12]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #13");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[13]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #14");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[14]);
        passed &= assertEquals(expectedExistence, actualExistence);

        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }
    
    private static void testBinaryKnapsackCaseCustom()
    {
        // Setup
        System.out.println("============testBinaryKnapsackCaseCustom=============");
        boolean passed = true;
        totalTestCount++;

        TreasureMap myMap = new TreasureMap(false);
        TreasureHunter myHunter = new TreasureHunter(1, 1, myMap);
        // MUST RESET accScore and accWeight because TreasureHunter constructor
        // triggers automatic knapsack algorithm for the newly generated map!!
        myHunter.accScore = 0;
        myHunter.accWeight = 0;

        TreasureTile[] testTiles = new TreasureTile[15];
        testTiles[0] = new TreasureTile(0, 0, '+', false);
        testTiles[0].getDataPair().setScore(12);
        testTiles[0].getDataPair().setWeight(3);

        testTiles[1] = new TreasureTile(1, 1, '+', false);
        testTiles[1].getDataPair().setScore(8);
        testTiles[1].getDataPair().setWeight(3);

        testTiles[2] = new TreasureTile(2, 2, '+', false);
        testTiles[2].getDataPair().setScore(17);
        testTiles[2].getDataPair().setWeight(5);

        testTiles[3] = new TreasureTile(3, 3, '+', false);
        testTiles[3].getDataPair().setScore(9);
        testTiles[3].getDataPair().setWeight(1);

        testTiles[4] = new TreasureTile(4, 4, '+', false);
        testTiles[4].getDataPair().setScore(13);
        testTiles[4].getDataPair().setWeight(2);

        testTiles[5] = new TreasureTile(5, 5, '+', false);
        testTiles[5].getDataPair().setScore(15);
        testTiles[5].getDataPair().setWeight(3);

        testTiles[6] = new TreasureTile(6, 6, '+', false);
        testTiles[6].getDataPair().setScore(18);
        testTiles[6].getDataPair().setWeight(4);

        testTiles[7] = new TreasureTile(7, 7, '+', false);
        testTiles[7].getDataPair().setScore(25);
        testTiles[7].getDataPair().setWeight(4);

        testTiles[8] = new TreasureTile(8, 8, '+', false);
        testTiles[8].getDataPair().setScore(14);
        testTiles[8].getDataPair().setWeight(3);

        testTiles[9] = new TreasureTile(9, 9, '+', false);
        testTiles[9].getDataPair().setScore(8);
        testTiles[9].getDataPair().setWeight(2);

        testTiles[10] = new TreasureTile(10, 10, '+', false);
        testTiles[10].getDataPair().setScore(17);
        testTiles[10].getDataPair().setWeight(2);

        testTiles[11] = new TreasureTile(11, 11, '+', false);
        testTiles[11].getDataPair().setScore(27);
        testTiles[11].getDataPair().setWeight(1);

        testTiles[12] = new TreasureTile(12, 12, '+', false);
        testTiles[12].getDataPair().setScore(18);
        testTiles[12].getDataPair().setWeight(4);

        testTiles[13] = new TreasureTile(13, 13, '+', false);
        testTiles[13].getDataPair().setScore(20);
        testTiles[13].getDataPair().setWeight(4);

        testTiles[14] = new TreasureTile(14, 14, '+', false);
        testTiles[14].getDataPair().setScore(17);
        testTiles[14].getDataPair().setWeight(3);

        int expectedAccScore = 165;
        int expectedAccWeight = 25;

        LinkedList<TreasureTile> treasureList = new LinkedList<>();
        for(int i = 0; i < 15; i++)
            treasureList.add(testTiles[i]);

        // Action
        LinkedList<TreasureTile> targetList = myHunter.planBinaryKnapsackDP(treasureList);
        int accScore = myHunter.getScore();        
        int accWeight = myHunter.getWeight();        
        
        System.out.println("\tTest Accumulated Score");
        passed &= assertEquals(expectedAccScore, accScore);

        System.out.println("\tTest Accumulated Weight");
        passed &= assertEquals(expectedAccWeight, accWeight);

        System.out.println("\tTest Treasures in Knapsack");
        boolean expectedExistence, actualExistence;

        System.out.println("\t\tTest Tiles #0");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[0]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #1");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[1]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #2");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[2]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #3");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[3]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #4");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[4]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #5");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[5]);
        passed &= assertEquals(expectedExistence, actualExistence);        

        System.out.println("\t\tTest Tiles #6");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[6]);
        passed &= assertEquals(expectedExistence, actualExistence);   

        System.out.println("\t\tTest Tiles #7");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[7]);
        passed &= assertEquals(expectedExistence, actualExistence);   

        System.out.println("\t\tTest Tiles #8");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[8]);
        passed &= assertEquals(expectedExistence, actualExistence); 

        System.out.println("\t\tTest Tiles #9");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[9]);
        passed &= assertEquals(expectedExistence, actualExistence); 

        System.out.println("\t\tTest Tiles #10");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[10]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #11");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[11]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #12");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[12]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #13");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[13]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #14");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[14]);
        passed &= assertEquals(expectedExistence, actualExistence);

        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }
    
    
    
    // 5 test cases for fractional knapsack
    private static void testFractionalKnapsackCase1()
    {
        // Setup
        System.out.println("============testFractionalKnapsackCase1=============");
        boolean passed = true;
        totalTestCount++;

        TreasureMap myMap = new TreasureMap(true);
        TreasureHunter myHunter = new TreasureHunter(1, 1, myMap);
        // MUST RESET accScore and accWeight because TreasureHunter constructor
        // triggers automatic knapsack algorithm for the newly generated map!!
        myHunter.accScore = 0; 
        myHunter.accWeight = 0;

        TreasureTile[] testTiles = new TreasureTile[6];
        testTiles[0] = new TreasureTile(0, 0, '+', false);
        testTiles[0].getDataPair().setScore(10);
        testTiles[0].getDataPair().setWeight(15);

        testTiles[1] = new TreasureTile(1, 1, '+', false);
        testTiles[1].getDataPair().setScore(40);
        testTiles[1].getDataPair().setWeight(8);

        testTiles[2] = new TreasureTile(2, 2, '+', false);
        testTiles[2].getDataPair().setScore(30);
        testTiles[2].getDataPair().setWeight(12);

        testTiles[3] = new TreasureTile(3, 3, '+', false);
        testTiles[3].getDataPair().setScore(50);
        testTiles[3].getDataPair().setWeight(13);

        testTiles[4] = new TreasureTile(4, 4, '+', false);
        testTiles[4].getDataPair().setScore(15);
        testTiles[4].getDataPair().setWeight(7);

        testTiles[5] = new TreasureTile(5, 5, '+', false);
        testTiles[5].getDataPair().setScore(60);
        testTiles[5].getDataPair().setWeight(10);

        int expectedAccScore = 126;
        int expectedAccWeight = 25; 

        LinkedList<TreasureTile> treasureList = new LinkedList<>();
        for(int i = 0; i < 6; i++)
            treasureList.add(testTiles[i]);

        // Action
        LinkedList<TreasureTile> targetList = myHunter.planFractionalKnapsack(treasureList);        
        int accScore = myHunter.getScore();        
        int accWeight = myHunter.getWeight(); 
        
        System.out.println("\tTest Accumulated Score");
        passed &= assertEquals(expectedAccScore, accScore);

        System.out.println("\tTest Accumulated Weight");
        passed &= assertEquals(expectedAccWeight, accWeight);

        System.out.println("\tTest Treasures in Knapsack");
        boolean expectedExistence, actualExistence;

        System.out.println("\t\tTest Tiles #0");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[0]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #1");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[1]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #2");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[2]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #3");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[3]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #4");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[4]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #5");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[5]);
        passed &= assertEquals(expectedExistence, actualExistence);        

        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }
    
    private static void testFractionalKnapsackCase2()
    {
        // Setup
        System.out.println("============testFractionalKnapsackCase2=============");
        boolean passed = true;
        totalTestCount++;

        TreasureMap myMap = new TreasureMap(true);
        TreasureHunter myHunter = new TreasureHunter(1, 1, myMap);
        // MUST RESET accScore and accWeight because TreasureHunter constructor
        // triggers automatic knapsack algorithm for the newly generated map!!
        myHunter.accScore = 0;
        myHunter.accWeight = 0;

        TreasureTile[] testTiles = new TreasureTile[8];
        testTiles[0] = new TreasureTile(0, 0, '+', false);
        testTiles[0].getDataPair().setScore(12);
        testTiles[0].getDataPair().setWeight(6);

        testTiles[1] = new TreasureTile(1, 1, '+', false);
        testTiles[1].getDataPair().setScore(21);
        testTiles[1].getDataPair().setWeight(5);

        testTiles[2] = new TreasureTile(2, 2, '+', false);
        testTiles[2].getDataPair().setScore(15);
        testTiles[2].getDataPair().setWeight(8);

        testTiles[3] = new TreasureTile(3, 3, '+', false);
        testTiles[3].getDataPair().setScore(25);
        testTiles[3].getDataPair().setWeight(7);

        testTiles[4] = new TreasureTile(4, 4, '+', false);
        testTiles[4].getDataPair().setScore(16);
        testTiles[4].getDataPair().setWeight(9);

        testTiles[5] = new TreasureTile(5, 5, '+', false);
        testTiles[5].getDataPair().setScore(12);
        testTiles[5].getDataPair().setWeight(5);

        testTiles[6] = new TreasureTile(6, 6, '+', false);
        testTiles[6].getDataPair().setScore(17);
        testTiles[6].getDataPair().setWeight(7);

        testTiles[7] = new TreasureTile(7, 7, '+', false);
        testTiles[7].getDataPair().setScore(15);
        testTiles[7].getDataPair().setWeight(8);

        int expectedAccScore = 77;
        int expectedAccWeight = 25;

        LinkedList<TreasureTile> treasureList = new LinkedList<>();
        for(int i = 0; i < 8; i++)
            treasureList.add(testTiles[i]);

        // Action
        LinkedList<TreasureTile> targetList = myHunter.planFractionalKnapsack(treasureList);
        int accScore = myHunter.getScore();        
        int accWeight = myHunter.getWeight();        

        System.out.println("\tTest Accumulated Score");
        passed &= assertEquals(expectedAccScore, accScore);

        System.out.println("\tTest Accumulated Weight");
        passed &= assertEquals(expectedAccWeight, accWeight);

        System.out.println("\tTest Treasures in Knapsack");
        boolean expectedExistence, actualExistence;

        System.out.println("\t\tTest Tiles #0");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[0]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #1");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[1]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #2");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[2]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #3");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[3]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #4");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[4]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #5");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[5]);
        passed &= assertEquals(expectedExistence, actualExistence);        

        System.out.println("\t\tTest Tiles #6");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[6]);
        passed &= assertEquals(expectedExistence, actualExistence);   

        System.out.println("\t\tTest Tiles #7");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[7]);
        passed &= assertEquals(expectedExistence, actualExistence);   

        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }
    
    private static void testFractionalKnapsackCase3()
    {
        // Setup
        System.out.println("============testFractionalKnapsackCase3=============");
        boolean passed = true;
        totalTestCount++;

        TreasureMap myMap = new TreasureMap(true);
        TreasureHunter myHunter = new TreasureHunter(1, 1, myMap);
        // MUST RESET accScore and accWeight because TreasureHunter constructor
        // triggers automatic knapsack algorithm for the newly generated map!!
        myHunter.accScore = 0;
        myHunter.accWeight = 0;

        TreasureTile[] testTiles = new TreasureTile[10];
        testTiles[0] = new TreasureTile(0, 0, '+', false);
        testTiles[0].getDataPair().setScore(10);
        testTiles[0].getDataPair().setWeight(2);

        testTiles[1] = new TreasureTile(1, 1, '+', false);
        testTiles[1].getDataPair().setScore(12);
        testTiles[1].getDataPair().setWeight(4);

        testTiles[2] = new TreasureTile(2, 2, '+', false);
        testTiles[2].getDataPair().setScore(21);
        testTiles[2].getDataPair().setWeight(3);

        testTiles[3] = new TreasureTile(3, 3, '+', false);
        testTiles[3].getDataPair().setScore(13);
        testTiles[3].getDataPair().setWeight(3);

        testTiles[4] = new TreasureTile(4, 4, '+', false);
        testTiles[4].getDataPair().setScore(18);
        testTiles[4].getDataPair().setWeight(1);

        testTiles[5] = new TreasureTile(5, 5, '+', false);
        testTiles[5].getDataPair().setScore(9);
        testTiles[5].getDataPair().setWeight(2);

        testTiles[6] = new TreasureTile(6, 6, '+', false);
        testTiles[6].getDataPair().setScore(22);
        testTiles[6].getDataPair().setWeight(5);

        testTiles[7] = new TreasureTile(7, 7, '+', false);
        testTiles[7].getDataPair().setScore(15);
        testTiles[7].getDataPair().setWeight(6);

        testTiles[8] = new TreasureTile(8, 8, '+', false);
        testTiles[8].getDataPair().setScore(16);
        testTiles[8].getDataPair().setWeight(4);

        testTiles[9] = new TreasureTile(9, 9, '+', false);
        testTiles[9].getDataPair().setScore(20);
        testTiles[9].getDataPair().setWeight(3);

        int expectedAccScore = 135;
        int expectedAccWeight = 25;

        LinkedList<TreasureTile> treasureList = new LinkedList<>();
        for(int i = 0; i < 10; i++)
            treasureList.add(testTiles[i]);

        // Action
        LinkedList<TreasureTile> targetList = myHunter.planFractionalKnapsack(treasureList);
        int accScore = myHunter.getScore();        
        int accWeight = myHunter.getWeight();        
        
        System.out.println("\tTest Accumulated Score");
        passed &= assertEquals(expectedAccScore, accScore);

        System.out.println("\tTest Accumulated Weight");
        passed &= assertEquals(expectedAccWeight, accWeight);

        System.out.println("\tTest Treasures in Knapsack");
        boolean expectedExistence, actualExistence;

        System.out.println("\t\tTest Tiles #0");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[0]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #1");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[1]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #2");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[2]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #3");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[3]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #4");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[4]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #5");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[5]);
        passed &= assertEquals(expectedExistence, actualExistence);        

        System.out.println("\t\tTest Tiles #6");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[6]);
        passed &= assertEquals(expectedExistence, actualExistence);   

        System.out.println("\t\tTest Tiles #7");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[7]);
        passed &= assertEquals(expectedExistence, actualExistence);   

        System.out.println("\t\tTest Tiles #8");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[8]);
        passed &= assertEquals(expectedExistence, actualExistence); 

        System.out.println("\t\tTest Tiles #9");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[9]);
        passed &= assertEquals(expectedExistence, actualExistence); 

        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }
    
    private static void testFractionalKnapsackCase4()
    {
        // Setup
        System.out.println("============testFractionalKnapsackCase4=============");
        boolean passed = true;
        totalTestCount++;

        TreasureMap myMap = new TreasureMap(true);
        TreasureHunter myHunter = new TreasureHunter(1, 1, myMap);
        // MUST RESET accScore and accWeight because TreasureHunter constructor
        // triggers automatic knapsack algorithm for the newly generated map!!
        myHunter.accScore = 0;
        myHunter.accWeight = 0;

        TreasureTile[] testTiles = new TreasureTile[15];
        testTiles[0] = new TreasureTile(0, 0, '+', false);
        testTiles[0].getDataPair().setScore(12);
        testTiles[0].getDataPair().setWeight(3);

        testTiles[1] = new TreasureTile(1, 1, '+', false);
        testTiles[1].getDataPair().setScore(7);
        testTiles[1].getDataPair().setWeight(2);

        testTiles[2] = new TreasureTile(2, 2, '+', false);
        testTiles[2].getDataPair().setScore(18);
        testTiles[2].getDataPair().setWeight(4);

        testTiles[3] = new TreasureTile(3, 3, '+', false);
        testTiles[3].getDataPair().setScore(9);
        testTiles[3].getDataPair().setWeight(1);

        testTiles[4] = new TreasureTile(4, 4, '+', false);
        testTiles[4].getDataPair().setScore(15);
        testTiles[4].getDataPair().setWeight(2);

        testTiles[5] = new TreasureTile(5, 5, '+', false);
        testTiles[5].getDataPair().setScore(11);
        testTiles[5].getDataPair().setWeight(3);

        testTiles[6] = new TreasureTile(6, 6, '+', false);
        testTiles[6].getDataPair().setScore(18);
        testTiles[6].getDataPair().setWeight(4);

        testTiles[7] = new TreasureTile(7, 7, '+', false);
        testTiles[7].getDataPair().setScore(22);
        testTiles[7].getDataPair().setWeight(4);

        testTiles[8] = new TreasureTile(8, 8, '+', false);
        testTiles[8].getDataPair().setScore(14);
        testTiles[8].getDataPair().setWeight(3);

        testTiles[9] = new TreasureTile(9, 9, '+', false);
        testTiles[9].getDataPair().setScore(8);
        testTiles[9].getDataPair().setWeight(2);

        testTiles[10] = new TreasureTile(10, 10, '+', false);
        testTiles[10].getDataPair().setScore(14);
        testTiles[10].getDataPair().setWeight(2);

        testTiles[11] = new TreasureTile(11, 11, '+', false);
        testTiles[11].getDataPair().setScore(7);
        testTiles[11].getDataPair().setWeight(1);

        testTiles[12] = new TreasureTile(12, 12, '+', false);
        testTiles[12].getDataPair().setScore(18);
        testTiles[12].getDataPair().setWeight(4);

        testTiles[13] = new TreasureTile(13, 13, '+', false);
        testTiles[13].getDataPair().setScore(20);
        testTiles[13].getDataPair().setWeight(4);

        testTiles[14] = new TreasureTile(14, 14, '+', false);
        testTiles[14].getDataPair().setScore(17);
        testTiles[14].getDataPair().setWeight(3);

        int expectedAccScore = 140;
        int expectedAccWeight = 25;

        LinkedList<TreasureTile> treasureList = new LinkedList<>();
        for(int i = 0; i < 15; i++)
            treasureList.add(testTiles[i]);

        // Action
        LinkedList<TreasureTile> targetList = myHunter.planFractionalKnapsack(treasureList);
        int accScore = myHunter.getScore();        
        int accWeight = myHunter.getWeight();  

        System.out.println("\tTest Accumulated Score");
        passed &= assertEquals(expectedAccScore, accScore);

        System.out.println("\tTest Accumulated Weight");
        passed &= assertEquals(expectedAccWeight, accWeight);

        System.out.println("\tTest Treasures in Knapsack");
        boolean expectedExistence, actualExistence;

        System.out.println("\t\tTest Tiles #0");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[0]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #1");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[1]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #2");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[2]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #3");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[3]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #4");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[4]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #5");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[5]);
        passed &= assertEquals(expectedExistence, actualExistence);        

        System.out.println("\t\tTest Tiles #6");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[6]);
        passed &= assertEquals(expectedExistence, actualExistence);   

        System.out.println("\t\tTest Tiles #7");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[7]);
        passed &= assertEquals(expectedExistence, actualExistence);   

        System.out.println("\t\tTest Tiles #8");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[8]);
        passed &= assertEquals(expectedExistence, actualExistence); 

        System.out.println("\t\tTest Tiles #9");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[9]);
        passed &= assertEquals(expectedExistence, actualExistence); 

        System.out.println("\t\tTest Tiles #10");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[10]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #11");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[11]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #12");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[12]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #13");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[13]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #14");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[14]);
        passed &= assertEquals(expectedExistence, actualExistence);

        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }
    
    private static void testFractionalKnapsackCaseCustom()
    {
        // Setup
        System.out.println("============testFractionalKnapsackCaseCustom=============");
        boolean passed = true;
        totalTestCount++;

        TreasureMap myMap = new TreasureMap(true);
        TreasureHunter myHunter = new TreasureHunter(1, 1, myMap);
        // MUST RESET accScore and accWeight because TreasureHunter constructor
        // triggers automatic knapsack algorithm for the newly generated map!!
        myHunter.accScore = 0;
        myHunter.accWeight = 0;

        TreasureTile[] testTiles = new TreasureTile[15];
        testTiles[0] = new TreasureTile(0, 0, '+', false);
        testTiles[0].getDataPair().setScore(12);
        testTiles[0].getDataPair().setWeight(3);

        testTiles[1] = new TreasureTile(1, 1, '+', false);
        testTiles[1].getDataPair().setScore(8);
        testTiles[1].getDataPair().setWeight(3);

        testTiles[2] = new TreasureTile(2, 2, '+', false);
        testTiles[2].getDataPair().setScore(17);
        testTiles[2].getDataPair().setWeight(5);

        testTiles[3] = new TreasureTile(3, 3, '+', false);
        testTiles[3].getDataPair().setScore(9);
        testTiles[3].getDataPair().setWeight(1);

        testTiles[4] = new TreasureTile(4, 4, '+', false);
        testTiles[4].getDataPair().setScore(13);
        testTiles[4].getDataPair().setWeight(2);

        testTiles[5] = new TreasureTile(5, 5, '+', false);
        testTiles[5].getDataPair().setScore(15);
        testTiles[5].getDataPair().setWeight(3);

        testTiles[6] = new TreasureTile(6, 6, '+', false);
        testTiles[6].getDataPair().setScore(18);
        testTiles[6].getDataPair().setWeight(4);

        testTiles[7] = new TreasureTile(7, 7, '+', false);
        testTiles[7].getDataPair().setScore(25);
        testTiles[7].getDataPair().setWeight(4);

        testTiles[8] = new TreasureTile(8, 8, '+', false);
        testTiles[8].getDataPair().setScore(14);
        testTiles[8].getDataPair().setWeight(3);

        testTiles[9] = new TreasureTile(9, 9, '+', false);
        testTiles[9].getDataPair().setScore(8);
        testTiles[9].getDataPair().setWeight(2);

        testTiles[10] = new TreasureTile(10, 10, '+', false);
        testTiles[10].getDataPair().setScore(17);
        testTiles[10].getDataPair().setWeight(2);

        testTiles[11] = new TreasureTile(11, 11, '+', false);
        testTiles[11].getDataPair().setScore(27);
        testTiles[11].getDataPair().setWeight(1);

        testTiles[12] = new TreasureTile(12, 12, '+', false);
        testTiles[12].getDataPair().setScore(18);
        testTiles[12].getDataPair().setWeight(4);

        testTiles[13] = new TreasureTile(13, 13, '+', false);
        testTiles[13].getDataPair().setScore(20);
        testTiles[13].getDataPair().setWeight(4);

        testTiles[14] = new TreasureTile(14, 14, '+', false);
        testTiles[14].getDataPair().setScore(17);
        testTiles[14].getDataPair().setWeight(3);

        int expectedAccScore = 166;
        int expectedAccWeight = 25;

        LinkedList<TreasureTile> treasureList = new LinkedList<>();
        for(int i = 0; i < 15; i++)
            treasureList.add(testTiles[i]);

        // Action
        LinkedList<TreasureTile> targetList = myHunter.planFractionalKnapsack(treasureList);
        int accScore = myHunter.getScore();        
        int accWeight = myHunter.getWeight();  

        System.out.println("\tTest Accumulated Score");
        passed &= assertEquals(expectedAccScore, accScore);

        System.out.println("\tTest Accumulated Weight");
        passed &= assertEquals(expectedAccWeight, accWeight);

        System.out.println("\tTest Treasures in Knapsack");
        boolean expectedExistence, actualExistence;

        System.out.println("\t\tTest Tiles #0");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[0]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #1");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[1]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #2");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[2]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #3");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[3]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #4");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[4]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #5");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[5]);
        passed &= assertEquals(expectedExistence, actualExistence);        

        System.out.println("\t\tTest Tiles #6");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[6]);
        passed &= assertEquals(expectedExistence, actualExistence);   

        System.out.println("\t\tTest Tiles #7");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[7]);
        passed &= assertEquals(expectedExistence, actualExistence);   

        System.out.println("\t\tTest Tiles #8");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[8]);
        passed &= assertEquals(expectedExistence, actualExistence); 

        System.out.println("\t\tTest Tiles #9");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[9]);
        passed &= assertEquals(expectedExistence, actualExistence); 

        System.out.println("\t\tTest Tiles #10");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[10]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #11");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[11]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #12");
        expectedExistence = false;
        actualExistence = targetList.contains(testTiles[12]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #13");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[13]);
        passed &= assertEquals(expectedExistence, actualExistence);

        System.out.println("\t\tTest Tiles #14");
        expectedExistence = true;
        actualExistence = targetList.contains(testTiles[14]);
        passed &= assertEquals(expectedExistence, actualExistence);

        // Tear Down
        totalPassed &= passed;
        if(passed) 
        {
            System.out.println("\tPassed");
            totalPassCount++;            
        }
    }
    

    ////// ASSERTIONS //////
    ////// ASSERTIONS //////
    ////// ASSERTIONS //////

    private static boolean assertEquals(TreasureTile expected, TreasureTile actual)
    {
        if(!expected.isEqual(actual))
        {
            System.out.println("\tAssert Failed!");
            System.out.printf("\tExpected:");
            expected.printTile();
            expected.printTileCoord();
            System.out.printf("\tActual:");
            actual.printTile();
            actual.printTileCoord();
            return false;
        }

        return true;
    }

    private static boolean assertEquals(int expected, int actual)
    {
        if(expected != actual)
        {
            System.out.println("\tAssert Failed!");
            System.out.printf("\tExpected: %d, Actual: %d\n\n", expected, actual);
            return false;
        }

        return true;
    }

    private static boolean assertEquals(boolean expected, boolean actual)
    {
        if(expected != actual)
        {
            System.out.println("\tAssert Failed!");
            System.out.printf("\tExpected: %b, Actual: %b\n\n", expected, actual);
            return false;
        }

        return true;
    }
    
}
