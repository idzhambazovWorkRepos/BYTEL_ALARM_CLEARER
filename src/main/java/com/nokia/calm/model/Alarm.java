/*
 * Nokia Software 2020
 * Created at: 22-Sep-2021 22:58:15
 * Created by: Caglar Kilincoglu
 */

package com.nokia.calm.model;

import com.google.gson.annotations.SerializedName;
import java.util.Date;
import java.util.List;

/**
 */
public class Alarm {
    @SerializedName("@type")
    public String type;
    public List<Link> links;
    public String id;
    public String sourceAdapter;
    public String sourceObject;
    public String sourceProblem;
    public String alarmCode;
    public String text;
    public String severity;
    public boolean toBeCleared;
    public Date createdAt;
    public Date lastSourceEventTime;
    public String mocs;
    public String noiAdditionalText;
    public String noiEventType;
    public String noiProbableCause;
    public String enterprise;
    public List<Object> augmentations;
    public int augmentations_count;
    public List<Metadata> meta;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Link> getLinks() {
        return links;
    }

    public void setLinks(List<Link> links) {
        this.links = links;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSourceAdapter() {
        return sourceAdapter;
    }

    public void setSourceAdapter(String sourceAdapter) {
        this.sourceAdapter = sourceAdapter;
    }

    public String getSourceObject() {
        return sourceObject;
    }

    public void setSourceObject(String sourceObject) {
        this.sourceObject = sourceObject;
    }

    public String getSourceProblem() {
        return sourceProblem;
    }

    public void setSourceProblem(String sourceProblem) {
        this.sourceProblem = sourceProblem;
    }

    public String getAlarmCode() {
        return alarmCode;
    }

    public void setAlarmCode(String alarmCode) {
        this.alarmCode = alarmCode;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public boolean isToBeCleared() {
        return toBeCleared;
    }

    public void setToBeCleared(boolean toBeCleared) {
        this.toBeCleared = toBeCleared;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getLastSourceEventTime() {
        return lastSourceEventTime;
    }

    public void setLastSourceEventTime(Date lastSourceEventTime) {
        this.lastSourceEventTime = lastSourceEventTime;
    }

    public String getMocs() {
        return mocs;
    }

    public void setMocs(String mocs) {
        this.mocs = mocs;
    }

    public String getNoiAdditionalText() {
        return noiAdditionalText;
    }

    public void setNoiAdditionalText(String noiAdditionalText) {
        this.noiAdditionalText = noiAdditionalText;
    }

    public String getNoiEventType() {
        return noiEventType;
    }

    public void setNoiEventType(String noiEventType) {
        this.noiEventType = noiEventType;
    }

    public String getNoiProbableCause() {
        return noiProbableCause;
    }

    public void setNoiProbableCause(String noiProbableCause) {
        this.noiProbableCause = noiProbableCause;
    }

    public String getEnterprise() {
        return enterprise;
    }

    public void setEnterprise(String enterprise) {
        this.enterprise = enterprise;
    }

    public List<Object> getAugmentations() {
        return augmentations;
    }

    public void setAugmentations(List<Object> augmentations) {
        this.augmentations = augmentations;
    }

    public int getAugmentations_count() {
        return augmentations_count;
    }

    public void setAugmentations_count(int augmentations_count) {
        this.augmentations_count = augmentations_count;
    }

    public List<Metadata> getMeta() {
        return meta;
    }

    public void setMeta(List<Metadata> meta) {
        this.meta = meta;
    }
    

    @Override
    public String toString() {
        return "calmalarm{" + "type=" + type + ", links=" + links + ", id=" + id + "," +
                " sourceAdapter=" + sourceAdapter + ", sourceObject=" + sourceObject + ", sourceProblem=" +
                sourceProblem + ", alarmCode=" + alarmCode + ", text=" + text + ", severity=" + severity +
                ", toBeCleared=" + toBeCleared + ", createdAt=" + createdAt + ", lastSourceEventTime=" +
                lastSourceEventTime + ", mocs=" + mocs + ", noiAdditionalText=" + noiAdditionalText + ", \n" +
                "oiEventType=" + noiEventType + ", noiProbableCause=" + noiProbableCause + ", enterprise=" +
                enterprise + ", augmentations=" + augmentations + ", augmentations_count=" + augmentations_count + ", meta=" + meta + '}';
    }
    
    
}
