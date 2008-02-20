package hanno.engine;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.FileWriter;

/**
 * Feeds events to the state machine
 * 
 * @author Chris Struble
 * @version 1.0
 */

public class HannoRunner
{
    /**
     * Default constructor to run the example state machine.
     * 
     */
    public HannoRunner( )
    {
        String logInFile = null;
    	String logOutFile = "examples.wikipedia.log";
        String className = "hanno.examples.wikipedia.WikipediaStateMachine";
        int stepLimit = 100;
        boolean stopOnCover = false;
        boolean stopOnLimit = true;

        try
        {
            runMachine( className, logInFile, logOutFile, stopOnCover, stopOnLimit, stepLimit );
        }
        catch ( IOException ioe )
        {
            System.err.println( "ERROR: Unable to create writer to log file " + logOutFile );
        }
    }

    /**
     * Run with specified command line arguments
     * @param argList
     */
    public HannoRunner( String[] argList)
    {
        String inputFile = null;
        String outputFile = null;
        String className = null;
        String stepStr = null;
        String stopOnCoverStr = null;
        int stepLimit = 0;
        boolean stopOnCover = false;
        boolean stopOnLimit = false;

        int i = 0;
        while ( i < argList.length )
        {
            if ( argList[i].equals( "-i" ) )
            {
                i++;
                inputFile = argList[i];
            }
            else if ( argList[i].equals( "-o" ) )
            {
                i++;
                outputFile = argList[i];
            }
            else if ( argList[i].equals( "-c" ) )
            {
                i++;
                className = argList[i];
            }
            else if ( argList[i].equals( "-s" ) )
            {
                i++;
                stopOnCoverStr = argList[i];
                stopOnCover = Boolean.parseBoolean(stopOnCoverStr);
            }
            else if ( argList[i].equals( "-l" ) )
            {
                i++;
                stepStr = argList[i];
                try
                {
                	stepLimit = Integer.parseInt(stepStr);
                }
                catch ( NumberFormatException nfe )
                {
                    System.err.println( "ERROR: Invalid step limit " + stepStr );
                }
                if(stepLimit > 0)
                	stopOnLimit = true;
                else
                	stopOnLimit = false;
            }
            i++;
        }

        try
        {
            runMachine( className, inputFile, outputFile, stopOnCover, stopOnLimit, stepLimit);
        }
        catch ( IOException ioe )
        {
            System.err.println( "ERROR: Unable to create writer to log file " + outputFile );
        }
    }

    /**
     * 
     * @throws IoException
     */
	public void runMachine( String className, String inputFile, String outputFile, boolean stopOnCover, boolean stopOnLimit, int stepLimit) throws IOException
    {
        PrintWriter logWriter;
        HannoStateMachine sm = null;    
        String nextEvt;
        
        try 
        {
        	Class theClass  = Class.forName(className);
            sm = (HannoStateMachine) theClass.newInstance();
        }
        catch ( ClassNotFoundException ex ){
            System.err.println( ex + " StateMachine class must be in class path.");
        }
        catch( InstantiationException ex ){
            System.err.println( ex + " StateMachine class must be concrete.");
        }
        catch( IllegalAccessException ex ){
            System.err.println( ex + " StateMachine class must have a no-arg constructor.");
        }

        
        if ( inputFile != null )
        {   
            try
            {
                BufferedReader in = new BufferedReader( new FileReader( inputFile ) );
                while ( ( nextEvt = in.readLine( ) ) != null )
                {
                    if ( sm.getErrState( ) )
                    {
                        System.err.println( "ERROR: " + sm.getErrMessage( ) );
                        break;
                    }
                    //System.err.println( "EVENT: " + nextEvt );
                    sm.setNextEvent( nextEvt );
                    sm.fireEvent( nextEvt );
                }

    	        // close the browser if no error
    	        if(sm.getErrState() == false)
    	        	sm.closeBrowser();
                
                System.err.println( "SUMMARY: End execution from log file." );
            }
            catch ( IOException ioe )
            {
                System.err.println( "ERROR: Unable to read from log file " + inputFile );
            }
        }
        else
        {
	        // create PrintWriter for event logging
	        logWriter = new PrintWriter( new BufferedWriter( new FileWriter( outputFile ) ) );
	
	        // loop until all events are covered or step limit is reached
	        int steps = 0;
	        while ( true )
	        {
	            if ( sm.getErrState( ) )
	            {
	                System.err.println( "ERROR: " + sm.getErrMessage( ) );
	                break;
	            }
	            else if ( stopOnCover && sm.eventsEmpty( ))
	            {
	                break;
	            }
	            else if ( stopOnLimit && steps >= stepLimit )
	            {
	                break;
	            }
	            else
	            {
	                nextEvt = sm.getNextEvent( );
	                // System.err.println( "EVENT: " + nextEvt );
	                logWriter.println( nextEvt );
	
	                sm.fireEvent( nextEvt );
	            }
	            steps++;
	        }
	
	        // close the browser if no error
	        if(sm.getErrState() == false)
	        	sm.closeBrowser();
	        
	        // print number of steps
	        System.err.println( "SUMMARY: Test steps " + steps );
	
	        // print number of total events
	        System.err.println( "SUMMARY: Total events " + sm.getEventsInitSize( ) );
	        
	        // print list of events not covered if any
	        if ( !sm.eventsEmpty( ) )
	        {
	            System.err.println( "SUMMARY: Events not covered: " + sm.getEventsSize( ));
	            System.err.println( sm.getEvents( ) );
	        }
	        else
	        {
	            System.err.println( "SUMMARY: All events covered." );
	        }
	
	        System.err.println( "SUMMARY: End execution from state machine." );
	        logWriter.close( );
        }
    }

    public static void main( String[] args)
    {
        if ( args.length == 0 )
        {
            new HannoRunner( );
        }
        else if ( args.length > 0 )
        {
            new HannoRunner( args );
        }
        else
        {
            System.out.println( "USAGE: [-c class] [-i inputfile] [-o outputfile] [-s stopOnCover] [-l limit]" );
        }
    }

}
