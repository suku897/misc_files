/*
 * @category PropertyManagement
 * @copyright Copyright (C) 2018 Contus. All rights reserved.
 * @license http://www.apache.org/licenses/LICENSE-2.0
 */

package com.tcl.property.repo;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.tcl.property.model.Pincode;

public interface PincodeRepo extends MongoRepository<Pincode, Long> {

	List<Pincode> findByCityId(String cityId);

}
