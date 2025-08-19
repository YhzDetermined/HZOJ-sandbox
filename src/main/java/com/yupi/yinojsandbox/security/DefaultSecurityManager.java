package com.yupi.yinojsandbox.security;

import java.security.Permission;

public class DefaultSecurityManager extends SecurityManager{
    @Override
    public void checkPermission(Permission perm) {
        super.checkPermission(perm);
        throw new SecurityException("权限不足" + perm.getActions());
    }

}
