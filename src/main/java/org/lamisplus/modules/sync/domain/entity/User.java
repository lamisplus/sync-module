package org.lamisplus.modules.sync.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.lamisplus.modules.sync.security.SecurityUtils;

import javax.persistence.*;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@RequiredArgsConstructor
@Table(name = "application_user")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class User {
    @Id
    @Column(name = "id", updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Basic
    @Column(name = "user_name")
    @NonNull
    private String userName;

    @Basic
    @Column(name = "email")
    private String email;

    @Basic
    @Column(name = "phone_number")
    private String phoneNumber;

    @Basic
    @Column(name = "gender")
    private String gender;

    @Basic
    @Column(name = "password")
    @NonNull
    private String password;

    @Basic
    @Column(name = "archived")
    @NonNull
    private Integer archived = 0;

    @Column(name = "created_by", nullable = false, updatable = false)
    @JsonIgnore
    @ToString.Exclude
    private String createdBy = SecurityUtils.getCurrentUserLogin().orElse(null);

    @Column(name = "date_created", nullable = false, updatable = false)
    @JsonIgnore
    @ToString.Exclude
    private LocalDateTime dateCreated = LocalDateTime.now();

    @Column(name = "modified_by")
    @JsonIgnore
    @ToString.Exclude
    private String modifiedBy = SecurityUtils.getCurrentUserLogin().orElse(null);

    @Column(name = "date_modified")
    @JsonIgnore
    @ToString.Exclude
    private LocalDateTime dateModified = LocalDateTime.now();

    @Basic
    @Column(name = "activation_key")
    private String activationKey;

    @Basic
    @Column(name = "date_reset")
    private Date dateReset;

    @Basic
    @Column(name = "reset_key")
    private String resetKey;

    @Basic
    @Column(name = "current_organisation_unit_id")
    private Long currentOrganisationUnitId;

    @Basic
    @Column(name = "first_name")
    private String firstName;

    @Basic
    @Column(name = "last_name")
    private String lastName;

    @ManyToMany(cascade = CascadeType.PERSIST)
    private Set<Role> role;

    @OneToMany(mappedBy = "applicationUserByApplicationUserId", cascade = CascadeType.PERSIST)
    private List<ApplicationUserOrganisationUnit> applicationUserOrganisationUnits;

    @ManyToOne
    @JoinColumn(name = "current_organisation_unit_id", referencedColumnName = "id", insertable = false, updatable = false)
    @ToString.Exclude
    private OrganisationUnit organisationUnitByCurrentOrganisationUnitId;

    @Transient
    private int managedPatientCount;

    @Type(type = "jsonb")
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "details", nullable = true, columnDefinition = "jsonb")
    private Object details;
}