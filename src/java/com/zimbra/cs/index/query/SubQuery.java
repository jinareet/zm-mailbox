/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2010 Zimbra, Inc.
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
package com.zimbra.cs.index.query;

import java.util.List;

import com.zimbra.cs.index.QueryOperation;

/**
 * Special query that wraps sub queries.
 *
 * @author tim
 * @author ysasaki
 */
public class SubQuery extends Query {
    private static final int SUBQUERY_TOKEN = 9999;

    private List<Query> mSubClauses;

    public SubQuery(List<Query> exp) {
        super(SUBQUERY_TOKEN);
        mSubClauses = exp;
    }

    public List<Query> getSubClauses() {
        return mSubClauses;
    }

    @Override
    public QueryOperation getQueryOperation(boolean truth) {
        assert(false);
        return null;
    }

    @Override
    public StringBuilder dump(StringBuilder out) {
        out.append(getModifier());
        out.append('(');
        for (Query sub : mSubClauses) {
            sub.dump(out);
        }
        return out.append(')');
    }
}
