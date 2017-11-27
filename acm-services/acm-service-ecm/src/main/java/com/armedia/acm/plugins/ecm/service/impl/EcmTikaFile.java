package com.armedia.acm.plugins.ecm.service.impl;

import com.armedia.acm.plugins.ecm.model.EcmFileVersion;

import java.util.Date;

public class EcmTikaFile
{
    private String contentType;
    private String nameExtension;
    private String gpsIso6709;
    private Double gpsLatitudeDegrees;
    private Double gpsLongitudeDegrees;
    private String gpsReadable;
    private Date created;
    private Integer heightPixels;
    private Integer widthPixels;
    private String deviceMake;
    private String deviceModel;
    private Double durationSeconds;

    public void stampVersionInfo(EcmFileVersion version)
    {
        version.setDeviceMake(getDeviceMake());
        version.setDeviceModel(getDeviceModel());
        version.setGpsIso6709(getGpsIso6709());
        version.setGpsLatitudeDegrees(getGpsLatitudeDegrees());
        version.setGpsLongitudeDegrees(getGpsLongitudeDegrees());
        version.setGpsReadable(getGpsReadable());
        version.setHeightPixels(getHeightPixels());
        version.setWidthPixels(getWidthPixels());
        version.setMediaCreated(getCreated());
        version.setDurationSeconds(getDurationSeconds());
    }


    public String getContentType()
    {
        return contentType;
    }

    public void setContentType(String contentType)
    {
        this.contentType = contentType;
    }

    public String getNameExtension()
    {
        return nameExtension;
    }

    public void setNameExtension(String nameExtension)
    {
        this.nameExtension = nameExtension;
    }

    public String getGpsIso6709()
    {
        return gpsIso6709;
    }

    public void setGpsIso6709(String gpsIso6709)
    {
        this.gpsIso6709 = gpsIso6709;
    }

    public Double getGpsLatitudeDegrees()
    {
        return gpsLatitudeDegrees;
    }

    public void setGpsLatitudeDegrees(Double gpsLatitudeDegrees)
    {
        this.gpsLatitudeDegrees = gpsLatitudeDegrees;
    }

    public Double getGpsLongitudeDegrees()
    {
        return gpsLongitudeDegrees;
    }

    public void setGpsLongitudeDegrees(Double gpsLongitudeDegrees)
    {
        this.gpsLongitudeDegrees = gpsLongitudeDegrees;
    }

    public String getGpsReadable()
    {
        return gpsReadable;
    }

    public void setGpsReadable(String gpsReadable)
    {
        this.gpsReadable = gpsReadable;
    }

    public Date getCreated()
    {
        return created;
    }

    public void setCreated(Date created)
    {
        this.created = created;
    }

    public Integer getHeightPixels()
    {
        return heightPixels;
    }

    public void setHeightPixels(Integer heightPixels)
    {
        this.heightPixels = heightPixels;
    }

    public Integer getWidthPixels()
    {
        return widthPixels;
    }

    public void setWidthPixels(Integer widthPixels)
    {
        this.widthPixels = widthPixels;
    }

    public String getDeviceMake()
    {
        return deviceMake;
    }

    public void setDeviceMake(String deviceMake)
    {
        this.deviceMake = deviceMake;
    }

    public String getDeviceModel()
    {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel)
    {
        this.deviceModel = deviceModel;
    }

    public Double getDurationSeconds()
    {
        return durationSeconds;
    }

    public void setDurationSeconds(Double durationSeconds)
    {
        this.durationSeconds = durationSeconds;
    }

    @Override
    public String toString()
    {
        return "EcmTikaFile{" +
                "contentType='" + contentType + '\'' +
                ", nameExtension='" + nameExtension + '\'' +
                ", gpsIso6709='" + gpsIso6709 + '\'' +
                ", gpsLatitudeDegrees=" + gpsLatitudeDegrees +
                ", gpsLongitudeDegrees=" + gpsLongitudeDegrees +
                ", gpsReadable='" + gpsReadable + '\'' +
                ", created=" + created +
                ", heightPixels=" + heightPixels +
                ", widthPixels=" + widthPixels +
                ", deviceMake='" + deviceMake + '\'' +
                ", deviceModel='" + deviceModel + '\'' +
                ", durationSeconds=" + durationSeconds +
                '}';
    }
}