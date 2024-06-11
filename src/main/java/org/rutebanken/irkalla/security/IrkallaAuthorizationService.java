package org.rutebanken.irkalla.security;

/**
 *  Service that verifies the privileges of the API clients.
 */
public interface IrkallaAuthorizationService {

    /**
     * Verify that the user has full administrator privileges.
     */
    void verifyAdministratorPrivileges();

}
