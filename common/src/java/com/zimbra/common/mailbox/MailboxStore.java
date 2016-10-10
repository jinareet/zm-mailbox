/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2016 Synacor, Inc.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation,
 * version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program.
 * If not, see <https://www.gnu.org/licenses/>.
 * ***** END LICENSE BLOCK *****
 */
package com.zimbra.common.mailbox;

import java.util.List;

import com.zimbra.common.service.ServiceException;

public interface MailboxStore {
    public String getAccountId() throws ServiceException;
    public FolderStore getFolderByPath(OpContext octxt, String path) throws ServiceException;
    public ExistingParentFolderStoreAndUnmatchedPart getParentFolderStoreAndUnmatchedPart(OpContext octxt, String path)
    throws ServiceException;
    /**
     * Copies the items identified in {@link idlist} to folder {@link targetFolder}
     * @param idlist - list of item ids for items to copy
     * @param targetFolder - Destination folder
     */
    List<ItemIdentifier> copyItemAction(OpContext ctxt, String authenticatedAcctId, ItemIdentifier targetFolder,
            List<Integer> idlist) throws ServiceException;
}