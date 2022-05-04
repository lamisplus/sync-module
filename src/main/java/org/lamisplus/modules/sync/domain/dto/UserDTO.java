package org.lamisplus.modules.sync.domain.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.lamisplus.modules.sync.domain.entity.ApplicationUserOrganisationUnit;
import org.lamisplus.modules.sync.domain.entity.Permission;
import org.lamisplus.modules.sync.domain.entity.Role;
import org.lamisplus.modules.sync.domain.entity.User;

import javax.validation.constraints.NotBlank;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class UserDTO {
    private Long id;

    @NotBlank(message = "userName is mandatory")
    private String userName;

    private Set<String> roles;

    private Set<String> permissions;

    @NotBlank(message = "firstName is mandatory")
    private String firstName;

    @NotBlank(message = "lastName is mandatory")
    private String lastName;

    private String email;

    @NotBlank(message = "phoneNumber is mandatory")
    private String phoneNumber;

    private String gender;

    private Long currentOrganisationUnitId;

    @ToString.Exclude
    private List<ApplicationUserOrganisationUnit> applicationUserOrganisationUnits;

    private String currentOrganisationUnitName;
    private int managedPatientCount;
    private Object details;



    public UserDTO(User user) {
        this.id = user.getId();
        this.userName = user.getUserName();
        this.roles = user.getRole().stream().map(Role::getName).collect(Collectors.toSet());
        permissions = new HashSet<>();
        user.getRole().forEach(roles1 ->{
            permissions.addAll(roles1.getPermission().stream().filter(p -> p.getArchived() == 0)
                    .map(Permission::getName).collect(Collectors.toSet()));
        });
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
        this.phoneNumber = user.getPhoneNumber();
        this.gender = user.getGender();
        this.currentOrganisationUnitId = user.getCurrentOrganisationUnitId();
        this.applicationUserOrganisationUnits = user.getApplicationUserOrganisationUnits();
        this.managedPatientCount = user.getManagedPatientCount();
        currentOrganisationUnitName = user.getOrganisationUnitByCurrentOrganisationUnitId() != null ? user.getOrganisationUnitByCurrentOrganisationUnitId().getName() : null;
        this.details = user.getDetails();
    }


    @Override
    public String toString() {
        return "UserDTO{" +
                "id=" + id +
                ", userName='" + userName + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", gender='" + gender + '\'' +
                ", details='" + details + '\'' +
                ", managedPatientCount='" + managedPatientCount + '\'' +
                ", currentOrganisationUnitId='" + currentOrganisationUnitId + '\'' +
                ", roles=" + roles + '\'' +
                ", permissions=" + permissions + '\'' +
                '}';
    }
}
