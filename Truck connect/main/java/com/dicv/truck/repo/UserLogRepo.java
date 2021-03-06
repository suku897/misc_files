package com.dicv.truck.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dicv.truck.model.UserLog;

@Repository
public interface UserLogRepo extends JpaRepository<UserLog, Integer> {

}
