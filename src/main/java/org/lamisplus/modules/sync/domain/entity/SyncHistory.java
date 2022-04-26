package org.lamisplus.modules.sync.domain.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Entity
@EqualsAndHashCode
@Table(name = "sync_history")
public class SyncHistory implements Serializable {
    @Id
    @Column(name = "id", updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Basic
    @Column(name = "table_name")
    private String tableName;

    @Basic
    @Column(name = "organisation_unit_id", updatable = false)
    private Long organisationUnitId;

    @Basic
    @Column(name = "date_last_sync")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd@HH:mm:ss")
    private LocalDateTime dateLastSync;

    @Basic
    @Column(name = "processed")
    private Integer processed;

    @Basic
    @Column(name = "sync_queue_id")
    private Long syncQueueId;

    @Basic
    @Column(name = "remote_access_token_id")
    private Long remoteAccessTokenId;

    @Basic
    @Column(name = "upload_size")
    private Integer uploadSize;

    @Basic
    @Column(name = "processedSize")
    private Integer processedSize;

    @Transient
    private String facilityName;

    @Transient
    private String status;
}
