package com.projectsugarglider.front.entity.id;

import java.io.Serializable;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@EqualsAndHashCode
@NoArgsConstructor(access=AccessLevel.PROTECTED)
@AllArgsConstructor(access=AccessLevel.PRIVATE)
public class KcaCallHistoryId implements Serializable {
    private String kcaCallDay;
    private String entpId;
    private String goodInspectDay;
}