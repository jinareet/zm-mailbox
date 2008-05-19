package com.zimbra.cs.account.accesscontrol;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AccountServiceException;
import com.zimbra.cs.account.Entry;
import com.zimbra.cs.account.NamedEntry;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Provisioning.AccountBy;
import com.zimbra.cs.account.Provisioning.DistributionListBy;
import com.zimbra.soap.ZimbraSoapContext;

public class PermUtil {
    
    /**
     * Get ACEs with specified rights
     * 
     * @param target 
     * @param rights specified rights.  If null, all ACEs in the ACL will be returned.
     * @return ACEs with right specified in rights
     * @throws ServiceException
     */
    public static Set<ZimbraACE> getACEs(Entry target, Set<Right> rights) throws ServiceException {
        ZimbraACL acl = target.getACL(); 
        if (acl != null)
            return acl.getACEs(rights);
        else
            return null;
    }
    
    public static void modifyACEs(Entry target, Set<ZimbraACE> aces) throws ServiceException {
        ZimbraACL acl = target.getACL(); 
        if (acl == null)
            acl = new ZimbraACL(aces);
        else
            acl.modifyACEs(aces); 
        
        serialize(target, acl);
    }
    
    /** Removes the right granted to the specified id.  If the right 
     *  was not previously granted to the target, no error is thrown.
     */
    public static void revokeACEs(Entry target, Set<ZimbraACE> aces) throws ServiceException {
        ZimbraACL acl = target.getACL(); 
        if (acl == null)
            throw ServiceException.INVALID_REQUEST("no such aces", null);
        
        acl.revokeACEs(aces);
        serialize(target, acl);
    }
    
    private static void serialize(Entry target, ZimbraACL acl) throws ServiceException {
        Map<String, Object> attrs = new HashMap<String, Object>();
        attrs.put(Provisioning.A_zimbraACE, acl.serialize());
        Provisioning.getInstance().modifyAttrs(target, attrs);
    }
    
    public static NamedEntry lookupEmailAddress(String name) throws ServiceException {
        NamedEntry nentry = null;
        Provisioning prov = Provisioning.getInstance();
        nentry = prov.get(AccountBy.name, name);
        if (nentry == null)
            nentry = prov.get(DistributionListBy.name, name);
        return nentry;
    }
    
    public static NamedEntry lookupGranteeByName(String name, GranteeType type, ZimbraSoapContext zsc) throws ServiceException {
        if (type == GranteeType.GT_PUBLIC)
            return null;

        Provisioning prov = Provisioning.getInstance();
        // for addresses, default to the authenticated user's domain
        if ((type == GranteeType.GT_USER || type == GranteeType.GT_GROUP) && name.indexOf('@') == -1) {
            Account authacct = prov.get(AccountBy.id, zsc.getAuthtokenAccountId(), zsc.getAuthToken());
            String authname = (authacct == null ? null : authacct.getName());
            if (authacct != null)
                name += authname.substring(authname.indexOf('@'));
        }

        NamedEntry nentry = null;
        if (name != null)
            switch (type) {
                case GT_USER:    nentry = lookupEmailAddress(name);                 break;
                case GT_GROUP:   nentry = prov.get(DistributionListBy.name, name);  break;
            }

        if (nentry != null)
            return nentry;
        switch (type) {
            case GT_USER:    throw AccountServiceException.NO_SUCH_ACCOUNT(name);
            case GT_GROUP:   throw AccountServiceException.NO_SUCH_DISTRIBUTION_LIST(name);
            default:  throw ServiceException.FAILURE("LDAP entry not found for " + name + " : " + type, null);
        }
    }
    
    public static NamedEntry lookupGranteeByZimbraId(String zid, GranteeType granteeType) {
        Provisioning prov = Provisioning.getInstance();
        try {
            switch (granteeType) {
                case GT_USER:    return prov.get(AccountBy.id, zid);
                case GT_GROUP:   return prov.get(DistributionListBy.id, zid);
                case GT_PUBLIC:
                default:                  return null;
            }
        } catch (ServiceException e) {
            return null;
        }
    }
    

}
