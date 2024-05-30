package com.huce.edu.models.response;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "create")
public class OrderHistoryResponse {
	private Date date;
	private String address;
	private Double price;
	private Integer quantity;
	private String phone;
	private String pname;
	private String pimage;
}
