package com.eucalyptus.reporting.event_store;

import javax.persistence.Column;
import javax.persistence.PersistenceContext;
import javax.persistence.Table;

import org.hibernate.annotations.Entity;

@SuppressWarnings("serial")
@Entity @javax.persistence.Entity
@PersistenceContext(name="eucalyptus_reporting")
@Table(name="reporting_s3_object_delete_events")
public class ReportingS3ObjectDeleteEvent
{
	@Column(name="s3_bucket_name", nullable=false)
	private String s3BucketName;
	@Column(name="s3_object_name", nullable=false)
	private String s3ObjectName;
	@Column(name="timestamp_ms", nullable=false)
	private Long timestampMs;

	protected ReportingS3ObjectDeleteEvent()
	{
		super();
		this.s3BucketName = null;
		this.s3ObjectName = null;
		this.timestampMs = null;
	}	
	
	protected ReportingS3ObjectDeleteEvent(String s3BucketName, String s3ObjectName, Long timestampMs)
	{
		super();
		this.s3BucketName = s3BucketName;
		this.s3ObjectName = s3ObjectName;
		this.timestampMs = timestampMs;
	}

	public String getS3BucketName()
	{
		return s3BucketName;
	}
	
	public String getS3ObjectName()
	{
		return s3ObjectName;
	}
	
	public Long getTimestampMs()
	{
		return timestampMs;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((s3BucketName == null) ? 0 : s3BucketName.hashCode());
		result = prime * result
				+ ((s3ObjectName == null) ? 0 : s3ObjectName.hashCode());
		result = prime * result
				+ ((timestampMs == null) ? 0 : timestampMs.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ReportingS3ObjectDeleteEvent other = (ReportingS3ObjectDeleteEvent) obj;
		if (s3BucketName == null) {
			if (other.s3BucketName != null)
				return false;
		} else if (!s3BucketName.equals(other.s3BucketName))
			return false;
		if (s3ObjectName == null) {
			if (other.s3ObjectName != null)
				return false;
		} else if (!s3ObjectName.equals(other.s3ObjectName))
			return false;
		if (timestampMs == null) {
			if (other.timestampMs != null)
				return false;
		} else if (!timestampMs.equals(other.timestampMs))
			return false;
		return true;
	}

}