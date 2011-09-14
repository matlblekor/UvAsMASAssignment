import java.io.*;
import java.lang.*;
import java.util.*;

/** This class defines the functionality of the predator. */
public class Predator
  extends Agent
{
  int xp[];		// pray x positions
  int yp[];		// pray y positions
  int xprd[];	// predators x positions
  int yprd[];	// predators y positions
  int nMoveDirection;
  int nRole = -1;

  public Predator() {
  	// init position vectors
  	this.xp = new int[10];
  	this.yp = new int[10];
  	this.xprd = new int[10];
  	this.yprd = new int[10];
  }
 
  /** This method initialize the predator by sending the initialization message
      to the server. */
  public void initialize()
    throws IOException
  {
    g_socket.send( "(init predator)" );
  }
  
  /** This message determines a new movement command. Currently it only moves
      random. This can be improved.. */
  public String determineMovementCommand()
  {
    switch( this.nMoveDirection )
    {
      case 0:  return( "(move north)" );
      case 1:  return( "(move south)" );
      case 2:  return( "(move east)"  );
      case 3:  return( "(move west)"  );
      default: return( "(move none)"  );
    }
  }
  
  
  /** This method processes the visual information. It receives a message
      containing the information of the preys and the predators that are
      currently  in the visible range of the predator. */
  public void processVisualInformation( String strMessage ) 
  {
    int i = 0, j = 0, x = 0, y = 0, tmpx = 0, tmpy = 0, targetx = 0, targety = 0, nPray = 0, nPredator = 1, nPrayToChase, nMinDinstanceToPray = 0;
    int dist[] = new int[10];
    int blocked[] = new int[10];
    String strName = "";
    StringTokenizer tok = new StringTokenizer( strMessage.substring(5), ") (");

    xprd[0] = 0;	// this is me
    yprd[0] = 0;	// this is me

    while( tok.hasMoreTokens( ) )
    {
      if( i == 0 ) strName = tok.nextToken();                // 1st = name
      if( i == 1 ) x = Integer.parseInt( tok.nextToken() );  // 2nd = x coord
      if( i == 2 ) y = Integer.parseInt( tok.nextToken() );  // 3rd = y coord
      if( i == 2 )
      {	

		// gather information about the 2 prays relative positions
      	if (strName.equals("prey")) {
      		xp[nPray] = x;
      		yp[nPray] = y;
      		nPray++;
      	}

		// gather information about the other 3 predators relative positions
      	if (strName.equals("predator")) {
      		xprd[nPredator] = x;
      		yprd[nPredator] = y;
      		nPredator++;
      	}

        System.out.println( strName + " seen at (" + x + ", " + y + ")" );

      // TODO: do something nice with this information!
      }
      i = (i+1)%3;
    }
    
    nPrayToChase = 0;
	nMinDinstanceToPray = 9999;
    if (nPray > 1) {
		for (i = 0; i < nPray; ++i) {
			dist[i] = 0;
			for (j = 0; j < nPredator; ++j) {
				dist[i] += Math.abs(xp[i] - xprd[j]) + Math.abs(yp[i] - yprd[j]);
			}
			
			if (dist[i] < nMinDinstanceToPray) {
				nMinDinstanceToPray = dist[i];
				nPrayToChase = i;		
			}
		}
	}
	else {
		nPrayToChase = 0;
	}
	
	// assume a role

	// first, for consistency, we have to have an order - order by x, y
	for (i = 0; i < nPredator - 1; ++i) {
		for (j = i + 1; j < nPredator; ++j) {
			if (xprd[i] > xprd[j] || (xprd[i] == xprd[j] && yprd[i] > yprd[j])) {
				tmpx = xprd[i];
				tmpy = yprd[i];
				xprd[i] = xprd[j];
				yprd[i] = yprd[j];
				xprd[j] = tmpx;
				yprd[j] = tmpy;
			}
		}
	}
	
	if (xprd[0] == 0 && yprd[0] == 0) {
		// I am the leftest, so I assume the "left" role
		nRole = 1;
	}
	else {
		if (xprd[3] == 0 && yprd[3] == 0) {
			// I am the rightest, so I assume the "right" role
			nRole = 3;
		}
		else {
			if (xprd[1] == 0 && yprd[1] == 0) {
				// I am the first in the middle
				if (yprd[1] > yprd[2]) {
					// I am the highest one, so I assume the "top" role
					nRole = 2;
				}
				else {
					if (yprd[1] < yprd[2]) {
						// I am the lowest one, so I assume the "bottom" role
						nRole = 4;
					}
					else {
						// y are equal, so, by convention, I assume the top role
						nRole = 2;
					}
				}
			}
			else {
				// I am the second in the middle
				if (yprd[2] > yprd[1]) {
					// I am the highest one, so I assume the "top" role
					nRole = 2;
				}
				else {
					// I am the lowest one, so I assume the "bottom" role OR y are equal, so, by convention, I assume the "bottom" role
					nRole = 4;
				}
			}
		}
	}
	
	// based on the role, determine current target for this predator
	switch (nRole) {
		case 1:
			targetx = xp[nPrayToChase] - 1;
			targety = yp[nPrayToChase];
		break;
		
		case 2:
			targetx = xp[nPrayToChase];
			targety = yp[nPrayToChase] + 1;
		break;

		case 3:
			targetx = xp[nPrayToChase] + 1;
			targety = yp[nPrayToChase];
		break;

		case 4:
			targetx = xp[nPrayToChase];
			targety = yp[nPrayToChase] - 1;
		break;
	}
	
	blocked[0] = 0;
	blocked[1] = 0;
	blocked[2] = 0;
	blocked[3] = 0;
	
	// are any adjent positions blocked by another predator?
	// do not enforce collision detection
	if (false) {
	for (i = 0; i < nPredator; ++i) {
		if ((xprd[i] == -1 || xprd[i] == -2) && Math.abs(yprd[i]) < 2) {
			blocked[3] = 1;
			System.out.println( "Blocked: 3" );

		}
		if (Math.abs(xprd[i]) < 2 && (yprd[i] == 1 || yprd[i] == 2)) {
			blocked[0] = 1;
			System.out.println( "Blocked: 1" );
		}		

		if ((xprd[i] == 1 || xprd[i] == 2) && Math.abs(yprd[i]) < 2) {
			blocked[2] = 1;
			System.out.println( "Blocked: 2" );
		}
		if (Math.abs(xprd[i]) < 2 && (yprd[i] == -1 || yprd[i] == -2)) {
			blocked[1] = 1;
			System.out.println( "Blocked: 1" );
		}
	}
	}
	
	// initially - bogus move directions, meaning "stand still"
	nMoveDirection = 5;

	// random() gives some chance to move on the smaller axis, to avoid "deadlocks" that happen otherwise 
   	if (Math.abs(targetx) > Math.abs(targety) && (Math.abs(targety) == 0 || Math.random() > 0.8) ) {
   		// move on the X axis
   		if (targetx < 0 && blocked[3] != 1) {
   			nMoveDirection = 3;
   		}
   		else {
   			if (blocked[2] != 1) {
   				nMoveDirection = 2;
   			}
   		}
   	}

   	if (nMoveDirection == 5) {
   		if (Math.abs(targety) > 0) {
   			// move on the Y axis
   			if (targety < 0 && blocked[1] != 1) {
   				nMoveDirection = 1;	
   			}
   			else {
   				if (blocked[0] != 1) {
   					nMoveDirection = 0;	
   				}
   			}
   		}
   	}
 
    System.out.println( "Target:" + " seen at (" + targetx + ", " + targety + ") " + nMoveDirection );


	for (i = 0; i < nPredator; ++i) {
//	    System.out.println( "Predator:" + i + " seen at (" + xprd[i] + ", " + yprd[i] + ")" );
	}

//    System.out.println( "Pray 1:" + " seen at (" + xp[0] + ", " + yp[0] + ")" );
//    System.out.println( "Pray 2:" + " seen at (" + yp[1] + ", " + yp[1] + ")" );
//    System.out.println( "Chasing pray:" + nPrayToChase );
//    System.out.println( "My role:" + nRole );
  }


  /** This method is called after a communication message has arrived from
      another predator. */
  public void processCommunicationInformation( String strMessage) 
  {
    // TODO: exercise 3 to improve capture times
  }
  
  /** This method is called and can be used to send a message to all other
       predators. Note that this only will have effect when communication is
      turned on in the server. */
  public String determineCommunicationCommand() 
  { 
    // TODO: exercise 3 to improve capture times
    return "" ; 
  }

  /**
   * This method is called when an episode is ended and can be used to
   * reset some variables.
   */
  public void episodeEnded( )
  {
     // this method is called when an episode has ended and can be used to
     // reinitialize some variables
     System.out.println( "EPISODE ENDED\n" );
  }

  /**
   * This method is called when this predator is involved in a
   * collision.
   */
  public void collisionOccured( )
  {
     // this method is called when a collision occured and can be used to
     // reinitialize some variables
     System.out.println( "COLLISION OCCURED\n" );
  }

  /**
   * This method is called when this predator is involved in a
   * penalization.
   */
  public void penalizeOccured( )
  {
    // this method is called when a predator is penalized and can be used to
    // reinitialize some variables
    System.out.println( "PENALIZED\n" );
  }

  /**
   * This method is called when this predator is involved in a
   * capture of a prey.
   */
  public void preyCaught( )
  {
    System.out.println( "PREY CAUGHT\n" );
  }
 
  public static void main( String[] args )
  {
    Predator predator = new Predator();
    if( args.length == 2 )
      predator.connect( args[0], Integer.parseInt( args[1] ) );
    else
      predator.connect();
  }
}
  
