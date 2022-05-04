package org.lamisplus.modules.sync.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Persistable;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

@Data
@Entity
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Table(name = "menu")
@EqualsAndHashCode(of = {"name", "level"})
@ToString(of = {"id", "name", "state", "type", "subs", "level", "module", "position", "icon", "tooltip", "parentId", "parentName", "disabled", "breadcrumb", "url", "archived", "type", "level", "moduleId"})
@Slf4j
public final class Menu implements Serializable, Comparable<Menu>, Persistable<Long> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String name;

    //@NotNull
    private String state;

    @NotNull
    private int archived=0;

    //@NotNull
    //@Enumerated(EnumType.STRING)
    private String type ="LINK";

    @NotNull
    //@Enumerated(EnumType.STRING)
    //@JsonIgnore
    private String level = "LEVEL_1";

    @NotNull
    private Integer position = 1;

    private String icon;

    private String tooltip;

    private String breadcrumb;

    private String url;

    private Boolean disabled = false;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "menu_authorities",
            joinColumns = @JoinColumn(name = "menu_id"))
    private Set<String> authorities = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    //@NotNull - check for system menus
    @JoinColumn(name = "module_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Module module;

    @Column(name = "module_id")
    private Long moduleId;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "parent_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Menu parent;


    @Column(name = "parent_id")
    private Integer parentId;


    @OneToMany(mappedBy = "parent", fetch = FetchType.EAGER)
    private Set<Menu> subs = new TreeSet<>();

    @Override
    public boolean isNew() {
        return id == null;
    }

    @Override
    public int compareTo(Menu o) {
        if (position.equals(o.position)) {
            return name.compareTo(o.name);
        }
        return position.compareTo(o.position);
    }

    @PostLoad
    public void load() {
    }

    /*@PrePersist
    public void update() {
        if (parent != null) {
            if (subs.isEmpty()) {
                level = MenuLevel.LEVEL_2;
            } else {
                level = MenuLevel.LEVEL_3;
            }
            if (parent.parent != null) {
                level = MenuLevel.LEVEL_3;
            }
        } else {
            level = MenuLevel.LEVEL_1;
        }

        if (!subs.isEmpty()) {
            type = MenuType.DROP_DOWN;
        }
    }*/

    @PrePersist
    public void update(){
        if(this.tooltip == null && this.name != null) {
            tooltip = this.name;
        }

        if(module != null){
            moduleId = module.getId();
        }

        if(icon == null){
            icon = "flaticon-087-stop";
        }
    }
}
