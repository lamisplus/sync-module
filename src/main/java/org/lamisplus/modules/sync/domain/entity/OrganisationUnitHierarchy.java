package org.lamisplus.modules.sync.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;

@Data
@Entity
@EqualsAndHashCode
@Table(name = "organisation_unit_hierarchy")
@AllArgsConstructor
@NoArgsConstructor
public class OrganisationUnitHierarchy {
    @Id
    @Column(name = "id", updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Basic
    @Column(name = "organisation_unit_id")
    private Long organisationUnitId;

    @Basic
    @Column(name = "parent_organisation_unit_id")
    private Long parentOrganisationUnitId;

    @Basic
    @Column(name = "organisation_unit_level_id")
    private Long organisationUnitLevelId;

    @ManyToOne
    @JoinColumn(name = "organisation_unit_id", referencedColumnName = "id", insertable = false, updatable = false)
    @JsonIgnore
    @ToString.Exclude
    private OrganisationUnit organisationUnitByOrganisationUnitId;

    @ManyToOne
    @JoinColumn(name = "parent_organisation_unit_id", referencedColumnName = "id", insertable = false, updatable = false)
    @JsonIgnore
    @ToString.Exclude
    private OrganisationUnit organisationUnitByParentOrganisationUnitId;

    @ManyToOne
    @JoinColumn(name = "organisation_unit_level_id", referencedColumnName = "id", insertable = false, updatable = false)
    @JsonIgnore
    @ToString.Exclude
    private OrganisationUnitLevel organisationUnitLevelByOrganisationUnitLevelId;
}
