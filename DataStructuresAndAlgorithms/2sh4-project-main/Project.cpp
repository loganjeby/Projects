#include <iostream>
#include "MacUILib.h"
#include "objPos.h"
#include "GameMechs.h"
#include "Player.h"


using namespace std;

#define DELAY_CONST 100000

GameMechs* myGM;
Player* myPlayer;


void Initialize(void);
void GetInput(void);
void RunLogic(void);
void DrawScreen(void);
void LoopDelay(void);
void CleanUp(void);



int main(void)
{

    Initialize();

    while(myGM->getExitFlagStatus() == false)  
    {
        GetInput();
        RunLogic();
        DrawScreen();
        LoopDelay();
    }

    CleanUp();

}


void Initialize(void)
{
    MacUILib_init();
    MacUILib_clearScreen();

    myGM = new GameMechs();
    myPlayer = new Player(myGM);    
    
    objPos tempPos{1, 1, 'o'};
    myGM->generateFood(tempPos);
}

void GetInput(void)
{

}

void RunLogic(void)
{    
    // 3.1 logic for food generation
    objPos tempPos{1, 1, 'o'};
    
    // Player control
    myPlayer->updatePlayerDir();
    myPlayer->movePlayer();

    // To end game
    if(myGM->getInput() == '/')
    {
        myGM->setExitTrue();
    }
    // For Score
    if(myGM->getInput() == '+')
    {
        myGM->incrementScore();
    }
    // For Suicide
    if(myGM->getInput() == 'L')
    {
        myGM->setLoseFlag();
        myGM->setExitTrue();
    }
    // 3.1 logic for food generation
    if(myGM->getInput() == 'F')
    {
        myGM->generateFood(tempPos);
    }
    myGM->clearInput();


}

void DrawScreen(void)
{
    // Variables for code legibility
    int bX = myGM->getBoardSizeX();
    int bY = myGM->getBoardSizeY();

    objPosArrayList* playerBody = myPlayer->getPlayerPos();
    objPos tempBody;

    objPos foodPos;
    myGM->getFoodPos(foodPos);

    // Resetting screen
    MacUILib_clearScreen();

    // Initializing game board
    char board[bY][bX];
    for (int i = 0; i < bY; i++)
    {
        for (int j = 0; j < bX; j++)
        {
            //Initializing board
            board[i][j] = ' ';
        }
    }
    for (int i = 0; i < bY; i++)
    {
        for (int j = 0; j < bX; j++)
        {
            // Iterate through player list
            for(int k = 0; k < playerBody->getSize(); k++)
            {
                playerBody->getElement(tempBody, k);
                // Drawing player
                if (i == tempBody.y && j == tempBody.x)
                {
                    board[i][j] = tempBody.symbol;
                }
            }
            
            // Drawing border
            if (i == 0 || j == 0 || i == bY - 1 || j == bX - 1)
            {
                board[i][j] = '#';
            }

            // Drawing food
            else if (i == foodPos.y && j == foodPos.x)
            {
                board[i][j] = foodPos.symbol;
            }
            // Printing board row
            MacUILib_printf("%c",board[i][j]);
        }
        // Going to next row
        MacUILib_printf("\n");        
    }
    // Printing score
    MacUILib_printf("Score: %d", myGM->getScore());
    
}

void LoopDelay(void)
{
    MacUILib_Delay(DELAY_CONST); // 0.1s delay
}


void CleanUp(void)
{
    MacUILib_clearScreen();   
    if(myGM->getLoseFlagStatus() == true)
    {
        MacUILib_printf("LOSER");
    }
    
    delete myGM;
    delete myPlayer;
    MacUILib_uninit();
}
