package com.dang_ky_tham_quan_nhan.features.visit.mapper;

import com.dang_ky_tham_quan_nhan.features.visit.entity.Relative;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface RelativeMapper {
    
    @Insert("INSERT INTO relative(visit_registration_id, name, relationship, id_number) VALUES(#{visitRegistrationId}, #{name}, #{relationship}, #{idNumber})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Relative relative);

    @Select("SELECT * FROM relative WHERE visit_registration_id = #{registrationId}")
    List<Relative> findByRegistrationId(Long registrationId);

    @Update("UPDATE relative SET name=#{name}, relationship=#{relationship}, id_number=#{idNumber} WHERE id=#{id}")
    void update(Relative relative);
}