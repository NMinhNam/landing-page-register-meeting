package com.dang_ky_tham_quan_nhan.features.auth.mapper;

import com.dang_ky_tham_quan_nhan.features.auth.entity.AdminUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AdminUserMapper {
    
    @Select("SELECT * FROM admin_user WHERE username = #{username}")
    AdminUser findByUsername(String username);

    @Select("SELECT * FROM admin_user WHERE id = #{id}")
    AdminUser findById(Long id);
}