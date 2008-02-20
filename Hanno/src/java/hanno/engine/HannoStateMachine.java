package hanno.engine;

import java.lang.Class;
import java.lang.reflect.Method;

import java.util.Set;
import java.util.TreeSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import java.io.StringWriter;
import java.io.PrintWriter;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import org.apache.commons.scxml.SCXMLExecutor;
import org.apache.commons.scxml.Evaluator;
import org.apache.commons.scxml.Context;
import org.apache.commons.scxml.env.AbstractStateMachine;
import org.apache.commons.scxml.env.jexl.JexlEvaluator;
import org.apache.commons.scxml.env.jexl.JexlContext;
import org.apache.commons.scxml.model.SCXML;
import org.apache.commons.scxml.model.State;
import org.apache.commons.scxml.model.Transition;
import org.apache.commons.scxml.model.ModelException;

import watij.runtime.ie.IE;

/**
 * 
 * State machine handler for the Hanno engine
 * 
 * @author Chris Struble
 * @version 1.0
 */

public class HannoStateMachine extends AbstractStateMachine
{
    private SCXMLExecutor exec;

    private SCXML machine;
    
    private Evaluator eval;
    
    private Context ctxt;

    private HannoListener listener;

    private Class<? extends HannoStateMachine> cm;

    private Object cmObject;

    private Object[] parameters;

    private String nextEvent;

    private Set<String> eventsNotFired;

    private int eventInitSize;

    protected boolean errState = false;

    protected String errMessage = null;
    
    protected static IE ie = null;

    /**
     * Constructor - loads the default example SCXML file into the engine
     */
    public HannoStateMachine( )
    {
    	this("hanno.examples.wikipedia.Wikipedia.xml");
    }
    
    /**
     * Constructor - loads a specified SCXML file into the engine
     */
    public HannoStateMachine(String smXmlPath)
    {
       super(HannoStateMachine.class.getClassLoader( ).getResource( smXmlPath ));
        
        try {
            exec = this.getEngine( );
            machine = exec.getStateMachine( );
            listener = new HannoListener( );
            listener.registerMachine( this );
            exec.addListener( machine, listener );
            eval = new JexlEvaluator();
            exec.setEvaluator( eval );
            ctxt = new JexlContext();
            exec.setRootContext( ctxt );
            exec.go( ); 
        }
        catch (ModelException me)
        {
            errMessage = getStackTrace( me );
            errState = true;
        }

        cm = this.getClass( );
        cmObject = cm.cast( this );
        parameters = null;
        loadEvents( smXmlPath );
    }

    /*
     * Utility methods
     */

    /**
     * Utility method - Load a list of events
     * 
     */

    private void loadEvents(String smXmlPath)
    {
        // Adapted from XPath example at:
        // http://www.ibm.com/developerworks/xml/library/x-javaxpathapi.html
        try
        {
            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance( );
            domFactory.setNamespaceAware( true ); // never forget this!
            DocumentBuilder builder = domFactory.newDocumentBuilder( );
            Document doc = builder.parse( "src/java/" + smXmlPath );

            XPathFactory factory = XPathFactory.newInstance( );
            XPath xpath = factory.newXPath( );

            XPathExpression expr = xpath.compile( "//@event" );

            Object result = expr.evaluate( doc, XPathConstants.NODESET );
            NodeList nodes = (NodeList) result;

            eventsNotFired = new TreeSet<String>( );

            for ( int i = 0; i < nodes.getLength( ); i++ )
            {
                String test = nodes.item( i ).getNodeValue( );
                eventsNotFired.add( test );
            }
            eventInitSize = eventsNotFired.size( );
        }
        catch ( Exception e )
        {
            errMessage = getStackTrace( e );
            errState = true;
        }
    }

    /**
     * Utility method - Get stack trace as String
     * @param t
     * @return the stack trace
     */
    protected String getStackTrace( Throwable t)
    {
        // Adapted from http://www.devx.com/tips/Tip/27885
        StringWriter sw = new StringWriter( );
        PrintWriter pw = new PrintWriter( sw, true );
        t.printStackTrace( pw );
        pw.flush( );
        sw.flush( );
        return sw.toString( );
    }

    /**
     * Utility method - Get the error state
     * @return True if an error has occurred
     */
    public boolean getErrState()
    {
        return errState;
    }

    /**
     * Utility method - Get the error message
     * @return the error message
     */
    public String getErrMessage()
    {
        return errMessage;
    }

    /**
     * Utility method - Get list of events not fired
     * @return list
     */
    public String getEvents()
    {
        String evtOut = "";
        Iterator iter = eventsNotFired.iterator( );

        while ( iter.hasNext( ) )
        {
            evtOut += "INFO: " + (String) iter.next( ) + "\n";
        }
        return evtOut;
    }

    /**
     * Set the nextEvent global variable
     * Used when running from log file
     * @param evt 
     */
    public void setNextEvent( String evt)
    {
        nextEvent = evt;
    }

    /**
     * Get the next event to fire against the state machine
     * @return the next event
     */
    public String getNextEvent()
    {
        Random picker = new Random( );
        Set states = exec.getCurrentStatus( ).getStates( );
        State curState = (State) states.iterator( ).next( );
        List transitions = curState.getTransitionsList( );
        List<String> stateEvts = new ArrayList<String>( );

        // generate list of events from current state not fired
        for ( int i = 0; i < transitions.size( ); i++ )
        {
            String evt = ( (Transition) transitions.get( i ) ).getEvent( );
            if ( eventsNotFired.contains( evt ) )
            {
                stateEvts.add( evt );
            }
        }

        // check list of events from current state not fired
        if ( stateEvts.isEmpty( ) )
        {
            // perform a first level search
            // pick the first event with a child event not fired
            // exclude guarded events
            boolean eventFound = false;
            eventbreak: 
            for ( int i = 0; i < transitions.size( ); i++ )
            {
                Transition tran = (Transition) transitions.get( i );
                String evt = tran.getEvent( );
                State childState = (State) tran.getTarget( );
                List childTrans = childState.getTransitionsList( );
                for ( int j = 0; j < childTrans.size( ); j++ )
                {
                    Transition childTran = (Transition) childTrans.get( j );
                    String childEvt = childTran.getEvent( );
                    if ( eventsNotFired.contains( childEvt ) )
                    {
                        if ( tran.getCond( ) == null && childTran.getCond( ) == null )
                        {
                            nextEvent = evt;
                            eventFound = true;
                            break eventbreak;     
                        }
                    }
                }
            }
            // pick any event from the current state
            if ( !eventFound )
                nextEvent = ( (Transition) transitions.get( picker.nextInt( transitions.size( ) ) ) ).getEvent( );
        }
        // pick an event from current state not fired 
        else
        {
            nextEvent = (String) stateEvts.get( picker.nextInt( stateEvts.size( ) ) );
            eventsNotFired.remove( nextEvent );
        }
        return nextEvent;
    }

    /**
     * Check if list of events not fired is empty
     * @return true if all events have been covered
     */
    public boolean eventsEmpty()
    {
        return ( eventsNotFired.size( ) == 0 );
    }

    /**
     * Get size of initial event list
     * @return size of initial event list
     */
    public int getEventsInitSize()
    {
        return eventInitSize;
    }

    /**
     * Return number of events not fired
     * @return size of final event list
     */
    public int getEventsSize()
    {
        return eventsNotFired.size( );
    }

    /**
     * Handle events
     * @param evt Event intercepted by HannoListener
     */
    public void handleEvent( String evt)
    {
        System.err.println( "EVENT: " + evt );
        try
        {
            Class[] paramTypes = null;
            Method evtMethod = cm.getMethod( evt, paramTypes );
            evtMethod.invoke( cmObject, parameters );
        }
        catch ( Exception t )
        {
            errMessage = getStackTrace( t );
            errState = true;
        }
    }
    
    /**
     * Close browser object
     */
    protected void closeBrowser( )

    {
    	if (ie != null)
    	{	
    		try 
    		{
    			ie.close();
    		}
    		catch (Exception t)
    		{
                errMessage = getStackTrace( t );
                errState = true;
    		}
    	}	
    	ie = null;
    }
}
