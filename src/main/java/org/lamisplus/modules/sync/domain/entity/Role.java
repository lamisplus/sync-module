package org.lamisplus.modules.sync.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class Role extends Audit<String> {
    @Id
    @Getter
    @Setter
    @Column(name = "id", updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NonNull
    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private String code;

    @Getter
    @Setter
    @JsonIgnore
    private int archived;

    @LastModifiedDate
    @Column(name = "date_modified")
    @JsonIgnore
    @ToString.Exclude
    private LocalDateTime dateModified = LocalDateTime.now();

    @Getter
    @Setter
    @ManyToMany(cascade = CascadeType.PERSIST)
    private Set<Permission> permission;

    @Override
    public int hashCode() {
        if (id != null) {
            return id.hashCode();
        } else if (name != null) {
            return name.hashCode();
        }

        return 0;
    }

    @Override
    public boolean equals(Object another) {
        if (another == null || !(another instanceof Role))
            return false;

        Role anotherRole = (Role) another;

        return anotherRole.id != null && (anotherRole.id == this.id);
    }

    @Override
    public String toString() {
        return "Roles{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", dateModified='" + dateModified + '\'' +
                ", permissions=" + permission +
                '}';
    }
}
