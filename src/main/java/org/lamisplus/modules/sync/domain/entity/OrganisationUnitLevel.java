package org.lamisplus.modules.sync.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Data
@Entity
@EqualsAndHashCode
@Table(name = "organisation_unit_level")
public class OrganisationUnitLevel implements Serializable {
    @Id
    @Column(name = "id", updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Basic
    @Column(name = "name")
    private String name;
    @Basic
    @Column(name = "description")
    private String description;
    @Basic
    @Column(name = "archived")
    private Integer archived;
    @Basic
    @Column(name = "status")
    private Integer status;

    @Basic
    @Column(name = "parent_organisation_unit_level_id")
    private Long parentOrganisationUnitLevelId;

    @OneToMany(mappedBy = "organisationUnitLevelByOrganisationUnitLevelId")
    @ToString.Exclude
    @JsonIgnore
    public List<OrganisationUnit> organisationUnitsById;

    @OneToMany(mappedBy = "organisationUnitLevelByOrganisationUnitLevelId")
    @ToString.Exclude
    @JsonIgnore
    public List<OrganisationUnitHierarchy> organisationUnitHierarchiesById;

    @ManyToOne
    @JoinColumn(name = "parent_organisation_unit_level_id", referencedColumnName = "id", insertable = false, updatable = false)
    @JsonIgnore
    @ToString.Exclude
    private OrganisationUnitLevel parentOrganisationUnitLevelIdByOrganisationUnitLevelId;

    @OneToMany(mappedBy = "parentOrganisationUnitLevelIdByOrganisationUnitLevelId")
    @JsonIgnore
    @ToString.Exclude
    public List<OrganisationUnitLevel> ParentOrganisationUnitLevelByOrganisationUnitLevelId;
}
