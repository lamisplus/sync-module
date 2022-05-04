package org.lamisplus.modules.sync.controller.vm;

import org.lamisplus.modules.sync.domain.dto.UserDTO;

public class ManagedUserVM extends UserDTO {
    private String password;

    public ManagedUserVM() { }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "ManagedUserVM{" + super.toString() + "} ";
        }
}
