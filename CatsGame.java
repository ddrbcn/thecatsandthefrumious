package es.ddrbcn.thecatsandthefrumious;

/**
 * A class representing a Cats and Mouse game. Contains two boolean determining
 * the class of players, the current state, the depth used in the alpha-beta pruning algorithm 
 * and a boolean determining if the match has a winner.
 * 
 * @author ddrbcn
 * 
 */
public class CatsGame {

	private static final int INFINITY = 10000;
	boolean playerMouse;
	boolean playerCats;
	State currentState;
	private int plyDepth = 5;
	boolean gameHasWinner = false;

	public CatsGame() {
		currentState = new State();
		playerMouse = true;
		playerCats = false;

	}
	public void setplyDepth(int depth) {
		plyDepth=depth;
	}
	public State getState() {
		return currentState;
	}

	public void setState(State state) {
		currentState = state;
	}

	public boolean getPlayerCats() {
		return playerCats;
	}

	public boolean getPlayerMouse() {
		return playerMouse;
	}

	public void setGameHasWinner(boolean haswinner) {
		gameHasWinner = haswinner;
	}

	public boolean getGameHasWinner() {
		return gameHasWinner;
	}
	

	public State move(State state, boolean isMouseMove) {
		int alpha = -INFINITY;
		int beta = INFINITY;
		int bestScore = -Integer.MAX_VALUE;
		State gameTreeRoot = new State(state);
		State bestMove = null;
		for (State child : gameTreeRoot.getChilds()) {
			if (bestMove == null) {
				bestMove = child;
			}
			alpha = Math.max(alpha, miniMax(child, plyDepth - 1, alpha, beta, isMouseMove));
			if (alpha > bestScore) {
				bestMove = child;
				bestScore = alpha;
			}
			/* Adding some randomness */
			if (alpha == bestScore) {
				if (Math.random() > 0.99) {
					bestMove = child;

				}
			}
			System.out.println(alpha);
		}
		System.out.println(bestScore);
		return bestMove;
	}
    
	/* Alpha-Beta pruning algorithm */
	private int miniMax(State currentNode, int depth, int alpha, int beta, boolean isMouseMove) {
		if(isMouseMove){
		if (depth <= 0 || terminalNode(currentNode)) {
			
				return getHeuristicMouse(currentNode);
			
		}
		
		if (currentNode.getIsMouseturn()) {
			for (State child : currentNode.getChilds()) {
				alpha = Math.max(alpha, miniMax(child, depth - 1, alpha, beta, isMouseMove));

				if (alpha >= beta) {
					return beta;
				}
			}
			return alpha;
		} else {
			for (State child : currentNode.getChilds()) {
				beta = Math.min(beta, miniMax(child, depth - 1, alpha, beta, isMouseMove));

				if (alpha >= beta) {
					return alpha;
				}
			}
			return beta;
		}
		}else{
			if (depth <= 0 || terminalNode(currentNode)) {
				
				return getHeuristicCats(currentNode);
			
		}
		
		if (!currentNode.getIsMouseturn()) {
			for (State child : currentNode.getChilds()) {
				alpha = Math.max(alpha, miniMax(child, depth - 1, alpha, beta, isMouseMove));

				if (alpha >= beta) {
					return beta;
				}
			}
			return alpha;
		} else {
			for (State child : currentNode.getChilds()) {
				beta = Math.min(beta, miniMax(child, depth - 1, alpha, beta, isMouseMove));

				if (alpha >= beta) {
					return alpha;
				}
			}
			return beta;
		}
			
		}
	}
    /* Cats player heuristic */
	private int getHeuristicCats(State currentNode) {
		int count = 0;
		
		if (currentNode.getIsMouseturn()) {
			if ((currentNode.getChilds().size() == 0))
				return 1200;

		}
		
	
			if ((currentNode.getMouseLocation().getY() == 0))				
				return -1200;
			
			
			count -= 10*(Math.min(currentNode.getCatsLocations().get(0).getY(),
				     Math.min(currentNode.getCatsLocations().get(1).getY(),
				     Math.min(currentNode.getCatsLocations().get(2).getY(),currentNode.getCatsLocations().get(3).getY())))
				     -
				     currentNode.getMouseLocation().getY())
				     
				     +50*Math.abs(Math.max(currentNode.getCatsLocations().get(0).getY(),
							   Math.max(currentNode.getCatsLocations().get(1).getY(),
									   Math.max(currentNode.getCatsLocations().get(2).getY(),currentNode.getCatsLocations().get(3).getY())))
									  -Math.min(currentNode.getCatsLocations().get(0).getY(),
											    Math.min(currentNode.getCatsLocations().get(1).getY(),
											    Math.min(currentNode.getCatsLocations().get(2).getY(),currentNode.getCatsLocations().get(3).getY()))))
					
					+50*(Math.max(currentNode.getCatsLocations().get(0).getY(),
						 Math.max(currentNode.getCatsLocations().get(1).getY(),
						 Math.max(currentNode.getCatsLocations().get(2).getY(),currentNode.getCatsLocations().get(3).getY())))
						 -
						currentNode.getMouseLocation().getY())
					 + 50*Math.abs(currentNode.getCatsLocations().get(1).getX()-currentNode.getCatsLocations().get(0).getX())
    		         
  			   + 50*Math.abs(currentNode.getCatsLocations().get(2).getX()-currentNode.getCatsLocations().get(1).getX())
		           
  			   + 50*Math.abs(currentNode.getCatsLocations().get(3).getX()-currentNode.getCatsLocations().get(2).getX()) 
				     ;
			if (currentNode.getCatsLocations().get(0).getX() > 1)
				count-= 85;
		    if	(currentNode.getCatsLocations().get(1).getX() > 3)
			   	count-= 85;
		    if	(currentNode.getCatsLocations().get(2).getX() > 5)
				count-= 85;
		    if	(currentNode.getCatsLocations().get(3).getX() > 7)
				count-= 85;
		   
		    if (currentNode.getCatsLocations().get(0).getX() < 0)
				count-= 85;
		    if	(currentNode.getCatsLocations().get(1).getX() < 2)
			   	count-= 85;
		    if	(currentNode.getCatsLocations().get(2).getX() < 4)
				count-= 85;
		    if	(currentNode.getCatsLocations().get(3).getX() < 6)
				count-= 85;
			

			
			

		/*int meanY = (currentNode.getCatsLocations().get(0).getY()
				      +currentNode.getCatsLocations().get(1).getY()
				      +currentNode.getCatsLocations().get(2).getY()
				      +currentNode.getCatsLocations().get(3).getY())/4;
		
		   int stdY = Math.abs(meanY-currentNode.getCatsLocations().get(0).getY())
				  + Math.abs(meanY-currentNode.getCatsLocations().get(1).getY())
				  +Math.abs(meanY-currentNode.getCatsLocations().get(2).getY())
				  +Math.abs(meanY-currentNode.getCatsLocations().get(3).getY());
		
		    count -= stdY*100;
		    
			if (currentNode.getMouseLocation().getY() <= currentNode.getCatsLocations().get(0).getY())
				count-= 50;
			if (currentNode.getMouseLocation().getY() <= currentNode.getCatsLocations().get(1).getY())
				count-= 50;
		    if	(currentNode.getMouseLocation().getY() <= currentNode.getCatsLocations().get(2).getY())
			   	count-= 50;
		    if	(currentNode.getMouseLocation().getY() <= currentNode.getCatsLocations().get(3).getY())
				count-= 50;
		    if (currentNode.getMouseLocation().getY() < currentNode.getCatsLocations().get(0).getY())
				count-= 50;
			if (currentNode.getMouseLocation().getY() < currentNode.getCatsLocations().get(1).getY())
				count-= 50;
		    if	(currentNode.getMouseLocation().getY() < currentNode.getCatsLocations().get(2).getY())
			   	count-= 50;
		    if	(currentNode.getMouseLocation().getY() < currentNode.getCatsLocations().get(3).getY())
				count-= 50;
		    
		   
			if (currentNode.getCatsLocations().get(0).getX() > 1)
				count-= 25;
		    if	(currentNode.getCatsLocations().get(1).getX() > 3)
			   	count-= 25;
		    if	(currentNode.getCatsLocations().get(2).getX() > 5)
				count-= 25;
		    if	(currentNode.getCatsLocations().get(3).getX() > 7)
				count-= 25;
		   
		    if (currentNode.getCatsLocations().get(0).getX() < 0)
				count-= 25;
		    if	(currentNode.getCatsLocations().get(1).getX() < 2)
			   	count-= 25;
		    if	(currentNode.getCatsLocations().get(2).getX() < 4)
				count-= 25;
		    if	(currentNode.getCatsLocations().get(3).getX() < 6)
				count-= 25;
			
		     
			 count -= 300*Math.abs(Math.max(currentNode.getCatsLocations().get(0).getY(),
						   Math.max(currentNode.getCatsLocations().get(1).getY(),
								   Math.max(currentNode.getCatsLocations().get(2).getY(),currentNode.getCatsLocations().get(3).getY())))
								  -Math.min(currentNode.getCatsLocations().get(0).getY(),
										    Math.min(currentNode.getCatsLocations().get(1).getY(),
										    Math.min(currentNode.getCatsLocations().get(2).getY(),currentNode.getCatsLocations().get(3).getY()))));
				  
			 
            if(currentNode.getCatsLocations().get(0).getY()==currentNode.getCatsLocations().get(1).getY()){
            	count+=200;
            }
            if(currentNode.getCatsLocations().get(0).getY()==currentNode.getCatsLocations().get(2).getY()){
            	count+=200;
            }
            if(currentNode.getCatsLocations().get(0).getY()==currentNode.getCatsLocations().get(3).getY()){
            	count+=200;
            }
            if(currentNode.getCatsLocations().get(1).getY()==currentNode.getCatsLocations().get(2).getY()){
            	count+=200;
            }
            if(currentNode.getCatsLocations().get(1).getY()==currentNode.getCatsLocations().get(3).getY()){
            	count+=200;
            }
            if(currentNode.getCatsLocations().get(2).getY()==currentNode.getCatsLocations().get(3).getY()){
            	count+=200;
            }

            count-= 100*currentNode.getMouseLocation().getY()
			   +(int) Math.round(Math.abs(currentNode.getMouseLocation().getX()-3.5))*100
			   +(int)(Math.abs(currentNode.getMouseLocation().getX()-currentNode.getCatsLocations().get(0).getX())
			  -(Math.abs(currentNode.getMouseLocation().getX()-currentNode.getCatsLocations().get(1).getX())
			   +Math.abs(currentNode.getMouseLocation().getX()-currentNode.getCatsLocations().get(2).getX())
			   +Math.abs(currentNode.getMouseLocation().getX()-currentNode.getCatsLocations().get(3).getX())
			   +Math.abs(currentNode.getMouseLocation().getY()-currentNode.getCatsLocations().get(0).getY())
			   +Math.abs(currentNode.getMouseLocation().getY()-currentNode.getCatsLocations().get(1).getY())
			   +Math.abs(currentNode.getMouseLocation().getY()-currentNode.getCatsLocations().get(2).getY())
			   +Math.abs(currentNode.getMouseLocation().getY()-currentNode.getCatsLocations().get(3).getY())))
			   /32;*/
	
	/*			count = count 			
			   
		   - 100*currentNode.getMouseLocation().getY()
			   -(int) Math.round(Math.abs(currentNode.getMouseLocation().getX()-3.5))*100
			   +(int)(Math.abs(currentNode.getMouseLocation().getX()-currentNode.getCatsLocations().get(0).getX())
			   +Math.abs(currentNode.getMouseLocation().getX()-currentNode.getCatsLocations().get(1).getX())
			   +Math.abs(currentNode.getMouseLocation().getX()-currentNode.getCatsLocations().get(2).getX())
			   +Math.abs(currentNode.getMouseLocation().getX()-currentNode.getCatsLocations().get(3).getX())
			   +Math.abs(currentNode.getMouseLocation().getY()-currentNode.getCatsLocations().get(0).getY())
			   +Math.abs(currentNode.getMouseLocation().getY()-currentNode.getCatsLocations().get(1).getY())
			   +Math.abs(currentNode.getMouseLocation().getY()-currentNode.getCatsLocations().get(2).getY())
			   +Math.abs(currentNode.getMouseLocation().getY()-currentNode.getCatsLocations().get(3).getY()))
			   /32
			   -300*Math.abs(Math.max(currentNode.getCatsLocations().get(0).getY(),
					   Math.max(currentNode.getCatsLocations().get(1).getY(),
							   Math.max(currentNode.getCatsLocations().get(2).getY(),currentNode.getCatsLocations().get(3).getY())))
			  
			   
                    
                    -Math.abs(Math.min(currentNode.getCatsLocations().get(0).getY(),
     					   Math.min(currentNode.getCatsLocations().get(1).getY(),
    							   Math.min(currentNode.getCatsLocations().get(2).getY(),currentNode.getCatsLocations().get(3).getY()))))
    			    )
    		   - 200*Math.abs(currentNode.getCatsLocations().get(1).getY()-currentNode.getCatsLocations().get(0).getY())*
    		         Math.max(currentNode.getCatsLocations().get(1).getY(), currentNode.getCatsLocations().get(0).getY())
    		         
  			   - 200*Math.abs(currentNode.getCatsLocations().get(2).getY()-currentNode.getCatsLocations().get(1).getY())*
		             Math.max(currentNode.getCatsLocations().get(2).getY(), currentNode.getCatsLocations().get(1).getY())
  			   
  			    -200*Math.abs(currentNode.getCatsLocations().get(3).getY()-currentNode.getCatsLocations().get(2).getY())*
		             Math.max(currentNode.getCatsLocations().get(3).getY(), currentNode.getCatsLocations().get(2).getY())
  		  
    			- 100*Math.abs(currentNode.getCatsLocations().get(1).getX()-currentNode.getCatsLocations().get(0).getX())
    		         
  			   - 100*Math.abs(currentNode.getCatsLocations().get(2).getX()-currentNode.getCatsLocations().get(1).getX())
		           
  			    -100*Math.abs(currentNode.getCatsLocations().get(3).getX()-currentNode.getCatsLocations().get(2).getX())
		            
    			
    			
    		   + 1200*(currentNode.getMouseLocation().getY()-currentNode.getCatsLocations().get(0).getY())
  			   + 1200*(currentNode.getMouseLocation().getY()-currentNode.getCatsLocations().get(1).getY())
  			   + 1200*(currentNode.getMouseLocation().getY()-currentNode.getCatsLocations().get(2).getY())
  			   + 1200*(currentNode.getMouseLocation().getY()-currentNode.getCatsLocations().get(3).getY())
    		    
			   ;*/		
		
		return count;
	}
	
	/* Mouse player heuristic */
	private int getHeuristicMouse(State currentNode) {
		int count = 0;
		if (currentNode.getIsMouseturn()) {
			if ((currentNode.getChilds().size() == 0))
				return -1200;

		}
		
		count = count 
			   +1200
			   - 100*currentNode.getMouseLocation().getY()+
			   -(int) Math.round(Math.abs(currentNode.getMouseLocation().getX()-3.5))*100
			   +(int)(Math.abs(currentNode.getMouseLocation().getX()-currentNode.getCatsLocations().get(0).getX())
			   +Math.abs(currentNode.getMouseLocation().getX()-currentNode.getCatsLocations().get(1).getX())
			   +Math.abs(currentNode.getMouseLocation().getX()-currentNode.getCatsLocations().get(2).getX())
			   +Math.abs(currentNode.getMouseLocation().getX()-currentNode.getCatsLocations().get(3).getX())
			   +Math.abs(currentNode.getMouseLocation().getY()-currentNode.getCatsLocations().get(0).getY())
			   +Math.abs(currentNode.getMouseLocation().getY()-currentNode.getCatsLocations().get(1).getY())
			   +Math.abs(currentNode.getMouseLocation().getY()-currentNode.getCatsLocations().get(2).getY())
			   +Math.abs(currentNode.getMouseLocation().getY()-currentNode.getCatsLocations().get(3).getY()))
			   /32;

		

		
		return count;
	}

	/* Checks if the node is terminal */
	private boolean terminalNode(State state) {
		if (state.getMouseLocation().getY() == 0
				|| (state.getChilds().size() == 0)) {

			return true;
		} else {
			return false;
		}
	}

}
