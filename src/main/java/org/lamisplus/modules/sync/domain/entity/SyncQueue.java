package org.lamisplus.modules.sync.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;


@Table(name = "sync_queue")
@Entity
@NoArgsConstructor
@Data
public class SyncQueue implements Serializable {
    @Id
    @Column(name = "id", updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Basic
    @Column(name = "file_name")
    @JsonIgnore
    private String fileName;

    @Basic
    @Column(name = "table_name")
    private String tableName;

    @Basic
    @Column(name = "organisation_unit_id", updatable = false)
    private Long organisationUnitId;

    @CreatedDate
    @Column(name = "date_created", nullable = false, updatable = false)
    private LocalDateTime dateCreated;

    @Basic
    @Column(name = "processed")
    private Integer processed;

    @Basic
    @Column(name = "upload_size")
    private Integer processedSize;
}
