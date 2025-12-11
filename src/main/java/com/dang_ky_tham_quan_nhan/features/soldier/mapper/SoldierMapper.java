package com.dang_ky_tham_quan_nhan.features.soldier.mapper;

import com.dang_ky_tham_quan_nhan.features.soldier.entity.Soldier;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SoldierMapper {
    
    @Select("<script>" +
            "SELECT s.*, u.name as unit_name FROM soldier s " +
            "LEFT JOIN unit u ON s.unit_id = u.id " +
            "WHERE 1=1 " +
            "<if test='unitId != null'> AND s.unit_id = #{unitId} </if>" +
            "<if test='keyword != null and keyword != \"\"'> " +
            "  AND (s.name LIKE CONCAT('%', #{keyword}, '%') OR s.code LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if>" +
            "</script>")
    List<Soldier> searchSoldiers(@Param("unitId") Long unitId, @Param("keyword") String keyword);

    @Select("SELECT * FROM soldier WHERE id = #{id}")
    Soldier findById(Long id);

    @Insert("INSERT INTO soldier (code, name, unit_id, status) VALUES (#{code}, #{name}, #{unitId}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Soldier soldier);

    @Update("UPDATE soldier SET code = #{code}, name = #{name}, unit_id = #{unitId}, status = #{status} WHERE id = #{id}")
    void update(Soldier soldier);

    @Delete("DELETE FROM soldier WHERE id = #{id}")
    void delete(Long id);
}