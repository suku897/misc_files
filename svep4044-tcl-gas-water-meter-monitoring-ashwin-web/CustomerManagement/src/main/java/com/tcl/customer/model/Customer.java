/*
 * @category Customer Management
 * @copyright Copyright (C) 2018 Contus. All rights reserved.
 * @license http://www.apache.org/licenses/LICENSE-2.0
 */

package com.tcl.customer.model;

import java.util.Date;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Document(collection = "customer")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class Customer {

	private static final long serialVersionUID = -4240718002259642160L;

	@Id
	@Field("customer_id")
	@Pattern(regexp = ".+@.+\\.[a-z]+")
	private String customerId;

	@Field("customer_name")
	private String customerName;

	@Field("customer_description")
	private String customerDescription;

	@Field("utility_id")
	private String utilityId;

	@Field("customer_logo")
	private String customerLogo;

	@Field("billing_type")
	private String billingType;

	@Field("business_type")
	private Boolean businessType;

	@Field("email_id")
	@Pattern(regexp = ".+@.+\\.[a-z]+")
	private String customerEmailId;

	@Field("group_email_id")
	@Pattern(regexp = ".+@.+\\.[a-z]+")
	private String customerGroupEmailId;

	@Field("phone_number")
	private String customerMobileNumber;

	@Field("mobile_number")
	private String customerPhoneNumber;

	@Field("alert_colour")
	private AlertColour customerColourConfig;

	@Field("address")
	private Address address;

	@Field("is_active")
	private boolean isActive = true;

	@Field("customer_status")
	private boolean customerStatus;

	@Field("created_date")
	@DateTimeFormat(iso = ISO.DATE_TIME)
	private Date createdDate;

	@Field("modified_date")
	@DateTimeFormat(iso = ISO.DATE_TIME)
	private Date modifiedDate;

	@Field("created_by")
	private String createdBy;

	@Field("modified_by")
	private String modifiedBy;

}
