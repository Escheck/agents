package agents;

import com.sun.media.jfxmediaimpl.MediaDisposer;
import negotiator.Bid;
import negotiator.actions.Action;

public interface EnterBidDialogInterface extends MediaDisposer.Disposable
{
    negotiator.actions.Action askUserForAction(Action opponentAction, Bid myPreviousBid);
}
