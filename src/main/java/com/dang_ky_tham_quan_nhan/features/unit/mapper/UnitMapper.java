package com.dang_ky_tham_quan_nhan.features.unit.mapper;

import com.dang_ky_tham_quan_nhan.features.unit.entity.Unit;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UnitMapper {
    @Select("SELECT * FROM unit")
    List<Unit> findAll();

    @Select("SELECT * FROM unit WHERE id = #{id}")
    Unit findById(Long id);

    @Insert("INSERT INTO unit (name, parent_id) VALUES (#{name}, #{parentId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Unit unit);

    @Update("UPDATE unit SET name = #{name}, parent_id = #{parentId} WHERE id = #{id}")
    void update(Unit unit);

    @Delete("DELETE FROM unit WHERE id = #{id}")
    void delete(Long id);
}