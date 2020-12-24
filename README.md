# Scrabble_Game

How to play/run the program:

1. Run the "scrabbleserver" project folder, then run the "scrabblegame" project folder after.

2. A javafx GUI settings will pop up which will give you two fields to enter the server's IP address and 
   the port number. In this case, it would be "localhost" and "50000"
   
3. Once you launch game, the Menu option will appear in which you have the option to play the game or exit.
   You will also be given the option to let the AI play first in the checkbox at the footer
   
4. If Play Game is clicked, it will lauch the scrabble game in which you will be able to play (if you had checked off the "AI plays first", then it will 
   take ~10 seconds for the GUI to launch as there is a 10 seconds timer on the server side in which the AI finds the best word to play)
   
5. You will have four options/actions: Play; Pass; Clear; Swap
		- Play: You must place a valid word in a valid position on the board then you can press play (if not a valid move or word, you will be NOTIFIED).
				Valid moves consists of horizontal and vertical placement of words of that same row or column and words part of the dictionary.
				You must also place the first word on the star of the board (center).
		- Pass: You must have all letters in your hand to click Pass. This will skip your turn and the AI will play instead
		- Clear: This will clear all the letters you have placed on the board this round (
			IMPORTANT -- Clear is the only way you can remove/move placed letters on the board 
		- Swap: You must have all letters in your hand. This will allow you to swap choose letter(s) with the bag. This will count as a turn
		
6. After each of these moves(besides clear), the program will communicate to the server to receive updates including:
		- AI mvoes
		- Updated bag amount
		- AI points
		- User points
	NOTE: each time the program receives an AI move, it will take approxiamtely 10 seconds to receive back information
	IMPORTANT: AVOID clicking anything when waiting for server response
	IMPORTANT: When waiting for server response, the application may have "is not responding". HOWEVER, you simple have to wait for the server response
	
7. Once the bag is empty, the game will calculate the user and ai points to find who has the greatest points, in which it will dispaly the winner.

8. The user will then be sent back to the Menu page in which he/she will be able to play again or exit