/*
 * ***** BEGIN LICENSE BLOCK *****
 * Version: ZPL 1.1
 * 
 * The contents of this file are subject to the Zimbra Public License
 * Version 1.1 ("License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.zimbra.com/license
 * 
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 * 
 * The Original Code is: Zimbra Collaboration Suite.
 * 
 * The Initial Developer of the Original Code is Zimbra, Inc.
 * Portions created by Zimbra are Copyright (C) 2005 Zimbra, Inc.
 * All Rights Reserved.
 * 
 * Contributor(s): 
 * 
 * ***** END LICENSE BLOCK *****
 */

package com.zimbra.cs.index;

import java.util.HashMap;
import com.zimbra.cs.service.ServiceException;

/**
 * @author tim
 *
 * A set of UngroupedQueryResults which groups by Message
 */
class MsgQueryResults extends ZimbraQueryResultsImpl 
{
//    QueryOperation mResults;
    ZimbraQueryResults mResults;

    public MsgQueryResults(ZimbraQueryResults topLevelQueryOperation, byte[] types, int searchOrder) {
        super(types, searchOrder);
        mResults = topLevelQueryOperation;
    }

    ZimbraHit mNextHit = null;
    
    HashMap mSeenMsgs = new HashMap();
    
    /**
     * Gets the next hit from the QueryOp.  
     * 
     * Side effect: will Op's iterator one or more entries forward
     *  
     * @return
     * @throws ServiceException
     */
    private ZimbraHit internalGetNextHit() throws ServiceException {
        while (mResults.hasNext()) {
            ZimbraHit opNext = mResults.getNext();
            
            MessageHit curHit = null;
            Integer msgId = new Integer(opNext.getItemId());
            
            curHit = (MessageHit)mSeenMsgs.get(msgId);
            if (curHit != null) {
                // we've seen this Message before...skip this hit
            } else {
                if (opNext instanceof ConversationHit) {
                    assert(false); // not written yet.  If we hit this, need to iterate conv and add ALL of its messages to the hit list here...
                } else if (opNext instanceof MessageHit) {
                    curHit = ((MessageHit)opNext);
                } else if (opNext instanceof MessagePartHit) {
                    curHit = ((MessagePartHit)opNext).getMessageResult();
                } else {
                    return curHit; // wasn't a Conv/Message/Part, so just return it as-is
                }
                
                mSeenMsgs.put(msgId, curHit);
                
                /* Iterate fwd a bit to see if we can pick up more message parts... */
                while (mResults.hasNext()) {
                    ZimbraHit nextHit = mResults.peekNext();
                    
                    int newMsgId = nextHit.getItemId();
                    
                    if (newMsgId != msgId.intValue()) {
                        return curHit;
                    } else {
                        mResults.getNext(); // same msg id -- so move iterator fwd
                        if (nextHit instanceof MessagePartHit) {
                            curHit.addPart((MessagePartHit) nextHit);
                        }
                    }
                }
                return curHit;
            }
        }
        return null;
    }
    
    private boolean bufferNextHit() throws ServiceException {
        if (mNextHit == null) {
            mNextHit = internalGetNextHit();
            assert(mNextHit == null || mNextHit instanceof MessageHit);
        }
        return (mNextHit != null);
    }
    
    public void resetIterator() throws ServiceException {
        mSeenMsgs.clear();
        mResults.resetIterator();
    }
    
    public ZimbraHit getNext() throws ServiceException {
        bufferNextHit();
        ZimbraHit toRet = mNextHit;
        assert(mNextHit == null || mNextHit instanceof MessageHit);
        mNextHit = null;
        return toRet;
    }
    
    public ZimbraHit peekNext() throws ServiceException {
        bufferNextHit();
        assert(mNextHit == null || mNextHit instanceof MessageHit);
        return mNextHit;
    }
    
    public void doneWithSearchResults() throws ServiceException {
        mResults.doneWithSearchResults();
    }

    public ZimbraHit skipToHit(int hitNo) throws ServiceException {
        if (hitNo > 0) {
            mResults.skipToHit(hitNo-1);
        } else {
            resetIterator();
        }
        return getNext();
    }

}
