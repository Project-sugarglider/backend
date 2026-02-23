package com.projectsugarglider.front.entity;

import com.projectsugarglider.front.entity.id.KcaCallHistoryId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "kca_call_history")
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access=AccessLevel.PROTECTED)
@AllArgsConstructor(access=AccessLevel.PRIVATE) 
@IdClass(KcaCallHistoryId.class)
@Builder
public class KcaCallHistory {

    @EqualsAndHashCode.Include
    @Id
    @Column(name = "kca_call_day", length = 8)
    private String kcaCallDay;

    @Id
    @Column(name = "entp_id")
    private String entpId;

    @Id
    @Column(name = "good_inspect_day", length = 8)
    private String goodInspectDay;

}