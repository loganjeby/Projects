#include "Player.h"


Player::Player(GameMechs* thisGMRef)
{
    mainGameMechsRef = thisGMRef;
    myDir = STOP;

    // more actions to be included
    objPos tempPos;
    tempPos.setObjPos(mainGameMechsRef->getBoardSizeX() / 2, mainGameMechsRef->getBoardSizeY() / 2, '@');

    playerPosList = new objPosArrayList();

    // Creating long snake to demonstrate 3.1 completion
    playerPosList->insertHead(tempPos);
    playerPosList->insertHead(tempPos);
    playerPosList->insertHead(tempPos);
    playerPosList->insertHead(tempPos);
    playerPosList->insertHead(tempPos);
    playerPosList->insertHead(tempPos);
    
}


Player::~Player()
{
    // delete any heap members here
    delete playerPosList;
}

objPosArrayList* Player::getPlayerPos()
{
    return playerPosList;
    // return the reference to the playerPos arrray list
}

void Player::updatePlayerDir()
{
    // PPA3 input processing logic
    char input = mainGameMechsRef->getInput();    
    if(input != 0) 
    {
        switch(input)
        {                      
            case 'w':
                if (myDir != DOWN && myDir != UP)
                {
                    myDir = UP;
                }
                break;
            case 's':
                if (myDir != DOWN && myDir != UP)
                {
                    myDir = DOWN;
                }
                break;
            case 'a':
                if (myDir != RIGHT && myDir != LEFT)
                {
                    myDir = LEFT;
                }
                break;
            case 'd':
                if (myDir != RIGHT && myDir != LEFT)
                {
                    myDir = RIGHT;
                }
                break;
                
            default:
                break;
        }
        input = 0;
    }
}

void Player::movePlayer()
{
    // PPA3 Finite State Machine logic
    objPos currHead; // holding current head pos info
    playerPosList->getHeadElement(currHead);

    switch (myDir)
    {
        case UP:
            currHead.y --;
            if (currHead.y <= 0)
            {
                currHead.y = mainGameMechsRef->getBoardSizeY() - 2;
            }
            break;
        case DOWN:
            currHead.y ++;
            if (currHead.y >= mainGameMechsRef->getBoardSizeY()-1)
            {
                currHead.y = 1;
            }
            break;
        case LEFT:
            currHead.x --;
            if (currHead.x <= 0)
            {
                currHead.x = mainGameMechsRef->getBoardSizeX() - 2;
            }
            break;
        case RIGHT:
            currHead.x ++;
            if (currHead.x >= mainGameMechsRef->getBoardSizeX()-1)
            {
                currHead.x = 1;
            }
            break;

        case STOP:
        default:
            break;
    }

    // New current head inserted to head
    playerPosList->insertHead(currHead);

    // remove tail
    playerPosList->removeTail();



}

