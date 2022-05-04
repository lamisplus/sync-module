package org.lamisplus.modules.sync.security;

public class PermissionConstants {

    public PermissionConstants() {
    }
    // Add or modify permission names here
    //Note* Renaming permissions would mean that roles affected have to be re-assigned the permissions
    public static enum PermissionsEnum {
        patient_read,
        patient_write,
        patient_delete,
        consultation_read,
        consultation_write,
        consultation_delete,
        laboratory_read,
        laboratory_write,
        laboratory_delete,
        pharmacy_read,
        pharmacy_write,
        pharmacy_delete,
        user_read,
        user_write,
        user_delete
        };
}
