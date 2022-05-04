package org.lamisplus.modules.sync.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;

@Data
@Entity
@EqualsAndHashCode
@Table(name = "application_user_organisation_unit")
public class ApplicationUserOrganisationUnit extends Audit<String> {

    @Id
    @Column(name = "id", updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Basic
    @Column(name = "application_user_id")
    private Long applicationUserId;

    @Basic
    @Column(name = "organisation_unit_id")
    private Long organisationUnitId;

    @Basic
    @Column(name = "archived")
    private int archived = 0;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "application_user_id", referencedColumnName = "id", insertable = false, updatable = false)
    @ToString.Exclude
    private User applicationUserByApplicationUserId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JsonIgnore
    @JoinColumn(name = "organisation_unit_id", referencedColumnName = "id", insertable = false, updatable = false)
    @ToString.Exclude
    private OrganisationUnit organisationUnitByOrganisationUnitId;

    public String getOrganisationUnitName(){
        if (organisationUnitByOrganisationUnitId != null){
            return organisationUnitByOrganisationUnitId.getName();
        }
        return null;
    }
}
