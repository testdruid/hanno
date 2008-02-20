package hanno.examples.wikipedia;

import static watij.finders.SymbolFactory.href;

import watij.runtime.ie.IE;

import org.junit.Assert;

import hanno.engine.HannoStateMachine;

/**
 * 
 * Example state machine -- navigates links on the Wikipedia site
 * 
 * @author Chris Struble
 * @version 1.0
 */

public class WikipediaStateMachine extends HannoStateMachine
{
    // private static IE ie;

    /**
     * Constructor - loads the SCXML file into the engine
     */
    public WikipediaStateMachine( )
    {
        super("hanno/examples/wikipedia/wikipedia.xml" );
    }

    /*
     * Event Methods
     * Called by HannoListener when an event in triggered in the SCXML engine
     * Should call Watij code to carry out the event
     */

    /**
     * Event method - start the browser and enter URL for the Wikipedia main page
     *
     */
    public void start() throws Exception
    {
		ie = new IE();
		ie.start("http://en.wikipedia.org/wiki/Main_Page");
		ie.maximize();
    }
    
    /**
     * Event method - select the Main Page link  
     *
     */
    public void wikipedia_main() throws Exception
    {
        ie.link( href, "/wiki/Main_Page" ).click( );
        ie.waitUntilReady( );
    }
    
    /**
     * Event method - select the Contents link  
     *
     */
    public void portal_contents() throws Exception
    {
        ie.link( href, "/wiki/Portal:Contents" ).click( );
        ie.waitUntilReady( );
    }
    
    /**
     * Event method - select the About Wikipedia link  
     *
     */
    public void wikipedia_about() throws Exception
    {
        ie.link( href, "/wiki/Wikipedia:About" ).click( );
        ie.waitUntilReady( );
    }
    
    /**
     * Event method - select the Commmunity Portal link  
     *
     */
    public void wikipedia_community() throws Exception
    {
        ie.link( href, "/wiki/Wikipedia:Community_Portal" ).click( );
        ie.waitUntilReady( );
    }    
    
    /**
     * Event method - select the Recent Changes link  
     *
     */
    public void special_recent() throws Exception
    {
        ie.link( href, "/wiki/Special:Recentchanges" ).click( );
        ie.waitUntilReady( );
    }
    
    /**
     * Event method - select the Help link  
     *
     */
    public void help_contents() throws Exception
    {
        ie.link( href, "/wiki/Help:Contents" ).click( );
        ie.waitUntilReady( );
    }
    
    
    
    /*
     * State methods
     * Called by the SCXML engine when entering a state
     * Should call Watij code to verify that correct page was loaded
     */

    /**
     * Reset state method - required by SCXML
     */
    public void reset()
    {
        try
        {
    		if (ie != null)
    			ie.close();
    		ie = null;
        }
        catch ( Throwable t )
        {
            errMessage = getStackTrace( t );
            errState = true;
        }
    }

    /**
     * Root state method - required only to support global variables -- no action needed
     */
    public void root_state()
    {

    }

    /**
     * State method - Wikipedia main page is loaded
     */
    public void wikipedia_main_state()
    {
        try
        {
        	Assert.assertTrue( ie.containsText( "Today's featured article" ) );
        }
        catch ( Throwable t )
        {
            errMessage = getStackTrace( t );
            errState = true;
        }
    }

    /**
     * State method - Wikipedia Contents page is loaded
     */
    public void portal_contents_state()
    {
        try
        {
        	Assert.assertTrue( ie.containsText( "Portal:Contents" ) );
        }
        catch ( Throwable t )
        {
            errMessage = getStackTrace( t );
            errState = true;
        }
    }    

    /**
     * State method - Wikipedia About page is loaded
     */
    public void wikipedia_about_state()
    {
        try
        {
        	Assert.assertTrue( ie.containsText( "Wikipedia:About" ) );
        }
        catch ( Throwable t )
        {
            errMessage = getStackTrace( t );
            errState = true;
        }
    }    

    /**
     * State method - Wikipedia Community Portal page is loaded
     */
    public void wikipedia_community_state()
    {
        try
        {
        	Assert.assertTrue( ie.containsText( "Wikipedia:Community Portal" ) );
        }
        catch ( Throwable t )
        {
            errMessage = getStackTrace( t );
            errState = true;
        }
    }    

    /**
     * State method - Wikipedia Recent Changes page is loaded
     */
    public void special_recent_state()
    {
        try
        {
        	Assert.assertTrue( ie.containsText( "Recentchanges for:" ) );
        }
        catch ( Throwable t )
        {
            errMessage = getStackTrace( t );
            errState = true;
        }
    }    

    /**
     * State method - Wikipedia Help page is loaded
     */
    public void help_contents_state()
    {
        try
        {
        	Assert.assertTrue( ie.containsText( "Help:Contents" ) );
        }
        catch ( Throwable t )
        {
            errMessage = getStackTrace( t );
            errState = true;
        }
    }    
}
