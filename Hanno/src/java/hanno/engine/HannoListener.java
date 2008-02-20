package hanno.engine;

import org.apache.commons.scxml.SCXMLListener;
import org.apache.commons.scxml.model.Transition;
import org.apache.commons.scxml.model.TransitionTarget;

/**
 * 
 * Listens for Transition events
 * 
 * @author Chris Struble
 * @version 1.0
 */

public class HannoListener implements SCXMLListener{
    
    private HannoStateMachine machine;  
    
    /**
     * @see SCXMLListener#onEntry(TransitionTarget)
     */
    public void onEntry(final TransitionTarget state) {

    }

    /**
     * @see SCXMLListener#onExit(TransitionTarget)
     */
    public void onExit(final TransitionTarget state) {

    }

    /**
* @see SCXMLListener#onTransition(TransitionTarget,TransitionTarget,Transition)
     */
    public void onTransition(final TransitionTarget from,
            final TransitionTarget to, final Transition transition) {
        
        String evt = transition.getEvent( );
        
        if(evt != null)
        {       
            machine.handleEvent( evt );
        }
    }
    
    /**
     * Make the listener aware of the State Machine instance
     * @param hsm the HannoStateMachine
     */
    public void registerMachine(HannoStateMachine hsm) {
        this.machine = hsm;
    }
}
