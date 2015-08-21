/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2015 Zimbra, Inc.
 * 
 * The contents of this file are subject to the Zimbra Public License
 * Version 1.3 ("License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 * http://www.zimbra.com/license.
 * 
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
 * ***** END LICENSE BLOCK *****
 */

package com.zimbra.cs.pushnotifications.filters;

import java.util.List;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.DataSource;
import com.zimbra.cs.mailbox.Folder;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.Message;

public class MessageFileIntoFilter implements Filter {

    private Message message;
    private Account account;
    private DataSource dataSource;
    private DataSourceInitialSyncFilter initialSyncFilter;

    public static final String DATA_SOURCE_INBOX = "Inbox";

    public MessageFileIntoFilter(Account account, Message message) {
        this.message = message;
        this.account = account;
    }

    public MessageFileIntoFilter(Account account, Message message, DataSource dataSource,
                                 DataSourceInitialSyncFilter intialSyncFilter) {
        this.message = message;
        this.account = account;
        this.dataSource = dataSource;
        this.initialSyncFilter = intialSyncFilter;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.zimbra.cs.pushnotifications.filters.Filter#apply()
     */
    @Override
    public boolean apply() {
        int folderId = message.getFolderId();
        if (Mailbox.ID_FOLDER_INBOX == folderId) {
            return true;
        } else if (dataSource != null && isDataSourceInbox(folderId)
            && dataSource.getEmailAddress() != null) {
            return true;
        }
        return false;
    }

    private boolean isDataSourceInbox(int folderId) {
        try {
            Mailbox mbox = message.getMailbox();
            Folder dataSourceFolder = mbox.getFolderById(null, dataSource.getFolderId());
            List<Folder> subFolders = dataSourceFolder.getSubfolders(null);
            for (Folder folder : subFolders) {
                if (DATA_SOURCE_INBOX.equalsIgnoreCase(folder.getName())
                    && folderId == folder.getId()) {
                    if (initialSyncFilter != null) {
                        initialSyncFilter.setInboxFolder(folder);
                    }
                    return true;
                }
            }
        } catch (ServiceException e) {
            ZimbraLog.mailbox.warn("ZMG: Exception in processing MessageFileIntoFilter", e);
            return false;
        }
        return false;
    }

}
