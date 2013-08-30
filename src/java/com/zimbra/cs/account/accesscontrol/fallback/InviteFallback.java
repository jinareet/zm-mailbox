/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2009, 2010, 2011, 2013 Zimbra Software, LLC.
 * 
 * The contents of this file are subject to the Zimbra Public License
 * Version 1.4 ("License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 * http://www.zimbra.com/license.
 * 
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
 * ***** END LICENSE BLOCK *****
 */
package com.zimbra.cs.account.accesscontrol.fallback;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Entry;
import com.zimbra.cs.account.accesscontrol.CheckRightFallback;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.ACL;
import com.zimbra.cs.mailbox.acl.FolderACL;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.util.Zimbra;

public class InviteFallback extends CheckRightFallback {
    
    //
    // allow the invite right if the authed user has admin folder right on the default calendar folder
    // (calendar folder in which the appointment is to be updated when the owner of the calendar receives 
    // an invite)
    //
    protected Boolean doCheckRight(Account authedAcct, Entry target, boolean asAdmin) throws ServiceException {
        // Don't do anything unless running inside the server
        if (!Zimbra.started())
            return null;
        
        if (!(target instanceof Account))
            return null;
        
        Account targetAcct = (Account)target;
        
        OperationContext octxt = new OperationContext(authedAcct, asAdmin);
        
        int defaultCalendarfolderId = Mailbox.ID_FOLDER_CALENDAR;
        
        FolderACL folderACL = new FolderACL(octxt, targetAcct, defaultCalendarfolderId, Boolean.FALSE);
        
        // bug 42146
        //     admin rights (granted by UI): rwidxa 
        //     manager rights (granted by UI): rwidx 
        //
        // don't need the action right - it's for accepting/denying invites on behave of the invitee
        // don't need the admin right - it's for granting/revoking rights on the owner's folder
        // 
        // don't worry about the private right: we are checking if the authed user can invite(public/private)
        // the target user, the authed user is composing the invite and he sees what's in his invite anyway.
        //
        short rightsNeeded = ACL.RIGHT_READ | ACL.RIGHT_WRITE | ACL.RIGHT_INSERT | ACL.RIGHT_DELETE;
        boolean hasRights = folderACL.canAccess(rightsNeeded);
        
        if (hasRights)
            return Boolean.TRUE;
        else
            return null;
    }
}
